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
import java.util.List;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmContextNames;
import pl.gda.eti.pg.enrich_your_text.models.AmbiguousTerm;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument;
import pl.gda.eti.pg.enrich_your_text.models.Meaning;

/**
 *
 * @author mnarusze
 */
public class RandomDisambiguationAlgorithm extends DisambiguationAlgorithm {  
    
    private final static String NAME = "Random";
    private final static String DESCRIPTION = "Chooses a random meaning.";
        
    public RandomDisambiguationAlgorithm() {
        super(DESCRIPTION, NAME);
    }

    @Override
    public void disambiguate(WikiDocument document) {
        for (AmbiguousTerm ambiguousTerm : (List<AmbiguousTerm>) document.getAlgorithmsContext().get(AlgorithmContextNames.AMGIBUOUS_TERMS)) {
            List<Meaning> meanings = ambiguousTerm.getPossibleMeanings();
            // Choose a random meaning
            Collections.shuffle(meanings);
            ambiguousTerm.setChosenMeaning(meanings.get(0));
        }
    }
}
