package pl.gda.eti.pg.enrich_your_text.extraction;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import pl.gda.eti.pg.enrich_your_text.models.WikiArticle;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;

public class WikiXMLExtractorHandler extends DefaultHandler {

    private static final String XML_ELEMENT_NAMESPACE = "namespace";
    private static final String XML_ELEMENT_PAGE = "page";
    private static final String XML_ELEMENT_ID = "id";
    private static final String XML_ELEMENT_TITLE = "title";
    private static final String XML_ELEMENT_REDIRECT = "redirect";
    private static final String XML_ELEMENT_TEXT = "text";
    private static final String XML_ELEMENT_WIKI_NAME = "dbname";
    private static final String XML_ELEMENT_WIKI_BASE = "base";
    private static final String XML_ELEMENT_SITEINFO = "siteinfo";
    
    private boolean titleElement = false;
    private boolean idElement = false;
    private boolean textElement = false;
    private boolean namespaceElement = false;
    private boolean wikiNameElement = false;
    private boolean wikiBaseElement = false;

    private String title = "";
    private String wikiName = "";
    private String baseName = "";
    private String namespaceName = "";
    private WikiArticle article = null;
    
    private final WikipediaExtractor.ExtractionStep extractionStep;
    private final Boolean stem;

    public WikiXMLExtractorHandler(WikipediaExtractor.ExtractionStep extractionStep, Boolean stem) {
        super();
        this.extractionStep = extractionStep;
        this.stem = stem;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        switch (qName.toLowerCase()) {
            case XML_ELEMENT_TEXT:
                textElement = true;
                break;
            case XML_ELEMENT_PAGE:
                article = new WikiArticle();
                break;
            case XML_ELEMENT_TITLE:
                titleElement = true;
                title = "";
                break;
            case XML_ELEMENT_REDIRECT:
                String redirectTo = (attributes.getValue("title"));
                if (redirectTo == null || redirectTo.isEmpty()) {
                    System.err.println("Empty redirect in article " + article.getTitle());
                } else {
                    article.setRedirect(redirectTo);
                }
                break;
            case XML_ELEMENT_WIKI_NAME:
                wikiNameElement = true;
                break;
            case XML_ELEMENT_WIKI_BASE:
                wikiBaseElement = true;
                break;
            case XML_ELEMENT_NAMESPACE:
                namespaceElement = true;
                break;
            case XML_ELEMENT_ID:
                if (article.getId() == 0) {
                    idElement = true;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char ch[], int start, int length)
            throws SAXException {
        if (namespaceElement) {
            namespaceName = new String(ch, start, length).trim();
            return;
        }
        
        if (titleElement) {
            title = title.concat(new String(ch, start, length));
            return;
        }
        
        if (idElement && article.getId() == 0) {
            article.setId(Integer.parseInt(new String(ch, start, length)));
            idElement = false;
            return;
        }
        
        if (textElement) {
            article.addText(new String(ch, start, length));
            return;
        }
        
        if (wikiNameElement) {
            wikiName += new String(ch, start, length);
            return;
        }
        
        if (wikiBaseElement) {
            baseName += new String(ch, start, length);
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        switch (qName.toLowerCase()) {
            case XML_ELEMENT_TEXT:
                textElement = false;
                break;
            case XML_ELEMENT_PAGE:
                switch(extractionStep) {
                    case PARSING_XML_ARTICLES:
                        // Store articles for their named & IDs
                        Wikipedia.getInstance().saveWikiArticle(article, false);
                        break;
                    case PARSING_XML_LINKS:
                        // Prepare the links
                        article.extractLinks();
                        // Save every link from this article to other ones
                        Wikipedia.getInstance().saveWikiLinks(article, stem);
                        // Update the articles with the categories
                        Wikipedia.getInstance().saveWikiArticle(article, true);
                        break;
                    default:
                        break;
                }
                break;
            case XML_ELEMENT_TITLE:
                titleElement = false;
                article.setTitle(title);
                break;
            case XML_ELEMENT_WIKI_NAME:
                wikiNameElement = false;
                Wikipedia.getInstance().setName(wikiName);
                break;
            case XML_ELEMENT_WIKI_BASE:
                wikiBaseElement = false;
                Wikipedia.getInstance().setBase(baseName);
                break;
            case XML_ELEMENT_SITEINFO:
                if (extractionStep == WikipediaExtractor.ExtractionStep.PARSING_XML_ARTICLES) {
                    Wikipedia.getInstance().setStemmed(stem);
                    Wikipedia.getInstance().saveSiteInfo();
                }
                break;
            case XML_ELEMENT_NAMESPACE:
                if (extractionStep == WikipediaExtractor.ExtractionStep.PARSING_XML_ARTICLES) {
                    Wikipedia.getInstance().addNamespace(namespaceName);
                }
                namespaceElement = false;
                namespaceName = "";
                break;
            default:
                break;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        Wikipedia.getInstance().saveDatabase();
    }
}
