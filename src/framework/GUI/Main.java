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

import framework.utils.QualityMeasures;
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

    static boolean filterPatterns;

    public static void main(String[] args) {
        try {
            String fully_qualified_name = "";
            String algName = "";
            switch (args.length) {
                case 0:
                    new GUI().setVisible(true);
                    break;
                case 1:
                    Document doc = GUI.readXML("config.xml");
                    NodeList nodes = doc.getElementsByTagName("algorithm");
                    boolean found = false;
                    HashMap<String, String> params = readParams(args[0]); // read parameters
                    InstanceSet training = new InstanceSet();
                    InstanceSet test = new InstanceSet();
                    boolean batchMode = false;
                    boolean imbalanced = false;
                    filterPatterns = params.getOrDefault("filter", "no").equalsIgnoreCase("yes");

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
                        //System.out.println("ERROR: Algorithm does not match with any algorithm implemented. Aborting...");
                        System.err.println("ERROR: Algorithm does not match with any algorithm implemented. Aborting...");
                        System.exit(-1);
                    }

                    batchMode = params.containsKey("directory");
                    if (params.containsKey("imbalanced mode")) {
                        imbalanced = params.get("imbalanced mode").equalsIgnoreCase("true")
                                || params.get("imbalanced mode").equalsIgnoreCase("yes");
                    }

                    if (!batchMode) {
                        // EXECUTE NORMAL MODE: ONLY TRAIN AND TEST
                        String filterBy = "CONF";
                        float threshold = 0.6f;

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
                        HashMap<String, QualityMeasures> Measures = GUI.filterPhase(newObject, training, test, filterBy, threshold, imbalanced, filterPatterns);

                        // Call predict method for ACC and AUC for training
                        System.out.println("Calculating precision for training...");

                        GUI.predictPhase(clase, newObject, training, test, Measures, true, (new File(params.get("training")).getAbsoluteFile().getParentFile()).getAbsolutePath(), 0);

                        // Save training measures in a file.
                        System.out.println("Save results in a file...");
                        Utils.saveMeasures2(new File(params.get("training")).getAbsoluteFile().getParentFile(), (Model) newObject, Measures, true, 0);

                        System.out.println("Done learning model.");
                        System.out.println("Testing instances...");

                        // Calculate test measures for unfiltered and filtered patterns
                        Measures = Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatterns(), false, "Unfiltered");
                        for (String key : ((Model) newObject).filters.keySet()) {
                            Measures.put(key, Utils.calculateDescriptiveMeasures(test, ((Model) newObject).filters.get(key), false, key).get(key));
                        }

                        GUI.predictPhase(clase, newObject, training, test, Measures, false, (new File(params.get("test")).getAbsoluteFile().getParentFile()).getAbsolutePath(), 0);

                        // Save Results
                        //Utils.saveResults(new File(rutaTst.getText()).getParentFile(), Measures.get(0), Measures.get(1), Measures.get(2), 1);
                        Utils.saveMeasures2(new File(params.get("test")).getAbsoluteFile().getParentFile(), (Model) newObject, Measures, false, 0);
                        System.out.println("Done.");

                    } else {
                        // BATCH EXECUTION
                        int NUM_FOLDS = Integer.parseInt(params.get("number of folds"));
                        String filterBy = "CONF";
                        float threshold = 0.6f;

                        File root = new File(params.get("directory"));
                        if (!root.isDirectory()) {
                            System.err.println("ERROR: \"directory\" is not a folder. Aborting...");
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
                                HashMap<String, QualityMeasures> totalMeasures = new HashMap<>();
                                // This must be changed in order to introduce the selection of filter by the user
                                totalMeasures.put("Unfiltered", new QualityMeasures());
                                if (filterPatterns) {
                                    totalMeasures.put("Minimals", new QualityMeasures());
                                    totalMeasures.put("Maximals", new QualityMeasures());
                                    totalMeasures.put("CONF", new QualityMeasures());
                                    totalMeasures.put("Chi", new QualityMeasures());
                                }
                                System.out.println("Executing..." + dir.getName() + "...");

                                /* HINT: 
                            In order to perform parallel execution, you can probe the function parallelstream().foreach()
                            on an arraylist with 1 to num_folds as the data.
                            This will do the execution of each fold in parallel.
                                 */
                                for (int i = 1; i <= NUM_FOLDS; i++) {
                                    // Search for the training and test files.
                                    for (File x : files) {
                                        // El formato es xx5xx-1tra.dat
                                        if (x.getName().matches(".*" + NUM_FOLDS + ".*-" + i + "tra.dat")) {
                                            try {
                                                Attributes.clearAll();
                                                training.readSet(x.getAbsolutePath(), true);
                                                training.setAttributesAsNonStatic();
                                                params.put("traData", algName);
                                            } catch (DatasetException | HeaderFormatException | NullPointerException ex) {
                                                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);

                                            }
                                        }
                                        if (x.getName().matches(".*" + NUM_FOLDS + ".*-" + i + "tst.dat")) {
                                            try {
                                                test.readSet(x.getAbsolutePath(), false);
                                                test.setAttributesAsNonStatic();
                                                params.put("testData", x.getAbsolutePath());
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
                                    ((Model) newObject).filters = new HashMap<>();

                                    // Second: get the argument class
                                    Class[] arg = new Class[2];
                                    arg[0] = InstanceSet.class;
                                    arg[1] = HashMap.class;

                                    // Third: Get the method 'learn' of the class and invoke it. (cambiar "new InstanceSet" por el training)
                                    long t_ini = System.currentTimeMillis();
                                    clase.getMethod("learn", arg).invoke(newObject, training, params);
                                    long t_end = System.currentTimeMillis();
                                    // Get learned patterns, filter, and calculate measures

                                    // Filter patterns
                                    HashMap<String, QualityMeasures> Measures = GUI.filterPhase(newObject, training, test, filterBy, threshold, imbalanced, filterPatterns);

                                    // Predict phase 
                                    System.out.println("Calculating precision for training...");
                                    if (filterPatterns) {
                                        GUI.predictPhase(clase, newObject, training, test, Measures, true, dir.getAbsolutePath(), i);
                                    }
                                    Measures.forEach((key, value)
                                            -> value.addMeasure("Exec. Time (s)", (double) (t_end - t_ini) / 1000.0)
                                    );

                                    // Save the training results file
                                    Utils.saveMeasures2(dir, (Model) newObject, Measures, true, i);

                                    // Now, process the test file
                                    // Calculate test measures for unfiltered and filtered patterns
                                    Measures = Utils.calculateDescriptiveMeasures(test, ((Model) newObject).getPatterns(), false, "Unfiltered");
                                    for (String key : ((Model) newObject).filters.keySet()) {
                                        Measures.put(key, Utils.calculateDescriptiveMeasures(test, ((Model) newObject).filters.get(key), false, key).get(key));
                                    }
                                    if (filterPatterns) {
                                        GUI.predictPhase(clase, newObject, training, test, Measures, false, dir.getAbsolutePath(), i);
                                    }
                                    Measures.forEach((key, value)
                                            -> value.addMeasure("Exec. Time (s)", (double) (t_end - t_ini) / 1000.0)
                                    );

                                    // Save meassures to a file
                                    Utils.saveMeasures2(dir, (Model) newObject, Measures, false, i);

                                    // Add number of rules
                                    if (!((Model) newObject).patterns.isEmpty()) {
                                        Measures.get("Unfiltered").addMeasure("NRULES", ((Model) newObject).patterns.size());
                                    } else {
                                        Measures.get("Unfiltered").addMeasure("NRULES", ((Model) newObject).patterns.size());
                                        Measures.get("Unfiltered").addMeasure("NaNs", Measures.get("Unfiltered").getMeasures().getOrDefault("NaNs", 0.0) + 1.0);
                                    }

                                    Measures.forEach((k, v) -> {
                                        if (!k.equalsIgnoreCase("Unfiltered")) {
                                            if (!((Model) newObject).filters.get(k).isEmpty()) {
                                                v.addMeasure("NRULES", ((Model) newObject).filters.get(k).size());
                                            } else {
                                                v.addMeasure("NRULES", ((Model) newObject).filters.get(k).size());
                                                v.addMeasure("NaNs", v.getMeasures().getOrDefault("NaNs", 0.0) + 1.0);
                                            }
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

                                }
                                // Average nrules and nvars
                                for (String key : totalMeasures.keySet()) {
                                    totalMeasures.get(key).addMeasure("NRULES", totalMeasures.get(key).getMeasure("NRULES") / (double) (NUM_FOLDS - totalMeasures.get(key).getMeasures().getOrDefault("NaNs", 0.0)));
                                    totalMeasures.get(key).addMeasure("NVAR", totalMeasures.get(key).getMeasure("NVAR") / (double) (NUM_FOLDS - totalMeasures.get(key).getMeasures().getOrDefault("NaNs", 0.0)));
                                    totalMeasures.get(key).getMeasures().remove("NaNs");
                                }

                                totalMeasures.forEach((key, value) -> {
                                    Utils.averageQualityMeasures(value, NUM_FOLDS);
                                });

                                // After finished the fold cross validation, make the average calculation of each quality measure.
                                Utils.saveResults(dir, totalMeasures, NUM_FOLDS);

                            }

                        }
                        System.out.println("FINISHED BATCH EXECUTION ! RESULTS ARE SAVED IN EACH DATASET FOLDER.");
                    }
                    break;
                default:
                    System.out.println("You have to specify only one argument to execute in command-line or no arguments to launch the GUI.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        /*catch (DatasetException | HeaderFormatException | ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }*/

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
