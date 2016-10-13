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
        for(int i = 0; i < root.getChildren().size(); i++){
            Tree node = root.getChildren().get(i);
        }
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
                int elements = (int) (sortedFrequency.size() * alpha);
                simpleItems.clear();
                simpleItems.addAll(sortedFrequency.subList(0, elements));
                sortedRatio.removeAll(simpleItems);
                simpleItems.addAll(sortedRatio);  
                // Sort instances
                instances.forEach((t) -> {
                    t.getKey().sort((o1, o2) -> {
                        int i1 = simpleItems.indexOf(o1);
                        int i2 = simpleItems.indexOf(o2);
                        if(i1 < i2) return 1;
                        else if(i1 > i2) return -1;
                        else return 0;
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
    
}
