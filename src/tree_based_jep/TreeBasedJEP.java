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
package tree_based_jep;

import epm_algorithms.Model;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import javafx.util.Pair;
import keel.Dataset.InstanceSet;
import utils.Item;
import utils.Pattern;
import utils.Utils;

/**
 *
 * @author Ángel M. García-Vico (agvico@ujaen.es)
 * @version 1.0
 * @since JDK 1.8
 */
public class TreeBasedJEP extends Model {

    private Tree root;

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        generateTree(training, "frequency", 0, (float) 0.5);
        mineTree();
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
            case "hibryd":
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
                        if (i1 < i2) {
                            return 1;
                        } else if (i1 > i2) {
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
            root.insert_tree(inst.getKey(), inst.getValue());
        });
    }

    public void mineTree() {
        mineTree(root, new Pattern(null, 0), 0);
    }

    private void mineTree(Tree node, Pattern p, int clas) {
        int negativeClass = clas == 0 ? 1 : 0;
        Tree CTRoot = getFirstNode(node);
        Pattern aux = p.clone();

        // for each node's children
        for (Tree target : node.getChildren()) {
            aux.getItems().add(target.getItem());
            // Check if pattern p is a potential JEP
            if (target.getCount(clas) != 0 && target.getCount(negativeClass) == 0) {
                // this is a JEP. Gets negative instances from this one.
                ArrayList<Pattern> negativeInstances = findNegativeInstances(target, CTRoot.getItem(), negativeClass);
                // Now, apply border_Diff if neccesary
                if (negativeInstances.isEmpty()) {
                    super.patterns.add(aux);
                } else {
                    // apply border_diff

                }
            }
        }
    }

    private ArrayList<Pattern> findNegativeInstances(Tree target, Item root, int clas) {
        if (target.getNextEqual() == null) {
            return null;
        }

        ArrayList<Pattern> result = new ArrayList<>();
        Tree next = target.getNextEqual();
        boolean end = true;
        // create the possible JEP
        do {
            Tree aux = next;
            end = true;
            Pattern p = new Pattern(null, clas);
            p.getItems().add(next.getItem());
            while ((next = next.getParent()) != null) {
                p.getItems().add(next.getItem());
            }

            if (p.getItems().get(p.getItems().size() - 1).equals(root)) {
                // If the pattern obtained share the same component tree, add to the list of negative instances. But first, we need to reverse it
                result.add(p.reverse());
                end = false;
            }

        } while (!end);
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

        while ((next = next.getParent()) != null) {
        }

        return next;
    }

    private Pattern borderDiff(Pattern target, ArrayList<Pattern> border) {

        Pattern result = new Pattern(new ArrayList<Item>(), 0);

        //Join with the first border
        result = result.merge(target.diference(border[0]));
        Pattern

        //Join with other borders
        for (int i = 1; i < border.length; i++) {
            result = result.merge(target.diference(border[i]));
        }

        return result;

    }//

}
