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
package algorithms.topk;

import framework.GUI.Model;
import framework.items.Item;
import framework.items.Pattern;
import framework.utils.Utils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import keel.Dataset.InstanceSet;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class TopK extends Model {

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
     * The priority queue where top-k negative patterns are stored.
     */
    private PriorityQueue<Pattern> topK_NegPatterns;
    /**
     * The minimum count for positive instances
     */
    private int minPosCount = 0;
    /**
     * The minimum count for negative instances
     */
    private int minNegCount = 0;

    private int collectedNegPatterns = 0;
    private int collectedPosPatterns = 0;

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        tree = new CPTree();
        countsPerItem = new HashMap<>();
        supportRatioPerItem = new HashMap<>();
        itemCountsForD1 = new HashMap<>();
        itemCountsForD2 = new HashMap<>();
        topK_NegPatterns = new PriorityQueue<>();

        int numClasses = training.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();
        for (int i = 0; i < numClasses; i++) {
            // Retrieve training patterns
            ArrayList<Pattern> instances = Utils.generatePatterns(training, i);
            // get the support ratio for each item
            getSupportRatioForItems(instances);
            getBitStrings(instances);
            // Fill the tree
            for (Pattern p : instances) {
                // Sort the pattern according to the support-ratio inverse ordering for efficiency
                p.getItems().sort((o1, o2) -> {
                    double gr1 = supportRatioPerItem.get(o1);
                    double gr2 = supportRatioPerItem.get(o2);
                    if (gr1 > gr2) {
                        return 1;
                    } else if (gr1 < gr2) {
                        return -1;
                    } else {
                        return 0;
                    }
                });
                tree.insert(p, supportRatioPerItem);
            }
            System.out.println(getPatternCount(instances.get(0), false));
            // Mine the tree looking for Top-k SJEPs !
            mineTree(tree.getRoot(), new Pattern(new ArrayList<Item>(), i));
        }

    }

    @Override
    public String[][] predict(InstanceSet test) {
        return null;
    }

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
            if (next.getValue().D1 == 0 && next.getValue().D2 == 0) {
                suppRatio = 0;
            } else if ((next.getValue().D1 != 0 && next.getValue().D2 == 0) || (next.getValue().D1 == 0 && next.getValue().D2 != 0)) {
                suppRatio = Double.POSITIVE_INFINITY;
            } else {
                suppRatio = Math.max(((Integer) next.getValue().D1).doubleValue() / ((Integer) next.getValue().D2).doubleValue(), ((Integer) next.getValue().D2).doubleValue() / ((Integer) next.getValue().D1).doubleValue());
            }
            supportRatioPerItem.put(next.getKey(), suppRatio);
        }
    }
    
    
    public void getBitStrings(ArrayList<Pattern> instances){
        Set<Item> keySet = countsPerItem.keySet();
        Iterator<Item> iterator = keySet.iterator();
        while(iterator.hasNext()){
            Item it = iterator.next();
            // set bits to zero
            BigInteger a = BigInteger.ZERO;
            itemCountsForD1.put(it, a);
            itemCountsForD2.put(it, a);
        }
        
        for(int i = 0; i < instances.size(); i++){
            for(Item it : instances.get(i).getItems()){
                if(instances.get(i).getClase() == 0){
                    itemCountsForD1.put(it, itemCountsForD1.get(it).setBit(i));
                } else {
                   itemCountsForD2.put(it, itemCountsForD2.get(it).setBit(i));
                }
            }
        }
    }

    public void mineTree(Node node, Pattern alpha) {
        // for all i in t.items
        for (Entry i : node.getItems()) {
            // if the subtree is not empty the merge
            if (i.getChild() != null) {
                node.merge(i.getChild(), node, supportRatioPerItem);
            }
            Pattern beta = alpha.clone();
            beta.add(i.getItem());

            // if accept-pattern
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
}
