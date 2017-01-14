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
package pl.gda.eti.pg.enrich_your_text.models;

import java.text.BreakIterator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import pl.gda.eti.pg.enrich_your_text.settings.Configuration;
import pl.gda.eti.pg.enrich_your_text.textprocessing.tools.TextProcessor;

/**
 *
 * @author mnarusze
 */
public class NGram {
    private final String text;
    private Integer occurenceCount;
    private Float firstMentioned, lastMentioned;

    public NGram(String text) {
        this.text = text;
        this.occurenceCount = 0;
        this.firstMentioned = -1.0f;
        this.lastMentioned = -1.0f;
    }

    public void setOccurenceCount(Integer occurenceCount) {
        this.occurenceCount = occurenceCount;
    }

    public void mention(Float textPercentage) {
        if (this.firstMentioned == -1.0) {
            this.firstMentioned = textPercentage;
        }

        this.lastMentioned = textPercentage;
    }

    public Boolean wasInBeginningOrEnd(double textBeginPercent, double textEndPercent) {
        return this.firstMentioned <= textBeginPercent || 
                this.lastMentioned >= textEndPercent;
    }

    public Integer getOccurenceCount() {
        return occurenceCount;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof NGram)
            return ((NGram)obj).text.equals(this.text);
        throw new ClassCastException();
    }

    // Returns a map of ngrams and their occurences from a given text
    public static Set<NGram> getNGrams(String inputText, Boolean stem, Integer maxNGramLength) {
        Map<NGram, Integer> ngrams = new HashMap<>();
        String lines[] = inputText.split("\\r?\\n");
        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.US);
        int charactersInTextCnt = inputText.length();
        int masterPosition = 0;

        for (String line : lines) {
            // Skip empty lines
            if (line.trim().length() == 0) {
                continue;
            }

            iterator.setText(line);
            int sentenceBeginIdx, sentenceEndIdx;
            // Split the line into sentences
            for (sentenceBeginIdx = iterator.first(), sentenceEndIdx = iterator.next(); sentenceEndIdx != BreakIterator.DONE; sentenceBeginIdx = sentenceEndIdx, sentenceEndIdx = iterator.next()) {
                String sentence = line.substring(sentenceBeginIdx, sentenceEndIdx).trim().toLowerCase();

                /**
                 * NOTE - we should do it, but the extraction step should do the
                 * same... :( So we only remove punctuation for each ngram
                 */
                // Remove typical punctuation
                // sentence = TextProcessor.removeTypicalPunctuation(sentence);
                // Get all ngrams between 1 and MAX_WORDS_IN_ARTICLE_TITLE for a given sentence
                // TODO move the 1...MAX part to NGram
                for (int i = maxNGramLength; i >= 1; i--) {
                    NGramsCreator ngramCreator = new NGramsCreator(sentence, i);
                    for (String ngramStr : ngramCreator.list()) {
                        float position = (masterPosition + sentenceBeginIdx + ngramStr.length() / 2) / (float) charactersInTextCnt;
                        ngramStr = ngramStr.trim();
                        if (ngramStr.length() < 2) {
                            continue;
                        }
                        ngramStr = TextProcessor.stripTypicalPunctuation(ngramStr).trim();
                        if (!ngramStr.isEmpty() && !NGramsCreator.isUnwantedKeyword(ngramStr) && !Wikipedia.getInstance().isAStopWord(ngramStr)) {
                            if (stem == true) {
                                ngramStr = TextProcessor.stemText(ngramStr);
                            }
                            NGram newOrNotNGram = new NGram(ngramStr);
                            Integer occurenceCount = ngrams.getOrDefault(newOrNotNGram, 0) + 1;
                            newOrNotNGram.mention(position);
                            newOrNotNGram.setOccurenceCount(occurenceCount);
                            ngrams.put(newOrNotNGram, occurenceCount);
                        }
                    }
                }
            }
            
            masterPosition += line.length();
        }
        
        for (NGram ngram : ngrams.keySet()) {
            ngram.setOccurenceCount(ngrams.get(ngram));
        }

        return ngrams.keySet();
    }
}
