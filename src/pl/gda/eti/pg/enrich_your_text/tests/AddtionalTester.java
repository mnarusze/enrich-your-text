/*
 * Copyright (C) 2014 mnarusze
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package pl.gda.eti.pg.enrich_your_text.tests;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import pl.gda.eti.pg.enrich_your_text.models.WikiArticle;
import pl.gda.eti.pg.enrich_your_text.models.WikiLink;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;
import pl.gda.eti.pg.enrich_your_text.settings.Configuration;
import pl.gda.eti.pg.enrich_your_text.textprocessing.tools.TextProcessor;

/**
 *
 * @author mnarusze
 */
public class AddtionalTester {

    private static final ArrayList<URI> TEST_WIKI_ARTICLES = new ArrayList<>();
    private static final ArrayList<URI> TEST_PLAIN_ARTICLES = new ArrayList<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Just for loading
        Wikipedia.getInstance().loadWikipediaFromDB();
        
        loadTestArticles();
        
        // testWikiArticleCleaning();
        testLinkExtraction();
    }

    private static void loadTestArticles() {
        URL resourceDir = AddtionalTester.class.getResource(Configuration.TEST_SETS_RESOURCES_DIR);        
        File testSetsDir = new File(resourceDir.getFile());
        File testWikiArticleFiles[] = testSetsDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("SimpleWiki_Colc");
            }
        });

        for (File articleFile : testWikiArticleFiles) {
            TEST_WIKI_ARTICLES.add(articleFile.toURI());
        }

        File testPlainTextArticleFiles[] = testSetsDir.listFiles(new FilenameFilter() {
            
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("PlainText_");
            }
        });

        for (File articleFile : testPlainTextArticleFiles) {
            TEST_PLAIN_ARTICLES.add(articleFile.toURI());
        }
    }

    private static void testWikiArticleCleaning() {
        for (URI article : TEST_WIKI_ARTICLES) {
            try {
                String text = new String(Files.readAllBytes(Paths.get(article)));
                WikiArticle wikiArticle = new WikiArticle();
                wikiArticle.addText(text);
                System.out.println(wikiArticle.getCleanText());
            } catch (IOException ex) {
                System.err.println("Exception while testing files parsing!");
                System.err.println(ex.getMessage());
            }
        }
    }

    private static void testLinkMatcher() {
        System.out.println("*** TESTING TEST LINK MATCHER ***");
        for (URI article : TEST_WIKI_ARTICLES) {
            try {
                String text = new String(Files.readAllBytes(Paths.get(article)));
                Pattern linkPattern = Pattern.compile(WikiLink.WIKI_LINK_REGEX);
                Matcher m = linkPattern.matcher(text);

                while (m.find()) {
                    System.out.println(m.group());
                }
            } catch (IOException ex) {
            }
        }
        System.out.println("*** FINISHED TESTING TEST LINK MATCHER ***");
    }

    private static void testLinkExtraction() {
        System.out.println("*** TESTING TEST LINK EXTRACTION ***");
        WikiArticle article = new WikiArticle();
        
        for (URI testArticle : TEST_WIKI_ARTICLES) {
            try {
                article.addText(new String(Files.readAllBytes(Paths.get(testArticle))));
                article.extractLinks();
                for (WikiLink link : article.getWikiLinks()) {
                    System.out.println(link.toString());
                }
            } catch (IOException ex) {
                
            }
        }
        System.out.println("*** FINISHED TESTING TEST LINK EXTRACTION ***");
    }

    private static void testFullLinksSimple() {
        System.out.println("*** TESTING FULL LINKS SIMPLE ***");
        
        String fullLinks[] = {
            "[[Wikipedia:Manual of Style#Italics|Italics]]",
            "[[Wikipedia:Manual of Style (headings)|]]",
            "[[Seattle, Washington|]]",
            "[[:pt:Internet|]]",
            "[[Star Wars : Return Of The Jedi#Han Solo|Han]]"
        };

        for (String link : fullLinks) {
            System.out.println(new WikiLink(link).toString());
        }
        System.out.println("*** FINISHED TESTING FULL LINKS SIMPLE ***");
    }

    private static void testLinkCleanup() {
        System.out.println("*** TESTING LINKS CLEANUP ***");
        String links[] = {
            "[[<!-- my perfect comment--><b>Link</b>]]",
            "[[<sub>L'''ink'''</sub>]]",};
        for (String link : links) {
            System.out.println(link + " -> " + TextProcessor.cleanupWikiLink(link));
        }
        System.out.println("*** FINISHED TESTING LINKS CLEANUP ***");
    }

    private static void testExtraTitleParsing() {
        String titles[] = {
            "Waterloo &amp; City line"
        };

        for (String title : titles) {
            WikiArticle article = new WikiArticle();
            article.setTitle(title);
            System.out.print(title + " == " + article.getTitle());
            if (title.equals(article.getTitle())) {
                System.out.println(" OK");
            } else {
                System.out.println(" NOT OK");
            }
        }
    }
}
