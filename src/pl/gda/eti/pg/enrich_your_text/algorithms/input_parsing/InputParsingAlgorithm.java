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

import pl.gda.eti.pg.enrich_your_text.algorithms.Algorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmParameter;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument;

/**
 *
 * @author mnarusze
 */
public abstract class InputParsingAlgorithm extends Algorithm {
    
    public final static String PARAM_MAX_NGRAM_LENGTH = "MaxNGramLength";
    
    public InputParsingAlgorithm(String description, String name) {
        super(description, name);
        
        parameters.put(PARAM_MAX_NGRAM_LENGTH, 
            new AlgorithmParameter<>("Maximum NGram Length", "Defines how many words can a NGram consist of.", 5, 1, 10, Integer.class));   
    }
    
    public static String ALGORITHM_TYPE_PRETTY = "Input Parsing Algorithm";
    
    public abstract void parseInput(WikiDocument document);
}
