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
package pl.gda.eti.pg.enrich_your_text.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import pl.gda.eti.pg.enrich_your_text.algorithms.Algorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.AlgorithmParameter;
import pl.gda.eti.pg.enrich_your_text.algorithms.annotation.AnnotationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.disambiguation.DisambiguationAlgorithm;
import pl.gda.eti.pg.enrich_your_text.algorithms.input_parsing.InputParsingAlgorithm;
import pl.gda.eti.pg.enrich_your_text.models.WikiDocument.DocumentType;
import pl.gda.eti.pg.enrich_your_text.algorithms.keyphrase_lookup.KeyphraseLookupAlgorithm;
import pl.gda.eti.pg.enrich_your_text.database.mongodb.MongoWikiDB;
import pl.gda.eti.pg.enrich_your_text.enrichment.TextEnricher;
import pl.gda.eti.pg.enrich_your_text.enrichment.TextEnricher.Options;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;
import pl.gda.eti.pg.enrich_your_text.settings.Configuration;
import pl.gda.eti.pg.enrich_your_text.tools.ToolKit;

/**
 *
 * @author mnarusze
 */
public class EnrichYourText extends javax.swing.JFrame {
    String textToEnrich;
    DocumentType inputType;

    Map<String, Algorithm> algorithms;

    SwingWorker enricherWorker;
    Boolean enrichmentInProgress;

    private final String START_ENRICHMENT_TEXT = "Start enrichment";
    private final String CANCEL_ENRICHMENT_TEXT = "Cancel enrichment";
    
    /**
     * Creates new form MainGUI
     */
    public EnrichYourText() {
        // Algorithms
        algorithms = new LinkedHashMap<>();
        algorithms.put(KeyphraseLookupAlgorithm.ALGORITHM_TYPE_PRETTY, TextEnricher.KEYPHRASE_LOOKUP_ALGORITHMS.get(0));
        algorithms.put(InputParsingAlgorithm.ALGORITHM_TYPE_PRETTY, TextEnricher.INPUT_PARSING_ALGORITHMS.get(0));
        algorithms.put(DisambiguationAlgorithm.ALGORITHM_TYPE_PRETTY, TextEnricher.DISAMBIGUATION_ALGORITHMS.get(0));
        algorithms.put(AnnotationAlgorithm.ALGORITHM_TYPE_PRETTY, TextEnricher.ANNOTATION_ALGORITHMS.get(0));

        // Wikipedia setup
        Wikipedia.getInstance().loadInitialData();
        this.enrichmentInProgress = false;

        // Graphics
        initComponents();
        drawAlgorithmsParams();
        setLocationRelativeTo(null);

        // Start opening the database
        openDatabase();
    }

    private void openDatabase() {
        Thread dbConnectionThread = new Thread(new DatabaseConnectionThread(this));
        dbConnectionThread.start();
    }

    private void drawAlgorithmsParams() {
        algorithmParamsPanel.removeAll();
        int gridY = 0;
        for (Algorithm algorithm : algorithms.values()) {
            for (AlgorithmParameter param : algorithm.getParameters().values()) {
                java.awt.GridBagConstraints gridBagConstraints;

                javax.swing.JLabel algorithmParamNameLabel = new javax.swing.JLabel();
                algorithmParamNameLabel.setText(param.getName());

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = gridY;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;

                algorithmParamsPanel.add(algorithmParamNameLabel, gridBagConstraints);

                NumberFormat numberFormat = NumberFormat.getNumberInstance();

                JFormattedTextField algorithmParamValueTextField = new JFormattedTextField(numberFormat);
                algorithmParamValueTextField.setValue(param.getValue());
                algorithmParamValueTextField.setColumns(10);
                algorithmParamValueTextField.addPropertyChangeListener("value", new AlgorithmParameterValueChangeListener(param));

                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = gridY;
                gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new java.awt.Insets(5, 8, 5, 8);

                algorithmParamsPanel.add(algorithmParamValueTextField, gridBagConstraints);

                gridY++;
            }
        }
        optionsPanel.validate();
        optionsPanel.repaint();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        inputFileChooser = new javax.swing.JFileChooser();
        outputFileChooser = new javax.swing.JFileChooser();
        mainPanel = new javax.swing.JPanel();
        inputDocLabel = new javax.swing.JLabel();
        inputDocScrollPane = new javax.swing.JScrollPane();
        inputText = new javax.swing.JTextArea();
        outputDocLabel = new javax.swing.JLabel();
        outputScrollPane = new javax.swing.JScrollPane();
        outputTextPane = new javax.swing.JTextPane();
        optionsPanel = new javax.swing.JPanel();
        optionsJLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        sourceDatabaseComboBox = new javax.swing.JComboBox<>();
        inputDocTypeLabel1 = new javax.swing.JLabel();
        inputDocTypeComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        inputParsingAlgorithmLabel = new javax.swing.JLabel();
        inputParsingAlgorithmComboBox = new javax.swing.JComboBox<>();
        keyphraseLookupAlgorithmLabel = new javax.swing.JLabel();
        keyphraseLookupAlgorithmComboBox = new javax.swing.JComboBox<>();
        disambiguationAlgorithmLabel = new javax.swing.JLabel();
        disambiguationAlgorithmComboBox = new javax.swing.JComboBox();
        annotationAlgorithmLabel = new javax.swing.JLabel();
        annotationAlgorithmComboBox = new javax.swing.JComboBox<>();
        algorithmOptionsJLabel = new javax.swing.JLabel();
        algorithmParamsPanel = new javax.swing.JPanel();
        controlButtonsPanel = new javax.swing.JPanel();
        reloadDatabaseButton = new javax.swing.JButton();
        removeDatabaseButton = new javax.swing.JButton();
        resetToDefaultValuesButton = new javax.swing.JButton();
        startStopEnrichmentButton = new javax.swing.JButton();
        logsPanel = new javax.swing.JPanel();
        logsScrollPane = new javax.swing.JScrollPane();
        logsJTextPane = new javax.swing.JTextPane();
        logsLabel = new javax.swing.JLabel();
        notifactionsPanel = new javax.swing.JPanel();
        bottomInfoText = new java.awt.Label();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        openFileMenuItem = new javax.swing.JMenuItem();
        saveOutputToFileMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        onlineHelpMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        inputFileChooser.setApproveButtonText("");
        inputFileChooser.setApproveButtonToolTipText("");
        inputFileChooser.setFileFilter(new TextFileFilter());

        outputFileChooser.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        outputFileChooser.setApproveButtonText("");
        outputFileChooser.setApproveButtonToolTipText("");
        outputFileChooser.setFileFilter(new HTMLFileFilter());

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Enrich Your Text");
        setExtendedState(6);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        inputDocLabel.setFont(inputDocLabel.getFont().deriveFont(inputDocLabel.getFont().getStyle() | java.awt.Font.BOLD, inputDocLabel.getFont().getSize()+4));
        inputDocLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        inputDocLabel.setText("Document to enrich");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        mainPanel.add(inputDocLabel, gridBagConstraints);

        inputDocScrollPane.setName(""); // NOI18N
        inputDocScrollPane.setPreferredSize(new java.awt.Dimension(102, 102));

        inputText.setFont(inputText.getFont().deriveFont(inputText.getFont().getSize()+2f));
        inputText.setLineWrap(true);
        inputText.setTabSize(4);
        inputText.setWrapStyleWord(true);
        inputText.setMaximumSize(null);
        inputText.setMinimumSize(new java.awt.Dimension(6, 6));
        inputText.setName("Test"); // NOI18N
        inputDocScrollPane.setViewportView(inputText);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        mainPanel.add(inputDocScrollPane, gridBagConstraints);

        outputDocLabel.setFont(outputDocLabel.getFont().deriveFont(outputDocLabel.getFont().getStyle() | java.awt.Font.BOLD, outputDocLabel.getFont().getSize()+4));
        outputDocLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        outputDocLabel.setText("Output");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        mainPanel.add(outputDocLabel, gridBagConstraints);

        outputTextPane.setEditable(false);
        outputTextPane.setContentType("text/html"); // NOI18N
        outputTextPane.setFont(outputTextPane.getFont().deriveFont(outputTextPane.getFont().getSize()+2f));
        outputTextPane.setMaximumSize(new java.awt.Dimension(6, 6));
        outputTextPane.setPreferredSize(new java.awt.Dimension(100, 100));
        outputTextPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                outputTextPaneHyperlinkUpdate(evt);
            }
        });
        outputScrollPane.setViewportView(outputTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.5;
        mainPanel.add(outputScrollPane, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 4, 5);
        getContentPane().add(mainPanel, gridBagConstraints);

        optionsPanel.setLayout(new java.awt.GridBagLayout());

        optionsJLabel.setFont(optionsJLabel.getFont().deriveFont(optionsJLabel.getFont().getStyle() | java.awt.Font.BOLD, optionsJLabel.getFont().getSize()+4));
        optionsJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        optionsJLabel.setText("Options");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        optionsPanel.add(optionsJLabel, gridBagConstraints);

        jLabel1.setText("Source Database");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        optionsPanel.add(jLabel1, gridBagConstraints);

        sourceDatabaseComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sourceDatabaseComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        optionsPanel.add(sourceDatabaseComboBox, gridBagConstraints);

        inputDocTypeLabel1.setText("Input Document Type");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        optionsPanel.add(inputDocTypeLabel1, gridBagConstraints);

        inputDocTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel(DocumentType.names()));
        inputDocTypeComboBox.setSelectedIndex(0);
        inputDocTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputDocTypeComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(inputDocTypeComboBox, gridBagConstraints);

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 15)); // NOI18N
        jLabel2.setText("Algorithms");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        optionsPanel.add(jLabel2, gridBagConstraints);

        inputParsingAlgorithmLabel.setText("Input Parsing Algorithm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        optionsPanel.add(inputParsingAlgorithmLabel, gridBagConstraints);

        List<String> inputParingAlgorithmNames = new ArrayList<>();
        for (InputParsingAlgorithm algorithm: TextEnricher.INPUT_PARSING_ALGORITHMS) {
            inputParingAlgorithmNames.add(algorithm.getName());
        }
        inputParsingAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(inputParingAlgorithmNames.toArray(new String[0])));
        inputParsingAlgorithmComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputParsingAlgorithmComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(inputParsingAlgorithmComboBox, gridBagConstraints);

        keyphraseLookupAlgorithmLabel.setText("Keyphrase Lookup Algorithm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        optionsPanel.add(keyphraseLookupAlgorithmLabel, gridBagConstraints);

        List<String> keyphraseLookupAlgorithmNames = new ArrayList<>();
        for (KeyphraseLookupAlgorithm algorithm: TextEnricher.KEYPHRASE_LOOKUP_ALGORITHMS) {
            keyphraseLookupAlgorithmNames.add(algorithm.getName());
        }
        keyphraseLookupAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(keyphraseLookupAlgorithmNames.toArray(new String[0])));
        keyphraseLookupAlgorithmComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyphraseLookupAlgorithmComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(keyphraseLookupAlgorithmComboBox, gridBagConstraints);

        disambiguationAlgorithmLabel.setText("Disambiguation Algorithm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        optionsPanel.add(disambiguationAlgorithmLabel, gridBagConstraints);

        List<String> disambiguationAlgorithmNames = new ArrayList<>();
        for (DisambiguationAlgorithm algorithm: TextEnricher.DISAMBIGUATION_ALGORITHMS) {
            disambiguationAlgorithmNames.add(algorithm.getName());
        }
        disambiguationAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel(disambiguationAlgorithmNames.toArray(new String[0])));
        disambiguationAlgorithmComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disambiguationAlgorithmComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(disambiguationAlgorithmComboBox, gridBagConstraints);

        annotationAlgorithmLabel.setText("Annotation Algorithm");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        optionsPanel.add(annotationAlgorithmLabel, gridBagConstraints);

        List<String> annotationAlgorithmNames = new ArrayList<>();
        for (AnnotationAlgorithm algorithm: TextEnricher.ANNOTATION_ALGORITHMS) {
            annotationAlgorithmNames.add(algorithm.getName());
        }
        annotationAlgorithmComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(annotationAlgorithmNames.toArray(new String[0])));
        annotationAlgorithmComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                annotationAlgorithmComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        optionsPanel.add(annotationAlgorithmComboBox, gridBagConstraints);

        algorithmOptionsJLabel.setFont(algorithmOptionsJLabel.getFont().deriveFont(algorithmOptionsJLabel.getFont().getStyle() | java.awt.Font.BOLD, algorithmOptionsJLabel.getFont().getSize()+4));
        algorithmOptionsJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        algorithmOptionsJLabel.setText("Parameters");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        optionsPanel.add(algorithmOptionsJLabel, gridBagConstraints);

        algorithmParamsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        optionsPanel.add(algorithmParamsPanel, gridBagConstraints);

        controlButtonsPanel.setLayout(new java.awt.GridLayout(4, 1));

        reloadDatabaseButton.setText("Reload databases");
        reloadDatabaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadDatabaseButtonActionPerformed(evt);
            }
        });
        controlButtonsPanel.add(reloadDatabaseButton);

        removeDatabaseButton.setText("Remove database");
        removeDatabaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeDatabaseButtonActionPerformed(evt);
            }
        });
        controlButtonsPanel.add(removeDatabaseButton);

        resetToDefaultValuesButton.setText("Reset to defaults");
        resetToDefaultValuesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetToDefaultValuesButtonActionPerformed(evt);
            }
        });
        controlButtonsPanel.add(resetToDefaultValuesButton);

        startStopEnrichmentButton.setText(START_ENRICHMENT_TEXT);
        startStopEnrichmentButton.setEnabled(false);
        startStopEnrichmentButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startStopEnrichmentButtonActionPerformed(evt);
            }
        });
        controlButtonsPanel.add(startStopEnrichmentButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LAST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 0, 0);
        optionsPanel.add(controlButtonsPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 4, 5);
        getContentPane().add(optionsPanel, gridBagConstraints);

        logsPanel.setLayout(new java.awt.GridBagLayout());

        logsScrollPane.setBackground(new java.awt.Color(254, 254, 254));
        logsScrollPane.setForeground(new java.awt.Color(5, 0, 0));
        logsScrollPane.setMinimumSize(new java.awt.Dimension(500, 23));
        logsScrollPane.setPreferredSize(new java.awt.Dimension(500, 23));

        logsJTextPane.setEditable(false);
        logsJTextPane.setContentType(""); // NOI18N
        logsJTextPane.setFont(new java.awt.Font("Consolas", 0, 12)); // NOI18N
        logsJTextPane.setName(""); // NOI18N
        logsScrollPane.setViewportView(logsJTextPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        logsPanel.add(logsScrollPane, gridBagConstraints);

        logsLabel.setFont(logsLabel.getFont().deriveFont(logsLabel.getFont().getStyle() | java.awt.Font.BOLD, logsLabel.getFont().getSize()+4));
        logsLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        logsLabel.setText("Logs");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(10, 0, 10, 0);
        logsPanel.add(logsLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_START;
        gridBagConstraints.insets = new java.awt.Insets(4, 5, 4, 5);
        getContentPane().add(logsPanel, gridBagConstraints);

        notifactionsPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        notifactionsPanel.add(bottomInfoText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 11, 8, 11);
        getContentPane().add(notifactionsPanel, gridBagConstraints);

        jMenu1.setText("File");

        openFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openFileMenuItem.setText("Open File");
        openFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openFileMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(openFileMenuItem);

        saveOutputToFileMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveOutputToFileMenuItem.setText("Save output to File");
        saveOutputToFileMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveOutputToFileMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(saveOutputToFileMenuItem);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(exitMenuItem);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setText("Configure database");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuItem5.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem5.setText("Extract Wikipedia Dump");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        helpMenu.setText("Help");

        onlineHelpMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
        onlineHelpMenuItem.setText("Online Help");
        onlineHelpMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                onlineHelpMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(onlineHelpMenuItem);

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startStopEnrichmentButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopEnrichmentButtonActionPerformed
        if (enrichmentInProgress) {
            enricherWorker.cancel(true);
        } else {
            textToEnrich = inputText.getText();
            if (textToEnrich.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No text to enrich!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            outputTextPane.setText("");
            
            addLog(false, "\n####################################################\n");
            addLog(false, "Starting enrichment!\n");
            addLog(false, "Selected algorithms:");
            for (String algorithmType : algorithms.keySet()) {
                Algorithm algorithm = algorithms.get(algorithmType);
                addLog(false, "* " + algorithmType + " -> " + algorithm.getName());
                if (!algorithm.getParameters().keySet().isEmpty()) {
                    for (String paramName : algorithm.getParameters().keySet()) {
                        AlgorithmParameter param = algorithm.getParameters().get(paramName);
                        addLog(false, "  - " + param.getName()+ " -> " + param.getValue());
                    }
                }
            }
            addLog(false, "");

            enricherWorker = new SwingWorker<Long, Void>() {
                private TextEnricher textEnricher;
                Thread enricherThread;
                long startTime = 0L;

                @Override
                public Long doInBackground() {
                    startTime = System.currentTimeMillis();
                    textEnricher = new TextEnricher(textToEnrich);
                    textEnricher.options = textEnricher.new Options(
                            (InputParsingAlgorithm) algorithms.get(InputParsingAlgorithm.ALGORITHM_TYPE_PRETTY),
                            (KeyphraseLookupAlgorithm) algorithms.get(KeyphraseLookupAlgorithm.ALGORITHM_TYPE_PRETTY),
                            (DisambiguationAlgorithm) algorithms.get(DisambiguationAlgorithm.ALGORITHM_TYPE_PRETTY),
                            (AnnotationAlgorithm) algorithms.get(AnnotationAlgorithm.ALGORITHM_TYPE_PRETTY),
                            inputType);
                    enrichmentInProgress = true;
                    changeFormEnabled(false);

                    enricherThread = new Thread(textEnricher);
                    enricherThread.start();

                    while (enricherThread.isAlive()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            System.err.println(ex.getMessage());
                        }
                    }
                    return 0L;
                }

                @Override
                public void done() {
                    long time = (System.currentTimeMillis() - startTime) / 1000;
                    try {
                        get();
                    } catch (InterruptedException | ExecutionException ex) {
                    } catch (CancellationException ex) {
                        enricherThread.interrupt();
                    }
                    // addLog(false, textEnricher.getDetailsText());
                    outputTextPane.setText(textEnricher.getEnrichedText());
                    changeFormEnabled(true);
                    enrichmentInProgress = false;
                    addLog(false, "Execution took " + time + " seconds!");
                    if (textEnricher.succeeded()) {
                        JOptionPane.showMessageDialog(null, "Enrichment was successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Enrichment failed. " + textEnricher.getOutputMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            };

            enricherWorker.execute();
        }
    }//GEN-LAST:event_startStopEnrichmentButtonActionPerformed

    private void outputTextPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_outputTextPaneHyperlinkUpdate
        HyperlinkEvent.EventType eventType = evt.getEventType();
        if (eventType == HyperlinkEvent.EventType.ACTIVATED) {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(evt.getURL().toURI());
                } catch (IOException | URISyntaxException e1) {
                    e1.printStackTrace();
                }
            }
        } else if (eventType == HyperlinkEvent.EventType.ENTERED) {
            bottomInfoText.setText(evt.getURL().toString());
        } else if (eventType == HyperlinkEvent.EventType.EXITED) {
            bottomInfoText.setText("");
        }
    }//GEN-LAST:event_outputTextPaneHyperlinkUpdate

    private void resetToDefaultValuesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetToDefaultValuesButtonActionPerformed
        for (Algorithm algorithm : algorithms.values()) {
            for (AlgorithmParameter param : algorithm.getParameters().values()) {
                param.setDefaultValue();
            }
        }
        drawAlgorithmsParams();
    }//GEN-LAST:event_resetToDefaultValuesButtonActionPerformed

    private void keyphraseLookupAlgorithmComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyphraseLookupAlgorithmComboBoxActionPerformed
        algorithms.put(
                KeyphraseLookupAlgorithm.ALGORITHM_TYPE_PRETTY,
                TextEnricher.KEYPHRASE_LOOKUP_ALGORITHMS.get(keyphraseLookupAlgorithmComboBox.getSelectedIndex()));
        drawAlgorithmsParams();
    }//GEN-LAST:event_keyphraseLookupAlgorithmComboBoxActionPerformed

    private void inputParsingAlgorithmComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputParsingAlgorithmComboBoxActionPerformed
        algorithms.put(
                InputParsingAlgorithm.ALGORITHM_TYPE_PRETTY,
                TextEnricher.INPUT_PARSING_ALGORITHMS.get(inputParsingAlgorithmComboBox.getSelectedIndex()));
        drawAlgorithmsParams();
    }//GEN-LAST:event_inputParsingAlgorithmComboBoxActionPerformed

    private void disambiguationAlgorithmComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disambiguationAlgorithmComboBoxActionPerformed
        algorithms.put(
                DisambiguationAlgorithm.ALGORITHM_TYPE_PRETTY,
                TextEnricher.DISAMBIGUATION_ALGORITHMS.get(disambiguationAlgorithmComboBox.getSelectedIndex()));
        drawAlgorithmsParams();
    }//GEN-LAST:event_disambiguationAlgorithmComboBoxActionPerformed

    private void annotationAlgorithmComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_annotationAlgorithmComboBoxActionPerformed
        algorithms.put(
                AnnotationAlgorithm.ALGORITHM_TYPE_PRETTY,
                TextEnricher.ANNOTATION_ALGORITHMS.get(annotationAlgorithmComboBox.getSelectedIndex()));
        drawAlgorithmsParams();
    }//GEN-LAST:event_annotationAlgorithmComboBoxActionPerformed

    private void inputDocTypeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputDocTypeComboBoxActionPerformed
        inputType = DocumentType.myValueOf((String) inputDocTypeComboBox.getSelectedItem());
    }//GEN-LAST:event_inputDocTypeComboBoxActionPerformed

    private void onlineHelpMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onlineHelpMenuItemActionPerformed
        ToolKit.openWebpage("https://github.com/mnarusze/enrich-your-text");
    }//GEN-LAST:event_onlineHelpMenuItemActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        ExtractWikipedia extractorGUI = new ExtractWikipedia(this, true);
        extractorGUI.setVisible(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        DatabaseSettings databaseSettingsGUI = new DatabaseSettings(this, true);
        databaseSettingsGUI.setVisible(true);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void sourceDatabaseComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sourceDatabaseComboBoxActionPerformed
        String selectedDatabase = (String) sourceDatabaseComboBox.getSelectedItem();
        if (Wikipedia.getInstance().openDatabase(selectedDatabase) == false) {
            addLog(true, "Failed to open database \"" + selectedDatabase + "\"");
            
        } else {
            Wikipedia.getInstance().loadWikipediaFromDB();
            Configuration.setDatabaseName(selectedDatabase);
            addLog(false, "Successfully loaded Wikipedia from database \"" + selectedDatabase + "\"!");
            addLog(false, "Name      -> " + Wikipedia.getInstance().getName());
            addLog(false, "Link base -> " + Wikipedia.getInstance().getBase());
            addLog(false, "Stemmed   -> " + Wikipedia.getInstance().isStemmed());
        }
    }//GEN-LAST:event_sourceDatabaseComboBoxActionPerformed

    private void reloadDatabaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadDatabaseButtonActionPerformed
        openDatabase();
    }//GEN-LAST:event_reloadDatabaseButtonActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        int retVal = JOptionPane.showConfirmDialog(null, "Do you really want to exit?", "Exit - confirm", JOptionPane.YES_NO_OPTION);
        if (retVal == JOptionPane.YES_OPTION)
            this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void openFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openFileMenuItemActionPerformed
        Integer retVal = inputFileChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = inputFileChooser.getSelectedFile();
            try {
                String fileContents = "";
                for (String line : Files.readAllLines(file.toPath())) {
                    fileContents += line + "\n"; 
                }
                inputText.setText(fileContents);
            } catch (IOException ex) {
                Logger.getLogger(EnrichYourText.class.getName()).log(Level.SEVERE, null, ex);
                addLog(true, "Failed to open the file " + file.getAbsolutePath() + ". Error: " + ex.getLocalizedMessage());
            }
        }
    }//GEN-LAST:event_openFileMenuItemActionPerformed

    private void saveOutputToFileMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveOutputToFileMenuItemActionPerformed
        Integer retVal = outputFileChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = outputFileChooser.getSelectedFile();
            // Enforce html
            if (!file.getAbsolutePath().endsWith(".html")) {
                file = new File(file.getAbsolutePath() + ".html");
            }
            try {
                String output = outputTextPane.getText();
                List<String> outputText = Arrays.asList(output);
                Files.write(file.toPath(), outputText, Charset.forName("UTF-8"));
                JOptionPane.showMessageDialog(null, "Successfully saved the output text to " + file.getAbsolutePath() + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(EnrichYourText.class.getName()).log(Level.SEVERE, null, ex);
                addLog(true, "Failed to save the file " + file.getAbsolutePath() + ". Error: " + ex.getLocalizedMessage());
            }
        }
    }//GEN-LAST:event_saveOutputToFileMenuItemActionPerformed

    private void removeDatabaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeDatabaseButtonActionPerformed
        String databaseName = Configuration.getDatabaseName();
        int retVal = JOptionPane.showConfirmDialog(null, "Do you want to remove database " + databaseName + "?", "Removing database - confirm", JOptionPane.YES_NO_OPTION);
        
        if (retVal == JOptionPane.YES_OPTION) {
            if (Wikipedia.getInstance().removeDatabase(databaseName) == false) {
                JOptionPane.showMessageDialog(null, "Failed to remove database " + databaseName + "!", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "Successfully removed database " + databaseName + "!", "Success", JOptionPane.INFORMATION_MESSAGE);
                sourceDatabaseComboBox.setModel(new DefaultComboBoxModel<>(Wikipedia.getInstance().getWikiDatabaseNames().toArray(new String[0])));
            }
        }    
    }//GEN-LAST:event_removeDatabaseButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EnrichYourText.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EnrichYourText.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EnrichYourText.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EnrichYourText.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EnrichYourText().setVisible(true);
            }
        });
    }

    private void changeFormEnabled(Boolean enabled) {

        for (Component component : optionsPanel.getComponents()) {
            component.setEnabled(enabled);
        }

        for (Component component : algorithmParamsPanel.getComponents()) {
            component.setEnabled(enabled);
        }
        
        removeDatabaseButton.setEnabled(enabled);
        reloadDatabaseButton.setEnabled(enabled);
        resetToDefaultValuesButton.setEnabled(enabled);

        if (enabled) {
            startStopEnrichmentButton.setText(START_ENRICHMENT_TEXT);
        } else {
            startStopEnrichmentButton.setText(CANCEL_ENRICHMENT_TEXT);
        }
    }

    private void addLog(Boolean error, String log) {

        SimpleAttributeSet textAttributes = new SimpleAttributeSet();
        if (error) {
            StyleConstants.setForeground(textAttributes, Color.RED);
        } else {
            StyleConstants.setForeground(textAttributes, Color.BLACK);
        }

        try {
            javax.swing.text.Document doc = logsJTextPane.getDocument();
            doc.insertString(doc.getLength(), log + "\n", textAttributes);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JLabel algorithmOptionsJLabel;
    private javax.swing.JPanel algorithmParamsPanel;
    private javax.swing.JComboBox<String> annotationAlgorithmComboBox;
    private javax.swing.JLabel annotationAlgorithmLabel;
    private java.awt.Label bottomInfoText;
    private javax.swing.JPanel controlButtonsPanel;
    private javax.swing.JComboBox disambiguationAlgorithmComboBox;
    private javax.swing.JLabel disambiguationAlgorithmLabel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JLabel inputDocLabel;
    private javax.swing.JScrollPane inputDocScrollPane;
    private javax.swing.JComboBox inputDocTypeComboBox;
    private javax.swing.JLabel inputDocTypeLabel1;
    private javax.swing.JFileChooser inputFileChooser;
    private javax.swing.JComboBox<String> inputParsingAlgorithmComboBox;
    private javax.swing.JLabel inputParsingAlgorithmLabel;
    private javax.swing.JTextArea inputText;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JComboBox<String> keyphraseLookupAlgorithmComboBox;
    private javax.swing.JLabel keyphraseLookupAlgorithmLabel;
    private javax.swing.JTextPane logsJTextPane;
    private javax.swing.JLabel logsLabel;
    private javax.swing.JPanel logsPanel;
    private javax.swing.JScrollPane logsScrollPane;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel notifactionsPanel;
    private javax.swing.JMenuItem onlineHelpMenuItem;
    private javax.swing.JMenuItem openFileMenuItem;
    private javax.swing.JLabel optionsJLabel;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JLabel outputDocLabel;
    private javax.swing.JFileChooser outputFileChooser;
    private javax.swing.JScrollPane outputScrollPane;
    private javax.swing.JTextPane outputTextPane;
    private javax.swing.JButton reloadDatabaseButton;
    private javax.swing.JButton removeDatabaseButton;
    private javax.swing.JButton resetToDefaultValuesButton;
    private javax.swing.JMenuItem saveOutputToFileMenuItem;
    private javax.swing.JComboBox<String> sourceDatabaseComboBox;
    private javax.swing.JButton startStopEnrichmentButton;
    // End of variables declaration//GEN-END:variables

    private class AlgorithmParameterValueChangeListener implements PropertyChangeListener {

        AlgorithmParameter parameter;

        private AlgorithmParameterValueChangeListener(AlgorithmParameter parameter) {
            this.parameter = parameter;
        }

        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (this.parameter.getType() == Double.class) {
                parameter.setValue(((Number) pce.getNewValue()).doubleValue());
            } else if (this.parameter.getType() == Integer.class) {
                parameter.setValue(((Number) pce.getNewValue()).intValue());
            } else {
                addLog(false, "Unsupported parameter type detected for " + parameter.getName() + ". For more info, look for this message in the source code.");
            }
        }
    }

    private class DatabaseConnectionThread implements Runnable {

        Component parentComponent;

        public DatabaseConnectionThread(Component parentComponent) {
            this.parentComponent = parentComponent;
        }

        @Override
        public void run() {
            removeDatabaseButton.setEnabled(false);
            reloadDatabaseButton.setEnabled(false);
            resetToDefaultValuesButton.setEnabled(false);
            startStopEnrichmentButton.setEnabled(false);
            addLog(false, "Connecting to database under port " + Configuration.getDatabasePort() + " with " + MongoWikiDB.CONNECTION_TIMEOUT_MS / 1000 + " seconds timeout...");

            if (Wikipedia.getInstance().connectToDatabase(Configuration.getDatabasePort()) == false) {
                addLog(true, "Failed to connect to database!");
                startStopEnrichmentButton.setEnabled(false);
                JOptionPane.showMessageDialog(parentComponent, "Couldn't connect to database! Please test database configuration.", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                addLog(false, "Successfully connected to " + Configuration.currentDatabaseType.toString() + " database!");
                addLog(false, "Getting database names from Wikipedia...");
                List<String> databaseNames = Wikipedia.getInstance().getWikiDatabaseNames();
                if (databaseNames.isEmpty()) {
                    JOptionPane.showMessageDialog(parentComponent, "No databases detected. Prepare one using \"Edit->Extract Wikipedia Dump\"", "No databases", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    addLog(false, "Got following databases:");
                    for (String databaseName : databaseNames) addLog(false, "* " + databaseName);
                                       
                    sourceDatabaseComboBox.setModel(new DefaultComboBoxModel<>(databaseNames.toArray(new String[0])));
                    String databaseName = sourceDatabaseComboBox.getItemAt(0);
                    if (Wikipedia.getInstance().openDatabase(databaseName) == false) {
                        addLog(true, "Failed to open database \"" + databaseName + "\"");
                    } else {
                        if (Wikipedia.getInstance().loadWikipediaFromDB() == false) {
                            addLog(true, "Failed to load Wikipedia from database \"" + databaseName + "\"!");
                        } else {
                            addLog(false, "\nSuccessfully loaded Wikipedia from database \"" + databaseName + "\"!");
                            addLog(false, "Name      -> " + Wikipedia.getInstance().getName());
                            addLog(false, "Link base -> " + Wikipedia.getInstance().getBase());
                            addLog(false, "Stemmed   -> " + Wikipedia.getInstance().isStemmed());
                            changeFormEnabled(true);
                            startStopEnrichmentButton.setEnabled(true);
                        }
                    }
                }
            }
            reloadDatabaseButton.setEnabled(true);
        }
    }
    
    private class TextFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return (f.getAbsolutePath().endsWith(".txt"));
        }

        @Override
        public String getDescription() {
            return "Text files (*.txt)";
        }

    }
    
    private class HTMLFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return (f.getAbsolutePath().endsWith(".html"));
        }

        @Override
        public String getDescription() {
            return "Enriched text (*.html)";
        }

    }
}
