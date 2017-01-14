package pl.gda.eti.pg.enrich_your_text.models;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.bson.Document;
import org.bson.types.ObjectId;
import pl.gda.eti.pg.enrich_your_text.database.WikiDatabase;
import pl.gda.eti.pg.enrich_your_text.database.WikiDatabase.WikiCollections;
import pl.gda.eti.pg.enrich_your_text.extraction.WikipediaExtractor;
import pl.gda.eti.pg.enrich_your_text.settings.Configuration;

public class Wikipedia {

    public static final String DB_WIKIPEDIA__ID = "_id";
    public static final String DB_WIKIPEDIA_NAME = "name";
    public static final String DB_WIKIPEDIA_BASE = "base";
    public static final String DB_WIKIPEDIA_STEMMED = "stemmed";
    public static final String DB_WIKIPEDIA_NAMESPACES = "namespaces";

    private static Wikipedia instance = null;

    private final WikiDatabase database;

    private final ArrayList<String> stopWords;

    private String name;
    private String base;
    private ObjectId _id;
    private Boolean stemmed;

    private Long articlesCount;

    private ArrayList<String> namespaces;
    /* Caseless */
    private ArrayList<String> interWikiPrefixes;
    /* Caseless */
    private ArrayList<String> namespaceAliases;
    /* Case sensitive */
    private ArrayList<String> pseudoNamespaces;

    
    private Wikipedia() {
        this.stopWords = new ArrayList<>();
        this.database = WikiDatabase.getNewDatabase(Configuration.currentDatabaseType);
        this.namespaces = new ArrayList<>();
        this.articlesCount = 0L;

        this._id = null;
        this.name = "";
        this.base = "";
        this.stemmed = false;

        addDefaultInterwikiLInks();
        addDefaultAliasesAndPseudoNamespaces();
    }

    public Wikipedia(Document wikipedia) {
        this.stopWords = new ArrayList<>();
        this.database = WikiDatabase.getNewDatabase(Configuration.currentDatabaseType);

        this._id = (ObjectId) wikipedia.get(DB_WIKIPEDIA__ID);
        this.name = (String) wikipedia.get(DB_WIKIPEDIA_NAME);
        this.base = (String) wikipedia.get(DB_WIKIPEDIA_BASE);
        this.base = this.base.substring(0, this.base.lastIndexOf("/"));
        this.stemmed = (Boolean) wikipedia.get(DB_WIKIPEDIA_STEMMED);
        this.namespaces = (ArrayList<String>) wikipedia.get(DB_WIKIPEDIA_NAMESPACES);
    }
    
    /* 
     * List from http://meta.wikimedia.org/wiki/Special:Interwiki 
     * as of 12.10.2014 
     * TODO we should read this from a dump or configuration file
     */
    private void addDefaultInterwikiLInks() {
        String interWikiLinks[] = {
            "acronym", "advisory", "advogato", "aew", "appropedia", "aquariumwiki",
            "arborwiki", "arxiv", "atmwiki", "b", "baden", "battlestarwiki", "bcnbio",
            "beacha", "betawiki", "betawikiversity", "biblewiki", "bluwiki", "blw",
            "botwiki", "boxrec", "brickwiki", "bugzilla", "bulba", "buzztard",
            "bytesmiths", "c", "c2", "c2find", "cache", "cellwiki", "centralwikia",
            "chapter", "chej", "choralwiki", "citizendium", "ckwiss", "cmn", "comixpedia",
            "commons", "communityscheme", "communitywiki", "comune", "corpknowpedia",
            "crazyhacks", "creativecommons", "creativecommonswiki", "creatureswiki",
            "cxej", "cz", "d", "dbdump", "dcc", "dcdatabase", "dcma", "delicious",
            "devmo", "dict", "dictionary", "disinfopedia", "distributedproofreaders",
            "distributedproofreadersca", "dk", "dmoz", "dmozs", "docbook", "doi",
            "donate", "doom_wiki", "download", "dpd", "drae", "dreamhost", "drumcorpswiki",
            "dwjwiki", "ecoreality", "ecxei", "elibre", "emacswiki", "encyc",
            "energiewiki", "englyphwiki", "enkol", "eokulturcentro", "epo", "esolang",
            "etherpad", "ethnologue", "ethnologuefamily", "evowiki", "exotica",
            "eĉei", "fanimutationwiki", "finalempire", "finalfantasy", "finnix",
            "flickrphoto", "flickruser", "floralwiki", "flyerwiki-de", "foldoc",
            "forthfreak", "foundation", "foxwiki", "freebio", "freebsdman",
            "freeculturewiki", "freedomdefined", "freefeel", "freekiwiki", "freenode",
            "freesoft", "ganfyd", "gardenology", "gausswiki", "gentoo", "genwiki",
            "gerrit", "git", "globalvoices", "glossarwiki", "glossarywiki", "google",
            "googledefine", "googlegroups", "gotamac", "greatlakeswiki",
            "guildwarswiki", "guildwiki", "gutenberg", "gutenbergwiki", "h2wiki",
            "hackerspaces", "hammondwiki", "heroeswiki", "hrfwiki", "hrwiki",
            "hupwiki", "iarchive", "imdbcharacter", "imdbcompany", "imdbname",
            "imdbtitle", "incubator", "infosecpedia", "infosphere", "irc", "ircrc",
            "iso639-3", "issn", "iuridictum", "jaglyphwiki", "jameshoward",
            "javanet", "javapedia", "jefo", "jerseydatabase", "jiniwiki", "jira",
            "jp", "jspwiki", "jstor", "kamelo", "karlsruhe", "kerimwiki", "kinowiki",
            "kmwiki", "komicawiki", "kontuwiki", "koslarwiki", "kpopwiki",
            "labsconsole", "libreplanet", "linguistlist", "linuxwiki", "linuxwikide",
            "liswiki", "literateprograms", "livepedia", "lojban", "lostpedia",
            "lqwiki", "lugkr", "luxo", "m", "mail", "mailarchive", "mariowiki",
            "marveldatabase", "meatball", "mediawikiwiki", "mediazilla", "memoryalpha",
            "meta", "metawiki", "metawikipedia", "mineralienatlas", "minnan",
            "moinmoin", "monstropedia", "mosapedia", "mozcom", "mozillawiki",
            "mozillazinekb", "musicbrainz", "mw", "mwod", "mwot", "n", "nara",
            "nkcells", "nosmoke", "nost", "nostalgia", "oeis", "oldwikisource",
            "olpc", "onelook", "openfacts", "openlibrary", "openstreetmap",
            "openwetware", "openwiki", "opera7wiki", "organicdesign", "orthodoxwiki",
            "osi_reference_model", "osmwiki", "otrs", "otrswiki", "ourmedia",
            "outreach", "outreachwiki", "panawiki", "patwiki", "perlnet",
            "personaltelco", "phab", "phabricator", "phpwiki", "phwiki",
            "planetmath", "pmeg", "proofwiki", "psycle", "pyrev", "pythoninfo",
            "pythonwiki", "pywiki", "q", "quality", "rcirc", "reuterswiki", "rev",
            "revo", "rfc", "rheinneckar", "robowiki", "rowiki", "rt", "rtfm", "s",
            "s23wiki", "scholar", "schoolswp", "scores", "scoutwiki", "scramble",
            "seapig", "seattlewiki", "seattlewireless", "sector001", "securewikidc",
            "semantic-mw", "senseislibrary", "sep11", "sharemap", "silcode",
            "slashdot", "slwiki", "smikipedia", "sourceforge", "spcom", "species",
            "squeak", "stats", "stewardry", "strategy", "strategywiki", "sulutil",
            "svgwiki", "svn", "swinbrain", "swtrain", "tabwiki", "tavi", "tclerswiki",
            "technorati", "tenwiki", "test2wiki", "testwiki", "testwikidata",
            "tfwiki", "thelemapedia", "theopedia", "thinkwiki", "tibiawiki",
            "ticket", "tmbw", "tmnet", "tmwiki", "toollabs", "tools", "translatewiki",
            "tswiki", "tviv", "tvtropes", "twiki", "tyvawiki", "uncyclopedia",
            "unihan", "unreal", "urbandict", "usability", "usej", "usemod", "v",
            "viaf", "vinismo", "vkol", "vlos", "voipinfo", "voy", "w", "webisodes",
            "werelate", "wg", "wikia", "wikiapiary", "wikiasite", "wikibooks",
            "wikichristian", "wikicities", "wikicity", "wikidata", "wikif1",
            "wikifur", "wikihow", "wikiindex", "wikilemon", "wikilivres",
            "wikimac-de", "wikimedia", "wikinews", "wikinfo", "wikinvest",
            "wikiotics", "wikipaltz", "wikipedia", "wikipediawikipedia",
            "wikiquote", "wikischool", "wikiskripta", "wikisophia", "wikisource",
            "wikispecies", "wikispot", "wikitech", "wikiti", "wikitree",
            "wikiversity", "wikivoyage", "wikiweet", "wikiwikiweb", "wikt",
            "wiktionary", "wipipedia", "wlug", "wm2005", "wm2006", "wm2007",
            "wm2008", "wm2009", "wm2010", "wm2011", "wm2012", "wm2013", "wm2014",
            "wm2015", "wmania", "wmar", "wmau", "wmbd", "wmbe", "wmbr", "wmca",
            "wmch", "wmcl", "wmco", "wmcz", "wmdc", "wmde", "wmdeblog", "wmdk",
            "wmee", "wmes", "wmet", "wmf", "wmfblog", "wmfi", "wmfr", "wmhk",
            "wmhu", "wmid", "wmil", "wmin", "wmit", "wmke", "wmmk", "wmmx", "wmnl",
            "wmno", "wmnyc", "wmpa-us", "wmph", "wmpl", "wmpt", "wmrs", "wmru",
            "wmse", "wmsk", "wmteam", "wmtr", "wmtw", "wmua", "wmuk", "wmve", "wmza",
            "wookieepedia", "wowwiki", "wqy", "wurmpedia", "zh-cfr", "zrhwiki",
            "zum", "zwiki", "ĉej", "aa", "ab", "ace", "af", "ak", "als", "am",
            "an", "ang", "ar", "arc", "arz", "as", "ast", "av", "ay", "az", "ba", "bar",
            "bat-smg", "bcl", "be", "be-tarask", "be-x-old", "bg", "bh", "bi", "bjn",
            "bm", "bn", "bo", "bpy", "br", "bs", "bug", "bxr", "ca", "cbk-zam",
            "cdo", "ce", "ceb", "ch", "cho", "chr", "chy", "ckb", "co", "cr", "crh",
            "cs", "csb", "cu", "cv", "cy", "da", "de", "diq", "dsb", "dv", "dz",
            "ee", "egl", "el", "eml", "en", "eo", "es", "et", "eu", "ext", "fa",
            "ff", "fi", "fiu-vro", "fj", "fo", "fr", "frp", "frr", "fur", "fy",
            "ga", "gag", "gan", "gd", "gl", "glk", "gn", "got", "gsw", "gu", "gv",
            "ha", "hak", "haw", "he", "hi", "hif", "ho", "hr", "hsb", "ht", "hu",
            "hy", "hz", "ia", "id", "ie", "ig", "ii", "ik", "ilo", "io", "is",
            "it", "iu", "ja", "jbo", "jv", "ka", "kaa", "kab", "kbd", "kg", "ki",
            "kj", "kk", "kl", "km", "kn", "ko", "koi", "kr", "krc", "ks", "ksh",
            "ku", "kv", "kw", "ky", "la", "lad", "lb", "lbe", "lez", "lg", "li",
            "lij", "lmo", "ln", "lo", "lt", "ltg", "lv", "lzh", "map-bms", "mdf",
            "mg", "mh", "mhr", "mi", "min", "mk", "ml", "mn", "mo", "mr", "mrj",
            "ms", "mt", "mus", "mwl", "my", "myv", "mzn", "na", "nah", "nan",
            "nap", "nb", "nds", "nds-nl", "ne", "new", "ng", "nl", "nn", "no",
            "nov", "nrm", "nso", "nv", "ny", "oc", "om", "or", "os", "pa", "pag",
            "pam", "pap", "pcd", "pdc", "pfl", "pi", "pih", "pl", "pms", "pnb",
            "pnt", "ps", "pt", "qu", "rm", "rmy", "rn", "ro", "roa-rup", "roa-tara",
            "ru", "rue", "rup", "rw", "sa", "sah", "sc", "scn", "sco", "sd", "se",
            "sg", "sgs", "sh", "si", "simple", "sk", "sl", "sm", "sn", "so", "sq",
            "sr", "srn", "ss", "st", "stq", "su", "sv", "sw", "szl", "ta", "te",
            "tet", "tg", "th", "ti", "tk", "tl", "tn", "to", "tpi", "tr", "ts",
            "tt", "tum", "tw", "ty", "tyv", "udm", "ug", "uk", "ur", "uz", "ve",
            "vec", "vep", "vi", "vls", "vo", "vro", "wa", "war", "wo", "wuu",
            "xal", "xh", "xmf", "yi", "yo", "yue", "za", "zea", "zh", "zh-classical",
            "zh-cn", "zh-min-nan", "zh-tw", "zh-yue", "zu"
        };

        interWikiPrefixes = new ArrayList<>(Arrays.asList(interWikiLinks));
    }

    /* 
     * List from http://en.wikipedia.org/wiki/Wikipedia:Shortcut#List_of_prefixes 
     * as of 12.10.2014 
     * TODO we should read this from a dump or configuration file
     */
    private void addDefaultAliasesAndPseudoNamespaces() {
        String namespaceAliasesStr[] = {
            "wp", "wt", "project", "project talk", "image", "image talk"
        };

        String pseudoNamespacesStr[] = {
            "CAT", "H", "MOS", "P", "T", "MP", "WikiProject", "Wikiproject", "MoS", "Mos"
        };

        namespaceAliases = new ArrayList<>(Arrays.asList(namespaceAliasesStr));
        pseudoNamespaces = new ArrayList<>(Arrays.asList(pseudoNamespacesStr));
    }

    public void loadInitialData() {
        loadStopWords();
    }

    public static Wikipedia getInstance() {
        if (instance == null) {
            instance = new Wikipedia();
        }
        return instance;
    }

    private void loadStopWords() {
        Scanner scanner;
        InputStream in = getClass().getClassLoader().
                getResourceAsStream(Configuration.STOP_WORDS_FILE);

        scanner = new Scanner(in);
        while (scanner.hasNext()) {
            stopWords.add(scanner.next());
        }
        scanner.close();
    }

    public boolean isAStopWord(String word) {
        return stopWords.contains(word);
    }

    public ArrayList<String> getNamespaces() {
        return namespaces;
    }

    public Boolean isAnInterwikiLink(String prefix) {
        return interWikiPrefixes.contains(prefix.toLowerCase());
    }

    public Boolean isAnAliasOrPseudoNamespace(String prefix) {
        return namespaceAliases.contains(prefix.toLowerCase()) || pseudoNamespaces.contains(prefix);
    }

    public Boolean isANamespace(String prefix) {
        for (String namespace : namespaces) {
            if (namespace.equalsIgnoreCase(prefix)) {
                return true;
            }
        }
        return false;
    }

    public Boolean isASpecialPrefix(String prefix) {
        return isANamespace(prefix) || isAnInterwikiLink(prefix) || isAnAliasOrPseudoNamespace(prefix);
    }

    public Long getArticlesCount() {
        return articlesCount;
    }

    public void countArticles() {
        articlesCount = database.getCountTable(WikiCollections.ARTICLES);
    }

    public void addNamespace(String newNamespace) {
        if (newNamespace.trim().isEmpty() || namespaces.contains(newNamespace)) {
            return;
        }
        namespaces.add(newNamespace);
    }

    public void setNamespaces(ArrayList<String> namespaces) {
        this.namespaces = namespaces;
    }

    public Boolean initializeDatabase(WikipediaExtractor.ExtractionStep startingStep, Boolean forceIfExists, Boolean createIndexes) throws IllegalArgumentException {
        return database.initializeDatabase(startingStep, forceIfExists, createIndexes);
    }

    public void saveWikiLinks(WikiArticle article, Boolean stem) {
        // TODO what about category pages?
        if (article.isPageFromAnotherNamespace()) {
            return;
        }

        database.saveWikiLinks(article, stem);
    }

    public WikiLabel getWikiLabelForGivenNGram(String ngram, WikiCollections databaseType) {
        return database.getWikiLabelForGivenNGram(ngram, databaseType);
    }

    public void saveDatabase() {
        database.saveDatabase();
    }

    public void setName(String wikiName) {
        this.name = wikiName;
    }
    
    public void setBase(String base) {
        this.base = base;
    }

    public void setId(ObjectId _id) {
        this._id = _id;
    }

    public ObjectId getId() {
        return _id;
    }

    public Document getMongoObject() {
        Document wikipedia = new Document();

        wikipedia.append(DB_WIKIPEDIA_NAME, this.name);
        wikipedia.append(DB_WIKIPEDIA_BASE, this.base);
        wikipedia.append(DB_WIKIPEDIA_NAMESPACES, this.namespaces);
        wikipedia.append(DB_WIKIPEDIA_STEMMED, this.stemmed);
        if (this._id != null) {
            wikipedia.append(DB_WIKIPEDIA__ID, _id);
        }

        return wikipedia;
    }

    public boolean loadWikipediaFromDB() {
        Wikipedia wikipedia = database.loadWikipedia();
        if (wikipedia != null) {
            instance._id = wikipedia._id;
            instance.base = wikipedia.base;
            instance.name = wikipedia.name;
            instance.namespaces = wikipedia.namespaces;
            instance.stemmed = wikipedia.stemmed;
        }
        return wikipedia != null;
    }

    public String getName() {
        return name;
    }
    
    public String getBase() {
        return base;
    }

    public void saveSiteInfo() {
        database.saveWikipedia(this);
    }

    public WikiArticle getArticleByID(Integer targetArticle) {
        return database.getArticleByID(targetArticle);
    }
    
    public void setLinksFromTo() {
        database.setLinksFromTo();
    }

    public void aggregateLabels() {
        database.aggregateLabels();
    }
    
    public int getCurrentStepMax() {
        return database.getCurrentStepMax();
    }
    
    public int getCurrentStepProgress() {
        return database.getCurrentStepProgress();
    }

    public void saveWikiArticle(WikiArticle article, Boolean update) {
        database.saveWikiArticle(article, update);
    }
    
    public boolean connectToDatabase(Integer databasePort) {
        return database.connectToDatabase(databasePort);
    }

    // We always want to prepend the database name with 'enrich-your-text'
    public boolean openDatabase(String databaseName) {
        return database.openDatabase(WikiDatabase.WIKI_DATABASE_PREFIX + databaseName);
    }
    
    // We also want to return the database names without our custom prefix
    public List<String> getWikiDatabaseNames() {
        List databaseNames = new ArrayList<>();
        
        for (String dbName : database.getDatabaseNames()) {
            databaseNames.add(dbName.substring(WikiDatabase.WIKI_DATABASE_PREFIX.length()));
        }
        
        return databaseNames;
    }
    
    public boolean removeDatabase(String databaseName) {
        return database.removeDatabase(WikiDatabase.WIKI_DATABASE_PREFIX + databaseName);
    }
    
    public void setStemmed(Boolean stemmed) {
        this.stemmed = stemmed;
    }
    
    public Boolean isStemmed() {
        return stemmed;
    }
}
