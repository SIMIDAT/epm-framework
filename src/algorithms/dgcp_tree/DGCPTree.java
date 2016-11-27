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

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        buildInitialTree(Utils.generatePatterns(training, 0), 0, 0);
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
        int minCounts = (int) minimumSupport * dataset.size();

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
        // Now, the DGCP-Tree is initialised
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

}
