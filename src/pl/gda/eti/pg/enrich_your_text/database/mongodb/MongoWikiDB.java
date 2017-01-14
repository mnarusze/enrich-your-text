package pl.gda.eti.pg.enrich_your_text.database.mongodb;

import pl.gda.eti.pg.enrich_your_text.database.WikiDatabase;
import org.bson.Document;
import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import pl.gda.eti.pg.enrich_your_text.extraction.WikipediaExtractor;
import pl.gda.eti.pg.enrich_your_text.models.WikiArticle;
import pl.gda.eti.pg.enrich_your_text.models.WikiLabel;
import pl.gda.eti.pg.enrich_your_text.models.WikiLink;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;
import pl.gda.eti.pg.enrich_your_text.textprocessing.tools.TextProcessor;
import static com.mongodb.client.model.Filters.eq;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MongoWikiDB extends WikiDatabase {

    private final Pattern UNDERSCORE_PATTERN = Pattern.compile("_");
    public final static Integer DEFAULT_PORT = 27017; // the default port for MongoDB installation
    public final static Integer CONNECTION_TIMEOUT_MS = 10000;
    
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;

    public MongoWikiDB() {
        mongoClient = null;
    }
    
    private void createCollection(String collectionName, Boolean forceIfExists) throws IllegalArgumentException {
        MongoCursor<String> collectionNamesIterator = mongoDatabase.listCollectionNames().iterator();
        while (collectionNamesIterator.hasNext()) {
            String existingCollectionName = collectionNamesIterator.next();
            if (collectionName.equals(existingCollectionName)) {
                if (forceIfExists) {
                    mongoDatabase.getCollection(collectionName).drop();
                    mongoDatabase.createCollection(collectionName);
                    break;
                }
                else
                    throw new IllegalArgumentException("Collection " + collectionName + " already exists!");
            }
        }
    }

    @Override
    public Boolean initializeDatabase(WikipediaExtractor.ExtractionStep startingStep, Boolean forceIfExists, Boolean createIndexes) {
        try {
            switch (startingStep) {
                case NONE:
                case PARSING_XML_ARTICLES:
                    createCollection(WikiCollections.WIKIPEDIA.toString(), forceIfExists);
                    createCollection(WikiCollections.ARTICLES.toString(), forceIfExists);

                    if (createIndexes) {
                        MongoCollection<Document> articlesCollection = mongoDatabase.getCollection(WikiCollections.ARTICLES.toString());
                        articlesCollection.createIndex(new Document(WikiArticle.DB_ARTICLE_ID, 1));
                    }
                case PARSING_XML_LINKS:
                    createCollection(WikiCollections.LINKS.toString(), forceIfExists);

                    if (createIndexes) {
                        MongoCollection<Document> linksCollection = mongoDatabase.getCollection(WikiCollections.LINKS.toString());
                        linksCollection.createIndex(new Document(WikiLink.DB_SOURCE_ARTICLE, 1));
                        linksCollection.createIndex(new Document(WikiLink.DB_TARGET_ARTICLE, 1));
                    }
                case LABELS_AGGREGATION:
                    createCollection(WikiCollections.LABELS.toString(), forceIfExists);
                    createCollection(WikiCollections.LABELS_STEMMED.toString(), forceIfExists);
                case SETTING_LINKS_FROM_TO:
            }
        } catch (IllegalArgumentException ex) {
            return false;
        }
        return true;
    }

    @Override
    public void saveDatabase() {
        // MongoDB doesn't require committing changes
    }

    @Override
    public Long getCountTable(WikiCollections table) {
        return mongoDatabase.getCollection(table.toString()).count();
    }

    // TODO move the logic out of MongoDB
    private void saveWikiLink(String labelStr, Integer sourceArticle, Integer targetArticles[], Boolean stem) {
        MongoCollection<Document>  linksCollections = mongoDatabase.getCollection(WikiCollections.LINKS.toString());
        
        // Prepare the label - always lower case, replace underscores with spaces then trim
        String label = UNDERSCORE_PATTERN.matcher(labelStr.toLowerCase()).replaceAll(" ").trim();
        
        // Stem label if we want to
        if (stem) {
            label = TextProcessor.stemText(label);
        }
        
        // If label is empty - use a whitespace which may still be valid
        if (label.isEmpty()) {
            label = " ";
        }
        
        // Don't store this link if the size of label is too big for indexing
        try {
            if (label.getBytes("UTF-8").length >= 1000) {
                System.err.println("Label " + label + " too big for indexing - ignoring!");
                return;
            }
        } catch (UnsupportedEncodingException ex) {
            System.err.println("Failed to get UTF-8 size for label " + label);
        }
        
        // For each target article store a new connection
        for (Integer targetArticle : targetArticles) {
            WikiLink link = new WikiLink(label, sourceArticle, targetArticle);
            try {
                linksCollections.insertOne(link.getMongoObjectForThisLink());
            } catch (Exception ex) {
                System.err.println(ex.getMessage());
            }
        }
    }

    private void saveWikiLink(String labelStr, Integer sourceArticle, Integer targetArticle, Boolean stem) {
        saveWikiLink(labelStr, sourceArticle, new Integer[]{targetArticle}, stem);
    }

    @Override
    public void saveWikiLinks(WikiArticle article, Boolean stem) {
        if (article.isARedirect()) {
            WikiArticle redirectArticle = findRealArticleByName(article.getRedirectTo());
            // Check for red or unwanted links
            if (redirectArticle != null && (!redirectArticle.isADisambiguationPage() || !redirectArticle.isPageFromAnotherNamespace())) {
                saveWikiLink(article.getTitle(), article.getId(), redirectArticle.getId(), stem);
            }
            return;
        }
        
        String label = "";
        
        if (article.isADisambiguationPage()) {
            label = article.getTitle().substring(0, 
                article.getTitle().length() - WikiArticle.DISAMBIGUATION_SUFFIX.length()).trim();
        }
        
        for (WikiLink link : article.getWikiLinks()) {
            WikiArticle targetArticle = findRealArticleByName(link.getTargetArticleName());
            if (targetArticle == null || targetArticle.isADisambiguationPage() || targetArticle.isPageFromAnotherNamespace()) {
                // Red or unwanted link -> don't store this link
                continue;
            }
            if (!article.isADisambiguationPage()) {
                label = link.getLabelName();
            }
            saveWikiLink(label, article.getId(), targetArticle.getId(), stem);
        }
    }

    @Override
    public Boolean openDatabase(String dbName) {
        try {
            mongoDatabase = mongoClient.getDatabase(dbName);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }

        return (mongoDatabase != null);
    }

    @Override
    public void closeDatabase() {
        mongoClient.close();
    }
    
    private WikiArticle findRealArticle(Document queriedArticleDB) {
        WikiArticle targetArticle = new WikiArticle(queriedArticleDB);
        MongoCollection<Document> articlesCollection = mongoDatabase.getCollection(WikiCollections.ARTICLES.toString());
        MongoCursor<Document> articlesDBIterator;
        
        while (targetArticle.isARedirect()) {
            articlesDBIterator = articlesCollection.find(new Document(WikiArticle.DB_ARTICLE__ID, targetArticle.getRedirectTo())).iterator();
            if (!articlesDBIterator.hasNext()) {
                targetArticle = null;
                break;
            }
            
            targetArticle = new WikiArticle(articlesDBIterator.next());

            if (targetArticle.getTitle().equals(targetArticle.getRedirectTo())) {
                System.err.println("Infinite redirect for article " + targetArticle.getId()
                        + " " + targetArticle.getTitle());
                targetArticle = null;
                break;
            }
        }

        return targetArticle;
    }

    @Override
    public WikiArticle findRealArticleByName(String targetArticleName) {
        MongoCollection<Document> articlesCollection = mongoDatabase.getCollection(WikiCollections.ARTICLES.toString());
        MongoCursor<Document> articlesDBCursor = articlesCollection.find(new Document(WikiArticle.DB_ARTICLE__ID, targetArticleName)).iterator();
        
        if (!articlesDBCursor.hasNext()) {
            return null;
        }

        return findRealArticle(articlesDBCursor.next());
    }

    @Override
    public WikiLabel getWikiLabelForGivenNGram(String ngram, WikiCollections labelsType) {
        String collectionName = labelsType.toString();
        MongoCollection<Document> chosenLabelCollection = mongoDatabase.getCollection(collectionName);
        Document labelQuery;
        MongoCursor<Document> labelsDBCursor;
        WikiLabel label = null;
        
        labelQuery = new Document(WikiLabel.DB__ID, ngram);
        labelsDBCursor = chosenLabelCollection.find(labelQuery).iterator();

        if (labelsDBCursor.hasNext()) {
            label = new WikiLabel(labelsDBCursor.next());
        }

        return label;
    }

    @Override
    public Wikipedia loadWikipedia() {
        MongoCollection<Document> wikiCollection = mongoDatabase.getCollection(WikiCollections.WIKIPEDIA.toString());
        MongoCursor<Document> mongoCursor = wikiCollection.find().iterator();
        if (mongoCursor.hasNext()) {
            // There should always be one Wikipedia per database
            return new Wikipedia(mongoCursor.next());
        }
        
        // We didn't find a Wikipedia collection in this database
        return null;
    }

    @Override
    public void saveWikipedia(Wikipedia wikipedia) {
        MongoCollection<Document> wikiCollection = mongoDatabase.getCollection(WikiCollections.WIKIPEDIA.toString());

        wikiCollection.insertOne(wikipedia.getMongoObject());
    }

    @Override
    public WikiArticle getArticleByID(Integer targetArticle) {
        MongoCollection<Document> articlesCollection = mongoDatabase.getCollection(WikiCollections.ARTICLES.toString());
        Document articleQuery;
        WikiArticle article = null;

        articleQuery = new Document(WikiArticle.DB_ARTICLE_ID, targetArticle);
        
        MongoCursor<Document> articleCursor = articlesCollection.find(articleQuery).iterator();
        
        if (articleCursor.hasNext()) {
            article = new WikiArticle(articleCursor.next());
        }

        return article;
    }

    Block<Document> printBlock = new Block<Document>() {
        @Override
        public void apply(final Document document) {
            System.out.println(document.toJson());
        }
    };
    
    @Override
    public void aggregateLabels() {
        MongoCollection<Document> linksCollection = mongoDatabase.getCollection(WikiCollections.LINKS.toString());
        
        currentStepMax = 1L;
        currentStepProgress = 0L;
        
        try {
            linksCollection.aggregate(asList(
                new Document("$group",
                    new Document(WikiLink.DB__ID, new Document(WikiLink.DB_LABEL_NAME, "$" + WikiLink.DB_LABEL_NAME).append(WikiLink.DB_TARGET_ARTICLE, "$" + WikiLink.DB_TARGET_ARTICLE))
                              .append(WikiLabel.DB_ARTICLE_CONN_COUNT, new Document("$sum", 1))),
                new Document("$sort",
                    new Document(WikiLabel.DB_ARTICLE_CONN_COUNT, -1)),
                new Document("$group",
                    new Document(WikiLink.DB__ID, "$" + WikiLink.DB__ID + "." + WikiLink.DB_LABEL_NAME).append(WikiLink.DB_TARGET_ARTICLES,
                        new Document("$push", new Document(WikiArticle.DB_ARTICLE_ID, "$" + WikiLabel.DB__ID + "." + WikiLabel.DB_TARGET_ARTICLE).
                                                              append(WikiLabel.DB_ARTICLE_CONN_COUNT, "$" + WikiLabel.DB_ARTICLE_CONN_COUNT)))),
                new Document("$project",
                    new Document("_id", 1).append(WikiLabel.DB_TARGET_ARTICLES, "$" + WikiLabel.DB_TARGET_ARTICLES)),
                new Document("$out", WikiCollections.LABELS.toString())
                )).allowDiskUse(true).toCollection();
        } catch (IllegalStateException ex) {
            System.out.println("Illegal state!");
            return;
        }
        
        currentStepProgress = 1L;
    }

    @Override
    public void setLinksFromTo() {        
        MongoCollection<Document> linksCollection = mongoDatabase.getCollection(WikiCollections.LINKS.toString());
        MongoCollection<Document> articlesCollection = mongoDatabase.getCollection(WikiCollections.ARTICLES.toString());
        try (MongoCursor<Document> articleCursor = articlesCollection.find().noCursorTimeout(true).iterator()) {
            MongoCursor<Document> linksCursor;
            
            currentStepMax = articlesCollection.count();
            currentStepProgress = 0L;
            
            while (articleCursor.hasNext()) {
                Document articleFromDB = articleCursor.next();
                WikiArticle article = new WikiArticle(articleFromDB);
                BasicDBObject linksToArticleQuery = new BasicDBObject(WikiLink.DB_TARGET_ARTICLE, article.getId());
                BasicDBObject linksFromArticleQuery = new BasicDBObject(WikiLink.DB_SOURCE_ARTICLE, article.getId());
                
                article.cleanLinks();
                
                linksCursor = linksCollection.find(linksToArticleQuery).iterator();
                while (linksCursor.hasNext()) {
                    Document linkToArticleDB = linksCursor.next();
                    WikiLink linkTo = new WikiLink(linkToArticleDB);
                    article.addLinkTo(linkTo);
                }
                
                linksCursor = linksCollection.find(linksFromArticleQuery).iterator();
                while (linksCursor.hasNext()) {
                    Document linkFromArticleDB = linksCursor.next();
                    WikiLink linkFrom = new WikiLink(linkFromArticleDB);
                    article.addLinkFrom(linkFrom);
                }
                
                // Sort by the count
                article.setLinksFromInt(sortHashMapByValueDesc(article.getLinksFromInt()));
                article.setLinksToInt(sortHashMapByValueDesc(article.getLinksToInt()));
                
                currentStepProgress++;
                
                articlesCollection.replaceOne(
                    eq(WikiArticle.DB_ARTICLE__ID, article.getTitle()),
                    article.getMongoObjectForThisArticle(false)
                );
            }
        }
        
        // Remove the unnecessary collection at the end
        linksCollection.drop();
    }

    @Override
    public void saveWikiArticle(WikiArticle article, Boolean update) {
        MongoCollection<Document> articlesCollection;
        articlesCollection = mongoDatabase.getCollection(WikiCollections.ARTICLES.toString());

        if (article.isPageFromAnotherNamespace()) {
            return;
        }
        
        if (article.isARedirect() && article.redirectsToUnwantedPage()) {
            return;
        }
        
        if (update) {
            articlesCollection.replaceOne(
                eq(WikiArticle.DB_ARTICLE__ID, article.getTitle()),
                article.getMongoObjectForThisArticle(false)
            );
        } else {
            try {
                articlesCollection.insertOne(article.getMongoObjectForThisArticle(true));
            } catch (MongoWriteException ex) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                
                System.out.println(sdf.format(cal.getTime()) + "*** Duplicate article detected. Title: " + article.getTitle());
                System.out.println("More info: " + article.toString());
            } catch (Exception ex) {
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                
                System.out.println(sdf.format(cal.getTime()) + "*** Exception occured when adding article. Details below...");
                ex.printStackTrace();
            }
        }
        
    }
    
    private static <K, V extends Comparable<? super V>> Map<K, V> sortHashMapByValueDesc(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        
        Collections.sort(list, new Comparator<Map.Entry<K, V>>()
        {
            @Override
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put(entry.getKey(), entry.getValue());
        }
        
        return result;
    }

    @Override
    public String[] getDatabaseNames() {
        if (mongoClient != null) {
            List<String> databaseNames = new ArrayList<>();
            MongoCursor<String> dbsCursor = mongoClient.listDatabaseNames().iterator();
            while(dbsCursor.hasNext()) {
                String databaseName = dbsCursor.next();
                if (databaseName.startsWith(WIKI_DATABASE_PREFIX)) databaseNames.add(databaseName);
            }
            return databaseNames.toArray(new String[0]);
        }
        return new String[0];
    }

    @Override
    public boolean connectToDatabase(Integer databasePort) {
        try {
            MongoClientOptions.Builder optionsBuilder = MongoClientOptions.builder();
            optionsBuilder.serverSelectionTimeout(CONNECTION_TIMEOUT_MS);
            
            mongoClient = new MongoClient("localhost:" + databasePort, optionsBuilder.build());
            MongoDatabase database = mongoClient.getDatabase("admin");
            Document serverStatus = database.runCommand(new Document("serverStatus", 1));
        } catch (Exception ex) {
            mongoClient.close();
            return false;
        }
        return true;
    }

    @Override
    public Boolean removeDatabase(String dbName) {
        try {
            mongoClient.dropDatabase(dbName);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            return false;
        }
        return true;
    }
}
