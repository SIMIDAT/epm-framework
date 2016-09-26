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
package bcep;

import epm_algorithms.Model;
import java.util.HashMap;
import keel.Dataset.InstanceSet;
import keel.Dataset.Attribute;
import Utils.*;
import epm_algorithms.Main;
import exceptions.IllegalActionException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import keel.Dataset.Attributes;
import static sjep_classifier.SJEP_Classifier.getInstances;
import static sjep_classifier.SJEP_Classifier.getSimpleItems;
import static sjep_classifier.SJEP_Classifier.minSupp;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class BCEP_Model extends Model {

    ArrayList<Pattern> patterns;
    ArrayList<ArrayList<Pattern>> allPatterns;

    public BCEP_Model() {
        super.setFullyQualifiedName("bcep.BCEP_Model");
    }

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {

        try {
            checkDataset();

            int countD1 = 0;
            int countD2 = 0;
            float minSupp = Float.parseFloat(params.get("Minimum support"));
            ArrayList<String> classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());
            // Gets the count of examples for each class to calculate the growth rate.
            for (int i = 0; i < training.getNumInstances(); i++) {
                if (classes.indexOf(training.getInstance(i).getOutputNominalValues(0)) == 0) {
                    countD1++;
                } else {
                    countD2++;
                }
            }

            if (Attributes.getOutputAttribute(0).getNumNominalValues() <= 2) {
                // get simple itemsets to perform the ordering of the items and filter by gorwth rate
                // Class '0' is considered as positive
                ArrayList<Item> simpleItems = getSimpleItems(training, minSupp, 0);
                // sort items by growth rate
                simpleItems.sort(null);
                // gets all instances removing those itemset that not appear on simpleItems
                ArrayList<Pair<ArrayList<Item>, Integer>> instances = getInstances(training, simpleItems, 0);
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
                patterns = tree.mineTree(minSupp);
                ArrayList<Pair<ArrayList<Item>, Integer>> inst = getInstances(training, simpleItems, 0);
                patterns = pruneEPs(patterns, inst);
                Main.setInfoLearnText("Mining finished. eJEPs found: " + patterns.size());
            } else {
                // MULTICLASS EXECUTION
                // Execute the mining algorithm k times, with k the number of classes.
                allPatterns = new ArrayList<>();
                for (int i = 0; i < Attributes.getOutputAttribute(0).getNumNominalValues(); i++) {
                    // count the number of examples in the new binarized dataset
                    countD1 = countD2 = 0;
                    for (int j = 0; j < training.getNumInstances(); j++) {
                        if (training.getInstance(i).getOutputNominalValuesInt(0) == i) {
                            countD1++;
                        } else {
                            countD2++;
                        }
                    }

                    System.out.println("Mining class: " + Attributes.getOutputAttribute(0).getNominalValue(i));
                    // Class 'i' is considered de positive class, the rest of classes correspond to the negative one.
                    // Get the simple items.
                    ArrayList<Item> simpleItems = getSimpleItems(training, minSupp, i);
                    // sort items by growth rate
                    simpleItems.sort(null);
                    // gets all instances removing those itemset that not appear on simpleItems
                    ArrayList<Pair<ArrayList<Item>, Integer>> instances = getInstances(training, simpleItems, i);
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
                    if (patterns.isEmpty()) {
                        allPatterns.add(new ArrayList<>());
                    } else {
                        ArrayList<Pair<ArrayList<Item>, Integer>> inst = getInstances(training, simpleItems, i);
                        patterns = pruneEPs(patterns, inst);
                        allPatterns.add(patterns);
                    }
                }

                int sum = 0;
                for (ArrayList<Pattern> patterns : allPatterns) {
                    sum += patterns.size();
                }
                Main.setInfoLearnText("Mining finished. eJEPs found: " + sum);
            }
        } catch (IllegalActionException ex) {
            Main.setInfoLearnText(ex.getReason());
        }

    }

    @Override
    public void predict(InstanceSet test) {
        System.out.println("Que NO estoy hecho aun!");
    }

    public String toString() {
        return "UNSUPPORTED";
    }

    public void checkDataset() throws exceptions.IllegalActionException {
        for (int i = 0; i < Attributes.getInputNumAttributes(); i++) {
            if (Attributes.getAttribute(i).getType() != Attribute.NOMINAL) {
                throw new exceptions.IllegalActionException("ERROR: The dataset must contain only nominal attributes. Please, discretize the real ones.");
            }
        }

    }

    /**
     * It prunes the set of essential JEPs mined following by data class
     * coverage procedure.
     *
     * @param patterns
     * @param training
     * @return
     */
    private ArrayList<Pattern> pruneEPs(ArrayList<Pattern> patterns, ArrayList<Pair<ArrayList<Item>, Integer>> training) {
        // Sort the patterns by ranking.
        patterns.sort(new Comparator<Pattern>() {
            @Override
            public int compare(Pattern o1, Pattern o2) {
                if (o1.getSupport() > o2.getSupport()) {
                    return 1;
                } else if (o1.getSupport() < o2.getSupport()) {
                    return -1;
                } else if (o1.getItems().size() > o2.getItems().size()) {
                    return 1;
                } else if (o1.getItems().size() < o2.getItems().size()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        // Apply the data class covering procedure
        boolean allCovered = true;
        int counter = 0;
        ArrayList<Pattern> result = new ArrayList<>();
        boolean[] tokens = new boolean[training.size()];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = false;
        }

        do {
            boolean coverNew = false;
            allCovered = true;
            for (int i = 0; i < training.size(); i++) {
                if (patterns.get(counter).covers(training.get(i).getKey()) && !tokens[i]) {
                    coverNew = true;
                    tokens[i] = true;
                }
            }

            if (coverNew) {
                result.add(patterns.get(counter));
            }

            for (int i = 0; i < tokens.length && allCovered; i++) {
                if (!tokens[i]) {
                    allCovered = false;
                }
            }

            counter++;
        } while (!allCovered && counter < patterns.size());

        return result;

    }
}
