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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;
import javax.print.attribute.HashAttributeSet;
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
     * Learns a model from training data, and saves it to a file. The patterns obtained in the model must be saved on a 
     * class variable to make it accessible to "predict" and "test" methods.
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
     * This method perform on a single pass the complete test phase of the method, i.e. it calculates
     * descriptive quality measures and predictive ones. Additionally, saves on files the results.
     *
     * @param test the test set.
     * @param batch it is executed for a batch execution or not?
     */
    public HashMap<String, Double> test(InstanceSet test) {
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
     * Calculates the descriptive quality measures for each ruleof a given confusion matrix
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
    
    // Filtrado normal (las n mejores reglas) 
    
    // filtrado por clase (las n mejores reglas de cada clase), se necesita un parametro adicional que indique la clase de cada patron.
    
    
    /**
     * Get the averaged descriptive quality measures from a set of quality measures of each rule.
     * @param measures
     * @return 
     */
    protected static HashMap<String, Double> AverageQualityMeasures(ArrayList<HashMap<String, Double>> measures){
        
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
    protected HashMap<String, Double> updateHashMap(HashMap<String, Double> one, HashMap<String, Double> another) {
        HashMap<String, Double> sum = generateQualityMeasuresHashMap();
        one.forEach(new BiConsumer<String, Double>() {
            @Override
            public void accept(String t, Double u) {
                sum.put(t, u + another.get(t));
            }
        });

        return sum;
    }

    protected HashMap<String, Double> meanHashmap(HashMap<String, Double> element, int nPatterns) {
        HashMap<String, Double> mean = generateQualityMeasuresHashMap();
        element.forEach(new BiConsumer<String, Double>() {
            @Override
            public void accept(String t, Double u) {
                if (!t.equals("NVAR") || !t.equals("NRULES")) {
                    mean.put(t, u / (double) nPatterns);
                }
            }
        });

        return mean;
    }

}
