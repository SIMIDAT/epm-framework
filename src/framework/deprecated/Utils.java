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
package framework.deprecated;


import framework.utils.FisherExact;
import framework.GUI.Model;
import framework.items.FuzzyItem;
import framework.items.NominalItem;
import framework.items.NumericItem;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
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

    /*  ======================================================
                           COMPARATORS
        ====================================================== */
    /**
     * Sorts Items by frequency in descending order
     */
    public static Comparator<Item> frequency = (Item o1, Item o2) -> {
        if ((o1.getD1count() + o1.getD2count()) < (o2.getD2count() + o2.getD1count())) {
            return 1;
        } else if ((o1.getD1count() + o1.getD2count()) > (o2.getD2count() + o2.getD1count())) {
            return -1;
        } else {
            return 0;
        }
    };

    /**
     * Sorts by ratio ordering
     */
    public static Comparator<Item> ratio = (Item o1, Item o2) -> {
        double ratio1 = 0, ratio2 = 0;
        if (o1.getD2count() != 0) {
            ratio1 = (double) o1.getD1count() / (double) o1.getD2count();
        } else {
            ratio1 = Double.POSITIVE_INFINITY;
        }
        if (o2.getD2count() != 0) {
            ratio2 = (double) o2.getD1count() / (double) o2.getD2count();
        } else {
            ratio2 = Double.POSITIVE_INFINITY;
        }

        if (ratio1 < ratio2) {
            return 1;
        } else if (ratio1 > ratio2) {
            return -1;
        } else {
            return 0;
        }
    };
    /**
     * Sorts by ratio inverse ordering
     */
    public static Comparator<Item> ratioInverse = (Item o1, Item o2) -> {
        double ratio1 = 0, ratio2 = 0;
        if (o1.getD2count() != 0) {
            ratio1 = (double) o1.getD1count() / (double) o1.getD2count();
        } else {
            ratio1 = Double.POSITIVE_INFINITY;
        }
        if (o2.getD2count() != 0) {
            ratio2 = (double) o2.getD1count() / (double) o2.getD2count();
        } else {
            ratio2 = Double.POSITIVE_INFINITY;
        }

        if (ratio1 > ratio2) {
            return 1;
        } else if (ratio1 < ratio2) {
            return -1;
        } else {
            return 0;
        }
    };

    /**
     * Least Probable in the Negative Class ordering.
     */
    public static Comparator<Item> LPNC = (Item o1, Item o2) -> {
        if (o1.getD2count() > o2.getD2count()) {
            return 1;
        } else if (o2.getD2count() < o2.getD2count()) {
            return -1;
        } else {
            return 0;
        }
    };

    /**
     * Least Probable in the Negative Class ordering.
     */
    public static Comparator<Item> MPPC = (Item o1, Item o2) -> {
        if (o1.getD1count() < o2.getD1count()) {
            return 1;
        } else if (o2.getD1count() > o2.getD1count()) {
            return -1;
        } else {
            return 0;
        }
    };

    /* ============================================
                    END OF COMPARATORS
       ============================================     */
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
        qualityMeasures.put("AUC", 0.0); // Area Under the Curve

        return qualityMeasures;
    }

    /**
     * Calculates the descriptive quality measures
     *
     * @param data The dataset with the data
     * @param model The model where the rules are stored
     * @param isTrain The calculated measures are for training (true) or for
     * test (false)?
     * @return The descriptive quality measures for each rule.
     */
    public static ArrayList<HashMap<String, Double>> calculateDescriptiveMeasures(InstanceSet data, Model model, boolean isTrain) {
        // 0 -> tp
        // 1 -> tn
        // 2 -> fp
        // 3 -> fn
        // 4 -> n_vars
        int sumNvars = 0;
        data.setAttributesAsNonStatic();
        Attribute[] inputAttributes = data.getAttributeDefinitions().getInputAttributes();
        Attribute outputAttributes = data.getAttributeDefinitions().getOutputAttribute(0);
        int[][] confusionMatrices = new int[model.getPatterns().size()][5];
        for (int i = 0; i < model.getPatterns().size(); i++) {
            int tp = 0;
            int tn = 0;
            int fp = 0;
            int fn = 0;
            // for each instance
            for (int j = 0; j < data.getNumInstances(); j++) {
                // If the pattern covers the example
                if (model.getPatterns().get(i).covers(data.getInstance(j), inputAttributes)) {

                    if (model.getPatterns().get(i).getClase() == outputAttributes.convertNominalValue(data.getOutputNominalValue(j, 0))) {
                        tp++;
                    } else {
                        fp++;
                    }
                } else if (model.getPatterns().get(i).getClase() != outputAttributes.convertNominalValue(data.getOutputNominalValue(j, 0))) {
                    tn++;
                } else {
                    fn++;
                }

            }

            confusionMatrices[i][0] = tp;
            confusionMatrices[i][1] = tn;
            confusionMatrices[i][2] = fp;
            confusionMatrices[i][3] = fn;
            confusionMatrices[i][4] = model.getPatterns().get(i).getItems().size();
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
            measures.put("SUPP", supp); // Fishers's test
            measures.put("NVAR", (double) confusionMatrices[i][4]);
            measures.put("RULE_NUMBER", (double) i);

            qms.add(measures);
            if (isTrain) {
                model.getPatterns().get(i).setTra_measures(measures);
            } else {
                model.getPatterns().get(i).setTst_measures(measures);
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
        result.put("RULE_NUMBER", Double.NaN);

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
     * @param QMsGlobal
     * @param QMsByClass
     * @param NUM_FOLDS
     */
    public static void saveResults(File dir, HashMap<String, Double> QMsUnfiltered, HashMap<String, Double> QMsGlobal, HashMap<String, Double> QMsByClass, int NUM_FOLDS) {

        try {
            File measures1 = new File(dir.getAbsolutePath() + "/QM_Unfiltered.txt");
            File measures2 = new File(dir.getAbsolutePath() + "/QM_FilteredALL.txt");
            File measures3 = new File(dir.getAbsolutePath() + "/QM_FilteredBYCLASS.txt");
            if (measures1.exists()) {
                measures1.delete();
            }
            if (measures2.exists()) {
                measures2.delete();
            }
            if (measures3.exists()) {
                measures3.delete();
            }
            measures1.createNewFile();
            measures2.createNewFile();
            measures3.createNewFile();

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

            QMsGlobal.forEach(new BiConsumer<String, Double>() {
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

            QMsByClass.forEach(new BiConsumer<String, Double>() {
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

        if (Attributes.getOutputAttribute(0).getNominalValuesList().size() <= 2) {
            // For 2-class we calculate the confusion matrix
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
                results.get(i).put("ACC", acc);
                results.get(i).put("AUC", auc);
            }
        } else {
            for (int i = 0; i < predictions.length; i++) {
                float aciertos = 0;
                for (int j = 0; j < predictions[i].length; j++) {
                    if (predictions[i][j].equals(test.getOutputNominalValue(j, 0))) {
                        aciertos++;
                    }
                }

                double acc = ((double) aciertos / (double) test.getNumInstances());
                results.get(i).put("ACC", acc);
                results.get(i).put("AUC", Double.NaN);

            }
        }
    }

    

    /**
     * Saves the results of the patterns sets in the given folder. This creates
     * 4 files: RULES.txt, TRA_QUAC_NOFILTER.txt, TRA_QUAC_BEST.txt and
     * TRA_QUAC_BESTCLASS.txt
     *
     * @param dir A folder to save the results.
     * @param model The model where the pattern are stored.
     * @param Measures The Averaged quality measures for each set of patterns
     * (Unfiltered, filtered and filtered by class)
     */
    public static void saveTraining(File dir, Model model, ArrayList<HashMap<String, Double>> Measures) {
        PrintWriter pw1 = null;
        PrintWriter pw2 = null;
        PrintWriter pw3 = null;
        PrintWriter pw4 = null;
        try {
            // define the files to write
            pw1 = new PrintWriter(dir.getAbsolutePath() + "/RULES.txt");
            pw2 = new PrintWriter(dir.getAbsolutePath() + "/TRA_QUAC_NOFILTER.txt");
            pw3 = new PrintWriter(dir.getAbsolutePath() + "/TRA_QUAC_BEST.txt");
            pw4 = new PrintWriter(dir.getAbsolutePath() + "/TRA_QUAC_BESTCLASS.txt");

            DecimalFormat sixDecimals = new DecimalFormat("0.000000");
            // Write headers on file.
            Object[] keys = Measures.get(0).keySet().toArray();
            pw2.print("RULE_NUMBER\t\tN_VARS\t\t");
            pw3.print("RULE_NUMBER\t\tN_VARS\t\t");
            pw4.print("RULE_NUMBER\t\tN_VARS\t\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                    pw2.print(k + "\t\t");
                    pw3.print(k + "\t\t");
                    pw4.print(k + "\t\t");
                }
            }
            pw2.println();
            pw3.println();
            pw4.println();

            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatterns().size(); i++) {
                pw1.println("RULE NUMBER " + model.getPatterns().get(i).getTra_measures().get("RULE_NUMBER") + ": " + model.getPatterns().get(i).toString());
                pw2.print(model.getPatterns().get(i).getTra_measures().get("RULE_NUMBER") + "\t\t");
                pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get("NVAR")) + "\t\t");
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw2.print("--------\t\t");
                        } else {
                            pw2.print(sixDecimals.format(model.getPatterns().get(i).getTra_measures().get(k)) + "\t\t");
                        }
                    }
                }
                pw2.println();
            }
            // write mean results
            pw2.print("--------\t\t--------\t\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                    pw2.print(sixDecimals.format(Measures.get(0).get(k)) + "\t\t");
                }
            }

            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatternsFilteredMinimal().size(); i++) {
                pw3.print(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("RULE_NUMBER") + "\t\t");
                pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get("NVAR")) + "\t");
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw3.print("--------\t\t");
                        } else {
                            pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTra_measures().get(k)) + "\t\t");
                        }
                    }
                }
                pw3.println();
            }

            pw3.print("--------\t\t--------\t\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                    pw3.print(sixDecimals.format(Measures.get(1).get(k)) + "\t\t");
                }
            }

            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatternsFilteredMaximal().size(); i++) {
                pw4.print(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("RULE_NUMBER") + "\t\t");
                pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get("NVAR")) + "\t\t");
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw4.print("--------\t\t");
                        } else {
                            pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTra_measures().get(k)) + "\t\t");
                        }
                    }
                }
                pw4.println();
            }

            pw4.print("--------\t\t--------\t\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                    pw4.print(sixDecimals.format(Measures.get(2).get(k)) + "\t\t");
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw1.close();
            pw2.close();
            pw3.close();
            pw4.close();
        }
    }

    /**
     * Saves the results of the patterns sets in the given folder. This creates
     * 3 files: TST_QUAC_NOFILTER.txt, TST_QUAC_BEST.txt and
     * TST_QUAC_BESTCLASS.txt
     *
     * @param dir A folder to save the results.
     * @param model The model where the pattern are stored.
     * @param Measures The Averaged quality measures for each set of patterns
     * (Unfiltered, filtered and filtered by class)
     */
    public static void saveTest(File dir, Model model, ArrayList<HashMap<String, Double>> Measures) {
        //PrintWriter pw1 = null;
        PrintWriter pw2 = null;
        PrintWriter pw3 = null;
        PrintWriter pw4 = null;
        try {
            // define the files to write
            //  pw1 = new PrintWriter(dir.getAbsolutePath() + "/RULES.txt");
            pw2 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_NOFILTER.txt");
            pw3 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_BEST.txt");
            pw4 = new PrintWriter(dir.getAbsolutePath() + "/TST_QUAC_BESTCLASS.txt");

            DecimalFormat sixDecimals = new DecimalFormat("0.000000");
            // Write headers on file.
            Object[] keys = Measures.get(0).keySet().toArray();
            pw2.print("RULE_NUMBER\t\tN_VARS\t\t");
            pw3.print("RULE_NUMBER\t\tN_VARS\t\t");
            pw4.print("RULE_NUMBER\t\tN_VARS\t\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                    pw2.print(k + "\t\t");
                    pw3.print(k + "\t\t");
                    pw4.print(k + "\t\t");
                }
            }
            pw2.println();
            pw3.println();
            pw4.println();

            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatterns().size(); i++) {
                //pw1.println("RULE NUMBER " + model.getPatterns().get(i).getTra_measures().get("RULE_NUMBER") + ": " + model.getPatterns().get(i).toString());
                pw2.print(model.getPatterns().get(i).getTst_measures().get("RULE_NUMBER") + "\t\t");
                pw2.print(sixDecimals.format(model.getPatterns().get(i).getTst_measures().get("NVAR")) + "\t\t");
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw2.print("--------\t\t");
                        } else {
                            pw2.print(sixDecimals.format(model.getPatterns().get(i).getTst_measures().get(k)) + "\t\t");
                        }
                    }
                }
                pw2.println();
            }
            // write mean results
            pw2.print("--------\t\t--------\t\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                    pw2.print(sixDecimals.format(Measures.get(0).get(k)) + "\t\t");
                }
            }

            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatternsFilteredMinimal().size(); i++) {
                pw3.print(model.getPatternsFilteredMinimal().get(i).getTst_measures().get("RULE_NUMBER") + "\t\t");
                pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTst_measures().get("NVAR")) + "\t");
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw3.print("--------\t\t");
                        } else {
                            pw3.print(sixDecimals.format(model.getPatternsFilteredMinimal().get(i).getTst_measures().get(k)) + "\t\t");
                        }
                    }
                }
                pw3.println();
            }

            // write mean results
            pw3.print("--------\t\t--------\t\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                    pw3.print(sixDecimals.format(Measures.get(0).get(k)) + "\t\t");
                }
            }
            // write rules and training qms for all rules
            for (int i = 0; i < model.getPatternsFilteredMaximal().size(); i++) {
                pw4.print(model.getPatternsFilteredMaximal().get(i).getTst_measures().get("RULE_NUMBER") + "\t\t");
                pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTst_measures().get("NVAR")) + "\t\t");
                for (Object key : keys) {
                    String k = (String) key;
                    if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                        if (k.equals("ACC") || k.equals("AUC")) {
                            pw4.print("--------\t\t");
                        } else {
                            pw4.print(sixDecimals.format(model.getPatternsFilteredMaximal().get(i).getTst_measures().get(k)) + "\t\t");
                        }
                    }
                }
                pw4.println();
            }

            // write mean results
            pw4.print("--------\t\t--------\t\t");
            for (Object key : keys) {
                String k = (String) key;
                if (!k.equals("RULE_NUMBER") && !k.equals("NVAR")) {
                    pw4.print(sixDecimals.format(Measures.get(2).get(k)) + "\t\t");
                }
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            pw2.close();
            pw3.close();
            pw4.close();
        }
    }

    /**
     * Gets simple itemsets with a support higher than a threshold
     *
     * @param a
     * @param minSupp
     * @param positiveClass - The class to consider as positive. For multiclass
     * problems, the others classes are considered as negative.
     * @return
     */
    public static ArrayList<Item> getSimpleItems(InstanceSet a, double minSupp, int positiveClass) {
        // Reads the KEEL instance set.

        int countD1 = 0;   // counts of examples belonging to class D1 and D2.
        int countD2 = 0;
        ArrayList<Item> simpleItems = new ArrayList<>();
        // get classes
        ArrayList<String> classes;
        try {
            classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());
        } catch (NullPointerException ex) {
            classes = new ArrayList<>(a.getAttributeDefinitions().getOutputAttribute(0).getNominalValuesList());
        }
        // Gets the count of examples for each class to calculate the growth rate.
        for (int i = 0; i < a.getNumInstances(); i++) {
            if (a.getInstance(i).getOutputNominalValuesInt(0) == positiveClass) {
                countD1++;
            } else {
                countD2++;
            }
        }

        // Get the attributes
        Attribute[] attributes = Attributes.getInputAttributes();
        int countId = 0;
        // for each attribute
        for (int i = 0; i < attributes.length; i++) {
            // get nominal values of the attribute
            ArrayList<String> nominalValues = new ArrayList<>(attributes[i].getNominalValuesList());
            //for each nominal value
            for (String value : nominalValues) {
                int countValueInD1 = 0;
                int countValueInD2 = 0;
                // counts the times the value appear for each class
                for (int j = 0; j < a.getNumInstances(); j++) {
                    String p = a.getInputNominalValue(j, i);
                    if (value.equals(p)) {
                        // If are equals, check the class and increment counters
                        if (a.getInstance(j).getOutputNominalValuesInt(0) == positiveClass) {
                            countValueInD1++;
                        } else {
                            countValueInD2++;
                        }
                    }
                }
                double suppD1 = (double) countValueInD1 / (double) countD1;
                double suppD2 = (double) countValueInD2 / (double) countD2;
                // now calculate the growth rate of the item.
                double gr;
                if (suppD1 < minSupp && suppD2 < minSupp) {
                    gr = 0;
                } else if ((suppD1 == 0 && suppD2 >= minSupp) || (suppD1 >= minSupp && suppD2 == 0)) {
                    gr = Double.POSITIVE_INFINITY;
                } else {
                    gr = Math.max(suppD2 / suppD1, suppD1 / suppD2);
                }

                // Add the item to the list of simple items
                Item it = new Item(countId, value, attributes[i].getName(), gr);
                it.setD1count(countValueInD1);
                it.setD2count(countValueInD2);
                simpleItems.add(it);
                countId++;

            }
        }

        return simpleItems;
    }

    /**
     * Gets the instances of a dataset as set of Item class
     *
     * @param a
     * @param simpleItems
     * @return
     */
    public static ArrayList<Pair<ArrayList<Item>, Integer>> getInstances(InstanceSet a, ArrayList<Item> simpleItems, int positiveClass) {
        String[] att_names = new String[Attributes.getInputAttributes().length];
        ArrayList<Pair<ArrayList<Item>, Integer>> result = new ArrayList<>();
        ArrayList<String> classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());

        for (int i = 0; i < att_names.length; i++) {
            att_names[i] = Attributes.getAttribute(i).getName();
        }

        for (int i = 0; i < a.getNumInstances(); i++) {
            ArrayList<Item> list = new ArrayList<>();
            for (int j = 0; j < Attributes.getInputNumAttributes(); j++) {
                // Add the item into the pattern
                Item it = Item.find(simpleItems, att_names[j], a.getInputNominalValue(i, j));
                if (it != null) {
                    list.add(it);
                }
            }
            // Add into the set of instances, the second element is the class
            int clas = 0;
            if (a.getInstance(i).getOutputNominalValuesInt(0) != positiveClass) {
                clas = 1;
            }
            result.add(new Pair(list, clas));
        }

        return result;
    }

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
    public static ArrayList<framework.items.Pattern> removeDuplicates(ArrayList<framework.items.Pattern> original) {
        HashSet<framework.items.Pattern> distinct = new HashSet<>(original);
        Iterator<framework.items.Pattern> it = distinct.iterator();
        ArrayList<framework.items.Pattern> result = new ArrayList<>();
        while (it.hasNext()) {
            result.add((framework.items.Pattern) it.next());
        }
        return result;
    }

    public static framework.items.Pattern castToNewPatternFormat(framework.deprecated.Pattern oldPattern) {
        ArrayList<framework.items.Item> item = new ArrayList<>();
        for(framework.deprecated.Item it : oldPattern.getItems()){
            if(it.getType() == algorithms.bcep.Item.NOMINAL_ITEM){
                item.add(new NominalItem(it.getVariable(), it.getValue()));
            } else if (it.getType() == algorithms.bcep.Item.REAL_ITEM){   
                item.add(new NumericItem(it.getVariable(), ((Float) it.getValueNum()).doubleValue(), oldPattern.getALPHA()));
            } else {
                item.add(new FuzzyItem(it.getVariable(), it.getValueFuzzy()));
            }
        }
        
        return new framework.items.Pattern(item, oldPattern.getClase());
    }
}
