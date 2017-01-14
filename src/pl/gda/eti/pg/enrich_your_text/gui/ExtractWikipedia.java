/*
 * Copyright (C) 2014 maryl
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

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;
import pl.gda.eti.pg.enrich_your_text.database.WikiDatabase;
import pl.gda.eti.pg.enrich_your_text.extraction.WikipediaExtractor;
import static pl.gda.eti.pg.enrich_your_text.extraction.WikipediaExtractor.ExtractionStep;
import pl.gda.eti.pg.enrich_your_text.models.Wikipedia;
import pl.gda.eti.pg.enrich_your_text.settings.Configuration;

/**
 *
 * @author maryl
 */
public class ExtractWikipedia extends javax.swing.JDialog {

    private Boolean unpackInProgress;
    private ExtractSwingWorker unpackWorker;

    /**
     * Creates new form WikipediaExtractorGUI
     */
    public ExtractWikipedia(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        unpackWorker = new ExtractSwingWorker();
        unpackInProgress = false;

        initComponents();
    }

    public class ExtractSwingWorker extends SwingWorker<Long, Integer> {
        private WikipediaExtractor dumpExtractor;
        private ExtractionStep previousStep;
        private Boolean success;
        private String errorMessage;
        private Thread extractThread = null;

        @Override
        public Long doInBackground() {
            int startingStepIdx = startingStepComboBox.getSelectedIndex();
            success = true;
            errorMessage = "";
            try {
                dumpExtractor = new WikipediaExtractor(
                        Configuration.getWikiDumpPath(),
                        paramForceCheckbox.isSelected(),
                        paramAddIndexesCheckbox.isSelected(),
                        paramStemCheckbox.isSelected(),
                        ExtractionStep.values()[startingStepIdx + 1]);
            } catch (IllegalArgumentException ex) {
                errorMessage = ex.getMessage();
                success = false;
                return 0L;
            }

            previousStep = ExtractionStep.NONE;

            changeFormEnabled(false);
            unpackInProgress = true;
            extractThread = new Thread(dumpExtractor);
            
            try {
                extractThread.start();
                while (extractThread.isAlive()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        success = false;
                        errorMessage = "Extraction was interrupted!";
                    }
                    publish(dumpExtractor.getStepProgress());
                }
            }
            catch (Exception ex) {
                success = false;
                errorMessage = ex.getMessage();
            }
            return Wikipedia.getInstance().getArticlesCount();
        }

        @Override
        protected void process(List<Integer> list) {
            ExtractionStep currentStep = this.dumpExtractor.getCurrentStep();

            if (currentStep != previousStep) {
                unpackProgressBar.setMaximum(dumpExtractor.getCurrentStepMax());
                previousStep = currentStep;
            }
            unpackProgressBar.setValue(list.get(list.size() - 1));
            if (currentStep.getValue() > 0) {
                unpackProgressBar.setString((int) (unpackProgressBar.getPercentComplete() * 100) + "% - step " + currentStep.getValue() + "/" + (ExtractionStep.values().length - 1));
            }
        }

        @Override
        public void done() {
            unpackInProgress = false;
            if (extractThread != null) {
                try {
                    extractThread.interrupt();
                } catch (SecurityException ex) {
                    Logger.getLogger(WikipediaExtractor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            changeFormEnabled(true);
            if (success) {
                JOptionPane.showMessageDialog(null, "Succesfully unpacked Wikipedia database!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "An error has occured: " + errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void changeFormEnabled(boolean enabled) {
            setComponentAndChildrenEnabled(dumpFilePanel, enabled);
            if (enabled) {
                startStopUnpackButton.setText("Unpack Wikipedia");
            } else {
                startStopUnpackButton.setText("Cancel extraction");
            }
        }
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

        wikipediaDumpChooser = new javax.swing.JFileChooser();
        dumpFilePanel = new javax.swing.JPanel();
        dumpFileOptionsLabel = new javax.swing.JLabel();
        wikipediaDumpPathTextField = new javax.swing.JTextField(Configuration.getWikiDumpPath());
        wikipediaDumpPathChangeButton = new javax.swing.JButton();
        pathToDumpFileLabel = new javax.swing.JLabel();
        dbNameLabel = new javax.swing.JLabel();
        dbNameTextField = new javax.swing.JTextField(Configuration.getDatabaseName());
        startingStepLabel = new javax.swing.JLabel();
        startingStepComboBox = new javax.swing.JComboBox<>();
        dbParametersLabel = new javax.swing.JLabel();
        dbParametersPanel = new javax.swing.JPanel();
        paramAddIndexesCheckbox = new javax.swing.JCheckBox();
        paramForceCheckbox = new javax.swing.JCheckBox();
        paramStemCheckbox = new javax.swing.JCheckBox();
        buttonsPanel = new javax.swing.JPanel();
        startStopUnpackButton = new javax.swing.JButton();
        unpackProgressBarPanel = new javax.swing.JPanel();
        unpackProgressBar = new javax.swing.JProgressBar();

        wikipediaDumpChooser.setCurrentDirectory(null);
        wikipediaDumpChooser.setFileFilter(new WikiDumpFileFilter());
        wikipediaDumpChooser.setMinimumSize(new java.awt.Dimension(650, 350));
        wikipediaDumpChooser.setPreferredSize(new java.awt.Dimension(650, 350));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Dump Extractor");
        setIconImage(null);
        setModal(true);
        setResizable(false);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        dumpFilePanel.setLayout(new java.awt.GridBagLayout());

        dumpFileOptionsLabel.setFont(dumpFileOptionsLabel.getFont().deriveFont(dumpFileOptionsLabel.getFont().getStyle() | java.awt.Font.BOLD, dumpFileOptionsLabel.getFont().getSize()+2));
        dumpFileOptionsLabel.setText("Extraction Options");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 5, 0);
        dumpFilePanel.add(dumpFileOptionsLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 5);
        dumpFilePanel.add(wikipediaDumpPathTextField, gridBagConstraints);

        wikipediaDumpPathChangeButton.setText("Change");
        wikipediaDumpPathChangeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wikipediaDumpPathChangeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 5);
        dumpFilePanel.add(wikipediaDumpPathChangeButton, gridBagConstraints);

        pathToDumpFileLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        pathToDumpFileLabel.setText("Path to dump file");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        dumpFilePanel.add(pathToDumpFileLabel, gridBagConstraints);

        dbNameLabel.setText("Database Name");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        dumpFilePanel.add(dbNameLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 5);
        dumpFilePanel.add(dbNameTextField, gridBagConstraints);

        startingStepLabel.setText("Starting step");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        dumpFilePanel.add(startingStepLabel, gridBagConstraints);

        startingStepComboBox.setModel(new javax.swing.DefaultComboBoxModel(ExtractionStep.names()));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 5);
        dumpFilePanel.add(startingStepComboBox, gridBagConstraints);

        dbParametersLabel.setText("Parameters");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        dumpFilePanel.add(dbParametersLabel, gridBagConstraints);

        dbParametersPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        paramAddIndexesCheckbox.setSelected(true);
        paramAddIndexesCheckbox.setText("Add indexes");
        dbParametersPanel.add(paramAddIndexesCheckbox);

        paramForceCheckbox.setText("Force");
        dbParametersPanel.add(paramForceCheckbox);

        paramStemCheckbox.setText("Use stemming");
        dbParametersPanel.add(paramStemCheckbox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 2, 5);
        dumpFilePanel.add(dbParametersPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(dumpFilePanel, gridBagConstraints);

        buttonsPanel.setLayout(new java.awt.GridBagLayout());

        startStopUnpackButton.setText("Unpack Wikipedia");
        startStopUnpackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startStopUnpackButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        buttonsPanel.add(startStopUnpackButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(buttonsPanel, gridBagConstraints);

        unpackProgressBarPanel.setLayout(new java.awt.GridBagLayout());

        unpackProgressBar.setMinimumSize(new java.awt.Dimension(550, 17));
        unpackProgressBar.setPreferredSize(new java.awt.Dimension(550, 17));
        unpackProgressBar.setStringPainted(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 5, 2, 5);
        unpackProgressBarPanel.add(unpackProgressBar, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        gridBagConstraints.weightx = 1.0;
        getContentPane().add(unpackProgressBarPanel, gridBagConstraints);

        getAccessibleContext().setAccessibleDescription("");

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void startStopUnpackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startStopUnpackButtonActionPerformed
        if (unpackInProgress) {
            unpackWorker.cancel(true);
            unpackProgressBar.setValue(unpackProgressBar.getMinimum());
            unpackProgressBar.setString("0%");  
        } else {
            // Save the parameters from input
            storeFormInput();
            
            // Verify the input
            if (!isUnpackFormInputValid()) {
                return;
            }
            
            WikiDatabase.WikiDatabaseTypes wikiDBType = Configuration.currentDatabaseType;
            String dbName = Configuration.getDatabaseName();
            Integer dbPort = Configuration.getDatabasePort();         
         
            // Open the database
            if (!Wikipedia.getInstance().connectToDatabase(dbPort)) {
                JOptionPane.showMessageDialog(this, "Couldn't connect to database!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Get the data from the database
            if (!Wikipedia.getInstance().openDatabase(dbName)) {
                return;
            }

            // Set the progress bar
            unpackProgressBar.setValue(unpackProgressBar.getMinimum());
            unpackProgressBar.setMaximum((int) (new File(Configuration.getWikiDumpPath()).length() / WikipediaExtractor.EXTRACTION_STEP_DIVISOR));
            
            // Launch extraction
            unpackWorker = new ExtractSwingWorker();
            unpackWorker.execute();
        }
    }//GEN-LAST:event_startStopUnpackButtonActionPerformed

    private void wikipediaDumpPathChangeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wikipediaDumpPathChangeButtonActionPerformed
        int retVal;

        String currentWikiDumpPath = wikipediaDumpPathTextField.getText();
        File currentWikiDumpFile = new File(currentWikiDumpPath);
        if (currentWikiDumpFile.isFile() && currentWikiDumpFile.exists()) {
           wikipediaDumpChooser.setCurrentDirectory(currentWikiDumpFile.getParentFile());
        }

        retVal = wikipediaDumpChooser.showOpenDialog(this);
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = wikipediaDumpChooser.getSelectedFile();
            wikipediaDumpPathTextField.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_wikipediaDumpPathChangeButtonActionPerformed

    private void storeFormInput() {
        Configuration.setDatabaseName(dbNameTextField.getText());
        Configuration.setWikiDumpPath(wikipediaDumpPathTextField.getText());
    }

    private boolean isUnpackFormInputValid() {
        File WikipediaDumpFile;

        WikipediaDumpFile = new File(Configuration.getWikiDumpPath());
        if (!WikipediaDumpFile.isFile()) {
            JOptionPane.showMessageDialog(this, "The chosen file " + Configuration.getWikiDumpPath() + " doesn't exist", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (Configuration.getDatabaseName().isEmpty()) {
            JOptionPane.showMessageDialog(this, "The database name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    private void setComponentAndChildrenEnabled(Component component, Boolean enabled) {
        component.setEnabled(enabled);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                setComponentAndChildrenEnabled(child, enabled);
            }
        }
        
        if (enabled) {
            startStopUnpackButton.setText("Unpack Wikipedia");
        } else {
            startStopUnpackButton.setText("Cancel");
        }
    }

    private class WikiDumpFileFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
            return (f.getAbsolutePath().endsWith(".xml"));
        }

        @Override
        public String getDescription() {
            return "Wikipedia dump files (*.xml)";
        }

    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JLabel dbNameLabel;
    private javax.swing.JTextField dbNameTextField;
    private javax.swing.JLabel dbParametersLabel;
    private javax.swing.JPanel dbParametersPanel;
    private javax.swing.JLabel dumpFileOptionsLabel;
    private javax.swing.JPanel dumpFilePanel;
    private javax.swing.JCheckBox paramAddIndexesCheckbox;
    private javax.swing.JCheckBox paramForceCheckbox;
    private javax.swing.JCheckBox paramStemCheckbox;
    private javax.swing.JLabel pathToDumpFileLabel;
    private javax.swing.JButton startStopUnpackButton;
    private javax.swing.JComboBox<String> startingStepComboBox;
    private javax.swing.JLabel startingStepLabel;
    private javax.swing.JProgressBar unpackProgressBar;
    private javax.swing.JPanel unpackProgressBarPanel;
    private javax.swing.JFileChooser wikipediaDumpChooser;
    private javax.swing.JButton wikipediaDumpPathChangeButton;
    private javax.swing.JTextField wikipediaDumpPathTextField;
    // End of variables declaration//GEN-END:variables
}
