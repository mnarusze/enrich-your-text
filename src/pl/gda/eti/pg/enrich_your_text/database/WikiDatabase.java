package pl.gda.eti.pg.enrich_your_text.database;

import pl.gda.eti.pg.enrich_your_text.database.mongodb.MongoWikiDB;
import pl.gda.eti.pg.enrich_your_text.database.mysql.MySQLWikiDB;
import pl.gda.eti.pg.enrich_your_text.extraction.WikipediaExtractor;
import pl.gda.eti.pg.enrich_your_text.models.WikiArticle;
import pl.gda.eti.pg.enrich_your_text.models.WikiLabel;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;

public abstract class WikiDatabase {
    protected Long currentStepMax;
    protected Long currentStepProgress;
    
    public static final String WIKI_DATABASE_PREFIX = "enrich-your-text-";
    
    // CONSTRUCTOR
    protected WikiDatabase() {
        currentStepProgress = 0L;
        currentStepMax = 0L;
    }
    
    public static WikiDatabase getNewDatabase(WikiDatabaseTypes databaseType) {
        switch (databaseType) {
            case MongoDB:
                return new MongoWikiDB();
            case MySQL:
                return new MySQLWikiDB();
            default:
                return null;
        }
    }
    
    public int getCurrentStepMax() {
        return (int)(currentStepMax / WikipediaExtractor.EXTRACTION_STEP_DIVISOR);
    }

    public int getCurrentStepProgress() {
        return (int)(currentStepProgress / WikipediaExtractor.EXTRACTION_STEP_DIVISOR);
    }
  
    // Database
    public enum WikiDatabaseTypes {

        MongoDB,
        MySQL;

        public static String[] names() {
            WikiDatabaseTypes[] values = values();
            String[] names = new String[values.length];

            for (int i = 0; i < values.length; i++) {
                names[i] = values[i].toString();
            }

            return names;
        }
    };

    /*
     * WIKIPEDIA - name, namespaces 
     * ARTICLES - name, links from[]/to[] this article, categories[]
     * LINKS - name + source and target article, always one
     * LABELS - name + target articles[]
     * STEMMED LABELS - stemmed name + target articles[]
     */
    public enum WikiCollections {

        WIKIPEDIA,
        ARTICLES,
        LINKS,
        LABELS,
        LABELS_STEMMED;
        
        public static String[] names() {
            WikiCollections[] values = values();
            String[] names = new String[values.length];

            for (int i = 0; i < values.length; i++) {
                names[i] = values[i].name();
            }

            return names;
        }

        @Override
        public String toString() {
            return super.toString().substring(0, 1).toUpperCase() + super.toString().substring(1).toLowerCase().replaceAll("_", " ");
        }
    };
    
    // DATABASE
    public abstract boolean connectToDatabase(Integer databasePort);
    public abstract void closeDatabase();
    public abstract String[] getDatabaseNames();
    public abstract Boolean openDatabase(String dbName);
    public abstract Boolean initializeDatabase(WikipediaExtractor.ExtractionStep startingStep, Boolean forceIfExists, Boolean addIndexes);
    public abstract void saveDatabase();
    public abstract Boolean removeDatabase(String dbName);
    public abstract Long getCountTable(WikiCollections table);
    
    /*
     * Label   - if the link points to another namespace or disambiguation page, skip it
     *         - always add labels even if another one already exists, we'll deal with it later
     */
    public abstract void saveWikiLinks(WikiArticle article, Boolean stem);
    public abstract void aggregateLabels();
    // For every article get all links from it and to it, then add them to
    // article and update it.
    public abstract void setLinksFromTo();
    public abstract WikiLabel getWikiLabelForGivenNGram(String ngram, WikiCollections labelsType);
    /*
     * Article - if any namespace, don't store
     *         - if a regular article, just insert
     *         - if a redirect and the redirect doesn't point to unwanted page
     *           (disambiguation or another namespace), store it like a label
     *         - if a disambiguation page, then store the links like labels but
     *           remove the disambiguation suffix
     */     
    public abstract void saveWikiArticle(WikiArticle article, Boolean update);
    public abstract WikiArticle getArticleByID(Integer targetArticle);
    public abstract WikiArticle findRealArticleByName(String labelName);
    
    // WIKIPEDIA
    public abstract Wikipedia loadWikipedia();
    public abstract void saveWikipedia(Wikipedia wikipedia);
}
