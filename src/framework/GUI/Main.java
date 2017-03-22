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

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import keel.Dataset.DatasetException;
import keel.Dataset.HeaderFormatException;
import keel.Dataset.InstanceSet;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import framework.utils.Utils;
import java.util.Arrays;
import keel.Dataset.Attributes;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class Main {

    public static void main(String[] args) {
        try {
            String fully_qualified_name = "";
            String algName = "";
            switch (args.length) {
                case 0:
                    new GUI().setVisible(true);
                    break;
                case 1:
                    Document doc = GUI.readXML("algorithms.xml");
                    NodeList nodes = doc.getElementsByTagName("algorithm");
                    boolean found = false;
                    HashMap<String, String> params = readParams(args[0]); // read parameters
                    InstanceSet training = new InstanceSet();
                    InstanceSet test = new InstanceSet();
                    boolean batchMode = false;

                    // Find an algorithm that match on the list of algorithms
                    for (int i = 0; i < nodes.getLength() && !found; i++) {
                        Element node = (Element) nodes.item(i);
                        String nameAlg = node.getElementsByTagName("name").item(0).getTextContent();
                        if (nameAlg.equals(params.get("algorithm"))) {
                            found = true;
                            fully_qualified_name = node.getElementsByTagName("class").item(0).getTextContent();
                        }
                    }

                    if (!found) {
                        System.out.println("ERROR: Algorithm does not match with any algorithm implemented. Aborting...");
                        System.exit(-1);
                    }
                    batchMode = params.containsKey("directory");
                    if (!batchMode) {
                        // EXECUTE NORMAL MODE: ONLY TRAIN AND TEST

                        if (!params.containsKey("training") || !params.containsKey("test")) {
                            System.out.println("ERROR: You must specify the training or test file");
                            System.exit(-1);
                        }
                        // read training and test sets
                        training.readSet(params.get("training"), true);
                        test.readSet(params.get("test"), false);
                        training.setAttributesAsNonStatic();
                        test.setAttributesAsNonStatic();

                        //First: instantiate the class selected with the fully qualified name
                        Object newObject;
                        Class clase = Class.forName(fully_qualified_name);
                        newObject = clase.newInstance();

                        // Second: get the argument class
                        Class[] arg = new Class[2];
                        arg[0] = InstanceSet.class;
                        arg[1] = HashMap.class;

                        // Third: Get the method 'learn' of the class and invoke it. (cambiar "new InstanceSet" por el training)
                        System.out.println("Learning Model...");
                        clase.getMethod("learn", arg).invoke(newObject, training, params);
                        // Get learned patterns, filter, and calculate measures
                        //ArrayList<Pattern> patterns = (ArrayList<Pattern>) clase.getMethod("getPatterns", null).invoke(newObject, null);

                        ArrayList<HashMap<String, Double>> Measures = Utils.calculateDescriptiveMeasures(training, ((Model) newObject).getPatterns(), true);
                        ArrayList<HashMap<String, Double>> filterPatterns = Utils.filterPatterns((Model) newObject, "CONF", 0.6f);
                         for (int i = 0; i < filterPatterns.size(); i++) {
                        // Adds to Masures to write later the average results in the file.
                        Measures.add(filterPatterns.get(i));
                    }

                        // Call predict method for ACC and AUC for training
                        System.out.println("Calculating precision for training...");
                        arg = new Class[1];
                        arg[0] = InstanceSet.class;
                        String[][] predictionsTra = (String[][]) clase.getMethod("predict", arg).invoke(newObject, training);
                        Utils.calculatePrecisionMeasures(predictionsTra, training, training, Measures);
                        // Save training measures in a file.
                        System.out.println("Save results in a file...");
                        Utils.saveMeasures(new File(params.get("training")).getAbsoluteFile().getParentFile(), (Model) newObject, Measures, true, 0);
                        System.out.println("Done learning model.");
                        System.out.println("Testing instances...");

                        Measures = Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatterns(), false);
                        Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredMinimal(), false).get(0));
                        Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredMaximal(), false).get(0));
                        Measures.add(Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatternsFilteredByMeasure(), false).get(0));

                        arg = new Class[1];
                        arg[0] = InstanceSet.class;
                        // Call predict method
                        String[][] predictions = (String[][]) clase.getMethod("predict", arg).invoke(newObject, test);

                        // Calculate predictions
                        Utils.calculatePrecisionMeasures(predictions, test, training, Measures);
                        // Save Results
                        //Utils.saveResults(new File(rutaTst.getText()).getParentFile(), Measures.get(0), Measures.get(1), Measures.get(2), 1);
                        Utils.saveMeasures(new File(params.get("test")).getAbsoluteFile().getParentFile(), (Model) newObject, Measures, false, 0);
                        System.out.println("Done.");

                    } else {
                        // BATCH EXECUTION
                        int NUM_FOLDS = Integer.parseInt(params.get("number of folds"));
                        File root = new File(params.get("directory"));
                        if (!root.isDirectory()) {
                            System.out.println("ERROR: \"directory\" is not a folder. Aborting...");
                            System.exit(-1);
                        }
                        File[] folders = root.listFiles();
                        Arrays.sort(folders);
                        training = new InstanceSet();
                        test = new InstanceSet();

                        // Now, look for each directory inside root for datasets to be executed.
                        for (File dir : folders) {

                            if (dir.isDirectory()) {
                                File[] files = dir.listFiles();
                                Arrays.sort(files);
                                HashMap<String, Double> QMsUnfiltered = Utils.generateQualityMeasuresHashMap();
                                HashMap<String, Double> QMsMinimal = Utils.generateQualityMeasuresHashMap();
                                HashMap<String, Double> QMsMaximal = Utils.generateQualityMeasuresHashMap();
                                HashMap<String, Double> QMsByMeasure = Utils.generateQualityMeasuresHashMap();

                                System.out.println("Executing..." + dir.getName() + "...");
                                for (int i = 1; i <= NUM_FOLDS; i++) {
                                    // Search for the training and test files.
                                    for (File x : files) {
                                        if (x.getName().matches(".*" + NUM_FOLDS + ".*-" + i + "tra.dat")) {
                                            try {
                                                Attributes.clearAll();
                                                training.readSet(x.getAbsolutePath(), true);
                                                training.setAttributesAsNonStatic();
                                            } catch (DatasetException | HeaderFormatException | NullPointerException ex) {
                                                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                        if (x.getName().matches(".*" + NUM_FOLDS + ".*-" + i + "tst.dat")) {
                                            try {
                                                test.readSet(x.getAbsolutePath(), false);
                                                test.setAttributesAsNonStatic();
                                            } catch (DatasetException | HeaderFormatException | NullPointerException ex) {
                                                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                    }

                                    // Execute the method
                                    //First: instantiate the class selected with the fully qualified name
                                    Object newObject;
                                    Class clase = Class.forName(fully_qualified_name);
                                    newObject = clase.newInstance();
                                    ((Model) newObject).patterns = new ArrayList<>();
                                    ((Model) newObject).patternsFilteredByMeasure = new ArrayList<>();
                                    ((Model) newObject).patternsFilteredMaximal = new ArrayList<>();
                                    ((Model) newObject).patternsFilteredMinimal = new ArrayList<>();

                                    // Second: get the argument class
                                    Class[] arg = new Class[2];
                                    arg[0] = InstanceSet.class;
                                    arg[1] = HashMap.class;

                                    // Third: Get the method 'learn' of the class and invoke it. (cambiar "new InstanceSet" por el training)
                                    clase.getMethod("learn", arg).invoke(newObject, training, params);
                                    // Get learned patterns, filter, and calculate measures
                                    //ArrayList<Pattern> patterns = (ArrayList<Pattern>) clase.getMethod("getPatterns", null).invoke(newObject, null);

                                    // Call the test method. This method return in a hashmap the quality measures.
                                    // for unfiltered, filtered global, and filtered by class QMs.
                                    ArrayList<HashMap<String, Double>> Measures = Utils.calculateDescriptiveMeasures(training, ((Model) newObject).getPatterns(), true);
                                    ArrayList<HashMap<String, Double>> filterPatterns = Utils.filterPatterns((Model) newObject, "CONF", 0.6f);
                                    
                                    for(HashMap<String, Double> a : filterPatterns)
                                        Measures.add(a);
                                    
                                    // Calculate training measures
                                    String[][] predictionsTra = (String[][]) clase.getMethod("predict", arg).invoke(newObject, training);
                                    Utils.calculatePrecisionMeasures(predictionsTra, training, training, Measures);
                                     
                                     // Save the training results file
                                    Utils.saveMeasures(dir, (Model) newObject, Measures, true, i);


                                    arg = new Class[1];
                                    arg[0] = InstanceSet.class;
                                    // Call predict method
                                    String[][] predictions = (String[][]) clase.getMethod("predict", arg).invoke(newObject, test);
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
                        System.out.println("FINISHED BATCH EXECUTION ! RESULTS ARE SAVED IN EACH DATASET FOLDER.");
                    }
                    break;
                default:
                    System.out.println("You have to specify only one argument to execute in command-line or no arguments to launch the GUI.");
            }
        } catch (DatasetException | HeaderFormatException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public static HashMap<String, String> readParams(String path) {
        BufferedReader bf = null;
        HashMap<String, String> result = new HashMap<>();
        try {
            File file = new File(path);
            bf = new BufferedReader(new FileReader(file));

            String line;
            while ((line = bf.readLine()) != null) {
                StringTokenizer tok = new StringTokenizer(line, "=");
                String nombreParam = tok.nextToken();
                nombreParam = nombreParam.substring(0, nombreParam.length() - 1);
                String Valor = tok.nextToken();
                Valor = Valor.substring(1, Valor.length());
                result.put(nombreParam, Valor);
            }
            return result;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                bf.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
}
