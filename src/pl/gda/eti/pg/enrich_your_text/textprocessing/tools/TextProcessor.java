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
package pl.gda.eti.pg.enrich_your_text.textprocessing.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import pl.gda.eti.pg.enrich_your_text.models.WikiLink;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;

/**
 *
 * @author mnarusze
 */
public abstract class TextProcessor {
    
    private final static String TEMPLATE_PATTERN = "\\[\\[[^\\[\\]]+\\]\\][\\w]*";
    
    private final static Pattern HTML_WHITESPACE_PATTERN = Pattern.compile("&nbsp;");
    private final static Pattern HTML_BLOCKQUOTE_PATTERN = Pattern.compile("<\\/?blockquote>");
    private final static Pattern HTML_CENTER_PATTERN = Pattern.compile("<\\/?center>");
    private final static Pattern HTML_DIV_PATTERN = Pattern.compile("(?s)<div[^>]*?>(.*?)<\\/div>");
    private final static Pattern HTML_SMALL_PATTERN = Pattern.compile("(?s)<small[^>]*?>(.*?)<\\/small>");
    private final static Pattern HTML_GALLERY_PATTERN = Pattern.compile("(?s)<gallery[^>]*?>.*?<\\/gallery>");
    private final static Pattern HTML_TABLE_PATTERN = Pattern.compile("(?s)<table[^>]*?>.*?<\\/table>");
    private final static Pattern HTML_COMMENT_PATTERN = Pattern.compile("(?s)<!--.*?-->");
    private final static Pattern HTML_REFERENCE_PATTERN_1 = Pattern.compile("<ref[^>]+?/>");
    private final static Pattern HTML_REFERENCE_PATTERN_2 = Pattern.compile("(?s)<ref[^>]*?>.*?<\\/ref>");
    private final static Pattern HTML_TIMELINE_PATTERN = Pattern.compile("(?s)<timeline>.*?</timeline>");
    
    private final static Pattern WIKI_TABLE_PATTERN = Pattern.compile("(?s)\\{\\|.*?\\|\\}");
    private final static Pattern WIKI_SECTIONS_PATTERN = Pattern.compile("(?m)^=+.*?=+$");
    private final static Pattern WIKI_HEADERS_PATTERN = Pattern.compile("(?m)^''+.*?''+$");
    private final static Pattern WIKI_BOLD_FONT_PATTERN = Pattern.compile("''+");
    private final static Pattern WIKI_HORIZONTAL_LINE_PATTERN = Pattern.compile("----");
    private final static Pattern WIKI_ORDINARY_LINKS_PATTERN = Pattern.compile("[^\\[]\\[([^\\[\\]]+?)\\]");
    private final static Pattern WIKI_TYPICAL_PUNCTUATION_PATTERN = Pattern.compile("\\p{P}");
    private final static Pattern WIKI_LISTS_PATTERN = Pattern.compile("(?m)^[\\*#]+\\s*");
    private final static Pattern WIKI_REF_TITLES_PATTERN = Pattern.compile("(?m)^;.*$");
    private final static Pattern WIKI_LINK_PATTERN = Pattern.compile(WikiLink.WIKI_LINK_REGEX);
    
    private final static Pattern NEWLINE_PATTERN = Pattern.compile("\n");
    private final static Pattern DOS_NEWLINE_PATTERN = Pattern.compile("(?s)[\\r\\n]+");
    private final static Pattern REDUNDANT_WHITESPACE_PATTERN = Pattern.compile(" +");
    private final static Pattern EMPTY_LINE_PATTERN = Pattern.compile("(?m)^[ \\t]*\\r?\\n");
    private final static Pattern HYPERLINKS_PATTERN = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    
    public static String cleanupWikiLink(String link) {
        String cleanLink = link;
        cleanLink = wikiRemoveBoldFont(cleanLink);
        cleanLink = removeHTML(cleanLink);
        return cleanLink;
    }

    public static String wikiRemoveBoldFont(String text) {
        return WIKI_BOLD_FONT_PATTERN.matcher(text).replaceAll("");
    }

    public static String wikiRemoveSections(String text) {
        return WIKI_SECTIONS_PATTERN.matcher(text).replaceAll("");
    }

    public static String wikiRemoveHorizontalLines(String text) {
        return WIKI_HORIZONTAL_LINE_PATTERN.matcher(text).replaceAll("");
    }

    public static String wikiRemoveTemplates(String text) {
        String out = "";
        int bracesCounter;

        while (text.contains("{{")) {
            int templateStartPos = text.indexOf("{{");
            out += text.substring(0, templateStartPos);
            text = text.substring(templateStartPos + 2);
            bracesCounter = 2;
            int templateEndPos = 0;
            for (char c : text.toCharArray()) {
                ++templateEndPos;
                if (c == '}') {
                    --bracesCounter;
                } else if (c == '{') {
                    ++bracesCounter;
                }

                if (bracesCounter == 0) {
                    text = text.substring(templateEndPos);
                    break;
                }
            }
        }

        return out;
    }

    public static String wikiRemoveTables(String text) {
        return WIKI_TABLE_PATTERN.matcher(text).replaceAll(" ");
    }

    public static String wikiRemoveReferences(String text) {
        String out = HTML_REFERENCE_PATTERN_1.matcher(text).replaceAll("");
        out = HTML_REFERENCE_PATTERN_2.matcher(out).replaceAll("");
        return out;
    }

    public static String wikiRemoveTimelines(String text) {
        return HTML_TIMELINE_PATTERN.matcher(text).replaceAll("");
    }

    public static String wikiRemoveOrdinaryLinksMarkup(String text) {
        return WIKI_ORDINARY_LINKS_PATTERN.matcher(text).replaceAll("$1");
    }

    public static String removeHTML(String input) {
        if (input == null)
            return null;
        Document document = Jsoup.parse(input);
        document.outputSettings(new Document.OutputSettings().prettyPrint(false));
        document.select("br").append("\\n");
        document.select("p").prepend("\\n\\n");
        String preparedInput = document.html().replaceAll("\\\\n", "\n");
        return Jsoup.clean(preparedInput, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }

    public static String removeHTMLComments(String text) {
        return HTML_COMMENT_PATTERN.matcher(text).replaceAll(" ");
    }

    public static String removeUnnecessaryHTML(String input) {
        String out = HTML_WHITESPACE_PATTERN.matcher(input).replaceAll(" ");
        out = HTML_BLOCKQUOTE_PATTERN.matcher(out).replaceAll(" ");
        out = HTML_CENTER_PATTERN.matcher(out).replaceAll(" ");
        out = HTML_GALLERY_PATTERN.matcher(out).replaceAll(" ");
        out = HTML_TABLE_PATTERN.matcher(out).replaceAll(" ");
        return out;
    }
    
    public static String stripTypicalPunctuation(String text) {
        char firstChar = text.charAt(0);
        if (firstChar == '(' || firstChar == '\"' || firstChar == '\'') {
            text = text.substring(1);
        }
        if (text.isEmpty()) return "";
        char lastChar = text.charAt(text.length() - 1);
        if (lastChar == '.' || lastChar == ',' || lastChar == ':' || lastChar == '-' 
                || lastChar == ';' || lastChar == ')' || lastChar == '!' 
                || lastChar == '?'|| lastChar == '\"' || lastChar == '\'') {
            text = text.substring(0, text.length() - 1);
        }
        if (text.isEmpty()) return "";
        
        return text;
    }

    public static String stemText(String text) {
        PorterStemmer stemmer = new PorterStemmer();
        String stemmedText = "";
        
        if (text == null || text.isEmpty()) return "";
            
        // First, stem all words
        for (String word : text.split(" ")) {
            // Only stem words longer than 3 characters
            if (word.trim().length() > 3) {
                stemmer.add(word.toCharArray(), word.length());
                stemmer.stem();
                word = stemmer.toString();
                if (word.trim().length() > 0) {
                    stemmedText += word.trim() + " ";
                }
            } else {
                stemmedText += word + " ";
            }
        }

        return stemmedText.trim();
    }

    public static String removeRendundantWhitespaces(String text) {
        String out = DOS_NEWLINE_PATTERN.matcher(text).replaceAll("\n");
        out = REDUNDANT_WHITESPACE_PATTERN.matcher(out).replaceAll(" ");
        return out;
    }

    public static String wikiRemoveLists(String text) {
        return WIKI_LISTS_PATTERN.matcher(text).replaceAll("");
    }

    public static String wikiRemoveRefTitles(String text) {
        return WIKI_REF_TITLES_PATTERN.matcher(text).replaceAll(" ");
    }
    
    private static String wikiRemoveHeaders(String text) {
        return WIKI_HEADERS_PATTERN.matcher(text).replaceAll(" ");
    }
    
    private static String wikiRemoveHyperlinks(String text) {
        return HYPERLINKS_PATTERN.matcher(text).replaceAll("");
    }

    public static String wikiReplaceLinks(String text) {
        StringBuffer output = new StringBuffer();
        Matcher m = WIKI_LINK_PATTERN.matcher(text);

        /* 
         * This is extremely stupid, but it is the easiest way - we do the 
         * operations below twice for nested links (like links inside images
         * etc.), it won't work for more-than-two levels of nesting but so far
         * I haven't noticed such nestings in Wikipedia.
         */
        if (m.find()) {
            do {
                String linkStr = m.group();
                WikiLink wikiLink = new WikiLink(linkStr);
                if (wikiLink.isACategory() || wikiLink.isAnUnwantedLink()) {
                    m.appendReplacement(output, "");
                } else {
                    String before = wikiLink.getLabelName();
                    String after;
                    try {
                        after = before.replaceAll("\\$", "\\\\\\$");
                        m.appendReplacement(output, after);
                    } catch (Exception ex) {
                           ex.printStackTrace();
                    }
                }
            } while (m.find());
        } else {
            // No links found - just return original text
            return text;
        }
        
        m.appendTail(output);

        String afterFirstReplacement = output.toString();
        output = new StringBuffer();

        m = WIKI_LINK_PATTERN.matcher(afterFirstReplacement);
        
        if (m.find()) {
            do {
                String linkStr = m.group();
                WikiLink wikiLink = new WikiLink(linkStr);
                if (wikiLink.isACategory() || wikiLink.isAnUnwantedLink()) {
                    m.appendReplacement(output, "");
                } else {
                    String before = wikiLink.getLabelName();
                    String after;
                    try {
                        after = before.replaceAll("\\$", "\\\\\\$");
                        m.appendReplacement(output, after);
                    } catch (Exception ex) {
                           ex.printStackTrace();
                    }
                }
            } while (m.find());
        } else {
            // No links found in second loop - return text after first loop
            return afterFirstReplacement;
        }
        
        m.appendTail(output);

        return output.toString();
    }

    private static String removeEmptyLines(String text) {
        return EMPTY_LINE_PATTERN.matcher(text).replaceAll("");
    }

    /* Don't change order */
    public static String cleanWikiArticle(String text, Boolean replaceLinks) {
        String out = text;

        out = wikiRemoveTemplates(out);
        out = removeHTMLComments(out);
        if (replaceLinks) {
            out = wikiReplaceLinks(out);
        }
        out = removeUnnecessaryHTML(out);
        out = wikiRemoveTables(out);
        out = wikiRemoveReferences(out);
        out = wikiRemoveTimelines(out);
        out = wikiRemoveOrdinaryLinksMarkup(out);
        out = wikiRemoveHeaders(out);
        out = wikiRemoveBoldFont(out);
        out = wikiRemoveHorizontalLines(out);
        out = wikiRemoveSections(out);
        out = wikiRemoveRefTitles(out);
        out = removeHTML(out);
        out = wikiRemoveLists(out);
        out = wikiRemoveHyperlinks(out);
        out = removeEmptyLines(out);
        out = removeRendundantWhitespaces(out);

        return out;
    }
}
