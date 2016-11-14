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
package algorithms.bcep;

import framework.GUI.Model;
import java.util.HashMap;
import keel.Dataset.InstanceSet;
import keel.Dataset.Attribute;
import framework.GUI.GUI;
import framework.exceptions.IllegalActionException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;
import keel.Dataset.Attributes;
import keel.Dataset.Instance;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class BCEP_Model extends Model implements Serializable {

    float[] classProbabilities;
    ArrayList<Item> simpleItems;
    float minSupp;

    public BCEP_Model() {
        super.setFullyQualifiedName("bcep.BCEP_Model");
    }

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {

        try {
            // First, check if the dataset has the correct format.
            checkDataset();

            int countD1 = 0;
            int countD2 = 0;
            minSupp = Float.parseFloat(params.get("Minimum support"));
            float minGR = Float.parseFloat(params.get("Minimum GrowthRate"));
            ArrayList<String> classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());
            classProbabilities = new float[classes.size()];
            for (int i = 0; i < classProbabilities.length; i++) {
                classProbabilities[i] = 0;
            }
            // Gets the count of examples for each class to calculate the growth rate.
            for (int i = 0; i < training.getNumInstances(); i++) {
                if (classes.indexOf(training.getInstance(i).getOutputNominalValues(0)) == 0) {
                    countD1++;
                } else {
                    countD2++;
                }
            }

            //Gets class probabilities
            for (Instance inst : training.getInstances()) {
                classProbabilities[inst.getOutputNominalValuesInt(0)]++;
            }
            for (int i = 0; i < classProbabilities.length; i++) {
                classProbabilities[i] /= (float) training.getNumInstances();
            }

            if (Attributes.getOutputAttribute(0).getNumNominalValues() <= 2) {
                // get simple itemsets to perform the ordering of the items and filter by gorwth rate
                // Class '0' is considered as positive
                simpleItems = Utils.getSimpleItems(training, minSupp, 0);
                // Calculate the probabilites of the items (We use M-estimate)
                for (Item it : simpleItems) {
                    it.calculateProbabilities(training, "M");
                }
                // sort items by growth rate
                simpleItems.sort(null);
                // gets all instances removing those itemset that not appear on simpleItems
                ArrayList<Pair<ArrayList<Item>, Integer>> instances = Utils.getInstances(training, simpleItems, 0);
                for (int i = 0; i < instances.size(); i++) {
                    // sort each arraylist of items
                    instances.get(i).getKey().sort(null);
                }

                //System.out.println("Loading the CP-Tree...");
                // Create the CP-Tree
                CPTree tree = new CPTree(countD1, countD2);
                // Add the instances on the CP-Tree
                for (Pair<ArrayList<Item>, Integer> inst : instances) {
                    tree.insertTree(inst.getKey(), inst.getValue());
                }

                // System.out.println("Mining SJEPs...");
                // Perform mining
                ArrayList<Pattern> mineTree = tree.mineTree(minSupp);

                ArrayList<Pair<ArrayList<Item>, Integer>> inst = Utils.getInstances(training, simpleItems, 0);
                for (Pattern pat : mineTree) {
                    pat.calculateMeasures(training);
                }
                ArrayList<Pattern> aux = new ArrayList<>();
                // Filter the patterns by minimum growth rate and support
                for (int i = 0; i < mineTree.size(); i++) {
                    if (mineTree.get(i).getGrowthRate() > Float.parseFloat(params.get("Minimum GrowthRate"))
                            && mineTree.get(i).getSupp() >= minSupp) {
                        aux.add(mineTree.get(i));
                    }
                }
                aux = pruneEPs(aux, inst);
                /**/ // CONVERT bcep.Pattern to utils.Pattern and set Model.patterns
                for (Pattern p : aux) {
                    framework.items.Pattern pat = Utils.castToNewPatternFormat(p);
                    super.patterns.add(pat);
                }
                // WE CHANGE THE DEFINITION OF PATTERN CLASS!

            } else {
                ArrayList<ArrayList<Pattern>> allPatterns;
                setPatterns(new ArrayList<>());
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
                    ArrayList<Item> simpleItems = Utils.getSimpleItems(training, minSupp, i);
                    // Calculate the probabilites of the items (We use M-estimate)
                    for (Item it : simpleItems) {
                        it.calculateProbabilities(training, "M");
                    }
                    // sort items by growth rate
                    simpleItems.sort(null);
                    // gets all instances removing those itemset that not appear on simpleItems
                    ArrayList<Pair<ArrayList<Item>, Integer>> instances = Utils.getInstances(training, simpleItems, i);
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
                    ArrayList<Pattern> p = new ArrayList<>();
                    while (it.hasNext()) {
                        Pattern next = (Pattern) it.next();
                        if (next.getClase() != 0) {
                            //it.remove();
                        } else {
                            next.setClase(i);
                            p.add(next);
                        }
                    }
                    patterns.clear();
//                    ArrayList<Pattern> aux = (ArrayList< Pattern>) patterns.clone();
//                    patterns.clear();
//                    for (int j = 0; j < aux.size(); j++) {
//                        if (aux.get(j).getGrowthRate() > Float.parseFloat(params.get("Minimum GrowthRate"))
//                                && aux.get(j).getSupp() >= minSupp) {
//                            patterns.add(aux.get(j));
//                        }
//                    }
                    if (p.isEmpty()) {
                        allPatterns.add(new ArrayList<>());
                    } else {
                        //ArrayList<Pair<ArrayList<Item>, Integer>> inst = getInstances(training, simpleItems, i);
                        patterns = pruneEPs(p, instances);
                        allPatterns.add(patterns);
                    }
                }

                int sum = 0;

                for (ArrayList<Pattern> pattern : allPatterns) {
                    sum += pattern.size();
                    if (!pattern.isEmpty()) {
                        for (Pattern pat : pattern) {
                            /**/ // The same conversion of patterns here
                            getPatterns().add(Utils.castToNewPatternFormat(pat));
                        }
                    }
                }

                // calculate measures of training
                // GUI.setInfoLearnText("Mining finished. eJEPs found: " + sum);
            }

//            for (items.Pattern pat : getPatterns()) {
//                pat.calculateMeasures(training);
//            }
        } catch (IllegalActionException ex) {
            Logger.getLogger(BCEP_Model.class.getName()).log(Level.SEVERE, null, ex);
            GUI.setInfoLearnTextError(ex.getReason());
        } catch (Exception ex) {
            Logger.getLogger(BCEP_Model.class.getName()).log(Level.SEVERE, null, ex);
            GUI.setInfoLearnTextError("ERROR: Excepcion: " + ex.toString());
        }

    }

    @Override
    public String[][] predict(InstanceSet test) {
        String[][] preds = null;
        try {

            String[] predictionsNoFilter = makePredictions(test, Utils.castToOldatternFormat(getPatterns()));
            String[] predictionsFilterGlobal = null;
            String[] predictionsFilterClass = null;

            if (getPatternsFilteredMinimal() != null) {
                predictionsFilterGlobal = makePredictions(test, Utils.castToOldatternFormat(getPatternsFilteredMinimal()));
            }
            if (getPatternsFilteredMaximal() != null) {
                predictionsFilterClass = makePredictions(test, Utils.castToOldatternFormat(getPatternsFilteredMaximal()));
            }

            String[][] preds1 = {predictionsNoFilter, predictionsFilterGlobal, predictionsFilterClass};
            return preds1;
        } catch (Exception ex) {
            Logger.getLogger(BCEP_Model.class.getName()).log(Level.SEVERE, null, ex);
            GUI.setInfoLearnTextError("ERROR: Excepcion: " + ex.toString());
        }
        return preds;

    }

    @Override
    public String toString() {
        return "UNSUPPORTED";
    }

    /**
     * Checks if the dataset is processable by the method, i.e. it checks if all
     * its attributes are nominal.
     *
     * @throws framework.exceptions.IllegalActionException
     */
    public void checkDataset() throws framework.exceptions.IllegalActionException {
        for (int i = 0; i < Attributes.getInputNumAttributes(); i++) {
            if (Attributes.getAttribute(i).getType() != Attribute.NOMINAL) {
                throw new framework.exceptions.IllegalActionException("ERROR: The dataset must contain only nominal attributes. Please, discretize the real ones.");
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
        // Sort the patterns by ranking (in ASCENDING ORDER)
        if(patterns.isEmpty()) return new ArrayList<>();
        patterns.sort((o1, o2) -> {
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
        });

        // Apply the data class covering procedure
        boolean allCovered = true;
        int counter = patterns.size() - 1;
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

            counter--;
        } while (!allCovered && counter >= 0);

        return result;

    }

    /**
     * Gets the next pattern of the candidate patterns.
     *
     * @param covered The list of covered items
     * @param B The sets of candidates
     * @return A Pattern.
     */
    public Pattern next(ArrayList<Item> covered, ArrayList<Pattern> B) {
        // Z = {s in B && |s - covered| >= 1}
        if (B.size() == 1) {
            return B.get(0);
        } else if (B.size() == 0) {
            return null;
        } else {
            ArrayList<Pattern> Z = new ArrayList<>();
            for (Pattern pat : B) {
                // fill Z
                Pattern copy = pat.clone();
                copy.getItems().removeAll(covered);
                if (!copy.getItems().isEmpty()) {
                    Z.add(pat);
                }
            }

            if (Z.isEmpty()) {
                return null;
            } else if (Z.size() == 1) {
                return Z.get(0);
            } else {
                // Now, sort Z in the order specified (ASCENDING ORDER)
                Z.sort((Pattern o1, Pattern o2) -> {
                    // Sort by strength
                    if (o1.getStrength() > o2.getStrength()) {
                        return 1;
                    } else if (o1.getStrength() < o2.getStrength()) {
                        return -1;
                    } else // Compare lengths
                    {
                        if (o1.getItems().size() > o2.getItems().size()) {
                            return -1;
                        } else if (o1.getItems().size() < o2.getItems().size()) {
                            return 1;
                        } else {
                            // Compare the number of new covered items.
                            Pattern copy1 = o1.clone();
                            Pattern copy2 = o2.clone();
                            copy1.getItems().removeAll(covered);
                            copy2.getItems().removeAll(covered);
                            if (copy1.getItems().size() > copy2.getItems().size()) {
                                return 1;
                            } else if (copy1.getItems().size() < copy2.getItems().size()) {
                                return -1;
                            } else {
                                return 0;
                            }
                        }
                    }
                });
            }

            // Once the list is sorted, return the LAST element (the better)
            return Z.get(Z.size() - 1);
        }
    }

    /**
     * It makes the predictions for a set of items.
     *
     * @param testInstances
     * @param patterns
     * @return
     */
    // for each instances on the test set
    private String[] makePredictions(InstanceSet test, ArrayList<Pattern> patterns) {
        String[] predictions = new String[test.getNumInstances()];
        ArrayList<Item> simpleItems = null;
        ArrayList<Pair<ArrayList<Item>, Integer>> testInstances = null;
        for (int i = 0; i < test.getNumInstances(); i++) {

            ArrayList<Item> covered = new ArrayList<>();
            ArrayList<Item> numerator = new ArrayList<>();
            ArrayList<Item> denominator = new ArrayList<>();
            ArrayList<Pattern> B = new ArrayList<>();
            // First, get the set of patterns that covers the example.
            for (Pattern p : patterns) {
                simpleItems = Utils.getSimpleItems(test, minSupp, p.getClase());
                testInstances = Utils.getInstances(test, simpleItems, p.getClase());
                for(Item it : simpleItems){
                    it.calculateProbabilities(test, "M");
                }
                if (p.covers(testInstances.get(i).getKey())) {
                    B.add(p);
                }
            }

            boolean allCovered = false;

            do {
                Pattern next = next(covered, B);
                if (next != null) {
                    // numerator = numerator U Bi
                    for (Item it : next.getItems()) {
                        if (!numerator.contains(it)) {
                            numerator.add(it);
                        }
                    }
                    // denominator = denominator U {Bi Intersect covered}
                    Pattern clone = next.clone();
                    clone.getItems().retainAll(covered); // Intersection.
                    // Union with denominator
                    for (Item it : clone.getItems()) {
                        if (!denominator.contains(it)) {
                            denominator.add(it);
                        }
                    }

                    // covered = covered U Bi
                    for (Item it : next.getItems()) {
                        if (!covered.contains(it)) {
                            covered.add(it);
                        }
                    }

                    B.remove(next);
                    // Check if all items are covered
                    if (covered.size() >= testInstances.get(i).getKey().size() || B.isEmpty()) {
                        allCovered = true;
                    }
                } else {
                    allCovered = true;

                }
            } while (!allCovered);

            // Calculate the probability of each class
            float maxProb = -1;
            int indexClass = -1;
            for (int j = 0; j < classProbabilities.length; j++) {
                float productNumerator = 1;
                float productDenominator = 1;
                for (Item it : numerator) {
                    productNumerator *= simpleItems.get(simpleItems.indexOf(it)).getProbabilityForClass(j);
                }
                for (Item it : denominator) {
                    productDenominator *= simpleItems.get(simpleItems.indexOf(it)).getProbabilityForClass(j);
                }
                float prob = classProbabilities[j] * (productNumerator / productDenominator);
                if (prob > maxProb) {
                    maxProb = prob;
                    indexClass = j;
                }
            }

            predictions[i] = Attributes.getOutputAttribute(0).getNominalValue(indexClass);
        }
        return predictions;
    }

}
