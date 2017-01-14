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
package pl.gda.eti.pg.enrich_your_text.enrichment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import pl.gda.eti.pg.enrich_your_text.algorithms.annotation.AnnotationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.annotation.HTMLAnnotationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.annotation.StemmedHTMLAnnotationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.disambiguation.CommonnessDisambiguationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.disambiguation.DisambiguationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.disambiguation.RandomDisambiguationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.disambiguation.TopicProximityDisambiguationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.input_parsing.InputParsingAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.input_parsing.InputToNGramsAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.input_parsing.InputToStemmedNGramsAlgorithm;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument.DocumentType;
import pl.gda.eti.pg.enrich_your_text.algorithms.keyphrase_lookup.KeyphraseLookupAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.keyphrase_lookup.PerfectMatchingKeyphraseLookupAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.keyphrase_lookup.PerfectMatchingStemmedKeyphraseLookupAlgorithm;

/**
 *
 * @author mnarusze
 */
public class TextEnricher implements Runnable {

    // Algorithm declaration
    public static final List<InputParsingAlgorithm> INPUT_PARSING_ALGORITHMS = Collections.unmodifiableList(
        new ArrayList<InputParsingAlgorithm>() {{
            add(new InputToNGramsAlgorithm());
            add(new InputToStemmedNGramsAlgorithm());
        }});
    
    public static final List<KeyphraseLookupAlgorithm> KEYPHRASE_LOOKUP_ALGORITHMS = Collections.unmodifiableList(
        new ArrayList<KeyphraseLookupAlgorithm>() {{
            add(new PerfectMatchingKeyphraseLookupAlgorithm());
            add(new PerfectMatchingStemmedKeyphraseLookupAlgorithm());
        }});
    
    public static final List<DisambiguationAlgorithm> DISAMBIGUATION_ALGORITHMS = Collections.unmodifiableList(
        new ArrayList<DisambiguationAlgorithm>() {{
            add(new RandomDisambiguationAlgorithm());
            add(new CommonnessDisambiguationAlgorithm());
            add(new TopicProximityDisambiguationAlgorithm());
        }});
    
    public static final List<AnnotationAlgorithm> ANNOTATION_ALGORITHMS = Collections.unmodifiableList(
        new ArrayList<AnnotationAlgorithm>() {{
            add(new HTMLAnnotationAlgorithm());
            add(new StemmedHTMLAnnotationAlgorithm());
        }});
    
    // Enricher fields
    private WikiDocument document;
    public Options options;
    private Boolean success;
    private String outputMessage;
    private final String input;

    public TextEnricher(String input) {
        this.success = false;
        this.outputMessage = "";
        this.input = input;
    }

    public String getEnrichedText() {
        return document.getOutputText();
    }

    public WikiDocument getDocument() {
        return document;
    }

    @Override
    public void run() {
        enrich();
    }

    public void enrich() {
        this.document = new WikiDocument(options.inputType, input);
        try {
            document.enrich(
                    options.inputParsingAlgorithm,
                    options.keyphraseLookupAlgorithm,
                    options.disambiguationLookupAlgorithm,
                    options.annotationAlgorithm
            );
            success = true;
        } catch (InterruptedException ex) {
            this.outputMessage = "Enrichment was interrupted!";
        } catch (Exception ex) {
            this.outputMessage = "An exception occured! For more info please look into console.";
            ex.printStackTrace();
        }
    }

    public String getDetailsText() {
        String details = "";
        
        return details;
    }

    public class Options {
        private final InputParsingAlgorithm inputParsingAlgorithm;
        private final KeyphraseLookupAlgorithm keyphraseLookupAlgorithm;
        private final DisambiguationAlgorithm disambiguationLookupAlgorithm;
        private final AnnotationAlgorithm annotationAlgorithm;
        private final DocumentType inputType;

        public Options(
                InputParsingAlgorithm inputParsingAlgorithm,
                KeyphraseLookupAlgorithm keyphraseLookupAlgorithm,
                DisambiguationAlgorithm disambiguationLookupAlgorithm,
                AnnotationAlgorithm annotationAlgorithm,
                DocumentType inputType) {
            this.inputParsingAlgorithm = inputParsingAlgorithm;
            this.keyphraseLookupAlgorithm = keyphraseLookupAlgorithm;
            this.disambiguationLookupAlgorithm = disambiguationLookupAlgorithm;
            this.annotationAlgorithm = annotationAlgorithm;
            this.inputType = inputType;
        }
    }
    
    public Boolean succeeded() {
        return success;
    }

    public String getOutputMessage() {
        return outputMessage;
    }    
}
