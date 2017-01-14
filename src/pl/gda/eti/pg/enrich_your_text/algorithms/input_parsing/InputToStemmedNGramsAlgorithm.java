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
package pl.gda.eti.pg.enrich_your_text.algorithms.input_parsing;

import java.util.Set;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmContextNames;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument;
import pl.gda.eti.pg.enrich_your_text.models.NGram;

/**
 *
 * @author mnarusze
 */
public class InputToStemmedNGramsAlgorithm extends InputParsingAlgorithm {
    private final static String NAME = "Input to stemmed NGrams";
    private final static String DESCRIPTION = "Translates input text to stemmed ngrams. Remember the ngram's position and number of occurences.";
     
    public InputToStemmedNGramsAlgorithm() {
        super(DESCRIPTION, NAME);
    }

    @Override
    public void parseInput(WikiDocument document) {
        int maxNgramLength = (int) parameters.get(PARAM_MAX_NGRAM_LENGTH).getValue();
        Set<NGram> nGrams = NGram.getNGrams(document.getInputText(), true, maxNgramLength);
        document.getAlgorithmsContext().put(AlgorithmContextNames.NGRAMS, nGrams);
        document.getAlgorithmsContext().put(AlgorithmContextNames.MAX_NGRAMS_LENGTH, maxNgramLength);
    }
}
