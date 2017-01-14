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
package pl.gda.eti.pg.enrich_your_text.algorithms.disambiguation;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmContextNames;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmParameter;
import pl.gda.eti.pg.enrich_your_text.models.AmbiguousTerm;
import pl.gda.eti.pg.enrich_your_text.models.ContextTerm;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument;
import pl.gda.eti.pg.enrich_your_text.models.Meaning;
import pl.gda.eti.pg.enrich_your_text.models.WikiArticle;

/**
 *
 * @author mnarusze
 */
public class TopicProximityDisambiguationAlgorithm extends DisambiguationAlgorithm {
    private final static String NAME = "Topic Proximity";
    private final static String DESCRIPTION = "Chooses a meaning which is the closest to the text's context.";
    
    public final static String PARAM_CONTEXT_TERMS_TO_STAY = "ContextTermsToStay";
    public final static String PARAM_MIN_CTX_TERM_SCORE = "MinCtxTermScore";
    public final static String PARAM_LINKS_TO_TEXT_RATIO = "LinksToTextRatio";
    public final static String PARAM_TEXT_BEGINNING_PERCENT = "TextBeginningPercent";
    public final static String PARAM_TEXT_ENDING_PERCENT = "TextEndPercent";
    public final static String PARAM_TEXT_BEGIN_OR_END_BONUS = "TextBeginOrEndBonus";
    public final static String PARAM_TEXT_OCCURENCE_BONUS = "TextOccurenceBonus";
    public final static String PARAM_TEXT_MAX_OCCURENCE_BONUS = "TextMaxOccurenceBonus";
    
     
    public TopicProximityDisambiguationAlgorithm() {
        super(DESCRIPTION, NAME);
        
        parameters.put(PARAM_CONTEXT_TERMS_TO_STAY, 
               new AlgorithmParameter<>("Context terms to stay", "Defines how many most-related context terms to leave for disambiguation.", 20, 1, 1000, Integer.class));
        parameters.put(PARAM_MIN_CTX_TERM_SCORE, 
               new AlgorithmParameter<>("Minimum context term score", "Defines the minimum score of context term, if its score is below this value then it is ignored.", 0.1, 0.0, 0.5, Double.class));
        parameters.put(PARAM_LINKS_TO_TEXT_RATIO, 
               new AlgorithmParameter<>("Links to text ratio", "Ratio between links and the whole text - how much of the text should be covered with links.", 0.12, 0.01, 1.0, Double.class));
        parameters.put(PARAM_TEXT_BEGINNING_PERCENT, 
               new AlgorithmParameter<>("Text beginning percentage", "Percentage of text before which bonus is added to keywords.", 0.1, 0.0, 1.0, Double.class));
        parameters.put(PARAM_TEXT_ENDING_PERCENT, 
               new AlgorithmParameter<>("Text ending percentage", "Percentage of text after which bonus is added to keywords.", 0.9, 0.0, 1.0, Double.class));
        parameters.put(PARAM_TEXT_BEGIN_OR_END_BONUS, 
               new AlgorithmParameter<>("Text beginning/end bonus", "Bonus added to context terms which are mentioned in the beginning or end.", 0.02, 0.0, 0.2, Double.class));
        parameters.put(PARAM_TEXT_OCCURENCE_BONUS, 
               new AlgorithmParameter<>("Text occurence bonus", "Bonus added to context terms which appear more than once. Bonus counts for every time the phrase is mentioned.", 0.02, 0.0, 0.2, Double.class));
        parameters.put(PARAM_TEXT_MAX_OCCURENCE_BONUS, 
               new AlgorithmParameter<>("Max text occurence bonus", "Maximum occurences bonus for one context term.", 0.08, 0.0, 0.4, Double.class));
    }
    
    private void calculateContextQuality(WikiDocument document) {
        Double contextQuality = 0.0;
        List<ContextTerm> contextTerms = (List<ContextTerm>) document.getAlgorithmsContext().get(AlgorithmContextNames.CONTEXT_TERMS);

        for (ContextTerm contextTerm : contextTerms) {
            contextQuality += contextTerm.getAvgRelWithOtherCtxTerms();
        }
        contextQuality /= contextTerms.size();
        
        document.getAlgorithmsContext().put(AlgorithmContextNames.CONTEXT_QUALITY, contextQuality);
    }
    
    private void trimContextTermsList(WikiDocument document) {
        List<ContextTerm> contextTerms = (List<ContextTerm>) document.getAlgorithmsContext().get(AlgorithmContextNames.CONTEXT_TERMS);
        int newCtxTermsSize = Math.min(contextTerms.size(), (int) parameters.get(PARAM_CONTEXT_TERMS_TO_STAY).getValue());
        contextTerms = contextTerms.subList(0, newCtxTermsSize);
        document.getAlgorithmsContext().put(AlgorithmContextNames.CONTEXT_TERMS, contextTerms);
    }
    
    private void calculateContextTermsRelatedness(WikiDocument document) {
        List<ContextTerm> contextTerms = (List<ContextTerm>) document.getAlgorithmsContext().get(AlgorithmContextNames.CONTEXT_TERMS);
        int ctxTermsSize = contextTerms.size();

        /* Calculate context terms relatedness to each other */
        for (int i = 0; i < ctxTermsSize; i++) {
            for (int j = i + 1; j < ctxTermsSize; j++) {
                ContextTerm ctxTerm = contextTerms.get(i);
                ContextTerm comparedCtxTerm = contextTerms.get(j);

                Double ctxArtRel = WikiArticle.getTopicProximityBetweenArts(ctxTerm.getTargetArticleID(), comparedCtxTerm.getTargetArticleID(),
                        ctxTerm.getArticleLinksTo(), comparedCtxTerm.getArticleLinksTo());

                ctxTerm.increaseAvgRel(ctxArtRel);
                comparedCtxTerm.increaseAvgRel(ctxArtRel);
            }
        }

        for (Iterator<ContextTerm> ctxTermIter = contextTerms.iterator(); ctxTermIter.hasNext();) {
            /* Divide to get average relatedness, include other metrics in the
             * ctx term's score
             */
            ContextTerm ctxTerm = ctxTermIter.next();
            ctxTerm.calcScore(
                ctxTermsSize,
                (double) parameters.get(PARAM_TEXT_BEGIN_OR_END_BONUS).getValue(),
                (double) parameters.get(PARAM_TEXT_OCCURENCE_BONUS).getValue(),
                (double) parameters.get(PARAM_TEXT_MAX_OCCURENCE_BONUS).getValue(),
                (double) parameters.get(PARAM_TEXT_BEGINNING_PERCENT).getValue(),
                (double) parameters.get(PARAM_TEXT_ENDING_PERCENT).getValue());

            // Check if context term is important enough and remove if not
            if (ctxTerm.getScore() < (double) parameters.get(PARAM_MIN_CTX_TERM_SCORE).getValue()) {
                ctxTermIter.remove();
            }
        }

        /* Sort by average relatedness */
        Collections.sort(contextTerms);
    }
    
    @Override
    public void disambiguate(WikiDocument document) {
        List<AmbiguousTerm> ambiguousTerms = (List<AmbiguousTerm>) document.getAlgorithmsContext().get(AlgorithmContextNames.AMGIBUOUS_TERMS);
        List<ContextTerm> contextTerms = (List<ContextTerm>) document.getAlgorithmsContext().get(AlgorithmContextNames.CONTEXT_TERMS);
        String inputText = document.getInputText();
        
        // Calculate relatedness between each context term
        calculateContextTermsRelatedness(document);

        // Leave fixed amount of context terms for performance and precision
        trimContextTermsList(document);

        // Calculate average context quality of the document
        calculateContextQuality(document);
        
        for (AmbiguousTerm ambiguousTerm : ambiguousTerms) {
            List<Meaning> meanings = ambiguousTerm.getPossibleMeanings();
            Double topicProximity = 0.0;
            // Calculate avg topic proximity between every meaning and the context terms
            for (Meaning tmpMeaning : meanings) {
                for (ContextTerm contextTerm : contextTerms) {
                    topicProximity += WikiArticle.getTopicProximityBetweenArts(
                            tmpMeaning.getArticleID(),
                            contextTerm.getTargetArticleID(),
                            tmpMeaning.getArticleLinksTo(),
                            contextTerm.getArticleLinksTo());
                }
                topicProximity /= contextTerms.size();
                tmpMeaning.setTopicProximity(topicProximity);
            }
            // Choose the meaning with biggest average relatedness to context terms
            Collections.sort(meanings, new Meaning.RelatednessComparator());
            ambiguousTerm.setChosenMeaning(meanings.get(0));
        }

        // Sort by relatedness
        Collections.sort(ambiguousTerms, new AmbiguousTerm.RelatednessComparator());
        
        // Trimming part
        
        // Count how many words should be changed to links
        int wordsMarkedAsLinks = 0;
        int targetWordsMarkedAsLinks = (int) (inputText.trim().split("\\s+").length * (double) parameters.get(PARAM_LINKS_TO_TEXT_RATIO).getValue());
        int ambigousTermsIncluded = 0;
        
        // Get the best links
        for (AmbiguousTerm ambTerm : ambiguousTerms) {
            wordsMarkedAsLinks += ambTerm.getLabelName().split("\\s+").length;
            ambigousTermsIncluded++;
            if (wordsMarkedAsLinks >= targetWordsMarkedAsLinks) {
                break;
            }
        }
        
        document.getAlgorithmsContext().put(AlgorithmContextNames.AMGIBUOUS_TERMS, ambiguousTerms.subList(0, ambigousTermsIncluded));
    }
}
