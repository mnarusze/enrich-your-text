/*
 * Copyright (C) 2014 Maciej Naruszewicz
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
package pl.gda.eti.pg.enrich_your_text.extraction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;

/**
 *
 * @author mnarusze
 */
public class WikipediaExtractor implements Runnable {

    public enum ExtractionStep {
        NONE(0),
        PARSING_XML_ARTICLES(1),
        PARSING_XML_LINKS(2),
        LABELS_AGGREGATION(3),
        SETTING_LINKS_FROM_TO(4);

        private final int id;

        ExtractionStep(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }

        public static String[] names() {
            ExtractionStep[] values = values();
            String[] names = new String[values.length - 1];

            // Skip NONE, lower all chars except first + replace '_' with ' '
            for (int i = 0; i < values.length - 1; i++) {
                String name = values[i + 1].toString().substring(0, 1).toUpperCase() + values[i + 1].toString().substring(1);
                names[i] = name.replaceAll("_", " ");
            }

            return names;
        }
    }

    public static Integer EXTRACTION_STEP_DIVISOR = 1024; // num of bytes

    private File dumpFile;
    private SAXParser wikiXMLFileParser;
    private MonitoredInputStream mis;
    private ExtractionStep currentExtractionStep;
    
    private final ExtractionStep startingStep;
    private final Boolean stem;

    public WikipediaExtractor(String dumpFilePath, Boolean forceIfExists, Boolean addIndexes, Boolean stem, ExtractionStep startingStep) {        
        // Try to initialize the collections, create indexes etc.
        if (Wikipedia.getInstance().initializeDatabase(startingStep, forceIfExists, addIndexes) == false) {
            throw new IllegalArgumentException("Database already exists, please use another name or \"Force\" flag.");
        }
        
        // Load the dump file
        dumpFile = new File(dumpFilePath);
        try {
            wikiXMLFileParser = SAXParserFactory.newInstance().newSAXParser();
            wikiXMLFileParser.setProperty("http://www.oracle.com/xml/jaxp/properties/totalEntitySizeLimit", "1000000000");
        } catch (ParserConfigurationException | SAXException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        // Set the starting step for extraction
        this.currentExtractionStep = ExtractionStep.NONE;
        this.startingStep = startingStep;
        this.stem = stem;
    }

    public ExtractionStep getStartingStep() {
        return startingStep;
    }

    private void initializeStream() {
        try {
            // The monitor will update status every 256 kb
            mis = new MonitoredInputStream(new FileInputStream(dumpFile), 256 * WikipediaExtractor.EXTRACTION_STEP_DIVISOR);
            mis.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {

                }
            });
        } catch (FileNotFoundException ex) {
            System.err.println(ex.getMessage());
        }
    }

    @Override
    public void run() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        try {
            switch (startingStep) {
                case NONE:
                case PARSING_XML_ARTICLES:
                    // Unpack articles into database
                    initializeStream();
                    System.out.println(sdf.format(Calendar.getInstance().getTime()) + "*** STARTED Unpacking articles from Wikipedia Dump ***");
                    currentExtractionStep = ExtractionStep.PARSING_XML_ARTICLES;
                    wikiXMLFileParser.parse(mis, new WikiXMLExtractorHandler(currentExtractionStep, stem));
                    System.out.println(sdf.format(Calendar.getInstance().getTime()) + "*** FINISHED Unpacking articles from Wikipedia Dump ***");
                    Wikipedia.getInstance().countArticles();
                case PARSING_XML_LINKS:
                    // Unpack labels into database
                    initializeStream();
                    System.out.println(sdf.format(Calendar.getInstance().getTime()) + "*** STARTED Unpacking links from Wikipedia Dump ***");
                    currentExtractionStep = ExtractionStep.PARSING_XML_LINKS;
                    wikiXMLFileParser.parse(mis, new WikiXMLExtractorHandler(currentExtractionStep, stem));
                    System.out.println(sdf.format(Calendar.getInstance().getTime()) + "*** FINISHED Unpacking links from Wikipedia Dump ***");
                case LABELS_AGGREGATION:
                    // Map reduce the labels so that we have unique labels instead of multiple ones
                    System.out.println(sdf.format(Calendar.getInstance().getTime()) + "*** STARTED Labels aggregation ***");
                    currentExtractionStep = ExtractionStep.LABELS_AGGREGATION;
                    Wikipedia.getInstance().aggregateLabels();
                    System.out.println(sdf.format(Calendar.getInstance().getTime()) + "*** FINISHED Labels aggregation  ***");
                case SETTING_LINKS_FROM_TO:
                    // Go through all articles and set their from/to links                   
                    System.out.println(sdf.format(Calendar.getInstance().getTime()) + "*** STARTED Setting links from/to ***");
                    currentExtractionStep = ExtractionStep.SETTING_LINKS_FROM_TO;
                    Wikipedia.getInstance().setLinksFromTo();
                    System.out.println(sdf.format(Calendar.getInstance().getTime()) + "*** FINISHED Setting links from/to ***");
            }
        } catch (SAXException | IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public ExtractionStep getCurrentStep() {
        return currentExtractionStep;
    }

    public int getStepProgress() {
        switch (currentExtractionStep) {
            case PARSING_XML_ARTICLES:
            case PARSING_XML_LINKS:
                return mis.getProgress();
            case LABELS_AGGREGATION:
            case SETTING_LINKS_FROM_TO:
                return Wikipedia.getInstance().getCurrentStepProgress();
            default:
                return 0;
        }
    }

    public int getCurrentStepMax() {
        switch (currentExtractionStep) {
            case PARSING_XML_ARTICLES:
            case PARSING_XML_LINKS:
                return (int) (dumpFile.length() / WikipediaExtractor.EXTRACTION_STEP_DIVISOR);
            case SETTING_LINKS_FROM_TO:
            case LABELS_AGGREGATION:
                return Wikipedia.getInstance().getCurrentStepMax();
            default:
                return 0;
        }
    }
}
