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
package algorithms.tree_based_jep;

import framework.GUI.Model;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javafx.util.Pair;
import keel.Dataset.Attribute;
import keel.Dataset.Instance;
import keel.Dataset.InstanceSet;
import sun.misc.REException;
import framework.deprecated.Item;
import framework.deprecated.Pattern;
import framework.deprecated.Utils;

/**
 *
 * @author Ángel M. García-Vico (agvico@ujaen.es)
 * @version 1.0
 * @since JDK 1.8
 */
public class TreeBasedJEP extends Model {

    private Tree root;
    private String ordering;
    private float alpha;
    private int prune;

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        super.patterns = new ArrayList<>();
        super.patternsFilteredMinimal = new ArrayList<>();
        super.patternsFilteredMaximal = new ArrayList<>();
        int numClasses = training.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();
        ordering = params.get("Ordering");
        alpha = Float.parseFloat(params.get("Alpha"));
        prune = Integer.parseInt(params.get("Pattern Max Length"));

        // generate and mine the tree for each class
        long init_time = System.currentTimeMillis();
        for (int i = 0; i < numClasses; i++) {
            // Generate Tree for class i and mine patterns of this tree
            generateTree(training, ordering, i, alpha);
            mineTree(i, prune);
        }
        System.out.println("Removing duplicated patterns...");
        patterns = Utils.removeDuplicates(patterns);
        System.out.println("Execution time: " + (System.currentTimeMillis() - init_time) / 1000.0 + " seconds");
        System.out.println("Number of patterns: " + super.patterns.size());
    }

    @Override
    public String[][] predict(InstanceSet test) {
        String[][] result = new String[4][test.getNumInstances()];
        result[0] = getPredictions(super.patterns, test);
        result[1] = getPredictions(super.patternsFilteredMinimal, test);
        result[2] = getPredictions(super.patternsFilteredMaximal, test);
        result[3] = getPredictions(super.patternsFilteredByMeasure, test);
        return result;
    }

    /**
     * Gets the class predictions for the test instances.
     *
     * @param patterns
     * @param test
     * @return
     */
    public String[] getPredictions(ArrayList<framework.items.Pattern> patterns, InstanceSet test) {
        Attribute[] attributes = test.getAttributeDefinitions().getInputAttributes();
        ArrayList<String> predictions = new ArrayList<>();
        float[] clasContrib = new float[test.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues()];

        //For each test instance
        for (Instance inst : test.getInstances()) {
            for (int i = 0; i < clasContrib.length; i++) {
                clasContrib[i] = 0;
            }

            // Checks the patterns that covers the instance for each class, and sum its support
            for (framework.items.Pattern pat : patterns) {
                if (pat.covers(inst, attributes)) {
                    clasContrib[pat.getClase()] += pat.getTra_measures().get("SUPP");
                }
            }

            // The max value wins and it is the value predicted.
            predictions.add(test.getAttributeDefinitions().getOutputAttribute(0).getNominalValue(Utils.getIndexOfMaxValue(clasContrib)));
        }

        //return the array of predictions
        return predictions.toArray(new String[0]);
    }

    /**
     * Generates the tree that represents the dataset.
     *
     * @param training The training data
     * @param mode The mode to sort the items:
     * <ul>
     * <li>"frequency"</li>
     * <li>"ratio"</li>
     * <li>"ratioInverse"</li>
     * <li>"LPNC"</li>
     * <li>"MPPC"</li>
     * <li>"hibryd"</li>
     * </ul>
     * @param clas The class to mine
     * @param alpha The alpha value for "hibryd" mode.
     */
    private void generateTree(InstanceSet training, String mode, int clas, float alpha) {
        ArrayList<Item> simpleItems = Utils.getSimpleItems(training, 0, clas);
        ArrayList<Pair<ArrayList<Item>, Integer>> instances = Utils.getInstances(training, simpleItems, clas);
        for (int i = 0; i < simpleItems.size(); i++) {
            simpleItems.get(i).calculateProbabilities(training, "M");
        }

        // Order simple itemsets and instances by a given mode.
        switch (mode) {
            case "frequency":
                simpleItems.sort(Utils.frequency);
                instances.forEach((t) -> {
                    t.getKey().sort(Utils.frequency);
                });
                break;
            case "ratio":
                simpleItems.sort(Utils.ratio);
                instances.forEach((t) -> {
                    t.getKey().sort(Utils.ratio);
                });
                break;
            case "ratioInverse":
                simpleItems.sort(Utils.ratioInverse);
                instances.forEach((t) -> {
                    t.getKey().sort(Utils.ratioInverse);
                });
                break;
            case "LPNC":
                simpleItems.sort(Utils.LPNC);
                instances.forEach((t) -> {
                    t.getKey().sort(Utils.LPNC);
                });
                break;
            case "MPPC":
                simpleItems.sort(Utils.MPPC);
                instances.forEach((t) -> {
                    t.getKey().sort(Utils.MPPC);
                });
                break;
            case "hybrid":
                // Gets items ordenred by frequency and ratio.
                ArrayList<Item> sortedFrequency = (ArrayList<Item>) simpleItems.clone();
                ArrayList<Item> sortedRatio = (ArrayList<Item>) simpleItems.clone();
                sortedFrequency.sort(Utils.frequency);
                sortedRatio.sort(Utils.ratio);

                //gets the alpha simple items from frequency
                int elements = (int) (sortedRatio.size() * alpha);
                simpleItems.clear();
                simpleItems.addAll(sortedRatio.subList(0, elements));
                sortedFrequency.removeAll(simpleItems);
                simpleItems.addAll(sortedFrequency);
                // Sort instances
                instances.forEach((t) -> {
                    t.getKey().sort((o1, o2) -> {
                        int i1 = simpleItems.indexOf(o1);
                        int i2 = simpleItems.indexOf(o2);
                        if (i1 > i2) {
                            return 1;
                        } else if (i1 < i2) {
                            return -1;
                        } else {
                            return 0;
                        }
                    });
                });
        }

        // Now that we have the instances sorted, generate the tree
        Tree.setSimpleItems(simpleItems);
        Tree.initializeNodeLinks(simpleItems.size());
        root = new Tree(null, 0, 2, null);
        root.setRoot(true);
        instances.forEach((inst) -> {
            // Insert the item from the root.
            root.insert_tree(inst.getKey(), inst.getValue(), simpleItems);
        });
    }

    /**
     * Public method to mine the tree.
     * @param clas The class to look for patterns
     * @param threshold  The threshold to prune patterns (<= 0 means no prune)
     */
    public void mineTree(int clas, int threshold) {
        for (Tree componentTree : root.getChildren()) {
            // mine the component tre
            mineTree(componentTree, new Pattern(new ArrayList<Item>(), clas), 0, threshold);
            // When mined the component tree, apply the "relocate_branches" procedure.
            relocate_branches(componentTree);
        }
    }

    /**
     * Private and recursive method to mine the tree for JEPs
     * @param node The actual node of the tree to mine
     * @param p The actual pattern
     * @param clas The class to look for patterns
     * @param threshold The threshold to prune the tree (<= 0 means no prune)
     */
    private void mineTree(Tree node, Pattern p, int clas, int threshold) {
        if (node != null && (p.getItems().size() <= threshold || threshold <= 0)) {
            node.visited();
            int negativeClass = clas == 0 ? 1 : 0;
            Tree CTRoot = getFirstNode(node);
            Pattern aux = p.clone();
            // Add the node's item to the possible JEP
            aux.add(node.getItem());
            // Check if pattern p is a potential JEP
            if (node.getCount(clas) != 0 && node.getCount(negativeClass) == 0) {
                // this is a JEP. Gets negative instances from this one.
                if (!node.getItem().equals(CTRoot.getItem())) {
                    ArrayList<Pattern> negativeInstances = findNegativeInstances(node, CTRoot.getItem(), negativeClass);
                    // Now, apply border_Diff if neccesary
                    if (negativeInstances.isEmpty()) {
                        // If there are no such negative transactions, adds as JEP a pattern with the first and last elements.
                        ArrayList<Item> items = new ArrayList<>();
                        items.add(aux.getItems().get(0));
                        items.add(aux.getItems().get(aux.getItems().size() - 1));
                        super.patterns.add(Utils.castToNewPatternFormat(new Pattern(items, aux.getClase())));
                    } else {
                        // apply border_diff
                        Pattern borderDiff = borderDiff(aux, negativeInstances);
                        if (!borderDiff.getItems().isEmpty()) {
                            super.patterns.add(Utils.castToNewPatternFormat(borderDiff));
                        }
                    }
                } else {
                    Pattern p1 = new Pattern(new ArrayList<Item>(), clas);
                    p1.add(node.getItem());
                    super.patterns.add(Utils.castToNewPatternFormat(p1));
                }

            }

            // Recursive call for each node's child
            for (Tree child : node.getChildren()) {
                mineTree(child, aux, clas, threshold);
            }

        }

    }

    /**
     * Finds the negative instances related to {@code target} node, i.e., shares the same base node and the same root.
     * @param target
     * @param root
     * @param clas
     * @return 
     */
    private ArrayList<Pattern> findNegativeInstances(Tree target, Item root, int clas) {
        Item it = target.getItem();
        ArrayList<Tree> node_link = Tree.getNode_link();
        int index = Tree.getSimpleItems().indexOf(it);
        ArrayList<Pattern> result = new ArrayList<>();

        Tree aux = target.getNextEqual();
        while (aux != null) {
            if (!aux.isVisited()) {
                // Search for instances using side links
                if (getFirstNode(aux).getItem().equals(root) && !aux.equals(target)) {
                    // If the node we are examining shares the root, is a negative instance.
                    Pattern p = new Pattern(new ArrayList<Item>(), clas);
                    Tree aux2 = aux;
                    while (!aux2.getParent().isRoot()) {
                        p.add(aux2.getItem());
                        aux2 = aux2.getParent();
                    }
                    // add the reversed pattern
                    result.add(p.reverse());
                } else {
                    // If the next does not share the same root, it means that
                    // it is on another component tree, so, consecuent equals nodes not share this root anymore. So, prune.
                    break;
                }
            }
            aux = aux.getNextEqual();
        }

        return result;
    }

   

    /**
     * Gets the root node of the node's component tree.
     *
     * @param node
     * @return
     */
    private Tree getFirstNode(Tree node) {
        Tree next = node;

        while (!next.getParent().isRoot()) {
            next = next.getParent();
        }

        return next;
    }

    
    /**
     * Applies the border-diff procedure, i.e., it returns a patterns with the items in {@code target} not present in target.
     * @param target
     * @param border
     * @return 
     */
    private Pattern borderDiff(Pattern target, ArrayList<Pattern> border) {

        Pattern result = new Pattern(new ArrayList<Item>(), target.getClase());

        //Join with the first border
        result = result.merge(target.difference(border.get(0)));

        //Join with other borders
        for (int i = 1; i < border.size(); i++) {
            result = result.merge(target.difference(border.get(i)));
        }

        return result;

    }//

    /**
     * Apply the relocate branches procedure. This is, for each node of a
     * component tree, sum to the next equal item their counts.
     *
     * @param node
     */
    private void relocate_branches(Tree node) {
        if (node != null) {
            Tree nextEqual = node.getNextEqual();
            if (nextEqual != null) {
                nextEqual.setCount(0, nextEqual.getCount(0) + node.getCount(0));
                nextEqual.setCount(1, nextEqual.getCount(1) + node.getCount(1));
            }
            for (Tree child : node.getChildren()) {
                relocate_branches(child);
            }
        }
    }

}
