package pl.gda.eti.pg.enrich_your_text.models;

import java.util.regex.Pattern;
import org.bson.Document;
import pl.gda.eti.pg.enrich_your_text.textprocessing.tools.TextProcessor;


/*
 * Link can be a:
 * - Regular link to another article
 * - Special link to another namespace
 * - Category at the end of the article
 * 
 * We should accept any type and set proper fields.
 */
public class WikiLink {

    public static final String DB__ID = "_id";
    public static final String DB_LABEL_NAME = "nm";
    public static final String DB_SOURCE_ARTICLE = "srcArt";
    public static final String DB_TARGET_ARTICLE = "tgtArt";
    public static final String DB_TARGET_ARTICLES = "tgtArts";
    public static final String DB_USAGE_COUNT = "cnt";
    
    /*
     * Notes about the pattern:
     * 
     * 1) We don't want to capture "links" which hold links inside of them -
     * only capture the internal ones.
     * 
     * 2) We __do__not__ want prefixes, but we __do__ want suffixes (if there
     * is any suffix)
     */
    public static final String WIKI_LINK_REGEX = "\\[\\[[^\\[\\]]+\\]\\][\\w]*";
    
    private final static Pattern WIKI_SUFFIX_PATTERN = Pattern.compile("\\]\\].*");
    private final static Pattern WIKI_LINK_MARKUP_PATTERN = Pattern.compile("[\\[\\]]");
    private final static Pattern WIKI_LINK_INSIDE_PARENTHESES_PATTERN = Pattern.compile("\\(.*\\)");
    private final static Pattern WIKI_LINK_AFTER_COMA_PATTERN = Pattern.compile(",.*");
    private final static Pattern WIKI_LINK_NAMESPACE_PATTERN = Pattern.compile(".*:");
    
    private String targetArticleName;
    private Integer targetArticleID;
    private Integer sourceArticleID;
    private String labelName;
    private String anchorName;
    private String namespace;

    private final String fullLink;
    private Boolean pointsToDisambiguationPage;

    /*
     * This constructor should be used when reading links from the XML file.
     * fullLink should be the original Wikipedia-style link taken from the
     * page's source including suffixes.
     *
     * http://en.wikipedia.org/wiki/Help:Link
     * http://en.wikipedia.org/wiki/Help:Pipe_trick
     *
     * TODO - reverse pipe trick
     */
    public WikiLink(String fullLink, Integer sourceArticle) {
        String suffix = "";
        String fullLinkCopy;
        Integer colonIndex;

        this.fullLink = fullLinkCopy = TextProcessor.cleanupWikiLink(fullLink);

        targetArticleName = "";
        labelName = "";
        anchorName = "";
        namespace = "";
        pointsToDisambiguationPage = false;
        targetArticleID = -1;

        // We don't want any prefixes
        if (!fullLink.startsWith("[")) {
            System.err.println("Faulty link " + fullLink + " with a prefix detected");
        }

        // Check if there is any suffix - if yes, check if they are literals
        if (!fullLink.endsWith("]")) {
            suffix = fullLink.substring(fullLink.lastIndexOf(']') + 1);
            if (!suffix.matches("[a-zA-Z]+")) {
                suffix = "";
            }
        }

        // Remove the suffix & link markup
        fullLinkCopy = WIKI_SUFFIX_PATTERN.matcher(fullLinkCopy).replaceAll("\\]\\]");
        fullLinkCopy = WIKI_LINK_MARKUP_PATTERN.matcher(fullLinkCopy).replaceAll("");

        // Several possibilities for colons: language, namespace (or both), 
        // category or just a title with a colon
        colonIndex = fullLinkCopy.indexOf(":");
        if (colonIndex == 0) {
            /* Colon trick - the link won't get translated to a special
             * page, it will rather produce a regular link to namespace.
             * We will probably ignore this namespace anyways.
             */
            fullLinkCopy = fullLinkCopy.substring(1);
            colonIndex = fullLinkCopy.indexOf(":");
        }
        if (colonIndex > 0) {
            String tmpNamespace = fullLinkCopy.substring(0, colonIndex);
            if (Wikipedia.getInstance().isASpecialPrefix(tmpNamespace)) {
                /*
                 * Recognized namespace/interwiki, so we don't really care about
                 * the rest
                 */
                namespace = tmpNamespace;
                fullLinkCopy = fullLinkCopy.substring(colonIndex + 1);
            }
        }
        int tmpLastIdxOf = fullLinkCopy.lastIndexOf('|');
        if (tmpLastIdxOf > 0) {
            labelName = fullLinkCopy.substring(tmpLastIdxOf + 1);
            fullLinkCopy = fullLinkCopy.substring(0, tmpLastIdxOf);
        }

        tmpLastIdxOf = fullLinkCopy.lastIndexOf('#');
        if (fullLinkCopy.lastIndexOf('#') > 0) {
            anchorName = fullLinkCopy.substring(tmpLastIdxOf + 1);
            fullLinkCopy = fullLinkCopy.substring(0, tmpLastIdxOf);
        }

        targetArticleName = fullLinkCopy;

        if (fullLink.indexOf('|') > 0 && labelName.trim().length() == 0) {
            // Pipe trick - hide namespace, comma and stuff in parentheses
            labelName = WIKI_LINK_INSIDE_PARENTHESES_PATTERN.matcher(targetArticleName).replaceAll("");
            labelName = WIKI_LINK_AFTER_COMA_PATTERN.matcher(labelName).replaceAll("");
            labelName = WIKI_LINK_NAMESPACE_PATTERN.matcher(labelName).replaceAll("").trim();
        }

        if (labelName.length() == 0) {
            labelName = targetArticleName;
        }

        if (suffix.length() > 0) {
            labelName = labelName + suffix;
        }

        // All articles start have first letter in uppercase & all spaces are changed to underscored
        targetArticleName = targetArticleName.replace(" ", "_");
        
        // The targetArticle might be empty, for instance with [[:en:]]
        if (targetArticleName.length() > 0) {
            targetArticleName = targetArticleName.substring(0, 1).toUpperCase() + targetArticleName.substring(1);
        }
        
        if (targetArticleName.toLowerCase().endsWith(WikiArticle.DISAMBIGUATION_SUFFIX)) {
            this.pointsToDisambiguationPage = true;
        }
    }
    
    public WikiLink(String labelStr, Integer sourceArticleID, Integer targetArticleID) {
        this.fullLink = "";
        this.labelName = labelStr;
        this.targetArticleID = targetArticleID;
        this.sourceArticleID = sourceArticleID;
    }

    public WikiLink(String link) {
        this(link, 0);
    }
    
    public WikiLink(Document dbObject) {
        this.fullLink = "";
        this.labelName = (String) dbObject.get(DB_LABEL_NAME);
        this.sourceArticleID = (Integer) dbObject.get(DB_SOURCE_ARTICLE);
        this.targetArticleID = (Integer) dbObject.get(DB_TARGET_ARTICLE);
    }
    
    public Document getMongoObjectForThisLink() {
        Document dbLabel = new Document();
        
        dbLabel.append(WikiLabel.DB_LABEL_NAME, this.labelName);
        dbLabel.append(WikiLabel.DB_SOURCE_ARTICLE, this.sourceArticleID);
        dbLabel.append(WikiLabel.DB_TARGET_ARTICLE, this.targetArticleID);

        return dbLabel;
    }

    public String getTargetArticleName() {
        return targetArticleName;
    }

    public String getLabelName() {
        return labelName;
    }
    
    public String getAnchorName() {
        return anchorName;
    }

    public Boolean pointsToAnotherNamespace() {
        return namespace.length() > 0;
    }
    
    public Boolean pointsToDisambiguationPage() {
        return this.pointsToDisambiguationPage;
    }
    
    public Boolean isAnUnwantedLink() {
        return this.pointsToAnotherNamespace() || this.pointsToDisambiguationPage();
    }

    public String getNamespace() {
        return namespace;
    }

    public Boolean isACategory() {
        return namespace.equalsIgnoreCase("category");
    }

    public Boolean hasAnchor() {
        return anchorName.length() > 0;
    }

    public String getFullLink() {
        return fullLink;
    }

    @Override
    public String toString() {
        StringBuilder strBld = new StringBuilder();
        strBld.append("Full link ")
                .append(this.getFullLink())
                .append(" links to ")
                .append(this.getTargetArticleName())
                .append(" labelled as ")
                .append(this.getLabelName());

        if (this.isACategory()) {
            strBld.append("  This is a category!");
        }

        if (this.pointsToAnotherNamespace()) {
            strBld.append("  It has a namespace - ").
                    append(this.getNamespace());
        }

        if (this.hasAnchor()) {
            strBld.append("  There is an anchor in the link - ").
                    append(this.getAnchorName());
        }
        return strBld.toString();
    }

    public Integer getTargetArticleID() {
        return targetArticleID;
    }

    public Integer getSourceArticleID() {
        return sourceArticleID;
    }

    public void setTargetArticleName(String targetArticleName) {
        this.targetArticleName = targetArticleName;
    }

    public void setTargetArticleID(int targetArticleID) {
        this.targetArticleID = targetArticleID;
    }
}
