/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sjep_classifier;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import keel.Dataset.*;
import org.core.*;

/**
 *
 * @author angel
 */
public class SJEP_Classifier {

    public static String tra_file, tst_file, nombre_alg;
    public static double minSupp;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        InstanceSet a = new InstanceSet();
        int countD1 = 0;
        int countD2 = 0;

        // Reads parameters
        ReadParameters(args[0]);

        if (!nombre_alg.equalsIgnoreCase("sjep-c")) {
            System.out.println("Algorithm is not SJEP-C. Aborting...");
            System.exit(-1);
        }
        // Reads the original dataset
        try {
            a.readSet(tra_file, true);
            ArrayList<String> classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());
            // Gets the count of examples for each class to calculate the growth rate.
            for (int i = 0; i < a.getNumInstances(); i++) {
                if (classes.indexOf(a.getInstance(i).getOutputNominalValues(0)) == 0) {
                    countD1++;
                } else {
                    countD2++;
                }
            }

        } catch (DatasetException | HeaderFormatException ex) {
            Logger.getLogger(SJEP_Classifier.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (Attributes.getOutputAttribute(0).getNumNominalValues() <= 2) {
            // Normal Execution (BINARY PROBLEM)
            long t_ini = System.currentTimeMillis();
            // get simple itemsets to perform the ordering of the items and filter by gorwth rate
            // Class '0' is considered as positive
            ArrayList<Item> simpleItems = getSimpleItems(a, minSupp, 0);
            // sort items by growth rate 
            simpleItems.sort(null);
            // gets all instances removing those itemset that not appear on simpleItems
            ArrayList<Pair<ArrayList<Item>, Integer>> instances = getInstances(a, simpleItems, 0);
            for (int i = 0; i < instances.size(); i++) {
                // sort each arraylist of items
                instances.get(i).getKey().sort(null);
            }

            System.out.println("Loading the CP-Tree...");
            // Create the CP-Tree
            CPTree tree = new CPTree(countD1, countD2);
            // Add the instances on the CP-Tree
            for (Pair<ArrayList<Item>, Integer> inst : instances) {
                tree.insertTree(inst.getKey(), inst.getValue());
            }

            System.out.println("Mining SJEPs...");
            // Perform mining
            ArrayList<Pattern> patterns = tree.mineTree(minSupp);

            if (patterns.size() == 0) {
                System.out.println("NO SJEPs FOUND");
            } else {
                try {
                    PrintWriter pw = new PrintWriter(tra_file + "-Patterns.csv");
                    for (int i = 0; i < patterns.size(); i++) {
                        pw.println(patterns.get(i).toString());
                    }
                    System.out.println("Testing patterns...");
                    // pattern TESTING here
                    // First, reads the test set
                    InstanceSet test = new InstanceSet();
                    ArrayList<Pair<ArrayList<Item>, Integer>> testInstances;

                    test.readSet(tst_file, false);
                    // Get the instances: It is not neccesary to sort that instances.
                    testInstances = getInstances(test, simpleItems, 0);
                    // Writes the confusion matrix file in CSV format
                    pw = new PrintWriter(tra_file + "-CM.csv", "UTF-8");
                    pw.println("TP,FP,FN,TN"); // Prints the header

                    // Calculate the confusion matrix for each pattern to compute other quality measures
                    for (int i = 0; i < patterns.size(); i++) {
                        int tp = 0;
                        int tn = 0;
                        int fp = 0;
                        int fn = 0;
                        // for each instance
                        for (int j = 0; j < testInstances.size(); j++) {
                            // If the pattern covers the example
                            if (patterns.get(i).covers(testInstances.get(j).getKey())) {

                                if (patterns.get(i).getClase() == testInstances.get(j).getValue()) {
                                    tp++;
                                } else {
                                    fp++;
                                }
                            } else {
                                if (patterns.get(i).getClase() != testInstances.get(j).getValue()) {
                                    tn++;
                                } else {
                                    fn++;
                                }
                            }

                        }
                        // Saves on the file
                        pw.println(tp + "," + fp + "," + fn + "," + tn);
                    }
                    // close the writer
                    pw.close();

                    // Show statistics
                    System.out.println("================ STATISTICS =====================");
                    System.out.println("SJEPs found: " + patterns.size());
                    // Calculate the test accuracy:
                    computeAccuracy(testInstances, patterns, test, countD1, countD2);
                } catch (DatasetException | HeaderFormatException ex) {
                    Logger.getLogger(SJEP_Classifier.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(SJEP_Classifier.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
            System.out.println("Execution time: " + (double) (System.currentTimeMillis() - t_ini) / 1000.0 + " seconds");

        } else {
            // MULTICLASS EXECUTION
            // For multi-class, mining using the OVA (One-vs-All) binarization technique.
            long t_ini = System.currentTimeMillis();
            // Here we store all patterns, each position of the ArrayList corresponds with patterns
            // of the class at position 'i'.
            ArrayList<ArrayList<Pattern>> allPatterns = new ArrayList<>();
            // Execute the mining algorithm k times, with k the number of classes.
            for (int i = 0; i < Attributes.getOutputAttribute(0).getNumNominalValues(); i++) {
                // count the number of examples in the new binarized dataset
                countD1 = countD2 = 0;
                for (int j = 0; j < a.getNumInstances(); j++) {
                    if (a.getInstance(i).getOutputNominalValuesInt(0) == i) {
                        countD1++;
                    } else {
                        countD2++;
                    }
                }

                System.out.println("Mining class: " + Attributes.getOutputAttribute(0).getNominalValue(i));
                // Class 'i' is considered de positive class, the rest of classes correspond to the negative one.
                // Get the simple items.
                ArrayList<Item> simpleItems = getSimpleItems(a, minSupp, i);
                // sort items by growth rate 
                simpleItems.sort(null);
                // gets all instances removing those itemset that not appear on simpleItems
                ArrayList<Pair<ArrayList<Item>, Integer>> instances = getInstances(a, simpleItems, i);
                for (int j = 0; j < instances.size(); j++) {
                    // sort each arraylist of items
                    instances.get(j).getKey().sort(null);
                }

                System.out.println("Loading the CP-Tree...");
                // Create the CP-Tree
                CPTree tree = new CPTree(countD1, countD2);
                // Add the instances on the CP-Tree
                for (Pair<ArrayList<Item>, Integer> inst : instances) {
                    tree.insertTree(inst.getKey(), inst.getValue());
                }

                System.out.println("Mining SJEPs...");
                // Perform mining
                ArrayList<Pattern> patterns = tree.mineTree(minSupp);
                // remove those patterns with class != 0 and change the value of the class
                Iterator it = patterns.iterator();
                while (it.hasNext()) {
                    Pattern next = (Pattern) it.next();
                    if (next.getClase() != 0) {
                        it.remove();
                    } else {
                        next.setClase(i);
                    }
                }
                allPatterns.add(patterns);
            }

            /* ========================
             PATTERNS TEST
             ========================
             */
            System.out.println("Testing patterns...");
            // pattern TESTING here
            // First, reads the test set
            InstanceSet test = new InstanceSet();
            ArrayList<Pair<ArrayList<Item>, Integer>> testInstances;

            try {
                PrintWriter pw = new PrintWriter(tra_file + "-Patterns.csv");
                PrintWriter CMpw = new PrintWriter(tra_file + "-CM.csv", "UTF-8");
                CMpw.println("TP,FP,FN,TN"); // Prints the header
                test.readSet(tst_file, false);
                // We have in 'allPatterns' an array with patterns of each class.
                // Now, we determine the confusion matrix of each pattern.
                for (int i = 0; i < allPatterns.size(); i++) {
                    ArrayList<Pattern> patterns = allPatterns.get(i);

                    for (int j = 0; j < patterns.size(); j++) {
                        pw.println(patterns.get(j).toString());
                    }

                    // Get the instances: It is not neccesary to sort that instances.
                    ArrayList<Item> simpleItems = getSimpleItems(test, minSupp, i);
                    testInstances = getInstances(test, simpleItems, i);

                    // Calculate the confusion matrix for each pattern to compute other quality measures
                    for (int j = 0; j < patterns.size(); j++) {
                        int tp = 0;
                        int tn = 0;
                        int fp = 0;
                        int fn = 0;
                        // for each instance
                        for (int k = 0; k < testInstances.size(); k++) {
                            // class '0' is considered the positive class (which is marked as the 'i' index
                            // If the pattern covers the example
                            if (patterns.get(j).covers(testInstances.get(k).getKey())) {

                                if (patterns.get(j).getClase() == testInstances.get(k).getValue()) {
                                    tp++;
                                } else {
                                    fp++;
                                }
                            } else {
                                if (patterns.get(j).getClase() != testInstances.get(k).getValue()) {
                                    tn++;
                                } else {
                                    fn++;
                                }

                            }
                        }
                        // Saves on the file
                        CMpw.println(tp + "," + fp + "," + fn + "," + tn);
                    }

                }

                // close the writer
                pw.close();
                CMpw.close();
                // Show statistics
                System.out.println("================ STATISTICS =====================");
                int sum = 0;
                for (ArrayList<Pattern> patterns : allPatterns) {
                    sum += patterns.size();
                }
                System.out.println("SJEPs found: " + sum);
            } catch (FileNotFoundException | DatasetException | HeaderFormatException | UnsupportedEncodingException ex) {
                Logger.getLogger(SJEP_Classifier.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("Execution time: " + (double) (System.currentTimeMillis() - t_ini) / 1000.0 + " seconds");

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
        ArrayList<String> classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());
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
            int countValueInD1 = 0;
            int countValueInD2 = 0;
            //for each nominal value
            for (String value : nominalValues) {
                // counts the times the value appear for each class
                for (int j = 0; j < a.getNumInstances(); j++) {
                    if (value.equals(a.getInputNominalValue(j, i))) {
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
                if (gr > 0) {
                    simpleItems.add(new Item(countId, value, attributes[i].getName(), gr));
                    countId++;
                }
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

    public static double median(ArrayList<Integer> values) {
        if (values.size() > 1) {
            int middle = values.size() >>> 1;
            if (middle % 2 == 1) {
                return values.get(middle);
            } else {
                return (values.get(middle - 1) + values.get(middle)) / 2.0;
            }
        } else {
            if (values.size() == 1) {
                return values.get(0);
            }
            return 0;
        }
    }

    public static void calculateAccuracy(InstanceSet testSet, int[] predictions) {
        // we consider class 0 as positive and class 1 as negative
        int tp = 0;
        int fp = 0;
        int tn = 0;
        int fn = 0;

        // Calculate the confusion matrix.
        for (int i = 0; i < predictions.length; i++) {
            if (testSet.getOutputNumericValue(i, 0) == 0) {
                if (predictions[i] == 0) {
                    tp++;
                } else {
                    fn++;
                }
            } else {
                if (predictions[i] == 0) {
                    fp++;
                } else {
                    tn++;
                }
            }
        }

        System.out.println("Test Accuracy: " + ((double) (tp + tn) / (double) (tp + tn + fp + fn)) * 100 + "%");
    }

    public static void computeAccuracy(ArrayList<Pair<ArrayList<Item>, Integer>> testInstances, ArrayList<Pattern> patterns, InstanceSet test, int countD1, int countD2) {
        int[] predictions = new int[testInstances.size()];
        //Now, for each pattern
        for (int i = 0; i < testInstances.size(); i++) {
            // calculate the score for each class for classify:
            double scoreD1 = 0;
            double scoreD2 = 0;
            ArrayList<Integer> scoresD1 = new ArrayList<>();  // This is to calculate the base-score, that is the median
            ArrayList<Integer> scoresD2 = new ArrayList<>();
            // for each pattern mined
            for (int j = 0; j < patterns.size(); j++) {
                if (patterns.get(j).covers(testInstances.get(i).getKey())) {
                    // If the example is covered by the pattern.
                    // sum it support to the class of the pattern
                    if (testInstances.get(i).getValue() == 0) {
                        scoreD1 += patterns.get(j).getSupport();
                        scoresD1.add(patterns.get(j).getSupport());
                    } else {
                        scoreD2 += patterns.get(j).getSupport();
                        scoresD2.add(patterns.get(j).getSupport());
                    }

                }
            }

            // Now calculate the normalized score to make the prediction
            double medianD1 = median(scoresD1);
            double medianD2 = median(scoresD2);

            if (medianD1 == 0) {
                scoreD1 = 0;
            } else {
                scoreD1 = scoreD1 / medianD1;
            }

            if (medianD2 == 0) {
                scoreD2 = 0;
            } else {
                scoreD2 = scoreD2 / medianD2;
            }

            // make the prediction:
            if (scoreD1 > scoreD2) {
                predictions[i] = 0;
            } else if (scoreD1 < scoreD2) {
                predictions[i] = 1;
            } else {
                // In case of ties, the majority class is setted
                if (countD1 < countD2) {
                    predictions[i] = 0;
                } else {
                    predictions[i] = 1;
                }
            }
        }

        calculateAccuracy(test, predictions);
    }

    public void computeAccuracyMulticlass(ArrayList<Pair<ArrayList<Item>, Integer>> testInstances, ArrayList<Pattern> patterns, InstanceSet test) {

    }

    public static void ReadParameters(String nFile) {
        try {
            int nl;
            String fichero, linea, tok;
            StringTokenizer lineasFichero, tokens;
            fichero = File.readFile(nFile);
            //fichero = fichero.toLowerCase() + "\n ";
            lineasFichero = new StringTokenizer(fichero, "\n\r");

            for (nl = 0, linea = lineasFichero.nextToken(); lineasFichero.hasMoreTokens(); linea = lineasFichero.nextToken()) {
                nl++;
                tokens = new StringTokenizer(linea, " ,\t");
                if (tokens.hasMoreTokens()) {
                    tok = tokens.nextToken();
                    if (tok.equalsIgnoreCase("algorithm")) {
                        nombre_alg = Utils.getParamString(tokens);
                    } else if (tok.equalsIgnoreCase("minsupp")) {
                        minSupp = Double.parseDouble(Utils.getParamString(tokens));
                    } else if (tok.equalsIgnoreCase("training")) {
                        tra_file = Utils.getParamString(tokens);
                    } else if (tok.equalsIgnoreCase("test")) {
                        tst_file = Utils.getParamString(tokens);
                    } else {
                        throw new IOException("Syntax error on line " + nl + ": [" + tok + "]\n");
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println(e + " Parameter file");
        } catch (IOException e) {
            System.err.println(e + "Aborting program");
            System.exit(-1);
        }
    }

}
