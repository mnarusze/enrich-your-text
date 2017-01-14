/*
 * Copyright (C) 2016 mnarusze
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
package pl.gda.eti.pg.enrich_your_text.algorithms.annotation;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import pl.gda.eti.pg.enrich_your_text.algorithms.Algorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmContextNames;
import pl.gda.eti.pg.enrich_your_text.models.AmbiguousTerm;
import pl.gda.eti.pg.enrich_your_text.models.ContextTerm;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument;
import pl.gda.eti.pg.enrich_your_text.models.NGramsCreator;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;
import pl.gda.eti.pg.enrich_your_text.textprocessing.tools.TextProcessor;

/**
 *
 * @author mnarusze
 */
public abstract class AnnotationAlgorithm extends Algorithm {
    
    public AnnotationAlgorithm(String description, String name) {
        super(description, name);
    }
    
    public static String ALGORITHM_TYPE_PRETTY = "Annotation Algorithm";
    
    public abstract void annotate(WikiDocument document);
    
    protected void _annotate(WikiDocument document, Boolean stem, Integer maxNGramLength) {
        String outputText = "";
        String lines[] = document.getInputText().split("\\r?\\n");
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);

        for (String line : lines) {
            // Skip empty lines
            if (line.trim().length() == 0) {
                continue;
            }

            outputText += "<p>";

            iterator.setText(line);
            int start, end;
            // Split the line into sentences
            for (start = iterator.first(), end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
                String sentence = line.substring(start, end).trim();
                // Get a list of all words in the given sentence
                String[] words = sentence.split(" ");
                int current = 0, last = words.length;
                while (current < last) {
                    // For every set of words try to find ngram of maximum size
                    // which may represent a phrase - if we find one, substitute
                    // the phrase with a link to the direct article
                    int i;
                    for (i = maxNGramLength; i > 0; i--) {
                        if (current + i > words.length) {
                            continue;
                        }
                        String ngram = "", originalNgram, linkedNGram;
                        // Construct ngram
                        for (int j = 0; j < i; j++) {
                            ngram += words[current + j] + " ";
                        }
                        ngram = ngram.trim();
                        originalNgram = ngram;
                        ngram = TextProcessor.stripTypicalPunctuation(ngram);
                        linkedNGram = ngram;

                        if (ngram != null && !NGramsCreator.isUnwantedKeyword(ngram) && !Wikipedia.getInstance().isAStopWord(ngram)) {
                            if (stem) {
                                ngram = TextProcessor.stemText(ngram);
                            }
                            /*
                            * Check if we have a keyword for this phrase.
                            * In case of commonness always choose the first meaning,
                            * in case of relatedness accept only those with nice
                            * relatedness to context
                             */
                            String targetArticleTitle = null;
                            for (AmbiguousTerm ambiguousTerm : (List<AmbiguousTerm>) document.getAlgorithmsContext().get(AlgorithmContextNames.AMGIBUOUS_TERMS)) {
                                if (ambiguousTerm.getLabelName().equalsIgnoreCase(ngram)) {
                                    targetArticleTitle = ambiguousTerm.getChosenMeaning().getArticleTitle();
                                    break;
                                }
                            }

                            if (targetArticleTitle == null) {
                                for (ContextTerm contextTerm : (List<ContextTerm>) document.getAlgorithmsContext().get(AlgorithmContextNames.CONTEXT_TERMS)) {
                                    if (contextTerm.getLabelName().equalsIgnoreCase(ngram)) {
                                        targetArticleTitle = contextTerm.getTargetArticleTitle();
                                        break;
                                    }
                                }
                            }

                            if (targetArticleTitle != null && !targetArticleTitle.isEmpty()) {
                                if (originalNgram.charAt(0) != linkedNGram.charAt(0)) {
                                    outputText += originalNgram.charAt(0);
                                }
                                outputText += "<a href=\"" + Wikipedia.getInstance().getBase() + "/" + targetArticleTitle + "\">" + linkedNGram + "</a>";
                                if (originalNgram.charAt(originalNgram.length() - 1) != linkedNGram.charAt(linkedNGram.length() - 1)) {
                                    outputText += originalNgram.charAt(originalNgram.length() - 1);
                                }
                                outputText += " ";
                                break;
                            }
                        }
                    }

                    if (i <= 0) {
                        outputText += words[current] + " ";
                        current++;
                    } else {
                        current += i;
                    }
                }
            }
            outputText += "</p>";
        }
        
        document.setOutputText(outputText);
    }
}
