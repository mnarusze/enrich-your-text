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
package pl.gda.eti.pg.enrich_your_text.algorithms.keyphrase_lookup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import pl.gda.eti.pg.enrich_your_text.algorithms.Algorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmContextNames;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmParameter;
import pl.gda.eti.pg.enrich_your_text.database.WikiDatabase;
import pl.gda.eti.pg.enrich_your_text.models.AmbiguousTerm;
import pl.gda.eti.pg.enrich_your_text.models.ContextTerm;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument;
import pl.gda.eti.pg.enrich_your_text.models.Meaning;
import pl.gda.eti.pg.enrich_your_text.models.NGram;
import pl.gda.eti.pg.enrich_your_text.models.WikiLabel;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;

/**
 *
 * @author mnarusze
 */
public abstract class KeyphraseLookupAlgorithm extends Algorithm {
    
    public final static String PARAM_MEANING_MIN_OCCURENCES = "MeaningMinOcc";
    
    public KeyphraseLookupAlgorithm(String description, String name) {
        super(description, name);
        parameters.put(PARAM_MEANING_MIN_OCCURENCES, 
            new AlgorithmParameter<>("Minimum commonness of a meaning", "Minimum amount of mentions a meaning needs to have in order to be taken into account when choosing the best meaning for a phrase.", 2, 0, 100, Integer.class));
    }
    
    public static String ALGORITHM_TYPE_PRETTY = "Keyphrase Lookup Algorithm";
    
    public abstract void lookupKeyphrases(WikiDocument document);
    
    protected void _lookupKeyphrases(WikiDocument document, Boolean stem) {
        Set<NGram> nGrams = (Set<NGram>) document.getAlgorithmsContext().get(AlgorithmContextNames.NGRAMS);
        Iterator<NGram> ngramsIter = nGrams.iterator();
        
        List<ContextTerm> contextTerms = new ArrayList<>();
        List<AmbiguousTerm> ambiguousTerms = new ArrayList<>();
        
        while (ngramsIter.hasNext()) {
            NGram ngram = ngramsIter.next();
            WikiLabel label;
            
            if (stem == true) {
                label = Wikipedia.getInstance().getWikiLabelForGivenNGram(ngram.getText(), WikiDatabase.WikiCollections.LABELS_STEMMED);
            } else {
                label = Wikipedia.getInstance().getWikiLabelForGivenNGram(ngram.getText(), WikiDatabase.WikiCollections.LABELS);
            }
            
            if (label != null) {
                Map<Integer, Integer> targetArticles = label.getTargetArticles();

                if (targetArticles.size() == 1) {
                    // Context term detected
                    String labelName = label.getName();
                    ContextTerm contextTerm = new ContextTerm(labelName);
                    // Don't add if we already have a context term with the same
                    // label
                    if (!contextTerms.contains(contextTerm)) {
                        Integer targetArticle = (Integer) targetArticles.keySet().toArray()[0];
                        contextTerm.setTargetArticle(Wikipedia.getInstance().getArticleByID(targetArticle));
                        contextTerm.setNGram(ngram);
                        contextTerms.add(contextTerm);
                    }
                } else if (targetArticles.size() > 1) {
                    // Ambiguous term detected
                    AmbiguousTerm ambiguousTerm = new AmbiguousTerm(label.getName());
                    Integer ambiguousTermMeaningOccurencesCount = 0;

                    // Take only these meanings which occur more than
                    // MEANING_MIN_OCCURENCES, delete the rest
                    for (Iterator<Map.Entry<Integer, Integer>> it = targetArticles.entrySet().iterator(); it.hasNext();) {
                        Integer meaningOccurenceCnt = it.next().getValue();
                        if (meaningOccurenceCnt < (int) parameters.get(PARAM_MEANING_MIN_OCCURENCES).getValue()) {
                            it.remove();
                            continue;
                        }
                        ambiguousTermMeaningOccurencesCount += meaningOccurenceCnt;
                    }

                    // Add the remaining meanings to the ambiguous term with
                    // their commonness
                    for (Integer targetArticleID : targetArticles.keySet()) {
                        Double meaningOccurenceCnt = new Double(targetArticles.get(targetArticleID));
                        ambiguousTerm.addNewMeaning(
                                new Meaning(
                                        Wikipedia.getInstance().getArticleByID(targetArticleID),
                                        meaningOccurenceCnt / ambiguousTermMeaningOccurencesCount
                                )
                        );
                    }

                    // Ignore ambiguous terms which only had meanings mentioned
                    // once
                    if (!ambiguousTerm.getPossibleMeanings().isEmpty()) {
                        ambiguousTerms.add(ambiguousTerm);
                    }
                } else {
                    System.err.println("No target article detected for label " + label.getName());
                }
            }
            // After ngram is processed, remove it
            ngramsIter.remove();
        }
        document.getAlgorithmsContext().remove(AlgorithmContextNames.NGRAMS);
        document.getAlgorithmsContext().put(AlgorithmContextNames.AMGIBUOUS_TERMS, ambiguousTerms);
        document.getAlgorithmsContext().put(AlgorithmContextNames.CONTEXT_TERMS, contextTerms);
    }
}
