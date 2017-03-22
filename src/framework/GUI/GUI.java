/*
 * The MIT License
 *
 * Copyright 2016 angel.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package framework.GUI;

import com.sun.javafx.runtime.SystemProperties;
import framework.exceptions.IllegalActionException;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.peer.PanelPeer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import keel.Dataset.Attributes;
import keel.Dataset.DatasetException;
import keel.Dataset.HeaderFormatException;
import keel.Dataset.InstanceSet;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;
import framework.deprecated.Pattern;
import framework.utils.Utils;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class GUI extends javax.swing.JFrame {

    private Vector<String> algorithms = new Vector<>();
    private Vector<JPanel> paramPanels = new Vector<>();
    private DefaultComboBoxModel modelo;
    private Document doc;
    private String actual_fully_qualified_class;
    private String preds;
    HashMap<String, Double> qualityMeasures = new HashMap<>();
    File lastDirectory;

    /**
     * Creates new form Main
     */
    public GUI() {
        // Here we have to read the algorithms XML and add to algorithms the names of the methods
        doc = readXML("algorithms.xml");
        preds = "";
        File f = new File("options.txt");
        if (f.exists()) {
            try {
                BufferedReader bf = new BufferedReader(new FileReader(f));
                lastDirectory = new File(bf.readLine());
            } catch (IOException ex) {
                lastDirectory = new File(System.getProperty("user.home"));
            }
        } else {
            lastDirectory = new File(System.getProperty("user.home"));
        }
        initComponents();

        // initializy quality measures hash map.
        resetMeasures();

        // Adds algorithm names
        NodeList nodes = doc.getElementsByTagName("algorithm");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            // Parse the name of the method
            algorithms.add(node.getElementsByTagName("name").item(0).getTextContent());
        }

        // Sets the first algorithm parameters
        addParamsToPanel(doc, 0, ParametersPanel);
        addParamsToPanel(doc, 0, ParametersPanel1);
        // Adds the list of algorithms to the list ad set the first as default
        modelo = new DefaultComboBoxModel(algorithms);
        AlgorithmList.setModel(modelo);
        AlgorithmList1.setModel(modelo);

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Tabs = new javax.swing.JTabbedPane();
        LearnPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        rutaTra = new javax.swing.JTextField();
        BrowseButtonTRA = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        rutaTst = new javax.swing.JTextField();
        BrowseButtonTST = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        AlgorithmList = new javax.swing.JComboBox<>();
        LearnButton = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        ParametersPanel = new javax.swing.JPanel();
        SaveModelCheckbox = new javax.swing.JCheckBox();
        rutaModel = new javax.swing.JTextField();
        BrowseButtonModel = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        ExecutionInfoLearn = new javax.swing.JTextPane();
        LoadPanel = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        InstancesPath = new javax.swing.JTextField();
        BrowseModelButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        ModelPath1 = new javax.swing.JTextField();
        BrowseInstances = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        PredictionsPanel = new javax.swing.JTextPane();
        jButton1 = new javax.swing.JButton();
        BatchPanel = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        AlgorithmList1 = new javax.swing.JComboBox<>();
        ParametersPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        rutaBatch = new javax.swing.JTextField();
        BrowseBatchFolder = new javax.swing.JButton();
        BatchButton = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        BatchOutput = new javax.swing.JTextPane();
        jLabel8 = new javax.swing.JLabel();
        numFolds = new javax.swing.JComboBox<>();
        ParallelCheckbox = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Emerging Pattern Mining Algorithms Framework");
        setBackground(new java.awt.Color(204, 204, 204));
        setMinimumSize(getPreferredSize());

        jLabel1.setText("Training file:");

        rutaTra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaTraActionPerformed(evt);
            }
        });

        BrowseButtonTRA.setText("Browse...");
        BrowseButtonTRA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseButtonTRAActionPerformed(evt);
            }
        });

        jLabel2.setText("Test file (optional): ");

        rutaTst.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaTstActionPerformed(evt);
            }
        });

        BrowseButtonTST.setText("Browse...");
        BrowseButtonTST.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseButtonTSTActionPerformed(evt);
            }
        });

        jLabel3.setText("Algorithm:");

        AlgorithmList.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AlgorithmListActionPerformed(evt);
            }
        });

        LearnButton.setText("Run!");
        LearnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LearnButtonActionPerformed(evt);
            }
        });

        ParametersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Algorithms Parameters"));
        ParametersPanel.setLayout(new java.awt.GridLayout(50, 2));
        jScrollPane2.setViewportView(ParametersPanel);

        SaveModelCheckbox.setSelected(true);
        SaveModelCheckbox.setText("Save Model:");
        SaveModelCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveModelCheckboxActionPerformed(evt);
            }
        });

        rutaModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaModelActionPerformed(evt);
            }
        });

        BrowseButtonModel.setText("Browse...");
        BrowseButtonModel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseButtonModelActionPerformed(evt);
            }
        });

        ExecutionInfoLearn.setEditable(false);
        jScrollPane1.setViewportView(ExecutionInfoLearn);

        javax.swing.GroupLayout LearnPanelLayout = new javax.swing.GroupLayout(LearnPanel);
        LearnPanel.setLayout(LearnPanelLayout);
        LearnPanelLayout.setHorizontalGroup(
            LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LearnPanelLayout.createSequentialGroup()
                .addGap(425, 425, 425)
                .addComponent(LearnButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(LearnPanelLayout.createSequentialGroup()
                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LearnPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LearnPanelLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3)
                            .addComponent(jLabel1)
                            .addComponent(SaveModelCheckbox))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LearnPanelLayout.createSequentialGroup()
                                .addComponent(AlgorithmList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(LearnPanelLayout.createSequentialGroup()
                                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rutaTst, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
                                    .addComponent(rutaModel)
                                    .addComponent(rutaTra))
                                .addGap(18, 18, 18)
                                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(BrowseButtonModel)
                                    .addComponent(BrowseButtonTRA)
                                    .addComponent(BrowseButtonTST)))))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        LearnPanelLayout.setVerticalGroup(
            LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LearnPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LearnPanelLayout.createSequentialGroup()
                        .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(rutaTra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(59, 59, 59))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LearnPanelLayout.createSequentialGroup()
                        .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(LearnPanelLayout.createSequentialGroup()
                                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(LearnPanelLayout.createSequentialGroup()
                                        .addComponent(BrowseButtonTRA)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                            .addComponent(BrowseButtonTST)
                                            .addComponent(rutaTst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(jLabel2))
                                        .addGap(35, 35, 35))
                                    .addComponent(BrowseButtonModel, javax.swing.GroupLayout.Alignment.TRAILING))
                                .addGap(1, 1, 1))
                            .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(SaveModelCheckbox)
                                .addComponent(rutaModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)))
                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(AlgorithmList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 284, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LearnButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE)
                .addContainerGap())
        );

        Tabs.addTab("Learn Model", LearnPanel);

        jLabel4.setText("Model: ");

        BrowseModelButton.setText("Browse...");
        BrowseModelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseModelButtonActionPerformed(evt);
            }
        });

        jLabel5.setText("Instances to Predict:");

        BrowseInstances.setText("Browse...");
        BrowseInstances.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseInstancesActionPerformed(evt);
            }
        });

        jButton2.setText("Run!");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        PredictionsPanel.setEditable(false);
        jScrollPane4.setViewportView(PredictionsPanel);

        jButton1.setText("Save to File...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LoadPanelLayout = new javax.swing.GroupLayout(LoadPanel);
        LoadPanel.setLayout(LoadPanelLayout);
        LoadPanelLayout.setHorizontalGroup(
            LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoadPanelLayout.createSequentialGroup()
                .addGroup(LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LoadPanelLayout.createSequentialGroup()
                        .addGap(457, 457, 457)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 391, Short.MAX_VALUE))
                    .addGroup(LoadPanelLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel4))
                        .addGap(5, 5, 5)
                        .addGroup(LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ModelPath1)
                            .addComponent(InstancesPath))
                        .addGap(18, 18, 18)
                        .addGroup(LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(BrowseModelButton)
                            .addComponent(BrowseInstances)))
                    .addGroup(LoadPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane4)))
                .addContainerGap())
            .addGroup(LoadPanelLayout.createSequentialGroup()
                .addGap(400, 400, 400)
                .addComponent(jButton1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        LoadPanelLayout.setVerticalGroup(
            LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoadPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(BrowseModelButton)
                    .addComponent(ModelPath1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(LoadPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(InstancesPath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BrowseInstances))
                .addGap(18, 18, 18)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 461, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap(69, Short.MAX_VALUE))
        );

        Tabs.addTab("Load Model", LoadPanel);

        jLabel6.setText("Algorithm:");

        AlgorithmList1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AlgorithmList1ActionPerformed(evt);
            }
        });

        ParametersPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Algorithms Parameters"));
        ParametersPanel1.setLayout(new java.awt.GridLayout(50, 2));

        jLabel7.setText("Folder with data:");

        rutaBatch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rutaBatchActionPerformed(evt);
            }
        });

        BrowseBatchFolder.setText("Browse...");
        BrowseBatchFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BrowseBatchFolderActionPerformed(evt);
            }
        });

        BatchButton.setText("Run!");
        BatchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BatchButtonActionPerformed(evt);
            }
        });

        BatchOutput.setEditable(false);
        jScrollPane3.setViewportView(BatchOutput);

        jLabel8.setText("Number of folds:");

        numFolds.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "3", "5", "7", "9", "10" }));
        numFolds.setSelectedIndex(1);
        numFolds.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                numFoldsActionPerformed(evt);
            }
        });

        ParallelCheckbox.setText("Parallel");
        ParallelCheckbox.setToolTipText("Execute in parallel using all possible threads -1 to prevent system hang.");
        ParallelCheckbox.setEnabled(false);
        ParallelCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ParallelCheckboxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout BatchPanelLayout = new javax.swing.GroupLayout(BatchPanel);
        BatchPanel.setLayout(BatchPanelLayout);
        BatchPanelLayout.setHorizontalGroup(
            BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BatchPanelLayout.createSequentialGroup()
                .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, BatchPanelLayout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(AlgorithmList1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ParallelCheckbox)
                        .addGap(278, 278, 278))
                    .addGroup(BatchPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ParametersPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 907, Short.MAX_VALUE))))
                .addContainerGap(15, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BatchPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BatchPanelLayout.createSequentialGroup()
                        .addComponent(rutaBatch, javax.swing.GroupLayout.PREFERRED_SIZE, 626, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(BrowseBatchFolder))
                    .addComponent(numFolds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(BatchPanelLayout.createSequentialGroup()
                .addGap(421, 421, 421)
                .addComponent(BatchButton)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        BatchPanelLayout.setVerticalGroup(
            BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BatchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rutaBatch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BrowseBatchFolder)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(numFolds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(AlgorithmList1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ParallelCheckbox))
                .addGap(33, 33, 33)
                .addComponent(ParametersPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(BatchButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                .addContainerGap())
        );

        Tabs.addTab("Batch Execution", BatchPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Tabs)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(Tabs)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        // Dinamically calls the method learn of the method: VERY INTERESTING FUNCTION!
        PredictionsPanel.setEditable(true);
        PredictionsPanel.setText("");
        preds = "";
        try {
            if (ModelPath1.getText().equals("")) {
                throw new IllegalActionException("ERROR: You must specify a model.");
            }
            if (InstancesPath.getText().equals("")) {
                throw new IllegalActionException("ERROR: You must specify a test set.");
            }
            Attributes.clearAll();
            InstanceSet test = new InstanceSet();
            try {
                test.readSet(InstancesPath.getText(), true);
            } catch (DatasetException | HeaderFormatException ex) {
                throw new IllegalActionException("ERROR: An error ocurred when reading the dataset. Possible bad format?");
            }

            test.setAttributesAsNonStatic();
            SwingWorker worker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {

                    //First: instantiate the class selected with th fully qualified name of the read model
                    Object model = Model.readModel(ModelPath1.getText());
                    Class clase = Class.forName(((Model) model).getFullyQualifiedName());
                    //Object newObject = clase.newInstance();

                    // Second: get the argument class
                    Class[] args = new Class[1];
                    args[0] = InstanceSet.class;

                    // Third: Get the method 'learn' of the class and invoke it.
                    String[][] predictions = (String[][]) clase.getMethod("predict", args).invoke(model, test);
                    for (int i = 0; i < predictions[0].length; i++) {
                        if (test.getAttributeDefinitions().getOutputAttributes() != null) {
                            appendToPane(PredictionsPanel, test.getOutputNominalValue(i, 0) + "  -  " + predictions[0][i], Color.BLUE, false);
                            preds += test.getOutputNominalValue(i, 0) + "  -  " + predictions[0][i] + "\n";
                        } else {
                            appendToPane(PredictionsPanel, predictions[0][i], Color.BLUE, false);
                            preds += predictions[0][i] + "\n";
                        }
                    }
                    PredictionsPanel.setEditable(false);

                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    } catch (ExecutionException ex) {
                        appendToPane(PredictionsPanel, "An unexpected error has ocurred: " + ex.getCause().toString(), Color.red);
                        ex.printStackTrace();
                    }
                }

            };
            worker.execute();
        } catch (IllegalActionException ex) {
            appendToPane(PredictionsPanel, ex.getReason(), Color.red);
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void BrowseInstancesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseInstancesActionPerformed
        // Create the file chooser pointing to the home directory of the actual user
        // Select only files and apply filter to select only *.dat files.
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home")));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("KEEL data files", "dat"));
        // This eliminate the option of "All files" on the file selection dialog
        fileChooser.setAcceptAllFileFilterUsed(false);
        // Show the dialog
        int result = fileChooser.showOpenDialog(BrowseButtonTST.getParent());
        // If the user press in 'Ok'...
        if (result == JFileChooser.APPROVE_OPTION) {
            File fileSelected = fileChooser.getSelectedFile();
            InstancesPath.setText(fileSelected.getAbsolutePath());
        }
    }//GEN-LAST:event_BrowseInstancesActionPerformed

    private void BrowseModelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseModelButtonActionPerformed
        // Create the file chooser pointing to the home directory of the actual user
        // Select only files and apply filter to select only *.dat files.
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home")));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".ser Model files", "ser"));
        // This eliminate the option of "All files" on the file selection dialog
        fileChooser.setAcceptAllFileFilterUsed(false);
        // Show the dialog
        int result = fileChooser.showOpenDialog(BrowseButtonTST.getParent());
        // If the user press in 'Ok'...
        if (result == JFileChooser.APPROVE_OPTION) {
            PrintWriter pw = null;
            try {
                File fileSelected = fileChooser.getSelectedFile();
                lastDirectory = fileSelected.getParentFile();
                pw = new PrintWriter(new File("options.txt"));
                pw.println(lastDirectory.getAbsolutePath());
                ModelPath1.setText(fileSelected.getAbsolutePath());

            } catch (FileNotFoundException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                pw.close();
            }
        }
    }//GEN-LAST:event_BrowseModelButtonActionPerformed

    private void SaveModelCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveModelCheckboxActionPerformed

        rutaModel.setEnabled(SaveModelCheckbox.isSelected());
        BrowseButtonModel.setEnabled(SaveModelCheckbox.isSelected());

    }//GEN-LAST:event_SaveModelCheckboxActionPerformed

    private void LearnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LearnButtonActionPerformed
        ExecutionInfoLearn.setEditable(true);
        // Reads the parameters of the user
        appendToPane(ExecutionInfoLearn, "Reading parameters and files...", Color.BLUE);

        HashMap<String, String> params = readParameters(ParametersPanel);
        InstanceSet training = new InstanceSet();
        InstanceSet test = new InstanceSet();

        // Dinamically calls the method learn of the method: VERY INTERESTING FUNCTION!
        try {
            if (SaveModelCheckbox.isSelected() && rutaModel.getText().equals("")) {
                throw new framework.exceptions.IllegalActionException("ERROR: You must specify a path to save the model.");
            }
            if (rutaTra.getText().equals("")) {
                throw new framework.exceptions.IllegalActionException("ERROR: You must specify a training file.");
            }

            // Execute the task in background to update the text area.
            SwingWorker worker;
            worker = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    // Reads training and test file
                    Attributes.clearAll();
                    try {
                        training.readSet(rutaTra.getText(), true);
                    } catch (DatasetException | HeaderFormatException | NullPointerException ex) {
                        throw new IllegalActionException("ERROR: Format error on training file.");
                    }
                    training.setAttributesAsNonStatic();
                    if (!rutaTst.getText().equals("")) {
                        test.readSet(rutaTst.getText(), false);
                        test.setAttributesAsNonStatic();
                    }

                    appendToPane(ExecutionInfoLearn, "Executing " + (String) AlgorithmList.getSelectedItem() + " algorithm... (This may take a while)", Color.BLUE);

                    //First: instantiate the class selected with the fully qualified name
                    Object newObject;
                    Class clase = Class.forName(actual_fully_qualified_class);
                    newObject = clase.newInstance();
                    ((Model) newObject).patterns = new ArrayList<>();
                    ((Model) newObject).patternsFilteredMinimal = new ArrayList<>();
                    ((Model) newObject).patternsFilteredMaximal = new ArrayList<>();
                    ((Model) newObject).patternsFilteredByMeasure = new ArrayList<>();

                    // Second: get the argument class
                    Class[] args = new Class[2];
                    args[0] = InstanceSet.class;
                    args[1] = HashMap.class;

                    System.out.println("Learning Model...");
                    // Third: Get the method 'learn' of the class and invoke it. (cambiar "new InstanceSet" por el training)
                    clase.getMethod("learn", args).invoke(newObject, training, params);
                    appendToPane(ExecutionInfoLearn, "Filtering patterns and calculating descriptive measures...", Color.BLUE);
                    System.out.println("Filtering patterns and calculating descriptive measures...");

                    // Get learned patterns, filter, and calculate measures for training
                    ArrayList<HashMap<String, Double>> Measures = Utils.calculateDescriptiveMeasures(training, ((Model) newObject).getPatterns(), true);

                    // Filter the patterns, returning the average quality measures for each set of patterns
                    ArrayList<HashMap<String, Double>> filterPatterns = Utils.filterPatterns((Model) newObject, "CONF", 0.6f);
                    for (int i = 0; i < filterPatterns.size(); i++) {
                        // Adds to Masures to write later the average results in the file.
                        Measures.add(filterPatterns.get(i));
                    }

                    // Call predict method for ACC and AUC for training
                    appendToPane(ExecutionInfoLearn, "Calculate precision for training...", Color.BLUE);
                    System.out.println("Calculating precision for training...");
                    args = new Class[1];
                    args[0] = InstanceSet.class;
                    String[][] predictionsTra = (String[][]) clase.getMethod("predict", args).invoke(newObject, training);
                    Utils.calculatePrecisionMeasures(predictionsTra, training, training, Measures);

                    // Save training measures in a file.
                    System.out.println("Save results in a file...");
                    appendToPane(ExecutionInfoLearn, "Save result in a file...", Color.BLUE);
                    Utils.saveMeasures(new File(rutaTra.getText()).getParentFile(), (Model) newObject, Measures, true, 0);
                    appendToPane(ExecutionInfoLearn, "Done", Color.BLUE);
                    System.out.println("Done learning model.");

                    // If there is a test set call the method "predict" to make the test phase.
                    if (!rutaTst.getText().equals("")) {
                        // Calculate descriptive measures for test
                        appendToPane(ExecutionInfoLearn, "Testing instances...", Color.BLUE);
                        System.out.println("Testing instances...");

                        // Calculate test measures for unfiltered and filtered patterns
                        Measures = Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatterns(), false);
                        Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredMinimal(), false).get(0));
                        Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredMaximal(), false).get(0));
                        Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredByMeasure(), false).get(0));

                        args = new Class[1];
                        args[0] = InstanceSet.class;
                        // Call predict method
                        String[][] predictions = (String[][]) clase.getMethod("predict", args).invoke(newObject, test);

                        // Calculate predictions
                        Utils.calculatePrecisionMeasures(predictions, test, training, Measures);
                        // Save Results
                        //Utils.saveResults(new File(rutaTst.getText()).getParentFile(), Measures.get(0), Measures.get(1), Measures.get(2), 1);
                        Utils.saveMeasures(new File(rutaTst.getText()).getParentFile(), (Model) newObject, Measures, false, 0);
                        appendToPane(ExecutionInfoLearn, "Done. Results of quality measures saved in " + new File(rutaTst.getText()).getParentFile().getAbsolutePath(), Color.BLUE);
                        System.out.println("Done. Results of quality measures saved in " + new File(rutaTst.getText()).getParentFile().getAbsolutePath());

                    }

                    // Invoke saveModel method if neccesary
                    if (SaveModelCheckbox.isSelected()) {
                        appendToPane(ExecutionInfoLearn, "Saving Model...", Color.BLUE);
                        args = new Class[1];
                        args[0] = String.class;
                        clase.getMethod("saveModel", args).invoke(newObject, rutaModel.getText());
                        appendToPane(ExecutionInfoLearn, "Done", Color.BLUE);
                    }
                    ExecutionInfoLearn.setEditable(false);
                    return null;

                }

                protected void done() {
                    try {
                        get();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        if (cause instanceof IllegalActionException) {
                            appendToPane(ExecutionInfoLearn, ((IllegalActionException) cause).getReason(), Color.red);
                        } else {
                            appendToPane(ExecutionInfoLearn, "An unexpected error has ocurred: " + cause.toString(), Color.red);
                        }
                        ex.printStackTrace();
                    }
                }

            };

            worker.execute();

        } catch (IllegalActionException ex) {
            appendToPane(ExecutionInfoLearn, ex.getReason(), Color.red);
            ex.printStackTrace();
        } catch (Exception ex) {
            appendToPane(ExecutionInfoLearn, ex.getMessage(), Color.red);
            ex.printStackTrace();
        }


    }//GEN-LAST:event_LearnButtonActionPerformed

    private void AlgorithmListActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AlgorithmListActionPerformed
        // TODO add your handling code here:
        int value = AlgorithmList.getSelectedIndex();
        if (value != -1 || value > paramPanels.size()) {
            addParamsToPanel(doc, value, ParametersPanel);
        }
    }//GEN-LAST:event_AlgorithmListActionPerformed

    private void BrowseButtonTSTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseButtonTSTActionPerformed
        // Create the file chooser pointing to the home directory of the actual user
        // Select only files and apply filter to select only *.dat files.
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("KEEL data files", "dat"));
        // This eliminate the option of "All files" on the file selection dialog
        fileChooser.setAcceptAllFileFilterUsed(false);
        // Show the dialog
        int result = fileChooser.showOpenDialog(BrowseButtonTST.getParent());
        // If the user press in 'Ok'...
        if (result == JFileChooser.APPROVE_OPTION) {
            PrintWriter pw = null;
            try {
                File fileSelected = fileChooser.getSelectedFile();
                lastDirectory = fileSelected.getParentFile();
                pw = new PrintWriter(new File("options.txt"));
                pw.println(lastDirectory.getAbsolutePath());
                rutaTst.setText(fileSelected.getAbsolutePath());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                pw.close();
            }
        }
    }//GEN-LAST:event_BrowseButtonTSTActionPerformed

    private void rutaTstActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaTstActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rutaTstActionPerformed

    private void BrowseButtonTRAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseButtonTRAActionPerformed
        // Create the file chooser pointing to the home directory of the actual user
        // Select only files and apply filter to select only *.dat files.
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("KEEL data files", "dat"));
        // This eliminate the option of "All files" on the file selection dialog
        fileChooser.setAcceptAllFileFilterUsed(false);
        // Show the dialog
        int result = fileChooser.showOpenDialog(BrowseButtonTRA.getParent());
        // If the user press in 'Ok'...
        if (result == JFileChooser.APPROVE_OPTION) {
            PrintWriter pw = null;
            try {
                File fileSelected = fileChooser.getSelectedFile();
                lastDirectory = fileSelected.getParentFile();
                pw = new PrintWriter(new File("options.txt"));
                pw.println(lastDirectory.getAbsolutePath());
                rutaTra.setText(fileSelected.getAbsolutePath());
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                pw.close();
            }
        }
    }//GEN-LAST:event_BrowseButtonTRAActionPerformed

    private void rutaTraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaTraActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rutaTraActionPerformed

    private void rutaModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaModelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rutaModelActionPerformed

    private void BrowseButtonModelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseButtonModelActionPerformed
        // Create the file chooser pointing to the home directory of the actual user
        // Select only files and apply filter to select only *.dat files.
        JFileChooser fileChooser = new JFileChooser(/*new File(System.getProperty("user.home"))*/);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(".ser files", "ser"));
        // This eliminate the option of "All files" on the file selection dialog
        fileChooser.setAcceptAllFileFilterUsed(false);
        // Show the dialog
        int result = fileChooser.showOpenDialog(BrowseButtonTRA.getParent());
        // If the user press in 'Ok'...
        if (result == JFileChooser.APPROVE_OPTION) {
            PrintWriter pw = null;
            try {
                File fileSelected = fileChooser.getSelectedFile();
                lastDirectory = fileSelected.getParentFile();
                pw = new PrintWriter(new File("options.txt"));
                pw.println(lastDirectory.getAbsolutePath());
                rutaModel.setText(fileSelected.getAbsolutePath() + ".ser");

            } catch (FileNotFoundException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                pw.close();
            }
        }
    }//GEN-LAST:event_BrowseButtonModelActionPerformed

    private void AlgorithmList1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AlgorithmList1ActionPerformed
        int value = AlgorithmList1.getSelectedIndex();
        if (value != -1 || value > paramPanels.size()) {
            addParamsToPanel(doc, value, ParametersPanel1);
        }
    }//GEN-LAST:event_AlgorithmList1ActionPerformed

    private void rutaBatchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rutaBatchActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rutaBatchActionPerformed

    private void BrowseBatchFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BrowseBatchFolderActionPerformed
        JFileChooser fileChooser = new JFileChooser(lastDirectory);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showOpenDialog(BrowseBatchFolder.getParent());
        // If the user press in 'Ok'...
        if (result == JFileChooser.APPROVE_OPTION) {
            PrintWriter pw = null;
            try {
                File fileSelected = fileChooser.getSelectedFile();
                lastDirectory = fileSelected.getParentFile();
                pw = new PrintWriter(new File("options.txt"));
                pw.println(lastDirectory.getAbsolutePath());
                rutaBatch.setText(fileSelected.getAbsolutePath());

            } catch (FileNotFoundException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                appendToPane(BatchOutput, "ERROR: Folder " + lastDirectory.getAbsolutePath() + " Not found", Color.red);
            } finally {
                pw.close();
            }
        }
    }//GEN-LAST:event_BrowseBatchFolderActionPerformed

    private void BatchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BatchButtonActionPerformed
        try {
            BatchOutput.setEditable(true);
            if (rutaBatch.getText().equals("")) {
                throw new IllegalActionException("ERROR: No folder selected.");
            }
            File root = new File(rutaBatch.getText());
            int NUM_FOLDS = Integer.parseInt(numFolds.getItemAt(numFolds.getSelectedIndex()));
            File[] folders = root.listFiles();
            Arrays.sort(folders);
            InstanceSet training = new InstanceSet();
            InstanceSet test = new InstanceSet();

            HashMap<String, String> params = readParameters(ParametersPanel1);

            SwingWorker work = new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    int NUM_THREADS = 1;
                    if (ParallelCheckbox.isSelected()) {
                        NUM_THREADS = Runtime.getRuntime().availableProcessors();
                    }
//                    ExecutorService exec = Executors.newFixedThreadPool(NUM_THREADS);
                    // for each folder in the root directory

                    for (File dir : folders) {

                        if (dir.isDirectory()) {
                            File[] files = dir.listFiles();
                            Arrays.sort(files);
                            HashMap<String, Double> QMsUnfiltered = Utils.generateQualityMeasuresHashMap();
                            HashMap<String, Double> QMsMinimal = Utils.generateQualityMeasuresHashMap();
                            HashMap<String, Double> QMsMaximal = Utils.generateQualityMeasuresHashMap();
                            HashMap<String, Double> QMsByMeasure = Utils.generateQualityMeasuresHashMap();

                            appendToPane(BatchOutput, "Executing " + dir.getName() + "...", Color.BLUE);
                            System.out.println("Executing..." + dir.getName() + "...");
                            for (int i = 1; i <= NUM_FOLDS; i++) {
                                // Search for the training and test files.
                                for (File x : files) {
                                    // El formato es xx5xx-1tra.dat
                                    if (x.getName().matches(".*" + NUM_FOLDS + ".*-" + i + "tra.dat")) {
                                        try {
                                            Attributes.clearAll();
                                            training.readSet(x.getAbsolutePath(), true);
                                            training.setAttributesAsNonStatic();
                                        } catch (DatasetException | HeaderFormatException | NullPointerException ex) {
                                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                                            appendToPane(BatchOutput, ex.toString(), Color.red);
                                        }
                                    }
                                    if (x.getName().matches(".*" + NUM_FOLDS + ".*-" + i + "tst.dat")) {
                                        try {
                                            test.readSet(x.getAbsolutePath(), false);
                                            test.setAttributesAsNonStatic();
                                        } catch (DatasetException | HeaderFormatException | NullPointerException ex) {
                                            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                                            appendToPane(BatchOutput, ex.toString(), Color.red);
                                        }
                                    }
                                }

                                // Execute the method
                                //First: instantiate the class selected with the fully qualified name
                                Object newObject;
                                Class clase = Class.forName(actual_fully_qualified_class);
                                newObject = clase.newInstance();
                                ((Model) newObject).patterns = new ArrayList<>();
                                ((Model) newObject).patternsFilteredByMeasure = new ArrayList<>();
                                ((Model) newObject).patternsFilteredMaximal = new ArrayList<>();
                                ((Model) newObject).patternsFilteredMinimal = new ArrayList<>();

                                // Second: get the argument class
                                Class[] args = new Class[2];
                                args[0] = InstanceSet.class;
                                args[1] = HashMap.class;

                                // Third: Get the method 'learn' of the class and invoke it. (cambiar "new InstanceSet" por el training)
                                clase.getMethod("learn", args).invoke(newObject, training, params);
                                // Get learned patterns, filter, and calculate measures
                                //ArrayList<Pattern> patterns = (ArrayList<Pattern>) clase.getMethod("getPatterns", null).invoke(newObject, null);

                                // Call the test method. This method return in a hashmap the quality measures.
                                // for unfiltered, filtered global, and filtered by class QMs.
                                ArrayList<HashMap<String, Double>> Measures = Utils.calculateDescriptiveMeasures(training, ((Model) newObject).getPatterns(), true);
                                ArrayList<HashMap<String, Double>> filterPatterns = Utils.filterPatterns((Model) newObject, "CONF", 0.6f);

                                for (HashMap<String, Double> a : filterPatterns) {
                                    Measures.add(a);
                                }

                                args = new Class[1];
                                args[0] = InstanceSet.class;
                                // Calculate training measures
                                String[][] predictionsTra = (String[][]) clase.getMethod("predict", args).invoke(newObject, training);
                                Utils.calculatePrecisionMeasures(predictionsTra, training, training, Measures);

                                // Save the training results file
                                Utils.saveMeasures(dir, (Model) newObject, Measures, true, i);

                                args = new Class[1];
                                args[0] = InstanceSet.class;
                                // Call predict method
                                String[][] predictions = (String[][]) clase.getMethod("predict", args).invoke(newObject, test);
                                // Calculate descriptive measures in test
                                Measures = Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatterns(), false);
                                Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredMinimal(), false).get(0));
                                Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredMaximal(), false).get(0));
                                Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredByMeasure(), false).get(0));

                                // Calculate predictions in test
                                Utils.calculatePrecisionMeasures(predictions, test, training, Measures);

                                Utils.saveMeasures(dir, (Model) newObject, Measures, false, i);
                                // Store the result to make the average result
                                QMsUnfiltered = Utils.updateHashMap(QMsUnfiltered, Measures.get(0));
                                QMsMinimal = Utils.updateHashMap(QMsMinimal, Measures.get(1));
                                QMsMaximal = Utils.updateHashMap(QMsMaximal, Measures.get(2));
                                QMsByMeasure = Utils.updateHashMap(QMsByMeasure, Measures.get(3));

                            }

                            // After finished the fold cross validation, make the average calculation of each quality measure.
                            Utils.saveResults(dir, QMsUnfiltered, QMsMinimal, QMsMaximal, QMsByMeasure, NUM_FOLDS);

                        }

                    }
                    appendToPane(BatchOutput, "Done.", Color.BLUE);
                    System.out.println("EXECUTIONS FINISHED!");
                    BatchOutput.setEditable(false);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                        ex.printStackTrace();
                    } catch (ExecutionException ex) {
                        appendToPane(BatchOutput, "An unexpected error has ocurred: " + ex.getCause().toString(), Color.red);
                        ex.printStackTrace();
                    }
                }

            };
            work.execute();

        } catch (IllegalActionException ex) {
            appendToPane(BatchOutput, ex.getReason(), Color.red);
            ex.printStackTrace();
        }


    }//GEN-LAST:event_BatchButtonActionPerformed

    private void numFoldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_numFoldsActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_numFoldsActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        JFileChooser fileChooser = new JFileChooser(new File(System.getProperty("user.home")));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int result = fileChooser.showOpenDialog(jButton1.getParent());
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (!selectedFile.exists()) {
                try {
                    selectedFile.createNewFile();
                } catch (IOException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                PrintWriter pw = new PrintWriter(selectedFile.getAbsolutePath());
                pw.println(preds);
                pw.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void ParallelCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ParallelCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ParallelCheckboxActionPerformed

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
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new GUI();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> AlgorithmList;
    private javax.swing.JComboBox<String> AlgorithmList1;
    private javax.swing.JButton BatchButton;
    private javax.swing.JTextPane BatchOutput;
    private javax.swing.JPanel BatchPanel;
    private javax.swing.JButton BrowseBatchFolder;
    private javax.swing.JButton BrowseButtonModel;
    private javax.swing.JButton BrowseButtonTRA;
    private javax.swing.JButton BrowseButtonTST;
    private javax.swing.JButton BrowseInstances;
    private javax.swing.JButton BrowseModelButton;
    private static javax.swing.JTextPane ExecutionInfoLearn;
    private javax.swing.JTextField InstancesPath;
    private javax.swing.JButton LearnButton;
    private javax.swing.JPanel LearnPanel;
    private javax.swing.JPanel LoadPanel;
    private javax.swing.JTextField ModelPath1;
    private javax.swing.JCheckBox ParallelCheckbox;
    private javax.swing.JPanel ParametersPanel;
    private javax.swing.JPanel ParametersPanel1;
    private static javax.swing.JTextPane PredictionsPanel;
    private javax.swing.JCheckBox SaveModelCheckbox;
    private javax.swing.JTabbedPane Tabs;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JComboBox<String> numFolds;
    private javax.swing.JTextField rutaBatch;
    private javax.swing.JTextField rutaModel;
    private javax.swing.JTextField rutaTra;
    private javax.swing.JTextField rutaTst;
    // End of variables declaration//GEN-END:variables

    /**
     * Reads an XML file
     *
     * @param path The path to the xml
     * @return A DOM object
     */
    public static Document readXML(String path) {
        try {
            File fXmlFile = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);

            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Put in the parameter panel the parameters of the selected algorithm
     *
     * @param doc A DOM object with the algorthims and params definitions
     * @param index The position of the algorithm in the file
     */
    private void addParamsToPanel(Document doc, int index, JPanel ParametersPanel) {
        //Get algorithm
        NodeList nodes = doc.getElementsByTagName("algorithm");
        Element node = (Element) nodes.item(index);
        actual_fully_qualified_class = node.getElementsByTagName("class").item(0).getTextContent();
        //Clear the actual panel
        ParametersPanel.removeAll();

        /* Spinners:
            jSpinner1.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(6.0f) ,  // Valor por defecto
            Float.valueOf(0.0f),   // minimo
            Float.valueOf(100.0f), // Maximo
            Float.valueOf(0.5f)));  Paso */
        // Now, get the parameters
        try {
            NodeList parameters = node.getElementsByTagName("parameter");
            for (int j = 0; j < parameters.getLength(); j++) {
                Element nodeParam = (Element) parameters.item(j);
                switch (nodeParam.getElementsByTagName("type").item(0).getTextContent()) {
                    case "integer":
                        ParametersPanel.add(new JLabel(nodeParam.getElementsByTagName("name").item(0).getTextContent() + ": "));
                        Integer defect,
                         min,
                         max;
                        defect = Integer.parseInt(nodeParam.getElementsByTagName("default").item(0).getTextContent());
                        Element domain = (Element) nodeParam.getElementsByTagName("domain").item(0);
                        min = Integer.parseInt(domain.getElementsByTagName("min").item(0).getTextContent());
                        max = Integer.parseInt(domain.getElementsByTagName("max").item(0).getTextContent());
                        ParametersPanel.add(new JSpinner(new SpinnerNumberModel(defect.intValue(), min.intValue(), max.intValue(), 1)));
                        break;

                    case "real":
                        ParametersPanel.add(new JLabel(nodeParam.getElementsByTagName("name").item(0).getTextContent() + ": "));
                        Float defecto,
                         mini,
                         maxi;
                        defecto = Float.parseFloat(nodeParam.getElementsByTagName("default").item(0).getTextContent());
                        domain = (Element) nodeParam.getElementsByTagName("domain").item(0);
                        mini = Float.parseFloat(domain.getElementsByTagName("min").item(0).getTextContent());
                        maxi = Float.parseFloat(domain.getElementsByTagName("max").item(0).getTextContent());
                        ParametersPanel.add(new JSpinner(new SpinnerNumberModel(defecto.floatValue(), mini.floatValue(), maxi.floatValue(), (float) 0.01)));
                        break;

                    case "nominal":
                        ParametersPanel.add(new JLabel(nodeParam.getElementsByTagName("name").item(0).getTextContent() + ": "));
                        domain = (Element) nodeParam.getElementsByTagName("domain").item(0);
                        Vector<String> values = new Vector<>();
                        NodeList list = domain.getElementsByTagName("item");
                        for (int i = 0; i < list.getLength(); i++) {
                            values.add(list.item(i).getTextContent());
                        }
                        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(values);
                        // Sets the default element:
                        comboBoxModel.setSelectedItem(values.get(Integer.parseInt(nodeParam.getElementsByTagName("default").item(0).getTextContent()) - 1));
                        JComboBox combo = new JComboBox(comboBoxModel);
                        ParametersPanel.add(combo);

                }

            }

            // Update the panel 
            ParametersPanel.setLayout(new GridLayout(parameters.getLength(), 2));
            ParametersPanel.validate();
            ParametersPanel.repaint();
            //jScrollPane2.setViewportView(ParametersPanel);

        } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
            // If algorithms.xml has an error disable all the interface.
            this.setEnabled(false);
            ExecutionInfoLearn.setText("");
            ExecutionInfoLearn.setText("FATAL ERROR: algorithms.xml has an error, interface blocked.");
        }
    }

    /**
     * Reads the parameters of the algorithm specified by the user on the
     * parameters panel
     *
     * @return A HashMap<String, String> with key the name of the parameter and
     * value the value of the parameter.
     */
    private HashMap<String, String> readParameters(JPanel ParametersPanel) {
        String key = "";
        HashMap<String, String> parameters = new HashMap<>();
        for (int i = 0; i < ParametersPanel.getComponentCount(); i++) {
            if (ParametersPanel.getComponent(i) instanceof JComboBox) {
                // Cast the component and add the value of the JLabel substracting the ': ' elements
                JComboBox element = (JComboBox) ParametersPanel.getComponent(i);
                parameters.put(key.substring(0, key.length() - 2), (String) element.getSelectedItem());
            } else if (ParametersPanel.getComponent(i) instanceof JSpinner) {
                JSpinner element = (JSpinner) ParametersPanel.getComponent(i);
                try {
                    parameters.put(key.substring(0, key.length() - 2), Integer.toString((Integer) element.getValue()));
                } catch (java.lang.ClassCastException ex) {
                    parameters.put(key.substring(0, key.length() - 2), Double.toString((Double) element.getValue()));
                }
            } else if (ParametersPanel.getComponent(i) instanceof JLabel) {
                JLabel element = (JLabel) ParametersPanel.getComponent(i);
                key = element.getText();
            }
        }
        return parameters;
    }

    public static void setInfoLearnText(String text) {
        appendToPane(ExecutionInfoLearn, text, Color.blue);
    }

    public static void setInfoLearnTextError(String text) {
        appendToPane(ExecutionInfoLearn, text, Color.red);
    }

    private synchronized static void appendToPane(JTextPane tp, String msg, Color c) {
        //SwingUtilities.invokeLater(() -> {

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);

        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());

        tp.replaceSelection(date + ": " + msg + "\n");

        //});
    }

    private synchronized static void appendToPane(JTextPane tp, String msg, Color c, boolean hour) {
        //SwingUtilities.invokeLater(() -> {

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = tp.getDocument().getLength();
        tp.setCaretPosition(len);
        tp.setCharacterAttributes(aset, false);

        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
        if (!hour) {
            tp.replaceSelection(msg + "\n");
        } else {
            tp.replaceSelection(date + ": " + msg + "\n");
        }

        //});
    }

    /**
     * Reset/Initialize the quality measures hash map
     */
    private void resetMeasures() {
        qualityMeasures.put("WRACC", 0.0);  // Normalized Unusualness
        qualityMeasures.put("NVAR", 0.0);  // Number of variables
        qualityMeasures.put("NRULES", 0.0);  // Number of rules
        qualityMeasures.put("GAIN", 0.0);  // Information Gain
        qualityMeasures.put("CONF", 0.0);   // Confidence
        qualityMeasures.put("GR", 0.0);     // Growth Rate
        qualityMeasures.put("TPR", 0.0);    // True positive rate
        qualityMeasures.put("FPR", 0.0);    // False positive rate
        qualityMeasures.put("SUPDIFF", 0.0);     // Support Diference
        qualityMeasures.put("FISHER", 0.0); // Fishers's test
        qualityMeasures.put("HELLINGER", 0.0); // Hellinger Distance
        qualityMeasures.put("ACC", 0.0); // Accuracy
        qualityMeasures.put("AUC", 0.0); // ROC Curve
    }

    private void updateMeasuresCV(HashMap<String, Double> measures) {
        if (measures.containsKey("WRACC")) {
            qualityMeasures.put("WRACC", qualityMeasures.get("WRACC") + measures.get("WRACC"));
        }
        if (measures.containsKey("NVAR")) {
            qualityMeasures.put("NVAR", qualityMeasures.get("NVAR") + measures.get("NVAR"));
        }
        if (measures.containsKey("NRULES")) {
            qualityMeasures.put("NRULES", qualityMeasures.get("NRULES") + measures.get("NRULES"));
        }
        if (measures.containsKey("GAIN")) {
            qualityMeasures.put("GAIN", qualityMeasures.get("GAIN") + measures.get("GAIN"));
        }
        if (measures.containsKey("CONF")) {
            qualityMeasures.put("CONF", qualityMeasures.get("CONF") + measures.get("CONF"));
        }
        if (measures.containsKey("GR")) {
            qualityMeasures.put("GR", qualityMeasures.get("GR") + measures.get("GR"));
        }
        if (measures.containsKey("TPR")) {
            qualityMeasures.put("TPR", qualityMeasures.get("TPR") + measures.get("TPR"));
        }
        if (measures.containsKey("FPR")) {
            qualityMeasures.put("FPR", qualityMeasures.get("FPR") + measures.get("FPR"));
        }
        if (measures.containsKey("SUPDIFF")) {
            qualityMeasures.put("SUPDIFF", qualityMeasures.get("SUPDIFF") + measures.get("SUPDIFF"));
        }
        if (measures.containsKey("FISHER")) {
            qualityMeasures.put("FISHER", qualityMeasures.get("FISHER") + measures.get("FISHER"));
        }
        if (measures.containsKey("HELLINGER")) {
            qualityMeasures.put("HELLINGER", qualityMeasures.get("HELLINGER") + measures.get("HELLINGER"));
        }
        if (measures.containsKey("ACC")) {
            qualityMeasures.put("ACC", qualityMeasures.get("ACC") + measures.get("ACC"));
        }
        if (measures.containsKey("AUC")) {
            qualityMeasures.put("AUC", qualityMeasures.get("AUC") + measures.get("AUC"));
        }

    }

}
