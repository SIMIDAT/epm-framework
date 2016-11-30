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
package algorithms.dgcp_tree;

import framework.GUI.Model;
import framework.items.Item;
import framework.items.Pattern;
import framework.utils.Utils;
import framework.utils.bsc_tree.BSCTree;
import framework.utils.cptree.Par;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javafx.util.Pair;
import javax.print.attribute.HashAttributeSet;
import keel.Dataset.InstanceSet;

/**
 * Class that represents a DGCP-Tree structure to dinamically mine SJEPs
 *
 * @see Liu, Q., Shi, P., Hu, Z., & Zhang, Y. (2014). A novel approach of mining
 * strong jumping emerging patterns based on BSC-tree. International Journal of
 * Systems Science, 45(3), 598-615.
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class DGCPTree extends Model {

    /**
     * The root node of the DGCP-Tree
     */
    Node root;

    /**
     * The support ratio of the items.
     */
    HashMap<Item, Double> supportRatio;

    /**
     * The bit strings for each item on the positive class.
     */
    HashMap<Item, String> bitStringsPos;
    /**
     * The bit strings for each item on the negative class.
     */
    HashMap<Item, String> bitStringsNeg;

    /**
     * It stores the items that are covered by another one in Dn
     */
    HashMap<Item, ArrayList<Item>> coverDn;

    /**
     * The path code for the positive dataset for each single item
     */
    HashMap<Item, BSCTree> pathCodeDp;

    /**
     * The path code for the negative dataset for each single item.
     */
    HashMap<Item, BSCTree> pathCodeDn;

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        double minSupport = Double.parseDouble(params.get("Min Support"));
        int minCounts = ((Double) (minSupport * training.getNumInstances())).intValue();
        long t_ini = System.currentTimeMillis();
        int numClasses = training.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();
        for (int i = 0; i < numClasses; i++) {
            System.out.println("Mining Class: " + training.getAttributeDefinitions().getOutputAttribute(0).getNominalValue(i));
            buildInitialTree(Utils.generatePatterns(training, i), minSupport, i);
            mineGrowingTree(root, minCounts, new Pattern(new ArrayList<Item>(), i));
        }
        System.out.println("Mining Time: " + ((System.currentTimeMillis() - t_ini) / 1000.0) + " seconds");
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
     * It builds the initial DGCP-Tree
     *
     * @param dataset The dataset where the class of each Pattern is 0 for the
     * possitive class and 1 for the negative one.
     * @param minimumSupport The minimum support for patterns. A number in
     * [0,1].
     */
    public void buildInitialTree(ArrayList<Pattern> dataset, double minimumSupport, int clase) {
        // it stores the BSC-Trees for positive (key) and negative (value) class for each item.
        HashMap<Item, Pair<BSCTree, BSCTree>> items = new HashMap<>();
        // it stores the counts for calculate the support-ratio of each item.
        HashMap<Item, Par> counts = new HashMap<>();

        // Initialise class variables.
        bitStringsNeg = new HashMap<>();
        bitStringsPos = new HashMap<>();
        supportRatio = new HashMap<>();
        this.pathCodeDn = new HashMap<>();
        this.pathCodeDp = new HashMap<>();
        this.coverDn = new HashMap<>();
        int minCounts = ((Double) (minimumSupport * dataset.size())).intValue();

        // Split the dataset into Dp and Dn, and calculate the counts for the supportRatio of each item.
        ArrayList<Pattern> Dp = new ArrayList<>();
        ArrayList<Pattern> Dn = new ArrayList<>();
        for (Pattern p : dataset) {
            if (p.getClase() == 0) {
                Dp.add(p);
            } else {
                Dn.add(p);
            }

            for (Item it : p.getItems()) {
                if (!counts.containsKey(it)) {
                    counts.put(it, new Par());
                }

                if (p.getClase() == 0) {
                    counts.get(it).D1++;
                } else {
                    counts.get(it).D2++;
                }
            }
        }

        // Calculates the support ratio of each item
        Set<Map.Entry<Item, Par>> entrySet = counts.entrySet();
        Iterator<Map.Entry<Item, Par>> iterator = entrySet.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Item, Par> next = iterator.next();
            double suppRatio;
            if (next.getValue().D1 == 0 && next.getValue().D2 == 0) {
                suppRatio = 0;
            } else if ((next.getValue().D1 != 0 && next.getValue().D2 == 0)) {
                suppRatio = Double.POSITIVE_INFINITY;
            } else {
                suppRatio = ((Integer) next.getValue().D1).doubleValue() / ((Integer) next.getValue().D2).doubleValue();
            }
            supportRatio.put(next.getKey(), suppRatio);
            bitStringsPos.put(next.getKey(), createZeroString(dataset.size()));
            bitStringsNeg.put(next.getKey(), createZeroString(dataset.size()));
        }

        // Calculates the bit string of each item. 
        for (int i = 0; i < dataset.size(); i++) {
            for (Item it : dataset.get(i).getItems()) {
                if (dataset.get(i).getClase() == 0) {
                    char[] charArray = bitStringsPos.get(it).toCharArray();
                    charArray[i] = '1';
                    bitStringsPos.put(it, String.valueOf(charArray));
                } else {
                    char[] charArray = bitStringsNeg.get(it).toCharArray();
                    charArray[i] = '1';
                    bitStringsNeg.put(it, String.valueOf(charArray));
                }
            }
        }

        // Calculate the BSC-Tree of each item and add it to an auxiliar node
        iterator = entrySet.iterator();
        root = new Node(); // initialises the root
        root.asRoot();
        Node aux = new Node();
        while (iterator.hasNext()) {
            Map.Entry<Item, Par> next = iterator.next();
            BSCTree positive = new BSCTree(bitStringsPos.get(next.getKey()));
            BSCTree negative = new BSCTree(bitStringsNeg.get(next.getKey()));
            // If the support of the item  in the positive class is greater then the threshold, add it to the auxiliar node
            if (positive.getCounts() >= minCounts && negative.getCounts() != dataset.size()) {
                Node child = new Node();
                child.setItem(next.getKey());
                child.setPcArrNeg(negative);
                child.setPcArrPos(positive);
                aux.addChild(child);
                this.pathCodeDp.put(next.getKey(), positive);
                this.pathCodeDn.put(next.getKey(), negative);
            }
        }

        // Now, we have Lp (here, the childs of auxiliar node) filled, now sort according the support-ratio descending order 
        aux.sortChilds(supportRatio);

        // Generate the childs of root and the first 1-length SJEPs
        for (int i = 0; i < aux.numChilds(); i++) {
            if (aux.getChild(i).getPcArrNeg().getCounts() == 0) {
                // Generate a SJEP of length 1.
                ArrayList<Item> it = new ArrayList<>();
                it.add(aux.getChild(i).getItem());
                Pattern sjep = new Pattern(it, clase);
                this.patterns.add(sjep);
            } else {
                // Create the child. Now, the child are sorted, thus, we dont need to re-sort again.
                root.addChild(aux.getChild(i));
            }
        }

        // Get the set of coveredDn values
        for (int i = 0; i < root.numChilds(); i++) {
            ArrayList<Item> cov = new ArrayList<>();
            for (int j = 0; j < root.numChilds(); j++) {
                if (j != i) {
                    if (root.getChild(i).getPcArrNeg().covers(root.getChild(j).getPcArrNeg())) {
                        cov.add(root.getChild(j).getItem());
                    }
                }
            }
            coverDn.put(root.getChild(i).getItem(), cov);
        }
        // Now, the DGCP-Tree is initialised
    }

    /**
     * Recursive function that mines and grows the DGCP-Tree looking for SJEPs
     *
     * @param T A subtree of the DGCP- Tree
     * @param minSupport The minimum support threshold. NOTE: The support here
     * is measured by COUNTS, not support.
     * @param prefix A pattern prefix.
     */
    public void mineGrowingTree(Node T, int minSupport, Pattern prefix) {
        // for each child node of T
        for (int i = 0; i < T.getChilds().size(); i++) {
            Node N = T.getChild(i);
            Pattern prefixN = prefix.clone();
            prefixN.add(N.getItem());
            int suppn = support(prefixN,true);
            if (N.getPcArrPos().getCounts() >= minSupport) {
                // Now, for each right sibling of N, S do
                for (int j = i + 1; j < T.getChilds().size(); j++) {
                    Node S = T.getChild(j);
                    Pattern prefixNS = prefixN.clone();
                    prefixNS.add(S.getItem());
                    //if S.item not in coverDn(N.item) and N.item not in coverDn(S.item)
                    if (!coverDn.get(N.getItem()).contains(S.getItem())
                            && !coverDn.get(S.getItem()).contains(N.getItem())
                            && support(prefixNS, true) >= minSupport && support(prefixNS, false) < minSupport) {
                        // The pattern is a possible JEP. Check if it is a JEP
                        if (support(prefixNS, false) == 0) {
                            // Is a SJEP !!
                            this.patterns.add(prefixNS);
                        } else {
                            // clone S in S''
                            Node S_Prime = S.clone();
                            // modify the path codes by the and operation of BSC-Tree
                            S_Prime.setPcArrPos(N.getPcArrPos().And(this.pathCodeDp.get(S.getItem())));
                            S_Prime.setPcArrNeg(N.getPcArrNeg().And(this.pathCodeDn.get(S.getItem())));
                            // add S' into T as a child node of N. (Obeying the order)
                            N.addChild(S_Prime);
                            N.sortChilds(supportRatio);
                        }
                    }
                }
                // Perfoms the recursive call
                mineGrowingTree(N, minSupport, prefixN);
            }
            // Prune subtree N from T
            N = null;
        }
    }

    /**
     * Generates a string of length {@code length} to be used to represent the
     * bit string representation.
     *
     * @param length
     * @return
     */
    private String createZeroString(int length) {
        String result = "";
        for (int i = 0; i < length; i++) {
            result += "0";
        }
        return result;
    }

    /**
     * It calculates the support (in COUNTS) of a given pattern by perform the
     * ANDing between the BSC-Trees of each single item that appears in the
     * pattern.
     *
     * @param X
     * @param positiveDataset
     * @return
     */
    private int support(Pattern X, boolean positiveDataset) {
        ArrayList<BSCTree> trees = new ArrayList<>();
        BSCTree first;
        if (positiveDataset) {
            first = this.pathCodeDp.get(X.get(0));
        } else {
            first = this.pathCodeDn.get(X.get(0));
        }
        // Get individual path codes for each item
        for (int i = 1; i < X.length(); i++) {
            Item it = X.get(i);
            if (positiveDataset) {
                trees.add(this.pathCodeDp.get(it));
            } else {
                trees.add(this.pathCodeDn.get(it));
            }
        }
        // Perfom the tree anding for each item
        return first.treeANDing(trees);
    }

}
