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
package framework.utils;

import static com.sun.corba.se.impl.util.Utility.printStackTrace;
import framework.items.Pattern;
import framework.items.Item;
import framework.utils.FisherExact;
import framework.GUI.Model;
import framework.items.FuzzyItem;
import framework.items.NominalItem;
import framework.items.NumericItem;
import framework.utils.cptree.Par;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import keel.Dataset.Attribute;
import keel.Dataset.Attributes;
import keel.Dataset.Instance;
import keel.Dataset.InstanceSet;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class Utils {

    /**
     * The maximum significance level for Fisher's exact test.
     */
    public static final Double SIGNIFICANCE_LEVEL = 0.1;

    /**
     * It generates the quality measures HashMap neccesary to be returned by the
     * test method.
     *
     * @return A {@code HashMap<String, Double>} with the initialized quality
     * measures.
     */
    public static HashMap<String, Double> generateQualityMeasuresHashMap() {
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
        qualityMeasures.put("AUC", 0.0); // Area Under the Curve (calculated from 

        return qualityMeasures;
    }

    /**
     * Calculates the descriptive quality measures. Stores the individual result on the individual and return
     * the average measures 
     *
     * @param data The dataset with the data
     * @param patterns The patterns
     * @param isTrain The calculated measures are for training (true) or for
     * test (false)?
     * @return An array with a single hashmap that contains the average quality measures of the set of rules.
     */
    public static ArrayList<HashMap<String, Double>> calculateDescriptiveMeasures(InstanceSet data, ArrayList<Pattern> patterns, boolean isTrain) {
        int sumNvars = 0;
        data.setAttributesAsNonStatic();
        Attribute[] inputAttributes = data.getAttributeDefinitions().getInputAttributes();
        Attribute outputAttributes = data.getAttributeDefinitions().getOutputAttribute(0);
        int[][] confusionMatrices = new int[patterns.size()][6];
        // 0 -> tp
        // 1 -> tn
        // 2 -> fp
        // 3 -> fn
        // 4 -> n_vars
        // 5 -> n_examples_class
        for (int i = 0; i < patterns.size(); i++) {
            int tp = 0;
            int tn = 0;
            int fp = 0;
            int fn = 0;
            // for each instance
            int examplesClass = 0;
            for (int j = 0; j < data.getNumInstances(); j++) {
                // If the pattern covers the example
                if (patterns.get(i).covers(data.getInstance(j), inputAttributes)) {

                    if (patterns.get(i).getClase() == outputAttributes.convertNominalValue(data.getOutputNominalValue(j, 0))) {
                        tp++;
                        examplesClass++;
                    } else {
                        fp++;
                    }
                } else if (patterns.get(i).getClase() != outputAttributes.convertNominalValue(data.getOutputNominalValue(j, 0))) {
                    tn++;
                } else {
                    fn++;
                    examplesClass++;
                }

            }

            confusionMatrices[i][0] = tp;
            confusionMatrices[i][1] = tn;
            confusionMatrices[i][2] = fp;
            confusionMatrices[i][3] = fn;
            confusionMatrices[i][4] = patterns.get(i).getItems().size();
            confusionMatrices[i][5] = examplesClass;
        }

        ArrayList<HashMap<String, Double>> qms = new ArrayList<>();
        for (int i = 0; i < confusionMatrices.length; i++) {
            HashMap<String, Double> measures = generateQualityMeasuresHashMap();

            double p = (double) confusionMatrices[i][0];
            double _n = (double) confusionMatrices[i][1];
            double n = (double) confusionMatrices[i][2];
            double _p = (double) confusionMatrices[i][3];
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
            // Normalize WRACC
            double classPCT = (double) confusionMatrices[i][5] / (double) data.getNumInstances();
            double maxWRACC = classPCT * (1.0 - classPCT);
            double minWRACC = classPCT * (0.0 - classPCT);
            wracc = (wracc - minWRACC) / (maxWRACC - minWRACC);

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
            double fisher = fe.getTwoTailedP(confusionMatrices[i][0], confusionMatrices[i][2], confusionMatrices[i][3], confusionMatrices[i][1]);

            measures.put("WRACC", wracc);  // Normalized Unusualness
            measures.put("GAIN", gain);  // Information Gain
            measures.put("CONF", conf);   // Confidence
            measures.put("GR", GR);     // Growth Rate
            measures.put("TPR", tpr);    // True positive rate
            measures.put("FPR", fpr);    // False positive rate
            measures.put("SUPDIFF", suppDif);     // Support Diference
            measures.put("FISHER", fisher); // Fishers's test
            measures.put("SUPP", supp); // Support
            measures.put("NVAR", (double) confusionMatrices[i][4]); // Number of variables
            measures.put("RULE_NUMBER", (double) i); // Rule ID

            // Add confusion matrix
            // 0 -> tp
            // 1 -> tn
            // 2 -> fp
            // 3 -> fn
            measures.put("TP", (double) confusionMatrices[i][0]);
            measures.put("TN", (double) confusionMatrices[i][1]);
            measures.put("FP", (double) confusionMatrices[i][2]);
            measures.put("FN", (double) confusionMatrices[i][3]);

            qms.add(measures);
            if (isTrain) {
                patterns.get(i).setTra_measures(measures);
            } else {
                patterns.get(i).setTst_measures(measures);
            }
        }

        // Average the results and return
        HashMap<String, Double> AverageQualityMeasures = AverageQualityMeasures(qms);
        qms.clear();
        qms.add(AverageQualityMeasures);
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
    public static ArrayList<HashMap<String, Double>> getBestNRulesBy(ArrayList<HashMap<String, Double>> qm, String by, int n) {

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
        for (int i = qm.size() - 1; i >= qm.size() - n; i--) {
            result.add(qm.get(i));
        }

        return result;
    }

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
    public static ArrayList<HashMap<String, Double>> getBestNRulesByClass(ArrayList<HashMap<String, Double>> qm, String by, int n, int[] classes) {
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
            for (int i = patterns.size() - 1; i >= patterns.size() - n && i >= 0; i--) {
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
    public static HashMap<String, Double> AverageQualityMeasures(ArrayList<HashMap<String, Double>> measures) {

        HashMap<String, Double> result = generateQualityMeasuresHashMap();
        double sumWRACC = 0.0;
        double sumGAIN = 0.0;
        double sumCONF = 0.0;
        double sumGR = 0.0;
        double sumTPR = 0.0;
        double sumFPR = 0.0;
        double sumSUPDIFF = 0.0;
        double sumSUPP = 0.0;
        double sumFISHER = 0.0;
        double sumNVAR = 0.0;

        for (HashMap<String, Double> a : measures) {
            sumWRACC += a.get("WRACC");
            sumGAIN += a.get("GAIN");
            sumCONF += a.get("CONF");
            sumTPR += a.get("TPR");
            sumFPR += a.get("FPR");
            sumSUPDIFF += a.get("SUPDIFF");
            sumSUPP += a.get("SUPP");
            sumNVAR += a.get("NVAR");
            if (a.get("GR") > 1) {
                sumGR++;
            }
            if (a.get("FISHER") < SIGNIFICANCE_LEVEL) {
                sumFISHER++;
            }
        }
        if (!measures.isEmpty()) {
            result.put("WRACC", sumWRACC / (double) measures.size());
            result.put("GAIN", sumGAIN / (double) measures.size());
            result.put("CONF", sumCONF / (double) measures.size());
            result.put("TPR", sumTPR / (double) measures.size());
            result.put("FPR", sumFPR / (double) measures.size());
            result.put("SUPDIFF", sumSUPDIFF / (double) measures.size());
            result.put("SUPP", sumSUPP / (double) measures.size());
            result.put("NVAR", sumNVAR / (double) measures.size());
            result.put("GR", sumGR / (double) measures.size());
            result.put("FISHER", sumFISHER / (double) measures.size());
            result.put("NRULES", (double) measures.size());
            result.put("RULE_NUMBER", Double.NaN);
        } else {
            result.put("WRACC", 0d);
            result.put("GAIN", 0d);
            result.put("CONF", 0d);
            result.put("TPR", 0d);
            result.put("FPR", 0d);
            result.put("SUPDIFF", 0d);
            result.put("SUPP", 0d);
            result.put("NVAR", 0d);
            result.put("GR", 0d);
            result.put("FISHER", 0d);
            result.put("NRULES", 0d);
            result.put("RULE_NUMBER", Double.NaN);
        }
        return result;
    }

    /**
     * Sum the values of both hash maps and return the sum as a hashmap
     *
     * @param one
     * @param another
     * @return
     */
    public static HashMap<String, Double> updateHashMap(HashMap<String, Double> one, HashMap<String, Double> another) {
        HashMap<String, Double> sum = generateQualityMeasuresHashMap();
        one.forEach(new BiConsumer<String, Double>() {
            @Override
            public void accept(String t, Double u) {
                sum.put(t, u + another.get(t));
            }
        });

        return sum;
    }

    /**
     * Saves the results of the HashMaps on files. Additionally this function
     * make the average results for cross-validation results
     *
     * @param dir
     * @param QMsUnfiltered
     * @param QMsMinimal
     * @param QMsMaximal
     * @param NUM_FOLDS
     */
    public static void saveResults(File dir, HashMap<String, Double> QMsUnfiltered, HashMap<String, Double> QMsMinimal, HashMap<String, Double> QMsMaximal, HashMap<String, Double> QMsByMeasure, int NUM_FOLDS) {

        try {
            File measures1 = new File(dir.getAbsolutePath() + "/SUMMARY_QM_Unfiltered.txt");
            File measures2 = new File(dir.getAbsolutePath() + "/SUMMARY_QM_MINIMALS.txt");
            File measures3 = new File(dir.getAbsolutePath() + "/SUMMARY_QM_MAXIMALS.txt");
            File measures4 = new File(dir.getAbsolutePath() + "/SUMMARY_QM_BYCONFIDENCE.txt");
            if (measures1.exists()) {
                measures1.delete();
            }
            if (measures2.exists()) {
                measures2.delete();
            }
            if (measures3.exists()) {
                measures3.delete();
            }
            if (measures4.exists()) {
                measures4.delete();
            }
            measures1.createNewFile();
            measures2.createNewFile();
            measures3.createNewFile();
            measures4.createNewFile();

            final PrintWriter w = new PrintWriter(measures1);

            QMsUnfiltered.forEach(new BiConsumer<String, Double>() {

                @Override
                public void accept(String t, Double u) {
                    DecimalFormat sixDecimals = new DecimalFormat("0.000000");
                    if (!u.isNaN()) {
                        u /= (double) NUM_FOLDS;
                        // Here is where you must made all the operations with each averaged quality measure.
                        w.println(t + " ==> " + sixDecimals.format(u));
                    } else {
                        w.println(t + " ==> --------");
                    }
                }
            });

            w.close();
            final PrintWriter w2 = new PrintWriter(measures2);

            QMsMinimal.forEach(new BiConsumer<String, Double>() {
                @Override
                public void accept(String t, Double u) {
                    DecimalFormat sixDecimals = new DecimalFormat("0.000000");
                    if (!u.isNaN()) {
                        u /= (double) NUM_FOLDS;
                        // Here is where you must made all the operations with each averaged quality measure.
                        w2.println(t + " ==> " + sixDecimals.format(u));
                    } else {
                        w2.println(t + " ==> --------");
                    }
                }
            });
            w2.close();
            final PrintWriter w3 = new PrintWriter(measures3);

            QMsByMeasure.forEach(new BiConsumer<String, Double>() {
                @Override
                public void accept(String t, Double u) {
                    DecimalFormat sixDecimals = new DecimalFormat("0.000000");
                    if (!u.isNaN()) {
                        u /= (double) NUM_FOLDS;
                        // Here is where you must made all the operations with each averaged quality measure.
                        w3.println(t + " ==> " + sixDecimals.format(u));
                    } else {
                        w3.println(t + " ==> --------");
                    }
                }
            });
            w3.close();
            final PrintWriter w4 = new PrintWriter(measures4);

            QMsByMeasure.forEach(new BiConsumer<String, Double>() {
                @Override
                public void accept(String t, Double u) {
                    DecimalFormat sixDecimals = new DecimalFormat("0.000000");
                    if (!u.isNaN()) {
                        u /= (double) NUM_FOLDS;
                        // Here is where you must made all the operations with each averaged quality measure.
                        w4.println(t + " ==> " + sixDecimals.format(u));
                    } else {
                        w4.println(t + " ==> --------");
                    }
                }
            });
            w4.close();

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * It calculates associated precision measures for predictions. Measures
     * calculated are: Accuracy and AUC (When the number of classes == 2).
     *
     * @param predictions The predictions made by: 1- unfiltered patterns 2-
     * patterns filtered by global measure 3- patterns filtered by class
     * @param test The test data
     * @param training The training data
     * @param results The Averaged quality measures for unfiltered, filtered and
     * filtered by class sets of quality measures.
     */
    public static void calculatePrecisionMeasures(String[][] predictions, InstanceSet test, InstanceSet training, ArrayList<HashMap<String, Double>> results) {
        //------ GET THE MINORITY CLASS ----------------
        String minorityClass = "";
        training.setAttributesAsNonStatic();
        Vector nominalValuesList = training.getAttributeDefinitions().getOutputAttribute(0).getNominalValuesList();
        int[] numInstances = new int[nominalValuesList.size()];
        for (int i = 0; i < numInstances.length; i++) {
            numInstances[i] = 0;
        }
        for (Instance inst : training.getInstances()) {
            numInstances[nominalValuesList.indexOf(inst.getOutputNominalValues(0))]++;
        }
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < numInstances.length; i++) {
            if (numInstances[i] < min) {
                minorityClass = (String) nominalValuesList.get(i);
                min = numInstances[i];
            }
        }
        // ----------------------------------------------

       
            // Calculate, for each set of patterns, their global confusion matrix
            // NOTE: If the dataset has more classes. The MINORITY CLASS is considered as the positive one
            // the rest of the classes are considered as negative.
            for (int i = 0; i < predictions.length; i++) {
                float tp = 0;
                float tn = 0;
                float fp = 0;
                float fn = 0;
                for (int j = 0; j < predictions[0].length; j++) {
                    if (test.getOutputNominalValue(j, 0).equals(minorityClass)) {
                        if (predictions[i][j].equals(minorityClass)) {
                            tp++;
                        } else {
                            fn++;
                        }
                    } else if (predictions[i][j].equals(minorityClass)) {
                        fp++;
                    } else {
                        tn++;
                    }
                }

                double acc = (double) (tp + tn) / (tp + tn + fp + fn);
                double tpr = 0;
                if ((tp + fn) > 0) {
                    tpr = (double) tp / (tp + fn);
                }
                double fpr = 0;
                if ((fp + tn) > 0) {
                    fpr = (double) fp / (fp + tn);
                }
                double auc = (1.0 + tpr - fpr) / 2.0;
                
                //  If the number of classes are grater than 2, we calculate accuracy via matching prediction-real value
                if (Attributes.getOutputAttribute(0).getNominalValuesList().size() > 2) {
                    float aciertos = 0;
                    for (int j = 0; j < predictions[i].length; j++) {
                        if (predictions[i][j].equals(test.getOutputNominalValue(j, 0))) {
                            aciertos++;
                        }
                    }
                    acc = ((double) aciertos / (double) test.getNumInstances());
                }
                // Save accuracy
                results.get(i).put("ACC", acc);
                
                // NOTE: This is the AUC for the minority class !!!
                results.get(i).put("AUC", auc);
                
                // Here is where you have to save the global confusion matrix CONSIDERING THE MINORITY CLASS
                // AS POSITIVE, AND THE REST AS NEGATIVE !!
                results.get(i).put("TP", (double) tp);
                results.get(i).put("FP", (double) fp);
                results.get(i).put("TN", (double) tn);
                results.get(i).put("FN", (double) fn);
            }
    }

    /**
     * It filters the original pattern set and gets the following pattern sets:
     * <ul>
     * <li> A set with only MINIMAL patterns
     * <li> A set with only MAXIMAL patterns
     * <li> A set with patterns with a given quality measures above the given
     * threshold
     * </ul>
     *
     * Note: This function does not check the complete minimality or maximility
     * of a pattern due to efficiency. Instead, it checks if the pattern is
     * minimal or maximal in the local set of patterns.
     *
     * @param model The model where the patterns are stored.
     * @param by A quality measure, this string must match with a key of the
     * quality measures hashmap.
     * @param threshold The threshold in [0,1]
     * @return
     */
    public static ArrayList<HashMap<String, Double>> filterPatterns(Model model, String by, float threshold) {

        ArrayList<HashMap<String, Double>> qmsFil = new ArrayList<>();
        ArrayList<HashMap<String, Double>> qmsMin = new ArrayList<>();
        ArrayList<HashMap<String, Double>> qmsMax = new ArrayList<>();
        ArrayList<Pattern> minimalPatterns = new ArrayList<>();
        ArrayList<Pattern> maximalPatterns = new ArrayList<>();
        ArrayList<Pattern> filteredPatterns = new ArrayList<>();

        // Check minimal patterns. Sort according to the length
        model.getPatterns().sort((o1, o2) -> {
            if (o1.length() > o2.length()) {
                return 1;
            } else if (o1.length() < o2.length()) {
                return -1;
            } else {
                return 0;
            }
        });

        // Phase 1: Check minimality
        boolean[] marks = new boolean[model.getPatterns().size()];
        for (int i = 0; i < model.getPatterns().size(); i++) {
            Pattern p1 = model.getPatterns().get(i);
            for (int j = i + 1; j < model.getPatterns().size(); j++) {
                Pattern p2 = model.getPatterns().get(j);
                if (!marks[j] && p1.length() < p2.length()) {
                    if (p1.covers(p2)) { // if p1 covers p2 and gr(p2) < gr(p1) it means that p2 is not minimal.
                        if (p1.getTraMeasure("GR") >= p2.getTraMeasure("GR")) {
                            marks[j] = true;
                        }
                    }
                }
            }
        }
        // retain those patterns with marks == false
        for (int i = 0; i < marks.length; i++) {
            if (!marks[i]) {
                minimalPatterns.add(model.getPatterns().get(i));
                qmsMin.add(model.getPatterns().get(i).getTra_measures());
            }
        }

        // Phase 2: check maximal patterns
        marks = new boolean[model.getPatterns().size()];
        for (int i = model.getPatterns().size() - 1; i >= 0; i--) {
            Pattern p1 = model.getPatterns().get(i);
            for (int j = i - 1; j >= 0; j--) {
                Pattern p2 = model.getPatterns().get(j);
                if (!marks[j] && p1.length() > p2.length()) {
                    if (p2.covers(p1)) { // if p1 covers p2 and gr(p2) > gr(p1) it means that p2 is not maximal.
                        if (p1.getTraMeasure("GR") >= p2.getTraMeasure("GR")) {
                            marks[j] = true;
                        }
                    }
                }
            }
        }
        // retain those patterns with marks == false
        for (int i = 0; i < marks.length; i++) {
            if (!marks[i]) {
                maximalPatterns.add(model.getPatterns().get(i));
                qmsMax.add(model.getPatterns().get(i).getTra_measures());
            }
        }

        // Phase 3: Filter patterns by the threshold of a quality measure
        for (int i = 0; i < model.getPatterns().size(); i++) {
            if (model.getPatterns().get(i).getTraMeasure(by) >= threshold) {
                filteredPatterns.add(model.getPatterns().get(i));
                qmsFil.add(model.getPatterns().get(i).getTra_measures());
            }
        }

        //Phase 4: Sets the patterns in model, get the averaged results and return
        model.setPatternsFilteredByMeasure(filteredPatterns);
        model.setPatternsFilteredMinimal(minimalPatterns);
        model.setPatternsFilteredMaximal(maximalPatterns);
        HashMap<String, Double> AverageQualityMeasuresFiltered = AverageQualityMeasures(qmsFil);
        HashMap<String, Double> AverageQualityMeasuresMin = AverageQualityMeasures(qmsMin);
        HashMap<String, Double> AverageQualityMeasuresMax = AverageQualityMeasures(qmsMax);
        qmsFil.clear();
        qmsFil.add(AverageQualityMeasuresMin);
        qmsFil.add(AverageQualityMeasuresMax);
        qmsFil.add(AverageQualityMeasuresFiltered);

        // Re-sort the patterns to be correctly copied to the input file
        Comparator<Pattern> ruleNumberSort = (o1, o2) -> {
            if (o1.getTraMeasure("RULE_NUMBER") > o2.getTraMeasure("RULE_NUMBER")) {
                return 1;
            } else if (o1.getTraMeasure("RULE_NUMBER") < o2.getTraMeasure("RULE_NUMBER")) {
                return -1;
            } else {
                return 0;
            }
        };
        model.getPatterns().sort(ruleNumberSort);
        model.getPatternsFilteredByMeasure().sort(ruleNumberSort);
        model.getPatternsFilteredMinimal().sort(ruleNumberSort);
        model.getPatternsFilteredMaximal().sort(ruleNumberSort);
        return qmsFil;

    }

    /**
     * Saves the results of the patterns sets in the given folder. This creates
     * 5 files: RULES.txt, TRA_QUAC_NOFILTER.txt, TRA_QUAC_MINIMAL.txt and
     * TRA_QUAC_MAXIMAL.txt and TRA_QUAC_CONFIDENCE.txt  or the respective QUAC files
     * for test if {@code train = false}
     *
     * @param dir A folder to save the results.
     * @param model The model where the pattern are stored.
     * @param Measures The Averaged quality measures for each set of patterns
     * @param train Are you writing the measures for training?
     * (Unfiltered, filtered and filtered by class)
     */
    public static void saveMeasures(File dir, Model model, ArrayList<HashMap<String, Double>> Measures, boolean train, int fold) {
        PrintWriter pw1 = null;
        PrintWriter pw2 = null;
        PrintWriter pw3 = null;
        PrintWriter pw4 = null;
        PrintWriter pw5 = null;
        try {
            // define the files to write
            if (train) {
                pw1 = new PrintWriter(dir.getAbsolutePath() + "/RULES.txt");
                pw2 = new PrintWriter(dir.getAbsolutePath() + "/TRA_QUAC_NOFILTER" + fold + ".txt");
                pw3 = new PrintWriter(dir.getAbsolutePath() + "/TRA_QUAC_MINIMAL" + fold + ".txt");
                pw4 = new PrintWriter(dir.getAbsolutePath() + "/TRA_QUAC_MAXIMAL"+ fold +".txt");
                pw5 = new PrintWriter(dir.getAbsolutePath() + "/TRA_QUAC_CONFIDENCE"+ fold +".txt");
            } else {
                pw2 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_NOFILTER"+ fold +".txt");
                pw3 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_MINIMAL"+ fold +".txt");
                pw4 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_MAXIMAL"+ fold +".txt");
                pw5 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_CONFIDENCE"+ fold +".txt");
            }

            // Define the decimal places to write and how to wirte the infinity symbol
            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
            symbols.setInfinity("INFINITY");
            DecimalFormat sixDecimals = new DecimalFormat("0.000000", symbols);

            // Write headers on file.
            Object[] keys = Measures.get(0).keySet().toArray();
            pw2.print("RULE_NUMBER\tN_VARS\tTP\tTN\tFP\tFN\t");
            pw3.print("RULE_NUMBER\tN_VARS\tTP\tTN\tFP\tFN\t");
            pw4.print("RULE_NUMBER\tN_VARS\tTP\tTN\tFP\tFN\t");
            pw5.print("RULE_NUMBER\tN_VARS\tTP\tTN\tFP\tFN\t");

            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("FP") && !k.equals("TN") && !k.equals("FN")) {
                    pw2.print(k + "\t");
                    pw3.print(k + "\t");
                    pw4.print(k + "\t");
                    pw5.print(k + "\t");
                }
            }
            pw2.println();
            pw3.println();
            pw4.println();
            pw5.println();

            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatterns().size(); i++) {
                if (train) {
                    pw1.println("RULE NUMBER " + model.getPatterns().get(i).getTra_measures().get("RULE_NUMBER") + ": " + model.getPatterns().get(i).toString());
                }

                pw2.print(model.getPatterns().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
                if (train) {
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("NVAR")) + "\t");
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("TP")) + "\t");
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("TN")) + "\t");
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("FP")) + "\t");
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("FN")) + "\t");
                } else {
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTst_measures().get("NVAR")) + "\t");
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTst_measures().get("TP")) + "\t");
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTst_measures().get("TN")) + "\t");
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTst_measures().get("FP")) + "\t");
                    pw2.print(sixDecimals.format(model.getPatterns().get(i).getTst_measures().get("FN")) + "\t");
                }
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("FP") && !k.equals("TN") && !k.equals("FN")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw2.print("--------\t");
                        } else if (train) {
                            pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get(k)) + "\t");
                        } else {
                            pw2.print(sixDecimals.format(model.getPatterns().get(i).getTst_measures().get(k)) + "\t");
                        }
                    }
                }
                pw2.println();
            }
            // write mean results
            pw2.print("--------\t--------\t"
                    + sixDecimals.format(Measures.get(0).get("TP")) + "\t"
                    + sixDecimals.format(Measures.get(0).get("TN")) + "\t"
                    + sixDecimals.format(Measures.get(0).get("FP")) + "\t"
                    + sixDecimals.format(Measures.get(0).get("FN")) + "\t");

            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("FP") && !k.equals("TN") && !k.equals("FN")) {
                    pw2.print(sixDecimals.format(Measures.get(0).get(k)) + "\t");
                }
            }

            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatternsFilteredMinimal().size(); i++) {
                if (train) {
                    pw3.print(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("NVAR")) + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("TP")) + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("TN")) + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("FP")) + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("FN")) + "\t");
                } else {
                    pw3.print(model.getPatternsFilteredMinimal().get(i).getTst_measures().get("RULE_NUMBER") + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTst_measures().get("NVAR")) + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTst_measures().get("TP")) + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTst_measures().get("TN")) + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTst_measures().get("FP")) + "\t");
                    pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTst_measures().get("FN")) + "\t");

                }
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("TN") && !k.equals("FP") && !k.equals("FN")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw3.print("--------\t");
                        } else if (train) {
                            pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get(k)) + "\t");
                        } else {
                            pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTst_measures().get(k)) + "\t");
                        }
                    }
                }
                pw3.println();
            }

            pw3.print("--------\t--------\t"
                    + sixDecimals.format(Measures.get(1).get("TP")) + "\t"
                    + sixDecimals.format(Measures.get(1).get("TN")) + "\t"
                    + sixDecimals.format(Measures.get(1).get("FP")) + "\t"
                    + sixDecimals.format(Measures.get(1).get("FN")) + "\t");

            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("FP") && !k.equals("TN") && !k.equals("FN")) {
                    pw3.print(sixDecimals.format(Measures.get(1).get(k)) + "\t");
                }
            }

            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatternsFilteredMaximal().size(); i++) {
                if (train) {
                    pw4.print(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("NVAR")) + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("TP")) + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("TN")) + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("FP")) + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("FN")) + "\t");
                } else {
                    pw4.print(model.getPatternsFilteredMaximal().get(i).getTst_measures().get("RULE_NUMBER") + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTst_measures().get("NVAR")) + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTst_measures().get("TP")) + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTst_measures().get("TN")) + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTst_measures().get("FP")) + "\t");
                    pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTst_measures().get("FN")) + "\t");
                }
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("FP") && !k.equals("TN") && !k.equals("FN")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw4.print("--------\t");
                        } else if (train) {
                            pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get(k)) + "\t");
                        } else {
                            pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTst_measures().get(k)) + "\t");
                        }
                    }
                }
                pw4.println();
            }

            pw4.print("--------\t--------\t"
                    + sixDecimals.format(Measures.get(2).get("TP")) + "\t"
                    + sixDecimals.format(Measures.get(2).get("TN")) + "\t"
                    + sixDecimals.format(Measures.get(2).get("FP")) + "\t"
                    + sixDecimals.format(Measures.get(2).get("FN")) + "\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("FP") && !k.equals("TN") && !k.equals("FN")) {
                    pw4.print(sixDecimals.format(Measures.get(2).get(k)) + "\t");
                }
            }
            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatternsFilteredByMeasure().size(); i++) {
                if (train) {
                    pw5.print(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("NVAR")) + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("TP")) + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("TN")) + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("FP")) + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("FN")) + "\t");
                } else {
                    pw5.print(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTst_measures().get("NVAR")) + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTst_measures().get("TP")) + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTst_measures().get("TN")) + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTst_measures().get("FP")) + "\t");
                    pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTst_measures().get("FN")) + "\t");
                }
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("FP") && !k.equals("TN") && !k.equals("FN")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw5.print("--------\t");
                        } else if (train) {
                            pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get(k)) + "\t");
                        } else {
                            pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTst_measures().get(k)) + "\t");
                        }
                    }
                }
                pw5.println();
            }

            pw5.print("--------\t--------\t"
                    + sixDecimals.format(Measures.get(3).get("TP")) + "\t"
                    + sixDecimals.format(Measures.get(3).get("TN")) + "\t"
                    + sixDecimals.format(Measures.get(3).get("FP")) + "\t"
                    + sixDecimals.format(Measures.get(3).get("FN")) + "\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("TP") && !k.equals("FP") && !k.equals("TN") && !k.equals("FN")) {
                    pw5.print(sixDecimals.format(Measures.get(3).get(k)) + "\t");
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (train) {
                pw1.close();
            }
            pw2.close();
            pw3.close();
            pw4.close();
            pw5.close();
        }
    }

//    /**
//     * Saves the results of the patterns sets in the given folder. This creates
//     * 3 files: TST_QUAC_NOFILTER.txt, TST_QUAC_MINIMAL.txt and
//     * TST_QUAC_MAXIMAL.txt and TST_QUAC_CONFIDENCE.txt
//     *
//     * @param dir A folder to save the results.
//     * @param model The model where the pattern are stored.
//     * @param Measures The Averaged quality measures for each set of patterns
//     * (Unfiltered, filtered and filtered by class)
//     */
//    public static void saveTest(File dir, Model model, ArrayList<HashMap<String, Double>> Measures) {
//        //PrintWriter pw1 = null;
//        PrintWriter pw2 = null;
//        PrintWriter pw3 = null;
//        PrintWriter pw4 = null;
//        PrintWriter pw5 = null;
//        try {
//            // define the files to write
//            //  pw1 = new PrintWriter(dir.getAbsolutePath() + "/RULES.txt");
//            pw2 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_NOFILTER.txt");
//            pw3 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_MINIMAL.txt");
//            pw4 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_MAXIMAL.txt");
//            pw5 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_CONFIDENCE.txt");
//            DecimalFormatSymbols symbols = new DecimalFormatSymbols();
//            symbols.setInfinity("INFINITY");
//            DecimalFormat sixDecimals = new DecimalFormat("0.000000", symbols);
//            // Write headers on file.
//            Object[] keys = Measures.get(0).keySet().toArray();
//             // Write headers on file.
//            pw2.print("RULE_NUMBER\tN_VARS\tTP\tTN\tFP\tFN\t");
//            pw3.print("RULE_NUMBER\tN_VARS\tTP\tTN\tFP\tFN\t");
//            pw4.print("RULE_NUMBER\tN_VARS\tTP\tTN\tFP\tFN\t");
//            pw5.print("RULE_NUMBER\tN_VARS\tTP\tTN\tFP\tFN\t");
//          
//            for (Object key : keys) {
//                String k = (String) key;
//                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("FP")&& !k.equals("TN")&& !k.equals("FN")) {
//                    pw2.print(k + "\t");
//                    pw3.print(k + "\t");
//                    pw4.print(k + "\t");
//                    pw5.print(k + "\t");
//                }
//            }
//            pw2.println();
//            pw3.println();
//            pw4.println();
//            pw5.println();
//
//            // write rules and training qms for all rules
//            for (int i = 0; i < model.getPatterns().size(); i++) {
//                //pw1.println("RULE NUMBER " + model.getPatterns().get(i).getTra_measures().get("RULE_NUMBER") + ": " + model.getPatterns().get(i).toString());
//                
//                pw2.print(model.getPatterns().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
//                pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("NVAR")) + "\t");
//                pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("TP")) + "\t");
//                pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("TN")) + "\t");
//                pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("FP")) + "\t");
//                pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("FN")) + "\t");
//                for (Object key : keys) {
//                    String k = (String) key;
//                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("FP")&& !k.equals("TN")&& !k.equals("FN")) {
//                        if (k.equals("ACC") || k.equals("AUC")) {
//                            pw2.print("--------\t");
//                        } else {
//                            pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get(k)) + "\t");
//                        }
//                    }
//                }
//                pw2.println();
//            }
//            // write mean results
//            pw2.print("--------\t--------\t" + 
//                    sixDecimals.format(Measures.get(0).get("TP")) + "\t" +
//                    sixDecimals.format(Measures.get(0).get("TN")) + "\t" +
//                    sixDecimals.format(Measures.get(0).get("FP")) + "\t" +
//                    sixDecimals.format(Measures.get(0).get("FN")) + "\t");
//            
//            for (Object key : keys) {
//                String k = (String) key;
//                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("FP")&& !k.equals("TN")&& !k.equals("FN")) {
//                    pw2.print(sixDecimals.format(Measures.get(0).get(k)) + "\t");
//                }
//            }
//
//            // write rules and training qms for all rules
//            for (int i = 0; i < model.getPatternsFilteredMinimal().size(); i++) {
//                pw3.print(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
//                pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("NVAR")) + "\t");
//                pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("TP")) + "\t");
//                pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("TN")) + "\t");
//                pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("FP")) + "\t");
//                pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("FN")) + "\t");
//                for (Object key : keys) {
//                    String k = (String) key;
//                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("TN")&& !k.equals("FP")&& !k.equals("FN")) {
//                        if (k.equals("ACC") || k.equals("AUC")) {
//                            pw3.print("--------\t");
//                        } else {
//                            pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get(k)) + "\t");
//                        }
//                    }
//                }
//                pw3.println();
//            }
//
//                pw3.print("--------\t--------\t" + 
//                    sixDecimals.format(Measures.get(1).get("TP")) + "\t" +
//                    sixDecimals.format(Measures.get(1).get("TN")) + "\t" +
//                    sixDecimals.format(Measures.get(1).get("FP")) + "\t" +
//                    sixDecimals.format(Measures.get(1).get("FN")) + "\t");
//                
//            for (Object key : keys) {
//                String k = (String) key;
//                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("FP")&& !k.equals("TN")&& !k.equals("FN")) {
//                    pw3.print(sixDecimals.format(Measures.get(1).get(k)) + "\t");
//                }
//            }
//
//            // write rules and training qms for all rules
//            for (int i = 0; i < model.getPatternsFilteredMaximal().size(); i++) {
//                pw4.print(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
//                pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("NVAR")) + "\t");
//                pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("TP")) + "\t");
//                pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("TN")) + "\t");
//                pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("FP")) + "\t");
//                pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("FN")) + "\t");
//                for (Object key : keys) {
//                    String k = (String) key;
//                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("FP")&& !k.equals("TN")&& !k.equals("FN")) {
//                        if (k.equals("ACC") || k.equals("AUC")) {
//                            pw4.print("--------\t");
//                        } else {
//                            pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get(k)) + "\t");
//                        }
//                    }
//                }
//                pw4.println();
//            }
//
//             pw4.print("--------\t--------\t" + 
//                    sixDecimals.format(Measures.get(2).get("TP")) + "\t" +
//                    sixDecimals.format(Measures.get(2).get("TN")) + "\t" +
//                    sixDecimals.format(Measures.get(2).get("FP")) + "\t" +
//                    sixDecimals.format(Measures.get(2).get("FN")) + "\t");
//            for (Object key : keys) {
//                String k = (String) key;
//                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("FP")&& !k.equals("TN")&& !k.equals("FN")) {
//                    pw4.print(sixDecimals.format(Measures.get(2).get(k)) + "\t");
//                }
//            }
//            // write rules and training qms for all rules
//            for (int i = 0; i < model.getPatternsFilteredByMeasure().size(); i++) {
//                pw5.print(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("RULE_NUMBER") + "\t");
//                pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("NVAR")) + "\t");
//                pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("TP")) + "\t");
//                pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("TN")) + "\t");
//                pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("FP")) + "\t");
//                pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get("FN")) + "\t");
//                for (Object key : keys) {
//                    String k = (String) key;
//                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("FP")&& !k.equals("TN")&& !k.equals("FN")) {
//                        if (k.equals("ACC") || k.equals("AUC")) {
//                            pw5.print("--------\t");
//                        } else {
//                            pw5.print(sixDecimals.format(model.getPatternsFilteredByMeasure().get(i).getTra_measures().get(k)) + "\t");
//                        }
//                    }
//                }
//                pw5.println();
//            }
//
//             pw5.print("--------\t--------\t" + 
//                    sixDecimals.format(Measures.get(3).get("TP")) + "\t" +
//                    sixDecimals.format(Measures.get(3).get("TN")) + "\t" +
//                    sixDecimals.format(Measures.get(3).get("FP")) + "\t" +
//                    sixDecimals.format(Measures.get(3).get("FN")) + "\t");
//            for (Object key : keys) {
//                String k = (String) key;
//                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR") && !k.equals("RULE_NUMBER") && !k.equals("NVAR")&& !k.equals("TP")&& !k.equals("FP")&& !k.equals("TN")&& !k.equals("FN")) {
//                    pw5.print(sixDecimals.format(Measures.get(3).get(k)) + "\t");
//                }
//            }
//
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            pw2.close();
//            pw3.close();
//            pw4.close();
//            pw5.close();
//        }
//    }

    /**
     * Gets simple itemsets with a support higher than a threshold
     *
     * @param a
     * @param minSupp
     * @param positiveClass - The class to consider as positive. For multiclass
     * problems, the others classes are considered as negative.
     * @return
     */
//    public static ArrayList<Item> getSimpleItems(InstanceSet a, double minSupp, int positiveClass) {
//        // Reads the KEEL instance set.
//
//        int countD1 = 0;   // counts of examples belonging to class D1 and D2.
//        int countD2 = 0;
//        ArrayList<Item> simpleItems = new ArrayList<>();
//        // get classes
//        ArrayList<String> classes;
//        try {
//            classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());
//        } catch (NullPointerException ex) {
//            classes = new ArrayList<>(a.getAttributeDefinitions().getOutputAttribute(0).getNominalValuesList());
//        }
//        // Gets the count of examples for each class to calculate the growth rate.
//        for (int i = 0; i < a.getNumInstances(); i++) {
//            if (a.getInstance(i).getOutputNominalValuesInt(0) == positiveClass) {
//                countD1++;
//            } else {
//                countD2++;
//            }
//        }
//
//        // Get the attributes
//        Attribute[] attributes = Attributes.getInputAttributes();
//        int countId = 0;
//        // for each attribute
//        for (int i = 0; i < attributes.length; i++) {
//            // get nominal values of the attribute
//            ArrayList<String> nominalValues = new ArrayList<>(attributes[i].getNominalValuesList());
//            //for each nominal value
//            for (String value : nominalValues) {
//                int countValueInD1 = 0;
//                int countValueInD2 = 0;
//                // counts the times the value appear for each class
//                for (int j = 0; j < a.getNumInstances(); j++) {
//                    String p = a.getInputNominalValue(j, i);
//                    if (value.equals(p)) {
//                        // If are equals, check the class and increment counters
//                        if (a.getInstance(j).getOutputNominalValuesInt(0) == positiveClass) {
//                            countValueInD1++;
//                        } else {
//                            countValueInD2++;
//                        }
//                    }
//                }
//                double suppD1 = (double) countValueInD1 / (double) countD1;
//                double suppD2 = (double) countValueInD2 / (double) countD2;
//                // now calculate the growth rate of the item.
//                double gr;
//                if (suppD1 < minSupp && suppD2 < minSupp) {
//                    gr = 0;
//                } else if ((suppD1 == 0 && suppD2 >= minSupp) || (suppD1 >= minSupp && suppD2 == 0)) {
//                    gr = Double.POSITIVE_INFINITY;
//                } else {
//                    gr = Math.max(suppD2 / suppD1, suppD1 / suppD2);
//                }
//
//                // Add the item to the list of simple items
//                Item it = new Item(countId, value, attributes[i].getName(), gr);
//                it.setD1count(countValueInD1);
//                it.setD2count(countValueInD2);
//                simpleItems.add(it);
//                countId++;
//
//            }
//        }
//
//        return simpleItems;
//    }
//
//    /**
//     * Gets the instances of a dataset as set of Item class
//     *
//     * @param a
//     * @param simpleItems
//     * @return
//     */
//    public static ArrayList<Pair<ArrayList<Item>, Integer>> getInstances(InstanceSet a, ArrayList<Item> simpleItems, int positiveClass) {
//        String[] att_names = new String[Attributes.getInputAttributes().length];
//        ArrayList<Pair<ArrayList<Item>, Integer>> result = new ArrayList<>();
//        ArrayList<String> classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());
//
//        for (int i = 0; i < att_names.length; i++) {
//            att_names[i] = Attributes.getAttribute(i).getName();
//        }
//
//        for (int i = 0; i < a.getNumInstances(); i++) {
//            ArrayList<Item> list = new ArrayList<>();
//            for (int j = 0; j < Attributes.getInputNumAttributes(); j++) {
//                // Add the item into the pattern
//                Item it = Item.find(simpleItems, att_names[j], a.getInputNominalValue(i, j));
//                if (it != null) {
//                    list.add(it);
//                }
//            }
//            // Add into the set of instances, the second element is the class
//            int clas = 0;
//            if (a.getInstance(i).getOutputNominalValuesInt(0) != positiveClass) {
//                clas = 1;
//            }
//            result.add(new Pair(list, clas));
//        }
//
//        return result;
//    }
    /**
     * Returns the index of the max element of an array, or the first occurrence
     * of the max in this array in case that there exists mor than one maximum
     * element.
     *
     * @param array
     * @return
     */
    public static int getIndexOfMaxValue(float[] array) {
        int maxIndex = -1;
        float maxVal = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxIndex = i;
                maxVal = array[i];
            }
        }
        return maxIndex;
    }

    /**
     * Returns the index of the max element of an array, or the first occurrence
     * of the max in this array in case that there exists mor than one maximum
     * element.
     *
     * @param array
     * @return
     */
    public static int getIndexOfMaxValue(int[] array) {
        int maxIndex = -1;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxIndex = i;
                maxVal = array[i];
            }
        }
        return maxIndex;
    }

    /**
     * Checks if the dataset is processable by the method, i.e. it checks if all
     * its attributes are nominal.
     *
     * @throws framework.exceptions.IllegalActionException
     */
    public static void checkDataset() throws framework.exceptions.IllegalActionException {
        for (int i = 0; i < Attributes.getInputNumAttributes(); i++) {
            if (Attributes.getAttribute(i).getType() != Attribute.NOMINAL) {
                throw new framework.exceptions.IllegalActionException("ERROR: The dataset must contain only nominal attributes. Please, discretize the real ones.");
            }
        }

    }

    /**
     * Returns the index of the max element of an array, or the first occurrence
     * of the max in this array in case that there exists mor than one maximum
     * element.
     *
     * @param array
     * @return
     */
    public static int getIndexOfMaxValue(double[] array) {
        int maxIndex = -1;
        double maxVal = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxIndex = i;
                maxVal = array[i];
            }
        }
        return maxIndex;
    }

    /**
     * Removes duplicated patterns of the original patterns set.
     *
     * @param original
     * @return
     */
    public static ArrayList<Pattern> removeDuplicates(ArrayList<Pattern> original) {
        HashSet<Pattern> distinct = new HashSet<>(original);
        Iterator<Pattern> it = distinct.iterator();
        ArrayList<Pattern> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add((Pattern) it.next());
        }
        return result;
    }

    public static framework.items.Pattern castToNewPatternFormat(algorithms.bcep.Pattern oldPattern) {
        ArrayList<framework.items.Item> item = new ArrayList<>();
        for (algorithms.bcep.Item it : oldPattern.getItems()) {
            if (it.getType() == algorithms.bcep.Item.NOMINAL_ITEM) {
                item.add(new NominalItem(it.getVariable(), it.getValue()));
            } else if (it.getType() == algorithms.bcep.Item.REAL_ITEM) {
                item.add(new NumericItem(it.getVariable(), ((Float) it.getValueNum()).doubleValue(), oldPattern.getALPHA()));
            } else {
                item.add(new FuzzyItem(it.getVariable(), it.getValueFuzzy()));
            }
        }

        return new framework.items.Pattern(item, oldPattern.getClase());
    }

    /**
     * Transform the given dataset into Patterns with class 0 or 1. This is
     * useful to transform the given dataset whe dealing with multiclass label
     * with the OVA method. Where {@code class} is the possitive class (marked
     * as 0), and the rest of classes belongs to the negative class (marked as
     * 1).
     *
     * @param set
     * @param clas
     * @return
     */
    public static ArrayList<Pattern> generatePatterns(InstanceSet set, int clas) {
        ArrayList<Pattern> trainingInstances = new ArrayList<>();
        for (Instance inst : set.getInstances()) {
            Pattern p = new Pattern(new ArrayList<Item>(), inst.getOutputNominalValuesInt(0) == clas ? 0 : 1);
            for (int j = 0; j < set.getAttributeDefinitions().getInputNumAttributes(); j++) {
                if (!inst.getInputMissingValues(j)) {
                    if (set.getAttributeDefinitions().getAttribute(j).getType() == Attribute.NOMINAL) {
                        p.add(new NominalItem(set.getAttributeDefinitions().getAttribute(j).getName(), inst.getInputNominalValues(j)));
                    } else {
                        p.add(new NumericItem(set.getAttributeDefinitions().getAttribute(j).getName(), inst.getInputRealValues(j), 0.1));
                    }
                }
            }
            trainingInstances.add(p);
        }

        return trainingInstances;
    }

    /**
     * Creates the power set of the given pattern. The result is stored in
     * {@code sets}.
     *
     * @param p The super pattern which the power set is generated
     * @param sets A HashSet where the result is stored.
     */
    public static void powerSet(Pattern p, HashSet<Pattern> sets) {
        if (!p.getItems().isEmpty()) {
            sets.add(p);
            for (int i = 0; i < p.length(); i++) {
                Pattern g = p.clone();
                g.drop(i);
                powerSet(g, sets);
            }
        }
    }

    /**
     * It calculates the median value of the given set of values
     *
     * @param values
     * @return
     */
    public static double median(ArrayList<Double> values) {

        values.sort(null);
        int middle = values.size() / 2;
        if (values.size() % 2 == 1) {
            return values.get(middle);
        } else {
            return (values.get(middle - 1) + values.get(middle)) / 2.0;
        }

    }

}
