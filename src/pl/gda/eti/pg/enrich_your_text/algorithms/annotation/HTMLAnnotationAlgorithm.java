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

import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmContextNames;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument;

/**
 *
 * @author mnarusze
 */
public class HTMLAnnotationAlgorithm extends AnnotationAlgorithm {
    private final static String NAME = "HTML Annotation";
    private final static String DESCRIPTION = "Annotates text with discovered context and ambiguous terms.";
     
    public HTMLAnnotationAlgorithm() {
        super(DESCRIPTION, NAME);
    }

    @Override
    public void annotate(WikiDocument document) {
        int maxNgramLength = (int) document.getAlgorithmsContext().get(AlgorithmContextNames.MAX_NGRAMS_LENGTH);
        _annotate(document, false, maxNgramLength);
    }
}
