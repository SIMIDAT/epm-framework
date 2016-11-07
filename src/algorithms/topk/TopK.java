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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
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

    /**
     * The k value to get the top-k patterns
     */
    private int k;

 
    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        tree = new CPTree();
        countsPerItem = new HashMap<>();
        supportRatioPerItem = new HashMap<>();
        itemCountsForD1 = new HashMap<>();
        itemCountsForD2 = new HashMap<>();

        topK_NegPatterns = new PriorityQueue<>((Pattern o1, Pattern o2) -> {
            double supp1 = o1.getTraMeasure("Supp");
            double supp2 = o2.getTraMeasure("Supp");
            if (supp1 > supp2) {
                return -1;
            } else if (supp1 < supp2) {
                return 1;
            } else {
                return 0;
            }
        });
        topK_PosPatterns = new PriorityQueue<>((Pattern o1, Pattern o2) -> {
            double supp1 = o1.getTraMeasure("Supp");
            double supp2 = o2.getTraMeasure("Supp");
            if (supp1 > supp2) {
                return -1;
            } else if (supp1 < supp2) {
                return 1;
            } else {
                return 0;
            }
        });
        k = 30;

        int numClasses = training.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();
        // Mine for each class separately 
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

            // Mine the tree looking for Top-k SJEPs !
            try {
                mineTree(tree.getRoot(), new Pattern(new ArrayList<Item>(), i));
            } catch (Exception e) {
                System.out.println("ALARMAAAAAAAA");
                System.out.println(e);
                e.printStackTrace();
                System.exit(-1);
            }
            // Reset counter values
            
            collectedNegPatterns = collectedPosPatterns = minNegCount = minPosCount = 0;
            itemCountsForD1.clear();
            itemCountsForD2.clear();
            countsPerItem.clear();
            supportRatioPerItem.clear();
            tree.clear();
        }
        System.out.println("EIIII ");
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

    public void mineTree(Node node, Pattern alpha) {
        // for all i in t.items
        for (int j = 0; j < node.getItems().size(); j++) {
            Entry i = node.getItems().get(j);
            // if the subtree is not empty the merge
            if (i.getChild() != null) {
                node.merge(i.getChild(), supportRatioPerItem);
            }
            Pattern beta = alpha.clone();
            beta.add(i.getItem());

            // if accept-pattern
            if (acceptPattern(beta, i.getCountD1(), i.getCountD2(), minNegCount, false)) {
                beta.setClase(1);
                HashMap<String, Double> m = new HashMap<>();
                m.put("Supp", ((Integer) i.getCountD2()).doubleValue());
                beta.setTra_measures(m);
                topK_NegPatterns.add(beta.clone());
                collectedNegPatterns++;
                if (collectedNegPatterns >= k) {
                    // raise-count procedure
                    minNegCount = getPatternCount(topK_NegPatterns.peek(), false) + 1;
                }
            } else if (acceptPattern(beta, i.getCountD1(), i.getCountD2(), minPosCount, true)) {
                beta.setClase(0);
                HashMap<String, Double> m = new HashMap<>();
                m.put("Supp", ((Integer) i.getCountD1()).doubleValue());
                beta.setTra_measures(m);
                topK_PosPatterns.add(beta.clone());
                collectedPosPatterns++;
                if (collectedPosPatterns >= k) {
                    minPosCount = getPatternCount(topK_PosPatterns.peek(), true);
                }
            } else if (visitSubTree(beta) && i.getChild() != null) {
                mineTree(i.getChild(), beta);
            }
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
    public boolean acceptPattern(Pattern beta, int countD1, int countD2, int minCount, boolean positiveClass) {
        boolean minimal = false;
        // check if the patter is a JEP
        if (positiveClass) {
            if (countD1 >= minCount && countD2 == 0) {
                minimal = true;
            }
        } else if (countD2 >= minCount && countD1 == 0) {
            minimal = true;
        }

        // Check minimality if the pattern is a JEP
        if (minimal && beta.length() > 1) {
            for (Item it : beta.getItems()) {
                Pattern other = beta.clone();
                other.drop(it);
                if (getPatternCount(beta, true) >= getPatternCount(other, true)) {
                    minimal = false;
                    break;
                }
            }
        }

        // if the patter obtained is a minimal JEP, check if it is a top-k
        if (minimal) {
            if (positiveClass) {
                if (collectedPosPatterns > this.k && countD1 < minCount) {
                    minimal = false;
                }
            } else if (collectedNegPatterns > this.k && countD2 < minCount) {
                minimal = false;
            }
        }
        return minimal;
    }

    public boolean visitSubTree(Pattern beta) {
        boolean visit = true;
        boolean minimalD1 = true;
        boolean minimalD2 = true;
        if (beta.length() > 1) {
            for (Item it : beta.getItems()) {
                Pattern other = beta.clone();
                other.drop(it);
                if (minimalD1 && getPatternCount(beta, true) >= getPatternCount(other, true)) {
                    minimalD1 = false;
                }
                if (minimalD2 && getPatternCount(beta, false) >= getPatternCount(other, false)) {
                    minimalD2 = false;
                }
                if (!(minimalD1 || minimalD2)) {
                    visit = false;
                    break;
                }
            }
        }

        return visit;
    }
}
