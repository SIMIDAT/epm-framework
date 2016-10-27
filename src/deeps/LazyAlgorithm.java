/**
 * *********************************************************************
 *
 * This file is part of KEEL-software, the Data Mining tool for regression,
 * classification, clustering, pattern mining and so on.
 *
 * Copyright (C) 2004-2010
 *
 * F. Herrera (herrera@decsai.ugr.es)
 * L. Sanchez (luciano@uniovi.es)
 * J. Alcal�-Fdez (jalcala@decsai.ugr.es)
 * S. Garc�a (sglopez@ujaen.es)
 * A. Fern�ndez (alberto.fernandez@ujaen.es)
 * J. Luengo (julianlm@decsai.ugr.es)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/
 *
 *********************************************************************
 */
/**
 *
 * File: LazyAlgorithm.java
 *
 * A general framework for Lazy Learning Algorithms.
 * This class contains all common operations in the development of a
 * Lazy Learning algorithm. Any Lazy algorithm can extend this class and,
 * by implementing the abstract "evaluate" and "readParameters" method,
 * getting most of its work already done.
 *
 * @author Written by Joaqu�n Derrac (University of Granada) 13/11/2008
 * @author Modified by Joaqu�n Derrac (University of Granada) 10/18/2008
 * @version 1.1
 * @since JDK1.5
 *
 */
package deeps;

import epm_algorithms.Model;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import keel.Dataset.Attribute;
import keel.Dataset.Attributes;
import keel.Dataset.Instance;
import keel.Dataset.InstanceSet;
import org.core.File;

import org.core.Files;
import utils.Item;
import utils.Pattern;

public abstract class LazyAlgorithm extends DeEPS_Wrapper {

    //Files
    protected String outFile[];
    protected String testFile;
    protected String trainFile;
    protected String referenceFile;

    //Instance Sets
    protected InstanceSet train;
    protected InstanceSet test;
    protected InstanceSet reference;

    protected Instance temp;

    //Data
    protected int inputAtt;
    protected Attribute[] inputs;
    protected Attribute output;
    protected boolean[] nulls;

    protected double trainData[][];
    protected int trainOutput[];
    protected double testData[][];
    protected int testOutput[];
    protected double referenceData[][];
    protected int referenceOutput[];
    protected String relation;

    protected int nClasses;
    protected int nInstances[];
    protected int nInstancesTest[];

    //Rules
    protected Vector rules;
    protected Vector rulesFilterAll;
    protected Vector rulesFilterByClass;

    //Timing
    protected long initialTime;

    protected double modelTime;
    protected double trainingTime;
    protected double testTime;

    //Naming
    protected String name;

    //Random seed
    protected long seed;

    //Results
    protected int confMatrix[][];
    protected int unclassified;
    protected int realClass[][];
    protected int prediction[][];
    protected int trainConfMatrix[][];
    protected int trainUnclassified;
    protected int trainRealClass[][];
    protected int trainPrediction[][];

    protected int epsClass;

    protected abstract int getEpsClass();
    protected double ALPHA;

    protected abstract double getALPHA();

    /**
     * Read the configuration and data files, and process it.
     *
     * @param script Name of the configuration script
     *
     */
    protected void readDataFiles(String script) {

        //Read of the script file
        readConfiguracion(script);
        readParameters(script);

        //Read of training data files
        try {
            train = new InstanceSet();

            train.readSet(trainFile, true);

            train.setAttributesAsNonStatic();

            inputAtt = train.getAttributeDefinitions().getInputNumAttributes();
            inputs = train.getAttributeDefinitions().getInputAttributes();
            output = train.getAttributeDefinitions().getOutputAttribute(0);

            //Normalize the data
            normalizeTrain();

        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }

//	    //Read of test data files
//	    try {
//			test = new InstanceSet();
//			test.readSet(testFile, false);
//		    test.setAttributesAsNonStatic();
//			//Normalize the data
//			normalizeTest();
//			
//	    } catch (Exception e) {
//			System.err.println(e);
//			System.exit(1);
//	    }
//  
//	    Attributes.clearAll();
//	    
//		//Read of reference data files
//		try {
//			reference = new InstanceSet();					
//			reference.readSet(referenceFile, true);
//			reference.setAttributesAsNonStatic();
//
//			//Normalize the data
//			normalizeReference();
//					
//		} catch (Exception e) {
//			System.err.println(e);
//			System.exit(1);
//		}
        //Now, the data is loaded and preprocessed
        //Get the number of classes
        nClasses = train.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();

        //And the number of instances on each class
        nInstances = new int[nClasses];
        for (int i = 0; i < nClasses; i++) {
            nInstances[i] = 0;
        }
        for (int i = 0; i < trainOutput.length; i++) {
            nInstances[trainOutput[i]]++;
        }
        nInstancesTest = new int[nClasses];
        for (int i = 0; i < nClasses; i++) {
            nInstancesTest[i] = 0;
        }
        for (int i = 0; i < testOutput.length; i++) {
            nInstancesTest[testOutput[i]]++;
        }

    }//end-method 

    /**
     * Reads configuration script, and extracts its contents.
     *
     * @param script Name of the configuration script
     *
     */
    protected void readConfiguracion(String script) {

        String fichero, linea, token;
        StringTokenizer lineasFichero, tokens;
        byte line[];
        int i, j;

        outFile = new String[5];

        fichero = Files.readFile(script);
        lineasFichero = new StringTokenizer(fichero, "\n\r");

        lineasFichero.nextToken();
        linea = lineasFichero.nextToken();

        tokens = new StringTokenizer(linea, "=");
        tokens.nextToken();
        token = tokens.nextToken();

        //Getting the names of training and test files
        //reference file will be used as comparision
        line = token.getBytes();
        for (i = 0; line[i] != '\"'; i++);
        i++;
        for (j = i; line[j] != '\"'; j++);
        trainFile = new String(line, i, j - i);
        for (i = j + 1; line[i] != '\"'; i++);
        i++;
        for (j = i; line[j] != '\"'; j++);
        referenceFile = new String(line, i, j - i);
        for (i = j + 1; line[i] != '\"'; i++);
        i++;
        for (j = i; line[j] != '\"'; j++);
        testFile = new String(line, i, j - i);

        //Getting the path and base name of the results files
        linea = lineasFichero.nextToken();
        tokens = new StringTokenizer(linea, "=");
        tokens.nextToken();
        token = tokens.nextToken();

        //Getting the names of output files
        line = token.getBytes();
        for (i = 0; line[i] != '\"'; i++);
        i++;
        for (j = i; line[j] != '\"'; j++);
        outFile[0] = new String(line, i, j - i);
        for (i = j + 1; line[i] != '\"'; i++);
        i++;
        for (j = i; line[j] != '\"'; j++);
        outFile[1] = new String(line, i, j - i);
        for (i = j + 1; line[i] != '\"'; i++);
        i++;
        for (j = i; line[j] != '\"'; j++);
        outFile[2] = new String(line, i, j - i);
        for (i = j + 1; line[i] != '\"'; i++);
        i++;
        for (j = i; line[j] != '\"'; j++);
        outFile[3] = new String(line, i, j - i);
        for (i = j + 1; line[i] != '\"'; i++);
        i++;
        for (j = i; line[j] != '\"'; j++);
        outFile[4] = new String(line, i, j - i);

    } //end-method

    /**
     * Reads the parameters of the algorithm. Must be implemented in the
     * subclass.
     *
     * @param script Configuration script
     *
     */
    protected abstract void readParameters(String script);

    /**
     * This function builds the data matrix for training data and normalizes
     * inputs values
     */
    protected void normalizeTrain() throws DataException {

        StringTokenizer tokens;
        double minimum[];
        double range[];

        //Check if dataset corresponding with a classification problem
        if (train.getAttributeDefinitions().getOutputNumAttributes() < 1) {
            throw new DataException("This dataset haven�t outputs, so it not corresponding to a classification problem.");
        } else if (train.getAttributeDefinitions().getOutputNumAttributes() > 1) {
            throw new DataException("This dataset have more of one output.");
        }

        if (train.getAttributeDefinitions().getOutputAttribute(0).getType() == Attribute.REAL) {
            throw new DataException("This dataset have an input attribute with float values, so it not corresponding to a classification 	problem.");
        }

        //Copy the data
        tokens = new StringTokenizer(train.getHeader(), " \n\r");
        tokens.nextToken();
        relation = tokens.nextToken();

        trainData = new double[train.getNumInstances()][inputAtt];
        trainOutput = new int[train.getNumInstances()];

        for (int i = 0; i < train.getNumInstances(); i++) {

            temp = train.getInstance(i);
            trainData[i] = temp.getAllInputValues();
            trainOutput[i] = (int) temp.getOutputRealValues(0);
            nulls = temp.getInputMissingValues();

            //Clean missing values
            for (int j = 0; j < nulls.length; j++) {
                if (nulls[j]) {
                    trainData[i][j] = 0.0;
                }
            }
        }

        //Normalice the data
        minimum = new double[inputAtt];
        range = new double[inputAtt];

        for (int i = 0; i < inputAtt; i++) {
            if (train.getAttributeDefinitions().getInputAttribute(i).getType() != Attribute.NOMINAL) {
                minimum[i] = train.getAttributeDefinitions().getInputAttribute(i).getMinAttribute();
                range[i] = train.getAttributeDefinitions().getInputAttribute(i).getMaxAttribute() - minimum[i];
            }
        }

        //Both real and nominal data are normaliced in [0,1]
        for (int i = 0; i < train.getNumInstances(); i++) {
            for (int j = 0; j < inputAtt; j++) {
                if (train.getAttributeDefinitions().getInputAttribute(j).getType() == Attribute.NOMINAL) {
                    if (train.getAttributeDefinitions().getInputAttribute(j).getNominalValuesList().size() > 1) {
                        trainData[i][j] /= train.getAttributeDefinitions().getInputAttribute(j).getNominalValuesList().size() - 1;
                    }
                } else {
                    trainData[i][j] -= minimum[j];
                    trainData[i][j] /= range[j];
                }
            }
        }

    } //end-method 

    /**
     * This function builds the data matrix for test data and normalizes inputs
     * values
     */
    protected void normalizeTest() throws DataException {

        StringTokenizer tokens;
        double minimum[];
        double range[];

        //Check if dataset corresponding with a classification problem
        if (test.getAttributeDefinitions().getOutputNumAttributes() < 1) {
            throw new DataException("This dataset haven�t outputs, so it not corresponding to a classification problem.");
        } else if (test.getAttributeDefinitions().getOutputNumAttributes() > 1) {
            throw new DataException("This dataset have more of one output.");
        }

        if (test.getAttributeDefinitions().getOutputAttribute(0).getType() == Attribute.REAL) {
            throw new DataException("This dataset have an input attribute with float values, so it not corresponding to a classification 	problem.");
        }

        //Copy the data
        tokens = new StringTokenizer(test.getHeader(), " \n\r");
        tokens.nextToken();
        tokens.nextToken();

        testData = new double[test.getNumInstances()][inputAtt];
        testOutput = new int[test.getNumInstances()];

        for (int i = 0; i < test.getNumInstances(); i++) {

            temp = test.getInstance(i);
            testData[i] = temp.getAllInputValues();
            testOutput[i] = (int) temp.getOutputRealValues(0);
            nulls = temp.getInputMissingValues();

            //Clean missing values
            for (int j = 0; j < nulls.length; j++) {
                if (nulls[j]) {
                    testData[i][j] = 0.0;
                }
            }
        }

        //Normalice the data
        minimum = new double[inputAtt];
        range = new double[inputAtt];

        for (int i = 0; i < inputAtt; i++) {
            if (test.getAttributeDefinitions().getInputAttribute(i).getType() != Attribute.NOMINAL) {
                minimum[i] = train.getAttributeDefinitions().getInputAttribute(i).getMinAttribute();
                range[i] = train.getAttributeDefinitions().getInputAttribute(i).getMaxAttribute() - minimum[i];
            }
        }

        //Both real and nominal data are normaliced in [0,1]
        for (int i = 0; i < test.getNumInstances(); i++) {
            for (int j = 0; j < inputAtt; j++) {
                if (test.getAttributeDefinitions().getInputAttribute(j).getType() == Attribute.NOMINAL) {
                    if (test.getAttributeDefinitions().getInputAttribute(j).getNominalValuesList().size() > 1) {
                        testData[i][j] /= test.getAttributeDefinitions().getInputAttribute(j).getNominalValuesList().size() - 1;
                    }
                } else {
                    testData[i][j] -= minimum[j];
                    testData[i][j] /= range[j];
                }
            }
        }

    } //end-method 

    /**
     * This function builds the data matrix for reference data and normalizes
     * inputs values
     */
    protected void normalizeReference() throws DataException {

        StringTokenizer tokens;
        double minimum[];
        double range[];

        //Check if dataset corresponding with a classification problem
        if (reference.getAttributeDefinitions().getOutputNumAttributes() < 1) {
            throw new DataException("This dataset haven�t outputs, so it not corresponding to a classification problem.");
        } else if (reference.getAttributeDefinitions().getOutputNumAttributes() > 1) {
            throw new DataException("This dataset have more of one output.");
        }

        if (reference.getAttributeDefinitions().getOutputAttribute(0).getType() == Attribute.REAL) {
            throw new DataException("This dataset have an input attribute with float values, so it not corresponding to a classification 	problem.");
        }

        //Copy the data
        tokens = new StringTokenizer(reference.getHeader(), " \n\r");
        tokens.nextToken();
        tokens.nextToken();

        referenceData = new double[reference.getNumInstances()][inputAtt];
        referenceOutput = new int[reference.getNumInstances()];

        for (int i = 0; i < reference.getNumInstances(); i++) {

            temp = reference.getInstance(i);
            referenceData[i] = temp.getAllInputValues();
            referenceOutput[i] = (int) temp.getOutputRealValues(0);
            nulls = temp.getInputMissingValues();

            //Clean missing values
            for (int j = 0; j < nulls.length; j++) {
                if (nulls[j]) {
                    referenceData[i][j] = 0.0;
                }
            }
        }

        //Normalice the data
        minimum = new double[inputAtt];
        range = new double[inputAtt];

        for (int i = 0; i < inputAtt; i++) {
            if (reference.getAttributeDefinitions().getInputAttribute(i).getType() != Attribute.NOMINAL) {
                minimum[i] = train.getAttributeDefinitions().getInputAttribute(i).getMinAttribute();
                range[i] = train.getAttributeDefinitions().getInputAttribute(i).getMaxAttribute() - minimum[i];
            }
        }

        //Both real and nominal data are normaliced in [0,1]
        for (int i = 0; i < reference.getNumInstances(); i++) {
            for (int j = 0; j < inputAtt; j++) {
                if (reference.getAttributeDefinitions().getInputAttribute(j).getType() == Attribute.NOMINAL) {
                    if (reference.getAttributeDefinitions().getInputAttribute(j).getNominalValuesList().size() > 1) {
                        referenceData[i][j] /= reference.getAttributeDefinitions().getInputAttribute(j).getNominalValuesList().size() - 1;
                    }
                } else {
                    referenceData[i][j] -= minimum[j];
                    referenceData[i][j] /= range[j];
                }
            }
        }

    } //end-method 

    /**
     * Executes the classification of train and test data sets
     *
     */
    public void executeTrain() {
//        super.patterns = new ArrayList<>();
//        super.patternsFilteredAllClasses = new ArrayList<>();
//        super.patternsFilteredByClass = new ArrayList<>();
ArrayList<Pattern> patt = new ArrayList<>();
        try {
            modelTime = ((double) System.currentTimeMillis() - initialTime) / 1000.0;
            System.out.println(name + " " + relation + " Model " + modelTime + "s");

            trainRealClass = new int[trainData.length][1];
            trainPrediction = new int[trainData.length][1];

            rules = new Vector();

            //Check  time		
            setInitialTime();

            //File.writeFile(outFile[3], "\t\n");
            //Working on training
            for (int i = 0; i < trainRealClass.length; i++) {
                //System.out.println("Instance number: "+i);
                trainRealClass[i][0] = trainOutput[i];
                trainPrediction[i][0] = evaluate(trainData[i], "null", 0, rules);
            }

            //Writing results
            //writeOutput(outFile[0], trainRealClass, trainPrediction);
            // Adapt the rules to EPM-FRAMEWORK 
            Attribute[] inputAttrs = train.getAttributeDefinitions().getInputAttributes();
            for (int i = 0; i < rules.size(); i++) {
                Rule r = (Rule) rules.get(i);
                ArrayList<Item> items = new ArrayList<>();
                for (int j = 0; j < r.getNVar(); j++) {
                    if (r.getVar(j) != -1) {
                        Item it;
                        if (inputAttrs[j].getType() == Attribute.INTEGER || inputAttrs[j].getType() == Attribute.REAL) {
                            it = new Item(inputAttrs[j].getName(), (float) r.getVar(j));
                        } else {
                            double aux = 1.0 / (inputAttrs[j].getNominalValuesList().size() - 1.0);
                            int valueVal = ((Double) (r.getVar(j) / aux)).intValue();
                            it = new Item(inputAttrs[j].getName(), inputAttrs[j].getNominalValue(valueVal));
                        }
                        items.add(it);
                    }
                }
                Pattern p = new Pattern(items, r.getRuleClass());
                p.setALPHA(ALPHA);
                patt.add(p);
            }
            super.setPatterns(patt);

            trainingTime = ((double) System.currentTimeMillis() - initialTime) / 1000.0;
            System.out.println(name + " " + relation + " Training " + trainingTime + "s");
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public String[][] executeTest(InstanceSet test) {
        this.test = test;
        this.test.setAttributesAsNonStatic();
        rulesFilterAll = new Vector();
        rulesFilterByClass = new Vector();
        String[][] preds;
        try {
            normalizeTest();
        } catch (DataException ex) {
            Logger.getLogger(LazyAlgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        //fill rules filteredAll and filteredByClass
        for (Pattern pat : super.patternsFilteredAllClasses) {
            int index = pat.getTra_measures().get("RULE_NUMBER").intValue();
            rulesFilterAll.add(rules.get(index));
        }
        for (Pattern pat : super.patternsFilteredByClass) {
            int index = pat.getTra_measures().get("RULE_NUMBER").intValue();
            rulesFilterByClass.add(rules.get(index));
        }

        //Working on test
        realClass = new int[testData.length][1];
        prediction = new int[testData.length][3];
        preds = new String[3][testData.length];

        //Check  time		
        setInitialTime();

        for (int i = 0; i < realClass.length; i++) {
            realClass[i][0] = testOutput[i];
            prediction[i][0] = evaluate(testData[i], "", 1, rules);
            prediction[i][1] = evaluate(testData[i], "", 1, rulesFilterAll);
            prediction[i][2] = evaluate(testData[i], "", 1, rulesFilterByClass);
        }
        // Get predictions strings.
        for (int i = 0; i < prediction.length; i++) {
            for (int j = 0; j < 3; j++) {
                if (prediction[i][j] != -1) {
                    preds[j][i] = test.getAttributeDefinitions().getOutputAttribute(0).getNominalValue(prediction[i][j]);
                } else {
                    preds[j][i] = "Unclassified";
                }
            }
        }

        //Imprimir la calidad 
        testTime = ((double) System.currentTimeMillis() - initialTime) / 1000.0;

        //Calcular calidad de las reglas
        //Ordenar reglas
        //Collections.sort(rules);
        //Imprimir calidad de las reglas
        //CalculateRules();
        //Writing results
//		writeOutput(outFile[1], realClass, prediction);	
		System.out.println(name+" "+ relation + " Test " + testTime + "s");
//		
//		printOutput();
        return preds;

    }//end-method 

    public void CalculateRules() {

        int nclasses = 0;
        int nrules = 0;
        int nrulesT = 0;
        int disparo, numVarNoInter = 0;
        float AvNVAR = 0, AvLENG = 0, AvUNUS = 0, AvGAIN = 0, AvSENS = 0, AvGR = 0;
        float AvDIFS = 0, AvFCNF = 0, AvDH = 0, AvTF = 0, AvTPr = 0, AvFPr = 0;
        String contents = "";
        DecimalFormat threeInts = new DecimalFormat("000");
        DecimalFormat sixInts = new DecimalFormat("0.000000");

        contents = "Number \tClass \tSize \tNVar \tLength \tUnusualness \tGain"
                + "\tSensitivity \tSupportP \tSupporN \tConfidence \tGrowthRate "
                + "\tDifSuppo \tTEFiser \tHellinge \tTPr \tFPr \n";

        Files.writeFile(outFile[4], contents);

        while (nclasses < output.getNumNominalValues()) {
            nrules = 0;
            for (int i = 0; (i < rules.size() && nrules < getEpsClass()); i++) {
                //Analyse test data for each rule
                Rule rul = (Rule) rules.get(i);

                if (rul.getRuleClass() == nclasses) {

                    float nvar = 0, leng = 0, unus = 0, gain = 0, sens = 0, supm = 0;
                    float supM = 0, difs = 0, fcnf = 0, grat = 0, cove = 0;
                    float dh = 0, tpr = 0, fpr = 0, fisher = 0;

                    float ejAnt = 0, ejAntClass = 0, ejAntNoClass = 0;
                    float ejNoAntClass = 0, ejNoAntNoClass = 0;

                    for (int j = 0; j < test.getNumInstances(); j++) {
                        // For each example of the dataset
                        disparo = 1;
                        numVarNoInter = 0;
                        // Compute all variables
                        for (int k = 0; k < inputs.length; k++) {
                            double minBeta = rul.getVar(k) - getALPHA();
                            double maxBeta = rul.getVar(k) + getALPHA();
                            if (rul.getVar(k) != -1) {
                                if (testData[j][k] < minBeta || testData[j][k] > maxBeta) {
                                    disparo = 0;
                                }
                            } else {
                                numVarNoInter++;  // Variable does not take part
                            }
                        } // End FOR all variables

                        // Update counters and mark example if needed
                        if (disparo > 0) {
                            ejAnt++;
                            if (rul.getRuleClass() == testOutput[j]) {
                                ejAntClass++;
                            } else {
                                ejAntNoClass++;
                            }
                        } else if (rul.getRuleClass() != testOutput[j]) {
                            ejNoAntNoClass++;
                        } else {
                            ejNoAntClass++;
                        }

                    } // End of cycle for each example
                    //Compute quality measures and write in the file
                    float ejT = 0;
                    float ejM = 0;
                    for (int k = 0; k < nClasses; k++) {
                        ejT += nInstancesTest[k];
                        if (nInstancesTest[k] > ejM) {
                            ejM = nInstancesTest[k];
                        }
                    }
                    //VARIABLES
                    nvar = rul.getNVar() - numVarNoInter;
                    //SENS
                    if (nInstancesTest[rul.getRuleClass()] != 0) {
                        sens = ((float) ejAntClass / nInstancesTest[rul.getRuleClass()]);
                    } else {
                        sens = 0;
                    }
                    if (numVarNoInter >= rul.getNVar()) {
                        sens = 0;
                    }

                    //SUPm
                    if (ejT != 0) {
                        supm = ((float) ejAntClass / (ejAntClass + ejNoAntClass));
                    } else {
                        supm = 0;
                    }
                    if (numVarNoInter >= rul.getNVar()) {
                        supm = 0;
                    }
                    //SUPM
                    if (ejT != 0) {
                        supM = ((float) ejAntNoClass / (ejAntNoClass + ejNoAntNoClass));
                    } else {
                        supM = 0;
                    }
                    if (numVarNoInter >= rul.getNVar()) {
                        supM = 0;
                    }

                    difs = supm - supM;

                    //LENGTH
                    if (ejAnt != 0) {
                        leng = 1 / (float) ejAnt;
                    } else {
                        leng = 0;
                    }
                    //COVE
                    if (ejAnt != 0) {
                        cove = ((float) ejAnt / ejT);
                    } else {
                        cove = 0;
                    }
                    //UNUS
                    if (ejAnt == 0) {
                        unus = 0;
                    } else {
                        unus = cove * ((float) ejAntClass / ejAnt - (float) nInstancesTest[rul.getRuleClass()] / ejT);
                    }
                    if (numVarNoInter >= rul.getNVar()) {
                        unus = 0;
                    }
                    float minUnus = (1 - (ejM / ejT)) * (0 - (ejM / ejT));
                    float maxUnus = (ejM / ejT) * (1 - (ejM / ejT));

                    unus = (unus - minUnus) / (maxUnus - minUnus);

                    //GAIN
                    if ((ejAnt == 0) || (sens == 0)) {
                        if (nInstancesTest[rul.getRuleClass()] != 0) {
                            gain = sens * (0 - ((float) Math.log10((float) nInstancesTest[rul.getRuleClass()] / ejT)));
                        } else {
                            gain = 0;
                        }
                    } else {
                        gain = sens * (((float) Math.log10(sens / cove)) - ((float) Math.log10((float) nInstancesTest[rul.getRuleClass()] / ejT)));
                    }

                    //CONF
                    if (ejAnt != 0) {
                        fcnf = (float) ejAntClass / ejAnt;
                    } else {
                        fcnf = 0;
                    }
                    //GRAT
                    if (supM != 0 && supm != 0) {
                        grat = supm / supM;
                    } else if (supM == 0 && supm != 0) {
                        grat = Float.POSITIVE_INFINITY;
                    } else {
                        grat = 0;
                    }

                    if (nInstancesTest[rul.getRuleClass()] == 0) {
                        tpr = 0;
                    } else {
                        tpr = (float) ejAntClass / (float) nInstancesTest[rul.getRuleClass()];
                    }

                    if ((ejT - nInstancesTest[rul.getRuleClass()]) == 0) {
                        fpr = 0;
                    } else {
                        fpr = ejAntNoClass / (float) (ejT - nInstancesTest[rul.getRuleClass()]);
                    }

                    //FISHER
                    FisherExact fe = new FisherExact((int) ejT);
                    fisher = (float) fe.getTwoTailedP((int) ejAntClass, (int) ejAntNoClass,
                            (int) ejNoAntClass, (int) ejNoAntNoClass);

                    //Hellinger
                    dh = (float) Math.sqrt(
                            ((Math.pow(Math.sqrt((float) (ejAntClass / nInstancesTest[rul.getRuleClass()]))
                                    - Math.sqrt((float) (ejNoAntClass / nInstancesTest[rul.getRuleClass()])), 2))
                            + (Math.pow(Math.sqrt((float) (ejAntNoClass) / (ejT - nInstancesTest[rul.getRuleClass()]))
                                    - Math.sqrt((float) (ejNoAntNoClass) / (ejT - nInstancesTest[rul.getRuleClass()])), 2))));

                    if (Float.isNaN(dh)) {
                        dh = 0;
                    }

                    contents = "Number \tClass \tSize \tNVar \tLength \tUnusualness \tGain"
                            + "\tSensitivity \tSupportP \tSupporN \tConfidence \tGrowthRate "
                            + "\tDifSuppo \tTEFiser \tHellinge \tTPr \tFPr \n";

                    contents = "" + threeInts.format(rul.getNumber()) + "   ";
                    contents += "\t" + threeInts.format(rul.getRuleClass());
                    contents += "\t-";
                    contents += "\t" + sixInts.format(nvar);
                    contents += "\t" + sixInts.format(leng);
                    contents += "\t" + sixInts.format(unus);
                    if (gain == Float.POSITIVE_INFINITY) {
                        contents += "\tINFINITY" + sixInts.format(gain);
                    } else {
                        contents += "\t" + sixInts.format(gain);
                    }
                    contents += "\t" + sixInts.format(sens);
                    contents += "\t" + sixInts.format(supm);
                    contents += "\t" + sixInts.format(supM);
                    contents += "\t" + sixInts.format(fcnf);
                    if (grat == Float.POSITIVE_INFINITY) {
                        contents += "\tINFINITY" + sixInts.format(grat);
                    } else {
                        contents += "\t" + sixInts.format(grat);
                    }
                    contents += "\t" + sixInts.format(difs);
                    contents += "\t" + sixInts.format(fisher);
                    contents += "\t" + sixInts.format(dh);
                    contents += "\t" + sixInts.format(tpr);
                    contents += "\t" + sixInts.format(fpr);
                    contents += "\n";

                    // Add values of quality measures for each rule
                    Files.addToFile(outFile[4], contents);

                    AvNVAR += nvar;
                    AvLENG += leng;
                    AvUNUS += unus;
                    AvGAIN += gain;
                    AvSENS += sens;
                    AvDIFS += difs;
                    AvFCNF += fcnf;
                    AvTPr += tpr;
                    AvFPr += fpr;
                    AvDH += dh;
                    if (fisher < 0.1) {
                        AvTF++;
                    }
                    if (grat > 1) {
                        AvGR++;
                    }
                    nrules++;
                }
            }
            nclasses++;
            nrulesT += nrules;
        }

        contents = "---\t";
        contents += "---";
        contents += "\t" + nrulesT;
        contents += "\t" + sixInts.format(AvNVAR / nrulesT);
        contents += "\t" + sixInts.format(AvLENG / nrulesT);
        contents += "\t" + sixInts.format(AvUNUS / nrulesT);
        contents += "\t" + sixInts.format(AvGAIN / nrulesT);
        contents += "\t" + sixInts.format(AvSENS / nrulesT);
        contents += "\t--------";
        contents += "\t--------";
        contents += "\t" + sixInts.format(AvFCNF / nrulesT);
        contents += "\t" + sixInts.format(AvGR / nrulesT);
        contents += "\t" + sixInts.format(AvDIFS / nrulesT);
        contents += "\t" + sixInts.format(AvTF / nrulesT);
        contents += "\t" + sixInts.format(AvDH / nrulesT);
        contents += "\t" + sixInts.format(AvTPr / nrulesT);
        contents += "\t" + sixInts.format(AvFPr / nrulesT);
        contents += "\n";

        // Add average values of quality measures for each rule
        Files.addToFile(outFile[4], contents);
    }

    /**
     * Executes the classification of reference and test data sets
     *
     */
    public void executeReference() {

        modelTime = ((double) System.currentTimeMillis() - initialTime) / 1000.0;
        System.out.println(name + " " + relation + " Model " + modelTime + "s");

        trainRealClass = new int[referenceData.length][1];
        trainPrediction = new int[referenceData.length][1];

        //Check  time		
        setInitialTime();

        //Working on training
        for (int i = 0; i < trainRealClass.length; i++) {
            trainRealClass[i][0] = referenceOutput[i];
            trainPrediction[i][0] = evaluate(referenceData[i], "", 0, rules);
        }

        trainingTime = ((double) System.currentTimeMillis() - initialTime) / 1000.0;

        //Writing results
        writeOutput(outFile[0], trainRealClass, trainPrediction);
        System.out.println(name + " " + relation + " Training " + trainingTime + "s");

        //Working on test
        realClass = new int[testData.length][1];
        prediction = new int[testData.length][1];

        //Check  time		
        setInitialTime();

        for (int i = 0; i < realClass.length; i++) {
            realClass[i][0] = testOutput[i];
            prediction[i][0] = evaluate(testData[i], "", 1, rules);
        }

        testTime = ((double) System.currentTimeMillis() - initialTime) / 1000.0;

        //Writing results
        writeOutput(outFile[1], realClass, prediction);
        System.out.println(name + " " + relation + " Test " + testTime + "s");

        printOutput();

    }//end-method 

    /**
     * Evaluates a instance to predict its class. Must be implemented in the
     * subclass.
     *
     * @param example Instance evaluated
     * @param file File to save the patterns
     * @param type Indicates 0 for training and 1 for test
     * @return The class predicted. -1 if the instance remains "Unclassified"
     *
     */
    protected abstract int evaluate(double example[], String file, int type, Vector setRule);

    /**
     * Calculates the Euclidean distance between two instances
     *
     * @param instance1 First instance
     * @param instance2 Second instance
     * @return The Euclidean distance
     *
     */
    protected double euclideanDistance(double instance1[], double instance2[]) {

        double length = 0.0;

        for (int i = 0; i < instance1.length; i++) {
            length += (instance1[i] - instance2[i]) * (instance1[i] - instance2[i]);
        }

        length = Math.sqrt(length);

        return length;

    } //end-method

    /**
     * Calculates the Manhattan distance between two instances
     *
     * @param instance1 First instance
     * @param instance2 Second instance
     * @return The Euclidean distance
     *
     */
    protected double manhattanDistance(double instance1[], double instance2[]) {

        double length = 0.0;

        for (int i = 0; i < instance1.length; i++) {
            length += Math.abs(instance1[i] - instance2[i]);
        }

        return length;

    } //end-method

    /**
     * Checks if two instances are the same
     *
     * @param a First instance
     * @param b Second instance
     * @return True if both instances are equal.
     *
     */
    protected boolean same(double a[], double b[]) {

        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }

        return true;

    }//end-method 

    /**
     * Generates a string with the contents of the instance
     *
     * @param instance Instance to print.
     *
     * @return A string, with the values of the instance
     *
     */
    public static String printInstance(int instance[]) {

        String exit = "";

        for (int i = 0; i < instance.length; i++) {
            exit += instance[i] + " ";
        }

        return exit;

    }//end-method 

    /**
     * Sets the time counter
     *
     */
    protected void setInitialTime() {

        initialTime = System.currentTimeMillis();

    }//end-method

    /**
     * Prints output files.
     *
     * @param filename Name of output file
     * @param realClass Real output of instances
     * @param prediction Predicted output for instances
     */
    private void writeOutput(String filename, int[][] realClass, int[][] prediction) {

        String text = "";

        /*Printing input attributes*/
        text += "@relation " + relation + "\n";

        for (int i = 0; i < inputs.length; i++) {

            text += "@attribute " + inputs[i].getName() + " ";

            if (inputs[i].getType() == Attribute.NOMINAL) {
                text += "{";
                for (int j = 0; j < inputs[i].getNominalValuesList().size(); j++) {
                    text += (String) inputs[i].getNominalValuesList().elementAt(j);
                    if (j < inputs[i].getNominalValuesList().size() - 1) {
                        text += ", ";
                    }
                }
                text += "}\n";
            } else {
                if (inputs[i].getType() == Attribute.INTEGER) {
                    text += "integer";
                } else {
                    text += "real";
                }
                text += " [" + String.valueOf(inputs[i].getMinAttribute()) + ", " + String.valueOf(inputs[i].getMaxAttribute()) + "]\n";
            }
        }

        /*Printing output attribute*/
        text += "@attribute " + output.getName() + " ";

        if (output.getType() == Attribute.NOMINAL) {
            text += "{";

            for (int j = 0; j < output.getNominalValuesList().size(); j++) {
                text += (String) output.getNominalValuesList().elementAt(j);
                if (j < output.getNominalValuesList().size() - 1) {
                    text += ", ";
                }
            }
            text += "}\n";
        } else {
            text += "integer [" + String.valueOf(output.getMinAttribute()) + ", " + String.valueOf(output.getMaxAttribute()) + "]\n";
        }

        /*Printing data*/
        text += "@data\n";

        Files.writeFile(filename, text);

        if (output.getType() == Attribute.INTEGER) {

            text = "";

            for (int i = 0; i < realClass.length; i++) {

                for (int j = 0; j < realClass[0].length; j++) {
                    text += "" + realClass[i][j] + " ";
                }
                for (int j = 0; j < realClass[0].length; j++) {
                    text += "" + prediction[i][j] + " ";
                }
                text += "\n";
                if ((i % 10) == 9) {
                    Files.addToFile(filename, text);
                    text = "";
                }
            }

            if ((realClass.length % 10) != 0) {
                Files.addToFile(filename, text);
            }
        } else {

            text = "";

            for (int i = 0; i < realClass.length; i++) {

                for (int j = 0; j < realClass[0].length; j++) {
                    text += "" + (String) output.getNominalValuesList().elementAt(realClass[i][j]) + " ";
                }
                for (int j = 0; j < realClass[0].length; j++) {
                    if (prediction[i][j] > -1) {
                        text += "" + (String) output.getNominalValuesList().elementAt(prediction[i][j]) + " ";
                    } else {
                        text += "" + "Unclassified" + " ";
                    }
                }
                text += "\n";

                if ((i % 10) == 9) {
                    Files.addToFile(filename, text);
                    text = "";
                }
            }

            if ((realClass.length % 10) != 0) {
                Files.addToFile(filename, text);
            }
        }

    }//end-method 

    /**
     * Prints the additional output file
     */
    private void printOutput() {

        double redIS;
        double redFS;
        double redIFS;

        String text = "";

        computeConfussionMatrixes();

        //Accuracy
        text += "Accuracy: " + getAccuracy() + "\n";
        text += "Accuracy (Training): " + getTrainAccuracy() + "\n";

        //Kappa
        text += "Kappa: " + getKappa() + "\n";
        text += "Kappa (Training): " + getTrainKappa() + "\n";

        //Unclassified
        text += "Unclassified instances: " + unclassified + "\n";
        text += "Unclassified instances (Training): " + trainUnclassified + "\n";
        //Reduction

        redIS = (1.0 - ((double) trainData.length / (double) referenceData.length));
        redFS = (1.0 - ((double) inputAtt / (double) referenceData[0].length));

        redIFS = (1.0 - (((double) trainData.length / (double) referenceData.length)
                * ((double) inputAtt / (double) referenceData[0].length)));

        //Reduction IS	
        text += "Reduction (instances): " + redIS + "\n";

        //Reduction FS
        text += "Reduction (features): " + redFS + "\n";

        //Reduction IFS
        text += "Reduction (both): " + redIFS + "\n";

        //Model time
        text += "Model time: " + modelTime + " s\n";

        //Training time
        text += "Training time: " + trainingTime + " s\n";

        //Test time
        text += "Test time: " + testTime + " s\n";

        //Confusion matrix
        text += "Confussion Matrix:\n";
        for (int i = 0; i < nClasses; i++) {

            for (int j = 0; j < nClasses; j++) {
                text += confMatrix[i][j] + "\t";
            }
            text += "\n";
        }
        text += "\n";

        text += "Training Confussion Matrix:\n";
        for (int i = 0; i < nClasses; i++) {

            for (int j = 0; j < nClasses; j++) {
                text += trainConfMatrix[i][j] + "\t";
            }
            text += "\n";
        }
        text += "\n";

        //Finish additional output file
        Files.writeFile(outFile[2], text);

    }//end-method 

    /**
     * Computes the confusion matrixes
     *
     */
    private void computeConfussionMatrixes() {

        confMatrix = new int[nClasses][nClasses];
        trainConfMatrix = new int[nClasses][nClasses];

        unclassified = 0;

        for (int i = 0; i < nClasses; i++) {
            Arrays.fill(confMatrix[i], 0);
        }

        for (int i = 0; i < prediction.length; i++) {
            if (prediction[i][0] == -1) {
                unclassified++;
            } else {
                confMatrix[prediction[i][0]][realClass[i][0]]++;
            }
        }

        trainUnclassified = 0;

        for (int i = 0; i < nClasses; i++) {
            Arrays.fill(trainConfMatrix[i], 0);
        }

        for (int i = 0; i < trainPrediction.length; i++) {
            if (trainPrediction[i][0] == -1) {
                trainUnclassified++;
            } else {
                trainConfMatrix[trainPrediction[i][0]][trainRealClass[i][0]]++;
            }
        }

    }//end-method 

    /**
     * Computes the accuracy obtained on test set
     *
     * @return Accuracy on test set
     */
    private double getAccuracy() {

        double acc;
        int count = 0;

        for (int i = 0; i < nClasses; i++) {
            count += confMatrix[i][i];
        }

        acc = ((double) count / (double) test.getNumInstances());

        return acc;

    }//end-method 

    /**
     * Computes the accuracy obtained on the training set
     *
     * @return Accuracy on test set
     */
    private double getTrainAccuracy() {

        double acc;
        int count = 0;

        for (int i = 0; i < nClasses; i++) {
            count += trainConfMatrix[i][i];
        }

        acc = ((double) count / (double) train.getNumInstances());

        return acc;

    }//end-method 

    /**
     * Computes the Kappa obtained on test set
     *
     * @return Kappa on test set
     */
    private double getKappa() {

        double kappa;
        double agreement, expected;
        int count, count2;
        double prob1, prob2;

        count = 0;
        for (int i = 0; i < nClasses; i++) {
            count += confMatrix[i][i];
        }

        agreement = ((double) count / (double) test.getNumInstances());

        expected = 0.0;

        for (int i = 0; i < nClasses; i++) {

            count = 0;
            count2 = 0;

            for (int j = 0; j < nClasses; j++) {
                count += confMatrix[i][j];
                count2 += confMatrix[j][i];
            }

            prob1 = ((double) count / (double) test.getNumInstances());
            prob2 = ((double) count2 / (double) test.getNumInstances());

            expected += (prob1 * prob2);
        }

        kappa = (agreement - expected) / (1.0 - expected);

        return kappa;

    }//end-method 

    /**
     * Computes the Kappa obtained on test set
     *
     * @return Kappa on test set
     */
    private double getTrainKappa() {

        double kappa;
        double agreement, expected;
        int count, count2;
        double prob1, prob2;

        count = 0;
        for (int i = 0; i < nClasses; i++) {
            count += trainConfMatrix[i][i];
        }

        agreement = ((double) count / (double) train.getNumInstances());

        expected = 0.0;

        for (int i = 0; i < nClasses; i++) {

            count = 0;
            count2 = 0;

            for (int j = 0; j < nClasses; j++) {
                count += trainConfMatrix[i][j];
                count2 += trainConfMatrix[j][i];
            }

            prob1 = ((double) count / (double) train.getNumInstances());
            prob2 = ((double) count2 / (double) train.getNumInstances());

            expected += (prob1 * prob2);
        }

        kappa = (agreement - expected) / (1.0 - expected);

        return kappa;

    }//end-method 

}//end-class

