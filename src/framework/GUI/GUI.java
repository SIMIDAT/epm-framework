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

import framework.exceptions.IllegalActionException;
import framework.items.Pattern;
import java.awt.Color;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
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
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import framework.utils.QualityMeasures;
import framework.utils.Utils;
import java.util.stream.Stream;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import keel.Dataset.Instance;
import org.w3c.dom.Comment;

/**
 *
 * @author Ángel M. García-Vico (agvico@ujaen.es)
 * @version 1.0
 * @since JDK 1.8
 */
public class GUI extends javax.swing.JFrame {

    private Vector<String> algorithms = new Vector<>();          // algorithms in the framework
    private Vector<String> measures = new Vector<>();            // measures in the framework
    private Vector<String> measuresDescriptions = new Vector<>();            // measures in the framework
    private Vector<JPanel> paramPanels = new Vector<>();         //
    private DefaultComboBoxModel modelo;
    private Document doc;
    private String actual_fully_qualified_class;
    private String preds;

    // Filters-related variables
    private String chiFilterString;
    private Double chiSupportThreshold;
    private Double chiGrowthRateThreshold;
    private Double chiChiThreshold;
    private String filter;
    private Double measureFilterThreshold;
    private boolean minimalFilter = true;
    private boolean maximalFilter = false;
    private boolean chiFilter = false;
    private boolean measureFilter = false;

    //File lastDirectory;
    String lastDirectory;

    /**
     * Creates new form Main
     */
    public GUI() {
        // Here we have to read the algorithms XML and add to algorithms the names of the methods
        doc = readXML("config.xml");
        preds = "";

        if (doc.getElementsByTagName("lastDir").getLength() == 0) {
            Element dir = doc.createElement("lastDir");
            Comment comment = doc.createComment("This option sets the first directory shown to the user");
            dir.insertBefore(comment, dir.getLastChild());
            dir.insertBefore(doc.createTextNode(System.getProperty("user.home")), dir.getLastChild());
            Element root = (Element) doc.getElementsByTagName("document").item(0);
            root.appendChild(dir);
        } else {
            lastDirectory = doc.getElementsByTagName("lastDir").item(0).getTextContent();
        }

        /*File f = new File("options.txt");
        if (f.exists()) {
            try {
                BufferedReader bf = new BufferedReader(new FileReader(f));
                lastDirectory = new File(bf.readLine());
            } catch (IOException ex) {
                lastDirectory = new File(System.getProperty("user.home"));
            }
        } else {
            lastDirectory = new File(System.getProperty("user.home"));
        }*/
        // Set the default chi thresholds
        chiFilterString = "0.02,10,3.84";
        chiChiThreshold = 3.84;
        chiGrowthRateThreshold = 10.0;
        chiSupportThreshold = 0.02;

        // Set the default measure filter threshold (0.6)
        measureFilterThreshold = 0.6;

        initComponents();

        // initializy quality measures hash map.
        //resetMeasures();
        // Adds algorithm names
        NodeList nodes = doc.getElementsByTagName("algorithm");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            // Parse the name of the method
            algorithms.add(node.getElementsByTagName("name").item(0).getTextContent());
        }

        // Gets the measures
        nodes = doc.getElementsByTagName("measure");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            measures.add(node.getElementsByTagName("shortName").item(0).getTextContent());
            measuresDescriptions.add(node.getElementsByTagName("name").item(0).getTextContent());
        }

        // Sets the first algorithm parameters
        addParamsToPanel(doc, 0, ParametersPanel);
        addParamsToPanel(doc, 0, ParametersPanel1);
        // Adds the list of algorithms to the list ad set the first as default
        modelo = new DefaultComboBoxModel(algorithms);
        AlgorithmList.setModel(modelo);
        AlgorithmList1.setModel(modelo);

        // Set the measures in the list of measures filters
        measureFilterListLearn.setModel(new DefaultComboBoxModel(measures));
        measureFilterListLearn.setToolTipText(measuresDescriptions.get(0));
        filter = measures.get(0);
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
        jPanel2 = new javax.swing.JPanel();
        learnImbalancedRadio = new javax.swing.JCheckBox();
        applyFiltersLearn = new javax.swing.JCheckBox();
        minimalFilterCheckboxLearn = new javax.swing.JCheckBox();
        maximalFilterCheckboxLearn = new javax.swing.JCheckBox();
        chiFilterCheckboxLearn = new javax.swing.JCheckBox();
        measureFilterCheckboxLearn = new javax.swing.JCheckBox();
        measureFilterListLearn = new javax.swing.JComboBox<>();
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
        jPanel1 = new javax.swing.JPanel();
        ParallelCheckbox = new javax.swing.JCheckBox();
        batchImbalanceRadio = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Emerging Pattern Mining Algorithms Framework");
        setBackground(new java.awt.Color(204, 204, 204));
        setMinimumSize(getPreferredSize());
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

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

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Additional options"));

        learnImbalancedRadio.setText("Results for Imbalance");
        learnImbalancedRadio.setToolTipText("It gets results with respect the minority class only.");

        applyFiltersLearn.setSelected(true);
        applyFiltersLearn.setText("Apply filters");
        applyFiltersLearn.setToolTipText("Return additional sets of rules and measures applying the specified filters");
        applyFiltersLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyFiltersLearnActionPerformed(evt);
            }
        });

        minimalFilterCheckboxLearn.setSelected(true);
        minimalFilterCheckboxLearn.setText("Minimals");
        minimalFilterCheckboxLearn.setToolTipText("Obtains minimal patterns");
        minimalFilterCheckboxLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                minimalFilterCheckboxLearnActionPerformed(evt);
            }
        });

        maximalFilterCheckboxLearn.setText("Maximals");
        maximalFilterCheckboxLearn.setToolTipText("Obtain maximal patterns");
        maximalFilterCheckboxLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maximalFilterCheckboxLearnActionPerformed(evt);
            }
        });

        chiFilterCheckboxLearn.setText("Chi-EP");
        chiFilterCheckboxLearn.setToolTipText("Obtain Chi-EPs with characteristics specified by the user");
        chiFilterCheckboxLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chiFilterCheckboxLearnActionPerformed(evt);
            }
        });

        measureFilterCheckboxLearn.setText("Measure");
        measureFilterCheckboxLearn.setToolTipText("Gets those patterns with a value of the specified quality measure above a threshold");
        measureFilterCheckboxLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                measureFilterCheckboxLearnActionPerformed(evt);
            }
        });

        measureFilterListLearn.setToolTipText("");
        measureFilterListLearn.setEnabled(false);
        measureFilterListLearn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                measureFilterListLearnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(learnImbalancedRadio)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(minimalFilterCheckboxLearn)
                        .addComponent(applyFiltersLearn))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(measureFilterCheckboxLearn)
                                .addGap(2, 2, 2)
                                .addComponent(measureFilterListLearn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(chiFilterCheckboxLearn)
                            .addComponent(maximalFilterCheckboxLearn))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(learnImbalancedRadio)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(applyFiltersLearn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minimalFilterCheckboxLearn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(maximalFilterCheckboxLearn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(chiFilterCheckboxLearn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(measureFilterListLearn, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(measureFilterCheckboxLearn))
                .addContainerGap())
        );

        javax.swing.GroupLayout LearnPanelLayout = new javax.swing.GroupLayout(LearnPanel);
        LearnPanel.setLayout(LearnPanelLayout);
        LearnPanelLayout.setHorizontalGroup(
            LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LearnPanelLayout.createSequentialGroup()
                .addGap(425, 425, 425)
                .addComponent(LearnButton, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(453, Short.MAX_VALUE))
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
                                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(rutaTst, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
                                    .addComponent(rutaModel)
                                    .addComponent(rutaTra, javax.swing.GroupLayout.Alignment.LEADING))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(BrowseButtonModel)
                                    .addComponent(BrowseButtonTRA)
                                    .addComponent(BrowseButtonTST)))
                            .addComponent(AlgorithmList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
        LearnPanelLayout.setVerticalGroup(
            LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LearnPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LearnPanelLayout.createSequentialGroup()
                        .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel1)
                                .addComponent(rutaTra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(BrowseButtonTRA))
                            .addGroup(LearnPanelLayout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(BrowseButtonTST)
                                    .addComponent(rutaTst, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rutaModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SaveModelCheckbox)
                            .addComponent(BrowseButtonModel))
                        .addGap(15, 15, 15)
                        .addGroup(LearnPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(AlgorithmList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(LearnButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
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
                        .addGap(0, 422, Short.MAX_VALUE))
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

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Additional options"));

        ParallelCheckbox.setText("Parallel");
        ParallelCheckbox.setToolTipText("Execute each fold in parallel");
        ParallelCheckbox.setEnabled(false);
        ParallelCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ParallelCheckboxActionPerformed(evt);
            }
        });

        batchImbalanceRadio.setText("Results for Imbalance");
        batchImbalanceRadio.setToolTipText(learnImbalancedRadio.getToolTipText());
        batchImbalanceRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                batchImbalanceRadioActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ParallelCheckbox)
                    .addComponent(batchImbalanceRadio))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(ParallelCheckbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(batchImbalanceRadio))
        );

        javax.swing.GroupLayout BatchPanelLayout = new javax.swing.GroupLayout(BatchPanel);
        BatchPanel.setLayout(BatchPanelLayout);
        BatchPanelLayout.setHorizontalGroup(
            BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(BatchPanelLayout.createSequentialGroup()
                .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(BatchPanelLayout.createSequentialGroup()
                        .addGap(114, 114, 114)
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(AlgorithmList1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(BatchPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ParametersPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 930, Short.MAX_VALUE))))
                .addContainerGap(23, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, BatchPanelLayout.createSequentialGroup()
                .addGap(0, 77, Short.MAX_VALUE)
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
                .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(BatchPanelLayout.createSequentialGroup()
                        .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(numFolds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(22, 22, 22)
                        .addGroup(BatchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(AlgorithmList1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(55, 55, 55)
                .addComponent(ParametersPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(BatchButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
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
            File fileSelected = fileChooser.getSelectedFile();
            lastDirectory = fileSelected.getParentFile().getAbsolutePath();
            //pw = new PrintWriter(new File("options.txt"));
            //pw.println(lastDirectory.getAbsolutePath());
            doc.getElementsByTagName("lastDirectory").item(0).setTextContent(lastDirectory);
            ModelPath1.setText(fileSelected.getAbsolutePath());
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
                    String filterBy = "CONF";
                    float threshold = 0.6f;

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
                    ((Model) newObject).setPatternsFilteredByChi(new ArrayList<>());
                    ((Model) newObject).filters = new HashMap<>();

                    // Second: get the argument class
                    Class[] args = new Class[2];
                    args[0] = InstanceSet.class;
                    args[1] = HashMap.class;

                    System.out.println("Learning Model...");
                    // Third: Get the method 'learn' of the class and invoke it. (cambiar "new InstanceSet" por el training)
                    long t_ini = System.currentTimeMillis();
                    clase.getMethod("learn", args).invoke(newObject, training, params);
                    long t_end = System.currentTimeMillis();
                    appendToPane(ExecutionInfoLearn, "Filtering patterns and calculating descriptive measures...", Color.BLUE);
                    System.out.println("Filtering patterns and calculating descriptive measures...");

                    //  Perfom the filter phase, filter the patterns
                    // NOTE: The behaviour of this function must be changed in the future to select a filter accorder to an user criterion
                    HashMap<String, QualityMeasures> Measures = filterPhase(newObject, training, test, filterBy, threshold, learnImbalancedRadio.isSelected(), applyFiltersLearn.isSelected());
                    Measures.forEach((key, value) -> value.addMeasure("Exec. Time (s)", (double) (t_end - t_ini) / 1000.0));

                    // Call predict method for ACC and AUC for training
                    appendToPane(ExecutionInfoLearn, "Calculate precision for training...", Color.BLUE);
                    System.out.println("Calculating precision for training...");

                    // Perform the prediction phase on training
                    String rute = (new File(rutaTra.getText())).getParentFile().getAbsolutePath();
                    predictPhase(clase, newObject, training, test, Measures, true, rute, 0);

                    // Save training measures in a file.
                    if (!((Model) newObject).patterns.isEmpty()) {
                        System.out.println("Save results in a file...");
                        appendToPane(ExecutionInfoLearn, "Save result in a file...", Color.BLUE);
                        Utils.saveMeasures2(new File(rutaTra.getText()).getParentFile(), (Model) newObject, Measures, true, 0);
                        appendToPane(ExecutionInfoLearn, "Done", Color.BLUE);
                        System.out.println("Done learning model.");
                    } else {
                        appendToPane(ExecutionInfoLearn, "No patterns extracted", Color.red);
                    }

                    // If there is a test set call the method "predict" to make the test phase.
                    if (!rutaTst.getText().equals("")) {
                        // Calculate descriptive measures for test
                        appendToPane(ExecutionInfoLearn, "Testing instances...", Color.BLUE);
                        System.out.println("Testing instances...");

                        // Calculate test measures for unfiltered and filtered patterns
                        Measures = Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatterns(), false, "Unfiltered");
                        for (String key : ((Model) newObject).filters.keySet()) {
                            Measures.put(key, Utils.calculateDescriptiveMeasures(test, ((Model) newObject).filters.get(key), false, key).get(key));
                        }

                        //  Perform the prediction phase to calculate the predictive
                        rute = (new File(rutaTst.getText())).getParentFile().getAbsolutePath();
                        predictPhase(clase, newObject, training, test, Measures, false, rute, 0);
                        Measures.forEach((key, value)
                                -> value.addMeasure("Exec. Time (s)", (double) (t_end - t_ini) / 1000.0)
                        );
                        // Save Results
                        //Utils.saveResults(new File(rutaTst.getText()).getParentFile(), Measures.get(0), Measures.get(1), Measures.get(2), 1);
                        if (!((Model) newObject).patterns.isEmpty()) {
                            Utils.saveMeasures2(new File(rutaTst.getText()).getParentFile(), (Model) newObject, Measures, false, 0);
                            appendToPane(ExecutionInfoLearn, "Done. Results of quality measures saved in " + new File(rutaTst.getText()).getParentFile().getAbsolutePath(), Color.BLUE);
                            System.out.println("Done. Results of quality measures saved in " + new File(rutaTst.getText()).getParentFile().getAbsolutePath());
                        } else {
                            appendToPane(ExecutionInfoLearn, "Done. However, no patterns have been extracted. Nothing to save.", Color.RED);
                            System.out.println("Done. However, no patterns have been extracted. Nothing to save.");
                        }
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
            File fileSelected = fileChooser.getSelectedFile();
            lastDirectory = fileSelected.getParentFile().getAbsolutePath();
            //pw = new PrintWriter(new File("options.txt"));
            //pw.println(lastDirectory.getAbsolutePath());
            doc.getElementsByTagName("lastDir").item(0).setTextContent(lastDirectory);
            rutaTst.setText(fileSelected.getAbsolutePath());
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
            File fileSelected = fileChooser.getSelectedFile();
            lastDirectory = fileSelected.getParentFile().getAbsolutePath();
            //pw = new PrintWriter(new File("options.txt"));
            //pw.println(lastDirectory.getAbsolutePath());
            doc.getElementsByTagName("lastDir").item(0).setTextContent(lastDirectory);
            rutaTra.setText(fileSelected.getAbsolutePath());
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
            File fileSelected = fileChooser.getSelectedFile();
            lastDirectory = fileSelected.getParentFile().getAbsolutePath();
            //pw = new PrintWriter(new File("options.txt"));
            //pw.println(lastDirectory.getAbsolutePath());
            doc.getElementsByTagName("lastDir").item(0).setTextContent(lastDirectory);
            rutaModel.setText(fileSelected.getAbsolutePath());
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
            File fileSelected = fileChooser.getSelectedFile();
            lastDirectory = fileSelected.getAbsolutePath();
            //pw = new PrintWriter(new File("options.txt"));
            //pw.println(lastDirectory.getAbsolutePath());
            doc.getElementsByTagName("lastDir").item(0).setTextContent(lastDirectory);
            rutaBatch.setText(fileSelected.getAbsolutePath());
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
                    String filterBy = "CONF";
                    float threshold = 0.6f;
                    ArrayList<Integer> numberFold = new ArrayList<>();
                    for (int i = 1; i <= NUM_FOLDS; i++) {
                        numberFold.add(i);
                    }
                    Stream<Integer> data = null;
                    if (ParallelCheckbox.isSelected()) {
                        data = numberFold.parallelStream();
                    } else {
                        data = numberFold.stream();
                    }
//                    ExecutorService exec = Executors.newFixedThreadPool(NUM_THREADS);
                    // for each folder in the root directory

                    for (File dir : folders) {
                        if (ParallelCheckbox.isSelected()) {
                            data = numberFold.parallelStream();
                        } else {
                            data = numberFold.stream();
                        }
                        if (dir.isDirectory()) {
                            File[] files = dir.listFiles();
                            Arrays.sort(files);
                            HashMap<String, QualityMeasures> totalMeasures = new HashMap<>();
                            // This must be changed in order to introduce the selection of filter by the user
                            totalMeasures.put("Unfiltered", new QualityMeasures());
                            totalMeasures.put("Minimals", new QualityMeasures());
                            totalMeasures.put("Maximals", new QualityMeasures());
                            totalMeasures.put("CONF", new QualityMeasures());
                            totalMeasures.put("Chi", new QualityMeasures());

                            appendToPane(BatchOutput, "Executing " + dir.getName() + "...", Color.BLUE);
                            System.out.println("Executing..." + dir.getName() + "...");

                            // Execute for each fold (in parallel, if necessary)
                            data.forEach(i -> {
                                try {
                                    // Search for the training and test files.z

                                    for (File x : files) {
                                        // El formato es xx5xx-1tra.dat
                                        if (x.getName().matches(".*" + NUM_FOLDS + ".*-" + i + "tra.dat")) {
                                            try {
                                                Attributes.token = false;
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
                                    ((Model) newObject).filters = new HashMap<>();

                                    // Second: get the argument class
                                    Class[] args = new Class[2];
                                    args[0] = InstanceSet.class;
                                    args[1] = HashMap.class;

                                    // Third: Get the method 'learn' of the class and invoke it. (cambiar "new InstanceSet" por el training)
                                    long t_ini = System.currentTimeMillis();
                                    clase.getMethod("learn", args).invoke(newObject, training, params);
                                    long t_end = System.currentTimeMillis();

                                    // Get learned patterns, filter, and calculate measures
                                    // Filter patterns
                                    HashMap<String, QualityMeasures> Measures = filterPhase(newObject, training, test, filterBy, threshold, batchImbalanceRadio.isSelected(),false);
                                    Measures.forEach((key, value) -> value.addMeasure("Exec. Time (s)", (double) (t_end - t_ini) / 1000.0));

                                    // Predict phase
                                    appendToPane(ExecutionInfoLearn, "Calculate precision for training...", Color.BLUE);
                                    System.out.println("Calculating precision for training...");
                                    predictPhase(clase, newObject, training, test, Measures, true, rutaBatch.getText(), i);

                                    // Save the training results file
                                    Utils.saveMeasures2(dir, (Model) newObject, Measures, true, i);

                                    // Now, process the test file
                                    // Calculate test measures for unfiltered and filtered patterns
                                    Measures = Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatterns(), false, "Unfiltered");
                                    for (String key : ((Model) newObject).filters.keySet()) {
                                        Measures.put(key, Utils.calculateDescriptiveMeasures(test, ((Model) newObject).filters.get(key), false, key).get(key));
                                    }

                                    predictPhase(clase, newObject, training, test, Measures, false, rutaBatch.getText(), i);

                                    // Save meassures to a file
                                    Utils.saveMeasures2(dir, (Model) newObject, Measures, false, i);

                                    // Add number of rules
                                    Measures.get("Unfiltered").addMeasure("NRULES", ((Model) newObject).patterns.size());
                                    Measures.forEach((k, v) -> {
                                        if (!k.equalsIgnoreCase("Unfiltered")) {
                                            v.addMeasure("NRULES", ((Model) newObject).filters.get(k).size());
                                        }
                                    });

                                    // Store the result to make the average result
                                    for (String key : totalMeasures.keySet()) {
                                        QualityMeasures updateHashMap = Utils.updateHashMap(totalMeasures.get(key), Measures.get(key));
                                        totalMeasures.put(key, updateHashMap);
                                    }
                                    //QMsUnfiltered = Utils.updateHashMap(QMsUnfiltered, Measures.get(0));
                                    //QMsMinimal = Utils.updateHashMap(QMsMinimal, Measures.get(1));
                                    //QMsMaximal = Utils.updateHashMap(QMsMaximal, Measures.get(2));
                                    //QMsByMeasure = Utils.updateHashMap(QMsByMeasure, Measures.get(3));
                                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
                                    appendToPane(BatchOutput, "An unexpected error has ocurred: " + ex.getCause().toString(), Color.red);
                                    ex.printStackTrace();
                                }

                            });

                            // Average the summary of the measures for the fold cross validation
                            totalMeasures.forEach((key, value) -> {
                                Utils.averageQualityMeasures(value, NUM_FOLDS);
                                value.addMeasure("NRULES", value.getMeasure("NRULES") / (double) NUM_FOLDS);
                                value.addMeasure("NVAR", value.getMeasure("NVAR") / (double) NUM_FOLDS);
                            });
                            

                            //Utils.averageQualityMeasures(totalMeasures, NUM_FOLDS);
                            // After finished the fold cross validation, make the average calculation of each quality measure.
                            Utils.saveResults(dir, totalMeasures, NUM_FOLDS);

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

    private void batchImbalanceRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_batchImbalanceRadioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_batchImbalanceRadioActionPerformed

    private void ParallelCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ParallelCheckboxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ParallelCheckboxActionPerformed

    private void applyFiltersLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyFiltersLearnActionPerformed
        // TODO add your handling code here:
        minimalFilterCheckboxLearn.setEnabled(applyFiltersLearn.isSelected());
        maximalFilterCheckboxLearn.setEnabled(applyFiltersLearn.isSelected());
        chiFilterCheckboxLearn.setEnabled(applyFiltersLearn.isSelected());
        measureFilterListLearn.setEnabled(applyFiltersLearn.isSelected());
        measureFilterCheckboxLearn.setEnabled(applyFiltersLearn.isSelected());

        measureFilter = applyFiltersLearn.isSelected();
        minimalFilter = applyFiltersLearn.isSelected();
        maximalFilter = applyFiltersLearn.isSelected();
        chiFilter = applyFiltersLearn.isSelected();

    }//GEN-LAST:event_applyFiltersLearnActionPerformed

    private void measureFilterListLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_measureFilterListLearnActionPerformed
        // TODO add your handling code here:
        JComboBox source = (JComboBox) evt.getSource();
        int index = measures.indexOf(source.getSelectedItem());
        source.setToolTipText(measuresDescriptions.get(index));

        Object value = JOptionPane.showInputDialog(BatchPanel, "Select the threshold of the selected quality measure: ",
                "Threshold Selection", JOptionPane.PLAIN_MESSAGE, null, null, measureFilterThreshold.toString());
        if (value != null) {
            measureFilterThreshold = Double.parseDouble((String) value);
            filter = (String) source.getSelectedItem();
        }
    }//GEN-LAST:event_measureFilterListLearnActionPerformed

    private void measureFilterCheckboxLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_measureFilterCheckboxLearnActionPerformed
        // TODO add your handling code here:
        measureFilterListLearn.setEnabled(((JCheckBox) evt.getSource()).isSelected());
        ((JCheckBox) evt.getSource()).isSelected();
    }//GEN-LAST:event_measureFilterCheckboxLearnActionPerformed

    private void minimalFilterCheckboxLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimalFilterCheckboxLearnActionPerformed
        // TODO add your handling code here:
        minimalFilter = ((JCheckBox) evt.getSource()).isSelected();
    }//GEN-LAST:event_minimalFilterCheckboxLearnActionPerformed

    private void maximalFilterCheckboxLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_maximalFilterCheckboxLearnActionPerformed
        // TODO add your handling code here:
        maximalFilter = ((JCheckBox) evt.getSource()).isSelected();
    }//GEN-LAST:event_maximalFilterCheckboxLearnActionPerformed

    private void chiFilterCheckboxLearnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chiFilterCheckboxLearnActionPerformed
        // TODO add your handling code here:
        chiFilter = ((JCheckBox) evt.getSource()).isSelected();
    }//GEN-LAST:event_chiFilterCheckboxLearnActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            // TODO add your handling code here:     
            // Save the DOM Document with the changes
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Result output = new StreamResult(new File("config.xml"));
            Source input = new DOMSource(doc);
            transformer.transform(input, output);
        } catch (TransformerConfigurationException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Close Windows
        this.dispose();
    }//GEN-LAST:event_formWindowClosing

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
    private javax.swing.JCheckBox applyFiltersLearn;
    private static javax.swing.JCheckBox batchImbalanceRadio;
    private javax.swing.JCheckBox chiFilterCheckboxLearn;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private static javax.swing.JCheckBox learnImbalancedRadio;
    private javax.swing.JCheckBox maximalFilterCheckboxLearn;
    private javax.swing.JCheckBox measureFilterCheckboxLearn;
    private javax.swing.JComboBox<String> measureFilterListLearn;
    private javax.swing.JCheckBox minimalFilterCheckboxLearn;
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
            ExecutionInfoLearn.setText("FATAL ERROR: config.xml has an error, interface blocked.");
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
     * It makes the prediction phase for the training or test data
     *
     * @param newObject
     * @param data
     */
    public static void predictPhase(Class clase, Object newObject, InstanceSet training, InstanceSet test, HashMap<String, QualityMeasures> Measures, boolean isTrain, String dir, int fold) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Class[] args = new Class[2];
        args[0] = InstanceSet.class;
        args[1] = ArrayList.class;
        // Call the predict method for each  filter
        //String[][] predictionsTra = (String[][]) clase.getMethod("predict", args).invoke(newObject, training);
        HashMap<String, String[]> predictionsTra = new HashMap<>();

        // First, the unfiltered pattern
        predictionsTra.put("Unfiltered", (String[]) clase.getMethod("predict", args).invoke(newObject, isTrain ? training : test, ((Model) newObject).patterns));
        // Now, the rest of filters
        for (String k : ((Model) newObject).filters.keySet()) {
            predictionsTra.put(k, (String[]) clase.getMethod("predict", args).invoke(newObject, isTrain ? training : test, ((Model) newObject).filters.get(k)));
        }

        // After the obtention of the predictions for each filter, calculate the prediction measures
        // Calculate tra and tst files
        if (isTrain) {
            Utils.calculatePrecisionMeasures(predictionsTra, training, training, Measures);
            predictionsTra.forEach((k, v) -> {
                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(new File(dir + "/" + k + "_" + fold + ".tra"));
                    for (int i = 0; i < training.getNumInstances(); i++) {
                        pw.println(training.getOutputNominalValue(i, 0) + " - " + v[i]);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    pw.close();
                }
            });

        } else {
            Utils.calculatePrecisionMeasures(predictionsTra, test, training, Measures);
            predictionsTra.forEach((k, v) -> {
                PrintWriter pw = null;
                try {
                    pw = new PrintWriter(new File(dir + "/" + k + "_" + fold + ".tst"));
                    for (int i = 0; i < test.getNumInstances(); i++) {
                        pw.println(test.getOutputNominalValue(i, 0) + " - " + v[i]);
                    }
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    pw.close();
                }
            });
        }
    }

    /**
     * It performs the filter phase of the framework
     *
     * @param newObject The model
     * @param training The training data
     * @param filterBy A measure to filter the data
     * @param threshold the threshold of the measure filter
     * @param imbalanced it calculates measures only for the minority class
     * @return
     */
    public static HashMap<String, QualityMeasures> filterPhase(Object newObject, InstanceSet training, InstanceSet test, String filterBy, float threshold, boolean imbalanced, boolean filter) {

        // Check if we need to work with imbalanced data.
        if (imbalanced) {
            // Gets the patterns of the minority class only.
            int minClass = Utils.getMinorityClass(test);
            ArrayList<Pattern> newPatterns = new ArrayList<>();
            for (Pattern p : ((Model) newObject).patterns) {
                if (p.getClase() == minClass) {
                    newPatterns.add(p);
                }
            }
            // Replace "patterns" with those patterns that are for the minority class only
            ((Model) newObject).patterns.clear();
            ((Model) newObject).patterns.addAll(newPatterns);
        }

        // Get learned patterns, filter, and calculate measures for training
        HashMap<String, QualityMeasures> Measures = Utils.calculateDescriptiveMeasures(training, ((Model) newObject).getPatterns(), true, "Unfiltered");

        // Filter the patterns, returning the average quality measures for each set of patterns
        //ArrayList<HashMap<String, Double>> filterPatterns = Utils.filterPatterns((Model) newObject, "CONF", 0.6f);
        
        HashMap<String, QualityMeasures> filterPatterns = new HashMap<>();
        if(filter)
            filterPatterns = Utils.filterPatterns2((Model) newObject, filterBy, threshold);

        // Add each averaged measur with key in "filters" to the Measures variable
        for (String key : filterPatterns.keySet()) {
            Measures.put(key, filterPatterns.get(key));
        }

        // Filter By Chi-EPs
        // Params used: Supp: 0.02; GR = 10; Chi: 3.84
        if(filter)
            Measures.put("Chi", Utils.filterByChiEP((Model) newObject, 0.02, 10.0, 3.84, training));

        return Measures;
    }

}
