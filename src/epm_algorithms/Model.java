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
package epm_algorithms;

import Utils.FisherExact;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.print.attribute.HashAttributeSet;
import keel.Dataset.Attributes;
import keel.Dataset.InstanceSet;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class Model implements Serializable {

    private String fullyQualifiedName;
    private static final double SIGNIFICANCE_LEVEL = 0.1;
    private static final int NUMBER_OF_RULES_FILTER = 3;

    /**
     * Saves the current model to a .ser file extension.
     *
     * @param path The path to save the file (without .ser extension)
     *
     * @throws IOException
     */
    public void saveModel(String path) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(path));
        stream.writeObject(this);
        stream.close();
        System.out.println("Model saved Correctly in " + path);
    }

    /**
     * Reads a previous saved model in .ser extension
     *
     * @param path The path to the model
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readModel(String path) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(path));
        return stream.readObject();
    }

    /**
     * Learns a model from training data, and saves it to a file. The patterns
     * obtained in the model must be saved on a class variable to make it
     * accessible to "predict" and "test" methods.
     *
     * @param training the training data
     * @param params the parameters of the algorithms
     */
    public void learn(InstanceSet training, HashMap<String, String> params) {

    }

    /**
     * Predict the class of the new unseen instances.
     *
     * @param test The set of instances to predict the class
     * @return An array with the class predicted for each test instance
     */
    public String[] predict(InstanceSet test) {

    }

    /**
     * This method perform on a single pass the complete test phase of the
     * method, i.e. it calculates descriptive quality measures and predictive
     * ones. Additionally, saves on files the results.
     *
     * @param test the test set.
     * @return An ArrayList with 3 HashMaps, this HashMaps store the measures
     * for the unfiltered, the filtered for all classes and the filtered for
     * each class set of rules respectively.
     */
    public ArrayList<HashMap<String, Double>> test(InstanceSet test) {
        /* MEASURES TO BE RETURNED
        "WRACC" 
        "NVAR"
        "NRULES"
        "GAIN"
        "CONF"
        "GR"
        "TPR"
        "FPR"
        "SUPDIFF"
        "FISHER"
        "HELLINGER"
        "ACC"
        "AUC"
         */
    }

    /**
     * @return the fullyQualifiedName
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * @param fullyQualifiedName the fullyQualifiedName to set
     */
    protected void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    /**
     * It generates the quality measures HashMap neccesary to be returned by the
     * test method.
     *
     * @return
     */
    protected static HashMap<String, Double> generateQualityMeasuresHashMap() {
        HashMap<String, Double> qualityMeasures = new HashMap<>();
        qualityMeasures.put("WRACC", 0.0);  // Normalized Unusualness
        qualityMeasures.put("NVAR", 0.0);  // Number of variables
        qualityMeasures.put("NRULES", 0.0);  // Number of rules
        qualityMeasures.put("SUPP", 0.0);  // SUPPORT
        qualityMeasures.put("GAIN", 0.0);  // Information Gain
        qualityMeasures.put("CONF", 0.0);   // Confidence
        qualityMeasures.put("GR", 0.0);     // Growth Rate
        qualityMeasures.put("TPR", 0.0);    // True positive rate
        qualityMeasures.put("FPR", 0.0);    // False positive rate
        qualityMeasures.put("SUPDIFF", 0.0);     // Support Diference
        qualityMeasures.put("FISHER", 0.0); // Fishers's test
        qualityMeasures.put("RULE_NUMBER", 0.0); // Rule number (for filtering purposes only)
        qualityMeasures.put("ACC", 0.0); // Accuracy
        qualityMeasures.put("AUC", 0.0); // ROC Curve

        return qualityMeasures;
    }

    /**
     * Calculates the descriptive quality measures for each ruleof a given
     * confusion matrix
     *
     * @param matrices A matrix with tp, tn, fp, fn and number of variables
     * @return A hash map with the descriptive quality measures.
     */
    protected static ArrayList<HashMap<String, Double>> calculateMeasuresFromConfusionMatrix(int[][] matrices) {
        // 0 -> tp
        // 1 -> tn
        // 2 -> fp
        // 3 -> fn
        // 4 -> n_vars
        ArrayList<HashMap<String, Double>> qms = new ArrayList<>();
        for (int i = 0; i < matrices.length; i++) {
            HashMap<String, Double> measures = generateQualityMeasuresHashMap();

            double p = (double) matrices[i][0];
            double _n = (double) matrices[i][1];
            double n = (double) matrices[i][2];
            double _p = (double) matrices[i][3];
            double P = p + _p;
            double N = n + _n;
            double P_N = P + N;

            // WRACC (Normalized)
            double wracc;
            if ((p + n) == 0) {
                wracc = 0;
            } else {
                wracc = ((p + n) / P_N) * ((p / (p + n)) - (P / P_N));
            }

            // CONF
            double conf;
            if ((p + n) == 0) {
                conf = 0;
            } else {
                conf = p / (p + n);
            }

            //TPr
            double tpr;
            if (P == 0) {
                tpr = 0;
            } else {
                tpr = p / P;
            }

            //FPr 
            double fpr;
            if (N == 0) {
                fpr = 0;
            } else {
                fpr = n / N;
            }

            // Support
            double supp;
            if (P_N == 0) {
                supp = 0;
            } else {
                supp = p / P_N;
            }

            // Information gain
            double gain;
            if (P == 0 || p == 0) {
                gain = 0;
            } else {
                if ((p + n) == 0 || tpr == 0) {
                    gain = (p / P) * (0.0 - Math.log(P / P_N));
                }
                gain = (p / P) * (Math.log(tpr / ((p + n) / P_N)) - Math.log(P / P_N));
            }

            //Support difference
            double suppDif;
            if (P_N == 0) {
                suppDif = 0;
            } else {
                suppDif = (p / P_N) - (n / P_N);
            }

            // Growth Rate
            double GR;
            if (tpr != 0 && fpr != 0) {
                GR = tpr / fpr;
            } else if (tpr != 0 && fpr == 0) {
                GR = Float.POSITIVE_INFINITY;
            } else {
                GR = 0;
            }

            // Fisher
            int ejT = (int) P_N;
            FisherExact fe = new FisherExact(ejT);
            double fisher = fe.getTwoTailedP(matrices[i][0], matrices[i][2], matrices[i][3], matrices[i][1]);

            measures.put("WRACC", wracc);  // Normalized Unusualness
            measures.put("GAIN", gain);  // Information Gain
            measures.put("CONF", conf);   // Confidence
            measures.put("GR", GR);     // Growth Rate
            measures.put("TPR", tpr);    // True positive rate
            measures.put("FPR", fpr);    // False positive rate
            measures.put("SUPDIFF", suppDif);     // Support Diference
            measures.put("FISHER", fisher); // Fishers's test
            measures.put("SUPP", supp); // Fishers's test
            measures.put("NVAR", (double) matrices[i][4]);
            measures.put("RULE_NUMBER", (double) i);

            qms.add(measures);
        }

        return qms;
    }

    /**
     * Gets the best n rules by a given quality measure. Note that this function
     * returns the best rules ignoring the class, i.e. it can return rules for
     * only one class.
     *
     * @param qm an ArrayList with the HashMaps with the quality measures for
     * each pattern
     * @param by A String with the short name of the quality measure.
     * @param n The number of patterns to get.
     * @return
     */
    protected static ArrayList<HashMap<String, Double>> getBestNRulesBy(ArrayList<HashMap<String, Double>> qm, String by, int n) {

        ArrayList<HashMap<String, Double>> result = new ArrayList<>();
        // Sort by the quality measure (in ASCENDING ORDER)
        qm.sort((o1, o2) -> {
            if (!by.equals("FPR")) {
                if (o1.get(by) < o2.get(by)) {
                    return -1;
                } else if (o1.get(by) > o2.get(by)) {
                    return 1;
                } else if (o1.get("NVAR") < o2.get("NVAR")) {
                    return 1;
                } else if (o1.get("NVAR") > o2.get("NVAR")) {
                    return -1;
                } else {
                    return 0;
                }
            } else // If it is FPR, then the less, the better
            {
                if (o1.get(by) > o2.get(by)) {
                    return -1;
                } else if (o1.get(by) < o2.get(by)) {
                    return 1;
                } else if (o1.get("NVAR") < o2.get("NVAR")) {
                    return 1;
                } else if (o1.get("NVAR") > o2.get("NVAR")) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        // get the best n rules and return
        for (int i = qm.size() - 1; i >= qm.size() - NUMBER_OF_RULES_FILTER; i--) {
            result.add(qm.get(i));
        }

        return result;
    }

    // filtrado por clase (las n mejores reglas de cada clase), se necesita un parametro adicional que indique la clase de cada patron.
    /**
     * Gets the best n rules by a given quality measure for each class.
     *
     * @param qm an ArrayList with the HashMaps with the quality measures for
     * each pattern
     * @param by A String with the short name of the quality measure.
     * @param n The number of patterns to get.
     * @param classes an array of integers with the class of the pattern.
     * @return
     */
    protected static ArrayList<HashMap<String, Double>> getBestNRulesByClass(ArrayList<HashMap<String, Double>> qm, String by, int n, int[] classes) {
        ArrayList<HashMap<String, Double>> result = new ArrayList<>();
        // Separate the value for each class
        int numClasses = Attributes.getOutputAttribute(0).getNumNominalValues();
        ArrayList<ArrayList<HashMap<String, Double>>> patternsByClass = new ArrayList<>(numClasses);
        for (int i = 0; i < numClasses; i++) {
            ArrayList<HashMap<String, Double>> pat = new ArrayList<>();
            for (int j = 0; j < classes.length; j++) {
                if (classes[j] == i) {
                    pat.add(qm.get(j));
                }
            }
            patternsByClass.add(i, pat);
        }

        // Now, sort each class by the quality measure
        for (ArrayList<HashMap<String, Double>> patterns : patternsByClass) {
            // sorts in ASCENDING ORDER !
            patterns.sort((o1, o2) -> {
                if (!by.equals("FPR")) {
                    if (o1.get(by) < o2.get(by)) {
                        return -1;
                    } else if (o1.get(by) > o2.get(by)) {
                        return 1;
                    } else if (o1.get("NVAR") < o2.get("NVAR")) {
                        return 1;
                    } else if (o1.get("NVAR") > o2.get("NVAR")) {
                        return -1;
                    } else {
                        return 0;
                    }
                } else // If it is FPR, then the less, the better
                {
                    if (o1.get(by) > o2.get(by)) {
                        return -1;
                    } else if (o1.get(by) < o2.get(by)) {
                        return 1;
                    } else if (o1.get("NVAR") < o2.get("NVAR")) {
                        return 1;
                    } else if (o1.get("NVAR") > o2.get("NVAR")) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
        }

        // And now, get the best n rules for each and return
        for (ArrayList<HashMap<String, Double>> patterns : patternsByClass) {
            for (int i = patterns.size() - 1; i >= patterns.size() - NUMBER_OF_RULES_FILTER && i >= 0; i--) {
                result.add(patterns.get(i));
            }
        }

        return result;
    }

    /**
     * Gets the averaged descriptive quality measures from a set of quality
     * measures of each rule.
     *
     * @param measures An array list with all the quality measures for each rule
     * @return A single HashMap with the averaged measures.
     */
    protected static HashMap<String, Double> AverageQualityMeasures(ArrayList<HashMap<String, Double>> measures) {

        HashMap<String, Double> result = generateQualityMeasuresHashMap();
        double sumWRACC = 0.0;
        double sumGAIN = 0.0;
        double sumCONF = 0.0;
        double sumGR = 0.0;
        double sumTPR = 0.0;
        double sumFPR = 0.0;
        double sumSUPDIFF = 0.0;
        double sumFISHER = 0.0;
        double sumNVAR = 0.0;

        for (HashMap<String, Double> a : measures) {
            sumWRACC += a.get("WRACC");
            sumGAIN += a.get("GAIN");
            sumCONF += a.get("CONF");
            sumTPR += a.get("TPR");
            sumFPR += a.get("FPR");
            sumSUPDIFF += a.get("SUPDIFF");
            sumNVAR += a.get("NVAR");
            if (a.get("GR") > 1) {
                sumGR++;
            }
            if (a.get("FISHER") < SIGNIFICANCE_LEVEL) {
                sumFISHER++;
            }
        }

        result.put("WRACC", sumWRACC / (double) measures.size());
        result.put("GAIN", sumGAIN / (double) measures.size());
        result.put("CONF", sumCONF / (double) measures.size());
        result.put("TPR", sumTPR / (double) measures.size());
        result.put("FPR", sumFPR / (double) measures.size());
        result.put("SUPDIFF", sumSUPDIFF / (double) measures.size());
        result.put("NVAR", sumNVAR / (double) measures.size());
        result.put("GR", sumGR / (double) measures.size());
        result.put("FISHER", sumFISHER / (double) measures.size());
        result.put("NRULES", (double) measures.size());

        return result;
    }

    /**
     * Sum the values of both hash maps and return the sum as a hashmap
     *
     * @param one
     * @param another
     * @return
     */
    protected static HashMap<String, Double> updateHashMap(HashMap<String, Double> one, HashMap<String, Double> another) {
        HashMap<String, Double> sum = generateQualityMeasuresHashMap();
        one.forEach(new BiConsumer<String, Double>() {
            @Override
            public void accept(String t, Double u) {
                sum.put(t, u + another.get(t));
            }
        });

        return sum;
    }

    public static void saveResults(File dir, HashMap<String, Double> QMsUnfiltered, HashMap<String, Double> QMsGlobal, HashMap<String, Double> QMsByClass, int NUM_FOLDS) {

        try {
            File measures1 = new File(dir.getAbsolutePath() + "/QM_Unfiltered.txt");
            File measures2 = new File(dir.getAbsolutePath() + "/QM_FilteredALL.txt");
            File measures3 = new File(dir.getAbsolutePath() + "/QM_FilteredBYCLASS.txt");
            measures1.createNewFile();
            measures2.createNewFile();
            measures3.createNewFile();
            
            final PrintWriter w = new PrintWriter(measures1);
           
            QMsUnfiltered.forEach(new BiConsumer<String, Double>() {
                      
                @Override
                public void accept(String t, Double u) {
                        u /= (double) NUM_FOLDS;
                        // Here is where you must made all the operations with each averaged quality measure.
                        w.println(t + " ==> " + u);
                }
            });
            
            w.close();
            final PrintWriter w2 = new PrintWriter(measures2);
            
            QMsGlobal.forEach(new BiConsumer<String, Double>() {
                @Override
                public void accept(String t, Double u) {
                    u /= (double) NUM_FOLDS;
                    // Here is where you must made all the operations with each averaged quality measure.
                    w2.println(t + " ==> " + u);
                }
            });
            w2.close();
            final PrintWriter w3 = new PrintWriter(measures3);
            
            QMsByClass.forEach(new BiConsumer<String, Double>() {
                @Override
                public void accept(String t, Double u) {
                    u /= (double) NUM_FOLDS;
                    // Here is where you must made all the operations with each averaged quality measure.
                     w3.println(t + " ==> " + u);
                }
            });
            w3.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
        } 

    }

}
