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
package pl.gda.eti.pg.enrich_your_text.models;

import pl.gda.eti.pg.enrich_your_text.algorithms.keyphrase_lookup.KeyphraseLookupAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.disambiguation.DisambiguationAlgorithm;
import java.util.HashMap;
import java.util.Map;
import pl.gda.eti.pg.enrich_your_text.algorithms.annotation.AnnotationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.input_parsing.InputParsingAlgorithm;
import pl.gda.eti.pg.enrich_your_text.textprocessing.tools.TextProcessor;

/**
 *
 * @author mnarusze
 */
public class WikiDocument {

    // Default values available for all algorithm types
    private String inputText;
    private String outputText;
    private WikiArticle article;
    private final DocumentType documentType;
    
    // Map for objects shared between algorithm steps. Place your custom data
    // here, hash keys should be placed in AlgorithmContextNames static class.
    private Map<String, Object> algorithmsContext;
    
    public WikiDocument(DocumentType documentType) {
        this.documentType = documentType;
        this.outputText = "";
        this.inputText = "";
        this.article = null;
        
        this.algorithmsContext = new HashMap<>();
    }

    public WikiDocument(DocumentType documentType, String inputText) {
        this(documentType);
        setInputText(inputText);
    }

    private void cleanUpDocument() {
        String cleanText = this.inputText;
        if (null != this.documentType) {
            switch (this.documentType) {
                case PLAIN_TEXT:
                    break;
                case WIKI_ARTICLE:
                    cleanText = TextProcessor.cleanWikiArticle(cleanText, true);
                    break;
                default:
                    System.err.println("Unknown document type " + this.documentType.toString());
                    break;
            }
        }

        this.inputText = cleanText;
        this.outputText = "";
    }
    
    private void parseInputText(InputParsingAlgorithm inputParsingAlgorithm) {
        inputParsingAlgorithm.parseInput(this);
    }
    
    private void findKeyphrases(KeyphraseLookupAlgorithm algorithm) {
        algorithm.lookupKeyphrases(this);
    }

    private void disambiguate(DisambiguationAlgorithm disambiguationAlgorithm) {
        disambiguationAlgorithm.disambiguate(this);
    }
    
    private void annotateText(AnnotationAlgorithm annotationAlgorithm) {
        annotationAlgorithm.annotate(this);
    }

    /* The main algorithm steps are done here! */
    public void enrich(
            InputParsingAlgorithm inputParsingAlgorithm,
            KeyphraseLookupAlgorithm keyphraseLookupAlgorithm,
            DisambiguationAlgorithm disambiguationAlgorithm,
            AnnotationAlgorithm annotationAlgorithm)
            throws InterruptedException {
        // Translate input text to clean text in case it's in another format.
        cleanUpDocument();
        
        // Given text input, parse it to prepare for keyphrase lookup,
        // disambiguation and tagging.
        parseInputText(inputParsingAlgorithm);
        
        // Step 1 -> keyphrase lookup
        findKeyphrases(keyphraseLookupAlgorithm);

        // Step 2 -> disambiguation
        disambiguate(disambiguationAlgorithm);

        // Step 3 -> annotation
        annotateText(annotationAlgorithm);
    }
    
    public String getInputText() {
        return inputText;
    }
    
    public final void setInputText(String inputText) {
        this.inputText = inputText;

        if (documentType == DocumentType.WIKI_ARTICLE) {
            article = new WikiArticle();
            article.addText(inputText);
        }
    }

    public void setOutputText(String outputText) {
        this.outputText = outputText;
    }
    
    public String getOutputText() {
        return outputText;
    }

    public Map<String, Object> getAlgorithmsContext() {
        return algorithmsContext;
    }

    public enum DocumentType {

        PLAIN_TEXT,
        WIKI_ARTICLE;

        public static String[] names() {
            DocumentType[] values = values();
            String[] names = new String[values.length];

            for (int i = 0; i < values.length; i++) {
                names[i] = values[i].name().substring(0, 1) + values[i].name().substring(1).toLowerCase().replaceAll("_", " ");
            }

            return names;
        }
        
        public static DocumentType myValueOf(String type) {
            String typeStr = type.toUpperCase().replaceAll(" ", "_");
            return DocumentType.valueOf(typeStr);
        }
    };
}
