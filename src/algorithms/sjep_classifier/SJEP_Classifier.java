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
package algorithms.sjep_classifier;

import framework.GUI.GUI;
import framework.GUI.Model;
import framework.exceptions.IllegalActionException;
import framework.items.Item;
import framework.items.NominalItem;
import framework.items.Pattern;
import framework.utils.Utils;
import framework.utils.cptree.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import keel.Dataset.Attributes;
import keel.Dataset.InstanceSet;

/**
 * A class that represents the classic SJEP-Classifier method that mines all
 * SJEPs with a support higher than a threshold
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class SJEP_Classifier extends Model {

    /**
     * The root of the CP-Tree
     */
    private CPTree tree;
    /**
     * The counts in D1 and D2 for each individual item
     */
    private HashMap<Item, Par> countsPerItem;
    /**
     * The support ratio for each individual item
     */
    private HashMap<Item, Double> supportRatioPerItem;
    /**
     * The bit string representation for those items that appear in D1. An "1"
     * at bit position k means that this Item appear in the transaction number k
     */
    private HashMap<Item, BigInteger> itemCountsForD1;
    /**
     * The bit string representation for those items that appear in D2. An "1"
     * at bit position k means that this Item appear in the transaction number k
     */
    private HashMap<Item, BigInteger> itemCountsForD2;
    /**
     * The priority queue where top-k positive patterns are stored.
     */
    private PriorityQueue<Pattern> topK_PosPatterns;

    /**
     * The minimum count for positive instances
     */
    private int minPosCount = 0;
    private int minNegCount = 0;
    private ArrayList<Pattern> SJEPs = new ArrayList<>();

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        try {

            Utils.checkDataset();
            tree = new CPTree();
            countsPerItem = new HashMap<>();
            supportRatioPerItem = new HashMap<>();
            itemCountsForD1 = new HashMap<>();
            itemCountsForD2 = new HashMap<>();

            int numClasses = training.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();
            // Mine for each class separately
            long t_ini = 0;

            if (numClasses == 2) {
                // NORMAL RUNNING
                // Retrieve training patterns
                ArrayList<Pattern> instances = Utils.generatePatterns(training, 0);

                // Fill the tree
                for (int k = 0; k < instances.size(); k++) {
                    Pattern p = instances.get(k);
                    if (p.getClase() == 0) {
                        minPosCount++;
                    } else {
                        minNegCount++;
                    }
                }
                minPosCount *= Float.parseFloat(params.get("Min Support"));
                minNegCount *= Float.parseFloat(params.get("Min Support"));

                // get the support ratio for each item
                getSupportRatioForItems(instances);

                for (int k = 0; k < instances.size(); k++) {
                    Pattern p = instances.get(k);
                    // Remove items with support ratio == 0
                    for (int j = 0; j < p.getItems().size(); j++) {
                        if (supportRatioPerItem.get(p.get(j)) == null) {
                            instances.get(k).drop(j);
                            j--;
                        }
                    }
                }

                for (Pattern p : instances) {
                    // Sort the patterns according to the support-ratio inverse ordering for efficiency
                    p.getItems().sort((o1, o2) -> {
                        double gr1 = supportRatioPerItem.get(o1);
                        double gr2 = supportRatioPerItem.get(o2);
                        if (gr1 > gr2) {
                            return 1;
                        } else if (gr1 < gr2) {
                            return -1;
                        } else {

                            return -1 * ((NominalItem) o1).compareTo(o2);

                        }
                    });
                    tree.insert(p, supportRatioPerItem);
                }
                t_ini = System.currentTimeMillis();
                // Mine the tree looking for SJEPs !
                mineTree(tree.getRoot(), new Pattern(new ArrayList<Item>(), 0), false);

            } else {
                //MULTICLASS - WITH ONE VS ALL BINARZATION
                System.out.println("EXECUTING SJEP-C WITH OVA: ");
                for (int i = 0; i < numClasses; i++) {
                    System.out.println("Mining for class: " + training.getAttributeDefinitions().getOutputAttribute(0).getNominalValue(i));
                    System.out.println("Converting the dataset...");
                    // Retrieve training patterns
                    ArrayList<Pattern> instances = Utils.generatePatterns(training, i);

                    // Fill the tree
                    for (int k = 0; k < instances.size(); k++) {
                        Pattern p = instances.get(k);
                        if (p.getClase() == 0) {
                            minPosCount++;
                        } else {
                            minNegCount++;
                        }
                    }
                    minPosCount *= Float.parseFloat(params.get("Min Support"));
                    minNegCount *= Float.parseFloat(params.get("Min Support"));

                    // get the support ratio for each item
                    getSupportRatioForItems(instances);

                    for (int k = 0; k < instances.size(); k++) {
                        Pattern p = instances.get(k);
                        // Remove items with support ratio == 0
                        for (int j = 0; j < p.getItems().size(); j++) {
                            if (supportRatioPerItem.get(p.get(j)) == null) {
                                instances.get(k).drop(j);
                                j--;
                            }
                        }
                    }
                    System.out.println("Creating the tree...");
                    for (Pattern p : instances) {
                        // Sort the patterns according to the support-ratio inverse ordering for efficiency
                        p.getItems().sort((o1, o2) -> {
                            double gr1 = supportRatioPerItem.get(o1);
                            double gr2 = supportRatioPerItem.get(o2);
                            if (gr1 > gr2) {
                                return 1;
                            } else if (gr1 < gr2) {
                                return -1;
                            } else {

                                return -1 * ((NominalItem) o1).compareTo(o2);

                            }
                        });
                        tree.insert(p, supportRatioPerItem);
                    }
                    System.out.println("Mining!");
                    t_ini = System.currentTimeMillis();
                    // Mine the tree looking for SJEPs !
                    mineTree(tree.getRoot(), new Pattern(new ArrayList<Item>(), i), true);
                    System.out.println("Mining took: " + (System.currentTimeMillis() - t_ini) / 1000d + " seconds.");
                    // Clean auxiliar variables for the next class computation
                    itemCountsForD1.clear();
                    itemCountsForD2.clear();
                    countsPerItem.clear();
                    supportRatioPerItem.clear();
                    tree.clear();
                    // filter obtained JEPs to get only those minimals

                }
            }

            System.out.println("Patterns mined: " + super.patterns.size() + "\n"
                    + "Execution time: " + (System.currentTimeMillis() - t_ini) / 1000d + " seconds.");
            
        } catch (IllegalActionException ex) {
            GUI.setInfoLearnTextError(ex.getReason());
        }
    }

    @Override
    public String[][] predict(InstanceSet test) {
        String[][] result = new String[4][test.getNumInstances()];
        result[0] = super.getPredictions(super.patterns, test);
        result[1] = super.getPredictions(super.patternsFilteredMinimal, test);
        result[2] = super.getPredictions(super.patternsFilteredMaximal, test);
        result[3] = super.getPredictions(super.patternsFilteredByMeasure, test);
        return result;
    }

    /**
     * Calculates the support ratio of the items to allow the sorting of the
     * CP-Tree NOTE: THIS ONLY CALCULATES THE GROWTH RATE FOR THE POSITIVE
     * CLASS! NOT THE REAL SUPPORT-RATIO!
     *
     * @param instances
     */
    public void getSupportRatioForItems(ArrayList<Pattern> instances) {
        // Get support ratio for the items (get counts)
        for (Pattern p : instances) {
            for (Item it : p.getItems()) {
                if (!countsPerItem.containsKey(it)) {
                    countsPerItem.put(it, new Par());
                }

                if (p.getClase() == 0) {
                    countsPerItem.get(it).D1++;
                } else {
                    countsPerItem.get(it).D2++;
                }
            }
        }
        // Get support ratio for the items (get support ratio)
        Set<Map.Entry<Item, Par>> entrySet = countsPerItem.entrySet();
        Iterator<Map.Entry<Item, Par>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Item, Par> next = iterator.next();
            double suppRatio;
            if (next.getValue().D1 < minPosCount && next.getValue().D2 < minNegCount) {
                suppRatio = 0;
            } else if ((next.getValue().D1 >= minPosCount && next.getValue().D2 == 0) || (next.getValue().D2 >= minPosCount && next.getValue().D1 == 0)) {
                suppRatio = Double.POSITIVE_INFINITY;
            } else {
                suppRatio = Math.max(((Integer) next.getValue().D1).doubleValue() / ((Integer) next.getValue().D2).doubleValue(),
                        ((Integer) next.getValue().D2).doubleValue() / ((Integer) next.getValue().D1).doubleValue());
            }

            if (suppRatio > 0) {
                // Put only those items with supportRatio > 0
                supportRatioPerItem.put(next.getKey(), suppRatio);
            }
        }
    }

    /**
     * Gets the bit string of each single item for each class. This string
     * represent an 1 at position k if the transaction k has that item for the
     * class, or 0 elsewhere.
     *
     * @param instances
     */
    public void getBitStrings(ArrayList<Pattern> instances) {
        Set<Item> keySet = countsPerItem.keySet();
        Iterator<Item> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            Item it = iterator.next();
            // set bits to zero
            BigInteger a = BigInteger.ZERO;
            itemCountsForD1.put(it, a);
            itemCountsForD2.put(it, a);
        }

        for (int i = 0; i < instances.size(); i++) {
            for (Item it : instances.get(i).getItems()) {
                if (instances.get(i).getClase() == 0) {
                    itemCountsForD1.put(it, itemCountsForD1.get(it).setBit(i));
                } else {
                    itemCountsForD2.put(it, itemCountsForD2.get(it).setBit(i));
                }
            }
        }
    }

    public void mineTree(Node node, Pattern alpha, boolean OVA) {
        // for all i in t.items
        for (int j = 0; j < node.getItems().size(); j++) {
            if (node.getItems().get(j).visited) {
                continue;
            }
            // if the subtree is not empty then merge
            if (node.getItems().get(j).getChild() != null) {
                if (!node.getItems().get(j).getChild().getItems().isEmpty()) {
                    node.merge(node.getItems().get(j).getChild(), supportRatioPerItem);
                }
            }
            Entry i = node.getItems().get(j);
            Pattern beta = alpha.clone();
            beta.add(i.getItem());

            // We are looking for patterns only on the positive class to allow multiclass problems.
            if (acceptPattern(beta, i.getCountD1(), i.getCountD2(), minPosCount)) {
                //beta.setClase(i);
                HashMap<String, Double> m = new HashMap<>();
                m.put("SUPP", ((Integer) i.getCountD1()).doubleValue());
                beta.setTra_measures(m);
                super.patterns.add(beta.clone());

            } else {
                if (acceptPattern(beta, i.getCountD2(), i.getCountD1(), minNegCount)) {
                    if (!OVA) { // If we are in OVA multiclass, we dont want patterns for the negative class, but we want the pruning.
                        beta.setClase(1);
                        HashMap<String, Double> m = new HashMap<>();
                        m.put("SUPP", ((Integer) i.getCountD2()).doubleValue());
                        beta.setTra_measures(m);
                        super.patterns.add(beta.clone());
                    }
                } else {
                    if (visitSubTree(beta, i) && i.getChild() != null) {
                        mineTree(i.getChild(), beta, OVA);
                    }
                }
            }

            i.setChild(null);
            //System.gc(); // Force garbage collector
        }
    }

    /**
     * Gets the pattern counts for D1 (or D2 if forD1 == false) using the bit
     * strings
     *
     * @param p
     * @param forD1
     * @return
     */
    public int getPatternCount(Pattern p, boolean forD1) {
        BigInteger a;
        if (forD1) {
            a = itemCountsForD1.get(p.get(0));
        } else {
            a = itemCountsForD2.get(p.get(0));
        }

        for (int i = 1; i < p.length(); i++) {
            if (forD1) {
                a = a.and(itemCountsForD1.get(p.get(i)));
            } else {
                a = a.and(itemCountsForD2.get(p.get(i)));
            }
        }
        return a.bitCount();
    }

    /**
     * The accept-pattern routine that accepts the inclusion of a top-k pattern
     * in the given result.
     *
     * @param beta The pattern to check
     * @param countD1 The counts for D1
     * @param countD2 The counts for D2
     * @param minCount The minimum count value
     * @param positiveClass {@code true} to look for incluse into the positive
     * class, {@code false} into the negative one.
     * @return
     */
    public boolean acceptPattern(Pattern beta, int countD1, int countD2, int minCount) {
        // check if the patter is a JEP
        return countD1 >= minCount && countD2 == 0;
    }

    /**
     * The visit-subtree routine. It checks whether a node subtree must be
     * visited or not.
     *
     * @param beta The pattern to check
     * @param entry The actual node
     * @return
     */
    public boolean visitSubTree(Pattern beta, Entry entry) {
        // check if child node has minimal counts
        return entry.getCountD1() >= minPosCount || entry.getCountD2() >= minNegCount;
    }

    private void filterMinimals() {
        patterns.sort((o1, o2) -> {
            if (o1.length() > o2.length()) {
                return 1;
            } else if (o1.length() < o2.length()) {
                return -1;
            } else {
                return 0;
            }
        });

        boolean[] marks = new boolean[patterns.size()];
        for (int i = 0; i < patterns.size(); i++) {
            Pattern p1 = patterns.get(i);
            for (int j = i + 1; j < patterns.size(); j++) {
                Pattern p2 = patterns.get(j);
                if (!marks[j] && p1.length() < p2.length()) {
                    if (p1.covers(p2)) { // if p1 covers p2 the p2 is not minimal. Remove it
                        marks[j] = true;
                    }
                }
            }
        }

        // retain those patterns with marks == false
        for (int i = 0; i < marks.length; i++) {
            if (!marks[i]) {
                SJEPs.add(patterns.get(i));
            }
        }
    }

}
