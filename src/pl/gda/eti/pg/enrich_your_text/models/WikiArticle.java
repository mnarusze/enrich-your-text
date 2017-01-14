package pl.gda.eti.pg.enrich_your_text.models;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bson.Document;
import pl.gda.eti.pg.enrich_your_text.textprocessing.tools.TextProcessor;

public class WikiArticle {
    public static final String DB_ARTICLE_LINKS_TO = "lnTo";
    public static final String DB_ARTICLE_LINKS_FROM = "lnFr";
    public static final String DB_ARTICLE_CATEGORIES = "ctg";
    public static final String DB_ARTICLE_ID = "artId";
    public static final String DB_ARTICLE_COUNT = "cnt";
    public static final String DB_ARTICLE__ID = "_id";
    public static final String DB_REDIRECTS_TO = "rdr";
    public static final String DB_DISAMBIGUATION_PAGE = "dmb";
    
    private int id;
    private String title;
    private ArrayList<String> categories;
    
    private ArrayList<WikiLink> myLinks;
    private String text;
    private String cleanText;
    private String redirectsTo;
    private String namespace;
    private Boolean isADisambiguationPage;

    private Map<String, Integer> linksFromStr; // this isn't stored in DB
    private Map<Integer, Integer> linksFromInt;
    private Map<Integer, Integer> linksToInt;
    
    private final static Pattern LINK_PATTERN = Pattern.compile(WikiLink.WIKI_LINK_REGEX);
    private final static Pattern SPACE_PATTERN = Pattern.compile(" ");
    
    public final static String DISAMBIGUATION_SUFFIX = "(disambiguation)";

    public WikiArticle() {
        this.id = 0;
        this.title = "";
        this.text = "";
        this.cleanText = "";
        this.redirectsTo = "";
        this.namespace = "";
        this.isADisambiguationPage = false;
        this.categories = new ArrayList<>();
        this.myLinks = new ArrayList<>();
        this.linksFromStr = new LinkedHashMap<>();
        this.linksFromInt = new LinkedHashMap<>();
        this.linksToInt = new LinkedHashMap<>();
    }

    public WikiArticle(Document articleObj) {
        this.linksFromInt = new LinkedHashMap<>();
        List<Document> linksFromDB = (ArrayList) articleObj.get(DB_ARTICLE_LINKS_FROM);
        if (linksFromDB != null && linksFromDB.size() > 0) {
            for (Document linkFromDB : linksFromDB.toArray(new Document[0])) {
                this.linksFromInt.put(linkFromDB.getInteger(DB_ARTICLE_ID), linkFromDB.getInteger(DB_ARTICLE_COUNT));
            }
        }
        
        this.linksToInt = new LinkedHashMap<>();
        List<Document> linksToDB = (ArrayList) articleObj.get(DB_ARTICLE_LINKS_TO);
        if (linksToDB != null && linksToDB.size() > 0) {
            for (Document linkTo : linksToDB) {
                this.linksToInt.put(linkTo.getInteger(DB_ARTICLE_ID), linkTo.getInteger(DB_ARTICLE_COUNT));
            }
        }
        
        this.categories = (ArrayList<String>) articleObj.get(DB_ARTICLE_CATEGORIES);
        if (this.categories == null) {
            this.categories = new ArrayList<>();
        }
        if (articleObj.get(DB_ARTICLE_ID) != null) {
            this.id = (int) articleObj.get(DB_ARTICLE_ID);
        }
        this.title = (String) articleObj.get(DB_ARTICLE__ID);
        this.isADisambiguationPage = (Boolean) articleObj.get(DB_DISAMBIGUATION_PAGE);
        if (this.isADisambiguationPage == null) {
            this.isADisambiguationPage = false;
        }
        this.redirectsTo = (String) articleObj.get(DB_REDIRECTS_TO);
        if (this.redirectsTo == null) {
            this.redirectsTo = "";
        }
        this.namespace = "";
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        Integer colonPos = title.indexOf(':');
        String newTitle = title;

        if (colonPos > 0) {
            String mbyNamespace = newTitle.substring(0, colonPos);
            Wikipedia wiki = Wikipedia.getInstance();
            if (wiki.isASpecialPrefix(mbyNamespace)) {
                this.namespace = mbyNamespace;
                newTitle = newTitle.substring(colonPos + 1);
            }
        }

        if (newTitle.toLowerCase().endsWith(DISAMBIGUATION_SUFFIX)) {
            this.isADisambiguationPage = true;
        }

        newTitle = newTitle.trim();
        newTitle = SPACE_PATTERN.matcher(newTitle).replaceAll("_");

        this.title = newTitle;
    }

    public void setRedirect(String redirectTo) {
        this.redirectsTo = SPACE_PATTERN.matcher(redirectTo).replaceAll("_");
    }

    public String getRedirectTo() {
        return redirectsTo;
    }

    @Override
    public boolean equals(Object obj) {
        return this.title.equals(((WikiArticle)obj).getTitle());
    }

    public boolean isARedirect() {
        return redirectsTo.length() > 0;
    }

    /*
     * By 'unwanted' we mean another namespace or disambiguation page
     */
    public boolean redirectsToUnwantedPage() {
        if (redirectsTo.toLowerCase().endsWith("(disambiguation)")) {
            return true;
        }
        int colonIdx = redirectsTo.indexOf(":");
        if (colonIdx != -1) {
            String prefix = redirectsTo.substring(0, colonIdx).trim();
            Wikipedia wiki = Wikipedia.getInstance();
            if (wiki.isASpecialPrefix(prefix)) {
                return true;
            }
        }

        return false;
    }

    public boolean isPageFromAnotherNamespace() {
        return this.namespace.length() > 0;
    }

    public void addText(String newText) {
        text += newText;
    }

    private void cleanText() {
        if (this.cleanText.isEmpty() && !this.text.isEmpty()) {
            this.cleanText = TextProcessor.cleanWikiArticle(this.text, true);
        }
    }
    
    public String getCleanText() {
        cleanText();
        return cleanText;
    }
    
    public ArrayList<WikiLink> getWikiLinks() {
        return myLinks;
    }

    /*
     * The extraction gets rid of all unwanted links, sets the categories and
     * regular links.
     */
    public void extractLinks() {
        Matcher m = LINK_PATTERN.matcher(this.text);
        while (m.find()) {
            WikiLink link = new WikiLink(m.group(), this.id);
            String targetArticle = link.getTargetArticleName();
            
            if (targetArticle.isEmpty()) {
                continue;
            }

            if (link.isACategory()) {
                this.categories.add(targetArticle);
                continue;
            }

            if (link.pointsToAnotherNamespace()) {
                continue;
            }

            this.myLinks.add(link);
            
            this.linksFromStr.put(targetArticle, this.linksFromStr.getOrDefault(targetArticle, 0) + 1);
        }
    }

    /*
     * This function returns an object ready to store in database. It assumes we
     * have already cleaned the links and categories and that we also want to 
     * store the article, so before using this function check if it's a category
     * or something special or just a regular article.
     */
    public Document getMongoObjectForThisArticle(Boolean includeID) {
        Document article = new Document();
        List<Document> linksFromDB = new ArrayList();
        List<Document> linksToDB = new ArrayList();
        
        if (includeID)
            article.append(DB_ARTICLE__ID, this.title);
        article.append(DB_ARTICLE_ID, this.id);
        
        for (Integer linkFrom : this.linksFromInt.keySet()) {
            Document targetArticle = new Document()
                    .append(DB_ARTICLE_ID, linkFrom)
                    .append(DB_ARTICLE_COUNT, this.linksFromInt.getOrDefault(linkFrom, 0));
            linksFromDB.add(targetArticle);
        }
        if (!linksFromDB.isEmpty()) article.append(DB_ARTICLE_LINKS_FROM, linksFromDB);
        
        for (Integer linkTo : this.linksToInt.keySet()) {
            Document targetArticle = new Document()
                    .append(DB_ARTICLE_ID, linkTo)
                    .append(DB_ARTICLE_COUNT, this.linksToInt.getOrDefault(linkTo, 0));
            linksToDB.add(targetArticle);
        }
        
        if (!linksToDB.isEmpty()) article.append(DB_ARTICLE_LINKS_TO, linksToDB);
        
        article.append(DB_ARTICLE_CATEGORIES, this.categories);
        
        if (this.isADisambiguationPage) {
            article.append(DB_DISAMBIGUATION_PAGE, this.isADisambiguationPage);
        }
        if (this.isARedirect()) {
            article.append(DB_REDIRECTS_TO, this.redirectsTo);
        }

        return article;
    }
    
    public Document getMongoFilterQueryForThisArticle() {
        Document article = new Document();
        
        article.append(DB_ARTICLE__ID, this.title);
        
        return article;
    }

    @Override
    public String toString() {
        if (isARedirect()) {
            return "Redirect to " + redirectsTo;
        }
        StringBuilder description = new StringBuilder();
        description.append("Title: ").append(title).append("\n");
        if (isADisambiguationPage()) {
            description.append("Disambiguation page\n");
        }
        description.append("Links:");
        for (WikiLink link : myLinks) {
            description.append(" ").append(link.getLabelName()).append(" -> ").append(link.getTargetArticleName()).append("\n");
        }
        return description.toString();
    }

    public Map<Integer, Integer> getLinksFromInt() {
        return linksFromInt;
    }

    public Map<Integer, Integer> getLinksToInt() {
        return linksToInt;
    }

    public void setLinksToInt(Map<Integer, Integer> linksToInt) {
        this.linksToInt = linksToInt;
    }

    public void setLinksFromInt(Map<Integer, Integer> linksFromInt) {
        this.linksFromInt = linksFromInt;
    }

    public Boolean isADisambiguationPage() {
        return this.isADisambiguationPage;
    }

    /* 
     * Main function returning how similar two articles are to each other.
     * Returns 1 for identical documents and 0 for totally unrelated ones.
     * The closer to 1, the more similar the articles are.
     */
    public static double getTopicProximityBetweenArts(Integer id1, Integer id2, Set<Integer> linksTo1, Set<Integer> linksTo2) {
        Integer commonLinksCount = 0;

        // Titles are unique so return 0 if the articles are the same 
        if (id1.equals(id2)) {
            return 1.0;
        }
        
        // If any of the articles is never linked to then ignore this corelation
        if (linksTo1.isEmpty() || linksTo2.isEmpty()) {
            return 0.0;
        }
        
        // Count set of common links
        for (Integer linkToThis : linksTo1) {
            if (linksTo2.contains(linkToThis)) {
                commonLinksCount++;
            }
        }

        Integer linksToSizeMax = Math.max(linksTo1.size(), linksTo2.size());
        Integer linksToSizeMin = Math.min(linksTo1.size(), linksTo2.size());

        if (linksToSizeMax == 0 || linksToSizeMin == 0 || commonLinksCount == 0) {
            return 0.0;
        }

        double numerator = Math.log(linksToSizeMax) - Math.log(commonLinksCount);
        double denominator = Math.log(Wikipedia.getInstance().getArticlesCount()) - Math.log(linksToSizeMin);

        return 1 - numerator / denominator;
    }
    
    public void addLinkFrom(WikiLink linkFromThis) {
        Integer targetArticleID = linkFromThis.getTargetArticleID();
        this.linksFromInt.put(targetArticleID, this.linksFromInt.getOrDefault(targetArticleID, 0) + 1);
    }
    
    public void addLinkTo(WikiLink linkToThis) {
        Integer sourceArticleID = linkToThis.getSourceArticleID();
        this.linksToInt.put(sourceArticleID, this.linksToInt.getOrDefault(sourceArticleID, 0) + 1);
    }
    
    public void cleanLinks() {
        this.linksFromInt = new LinkedHashMap<>();
        this.linksToInt = new LinkedHashMap<>();
    }
}
