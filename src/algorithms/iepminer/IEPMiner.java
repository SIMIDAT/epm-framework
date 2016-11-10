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
package algorithms.iepminer;

import framework.GUI.GUI;
import framework.GUI.Model;
import framework.exceptions.IllegalActionException;
import framework.items.Item;
import framework.items.NominalItem;
import framework.items.Pattern;
import framework.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import keel.Dataset.Instance;
import keel.Dataset.InstanceSet;
import sun.misc.Queue;

/**
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class IEPMiner extends Model {

    private PTree root;
    private ArrayList<Pattern> trainingInstances;
    private ArrayList<Pattern> patternSet;
    private int minimumSupport;
    private double minimumGrowthRate;
    private double minimumChiSquared;

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        try {
            Utils.checkDataset();
            // Catch Params
            minimumSupport = (int) (training.getNumInstances() * Float.parseFloat(params.get("Minimum Support")));
            minimumGrowthRate = Double.parseDouble(params.get("Minimum Growth Rate"));
            minimumChiSquared = Double.parseDouble(params.get("Minimum Chi-Squared"));

            // Algorithm begin
            PTree.headerTable = new ArrayList<>();
            int numClasses = training.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();
            trainingInstances = new ArrayList<>();
            patternSet = new ArrayList<>();

            // Mine patterns for each class
            long t_start = System.currentTimeMillis();
            for (int i = 0; i < numClasses; i++) {
                System.out.println("Mining class " + (i + 1) + " of " + numClasses);
                // gets the training instances
                trainingInstances.clear();
                root = new PTree(null, true);
                for (Instance inst : training.getInstances()) {
                    Pattern p = new Pattern(new ArrayList<Item>(), inst.getOutputNominalValuesInt(0) == i ? 0 : 1);
                    for (int j = 0; j < training.getAttributeDefinitions().getInputNumAttributes(); j++) {
                        if (!inst.getInputMissingValues(j)) {
                            p.add(new NominalItem(training.getAttributeDefinitions().getAttribute(j).getName(), inst.getInputNominalValues(j)));
                        }
                    }
                    trainingInstances.add(p);
                }
                long t_ini = System.currentTimeMillis();
                for (Pattern p : trainingInstances) {
                    root.insertTree(p);
                }
                System.out.println("Time to build the tree: " + (System.currentTimeMillis() - t_ini) / 1000f + " seconds.");
                t_ini = System.currentTimeMillis();
                mineTree(root, i);
                System.out.println("Time to mine the tree: " + (System.currentTimeMillis() - t_ini) / 1000f + " seconds.");
                //ArrayList<Pattern> iEPs = pruneEPs(patternSet);
                super.patterns.addAll(patternSet);
                //iEPs.clear();
                patternSet.clear();
                PTree.cleanLinks(-1);
                PTree.headerTable.clear();
            }
            System.out.println("Time to finish mining: " + (System.currentTimeMillis() - t_start) / 1000f + " seconds.");
            super.setPatterns(pruneEPs(super.patterns));
            System.out.println("Number of iEPs: " + patterns.size());
        } catch (IllegalActionException ex) {
            GUI.setInfoLearnTextError(ex.getReason());
        }
    }

    /**
     * It returns whether the given pattern is an iEP for the mining process.
     * NOTE: It does not check condition 3 and 4 !
     *
     * @param pattern The pattern to check
     * @param supp The support of the given pattern (i.e. the number of
     * instances in the class we are looking for patterns)
     * @param gr The growht rate of the pattern
     * @return
     */
    public boolean is_iEP(int supp, double gr) {
        if (supp < minimumSupport) { // Condition 1
            return false;
        }
        if (gr < minimumGrowthRate) { // Condition 2
            return false;
        }
        return true;
    }

    /**
     * Calculates the chi-squared value and compares if the value obtained is
     * greater than the given chi-squeared threshold.
     *
     * @param Y A vector with the counts in D1 and D2, respectively.
     * @param X A vector with the counts in D1 and D2
     * @return {@code true} is passes the test, {@code false} elsewhere.
     */
    public double chi(int[] Y, int[] X) {
        if (Y.length != 2 || X.length != 2) {
            return -1;
        }

        float observedTable[][] = new float[2][2];
        float expectedTable[][] = new float[2][2];
        float totalSum = Y[0] + Y[1] + X[0] + X[1];
        observedTable[0][0] = Y[0];
        observedTable[0][1] = X[0];
        observedTable[1][0] = Y[1];
        observedTable[1][1] = X[1];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                expectedTable[i][j] = Math.round(((observedTable[0][j] + observedTable[1][j]) * (observedTable[i][0] + observedTable[i][1])) / totalSum);
            }
        }

        float chiValue = 0;
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                chiValue += Math.pow(observedTable[i][j] - expectedTable[i][j], 2) / expectedTable[i][j];
            }
        }

        return chiValue;
    }

    /**
     * Public function to mine root tree
     *
     * @param root
     */
    public void mineTree(PTree root, int clase) {
        for (int i = PTree.headerTable.size() - 1; i >= 0; i--) {
            ArrayList<Item> its = new ArrayList<>();
            its.add(PTree.headerTable.get(i).item);
            Pattern beta = new Pattern(its, clase);
            // gets the counts for D1 and D2 for the header table to get support and gr.
            double D1 = ((Integer) PTree.headerTable.get(i).count1).doubleValue();
            double D2 = ((Integer) PTree.headerTable.get(i).count2).doubleValue();
            double gr;
            if (D1 != 0.0 && D2 == 0.0) {
                gr = Double.POSITIVE_INFINITY;
            } else if (D1 == 0.0 && D2 == 0.0) {
                gr = 0;
            } else {
                gr = D1 / D2;
            }
            HashMap<String, Double> measures = new HashMap<>();
            measures.put("GR", gr);
            measures.put("SUPP", D1);
            beta.setTra_measures(measures);
            if (is_iEP(PTree.headerTable.get(i).count1, gr)) {
                patternSet.add(beta);
            }

            /*
            We prune the tree if:
             1 - D1 and D2 counts are zero. This means that this pattern does not exist in the database
             2 - growth rate is infinite. This means the pattern is a JEP, so supersets of a JEP are not interesting
             3 - D1 counts < minimum support. This is due to Apriori property. If this pattern is not frequent, supersets of this one are not frequent too.
             */
            if (gr < Double.POSITIVE_INFINITY && PTree.headerTable.get(i).count1 >= minimumSupport) {
                mineSubTree(beta);
            }
        }
    }

    /**
     * Private and recursive function to mine the tree
     *
     * @param node
     */
    private void mineSubTree(Pattern beta) {
        Item k = beta.get(beta.length() - 1);
        int positionItem = PTree.headerTable.indexOf(new HeaderTableEntry(k, null));
        // Adjust the node links of k's subtrees and accumulate counts.
        PTree node = PTree.headerTable.get(positionItem).headNodeLink;
        PTree.cleanLinks(positionItem);
        while (node != null) { // Traverse all nodes with the same item
            for (int i = 0; i < node.numChildren(); i++) {
                // Adjust the node-links and counts
                adjust(node.getChildren(i));
            }
            // go to next item
            node = node.getNode_link();
        }

        for (int j = PTree.headerTable.size() - 1; j > positionItem; j--) {
            // gamma = beta U j
            Pattern gamma = beta.clone();
            gamma.add(PTree.headerTable.get(j).item);

            // Check if gamma is an iEP
            double D1 = ((Integer) PTree.headerTable.get(j).count1).doubleValue();
            double D2 = ((Integer) PTree.headerTable.get(j).count2).doubleValue();
            double gr;
            if (D1 != 0.0 && D2 == 0.0) {
                gr = Double.POSITIVE_INFINITY;
            } else if (D1 == 0.0 && D2 == 0.0) {
                gr = 0;
            } else {
                gr = D1 / D2;
            }
            HashMap<String, Double> measures = new HashMap<>();
            measures.put("GR", gr);
            measures.put("SUPP", D1);
            gamma.setTra_measures(measures);

            int[] Y = {(int) D1, (int) D2};
            int[] X = {PTree.headerTable.get(positionItem).count1, PTree.headerTable.get(positionItem).count2};
            if (is_iEP(PTree.headerTable.get(j).count1, gr, Y, X)) {
                patternSet.add(gamma);
            }
            // if chi(gamma,beta) >= nu the call recursively

            // Check chi-squared prunning. However, if the pattern obtained is a JEP, we prune, because non-minimal JEPs are not interesting
            if (chi(Y, X) >= minimumChiSquared && gr != Double.POSITIVE_INFINITY && Y[0] >= minimumSupport) {
                mineSubTree(gamma);
            }

        }

    }

    /**
     * Adjust the counts of the header table of the given node, and link at the
     * last element of it header table node-link list. This is done recursively,
     * so the entire node subtree is adjusted.
     *
     * @param node The node to adjust
     */
    private void adjust(PTree node) {
        // Go down to further processing
        for (int i = 0; i < node.numChildren(); i++) {
            adjust(node.getChildren(i));
        }
        // Get the header table associated with the node's item
        int pos = PTree.headerTable.indexOf(new HeaderTableEntry(node.getItem(), null));
        // Sum the counts in the header table
        PTree.headerTable.get(pos).count1 += node.getCountD1();
        PTree.headerTable.get(pos).count2 += node.getCountD2();
        // link this node on the header table
        PTree.headerTable.get(pos).addNodeLink(node);

    }

    private void adjust2(PTree node) {
        Stack<PTree> stack1 = new Stack<>();
        Stack<PTree> stack2 = new Stack<>();
        stack1.push(node);
        while (!stack1.empty()) {
            // TODO
        }
    }

    private ArrayList<Pattern> pruneEPs(ArrayList<Pattern> patternSet) {
        patternSet.sort((Pattern p1, Pattern p2) -> {
            if (p1.length() > p2.length()) {
                return 1;
            } else if (p1.length() < p2.length()) {
                return -1;
            } else {
                return 0;
            }
        });

        ArrayList<Pattern> newSet = new ArrayList<>();
        boolean[] marks = new boolean[patternSet.size()];

        // mark patterns to remove 
        // erase those patterns that are a superset and has a growth rate less than the subset.
        for (int i = 0; i < patternSet.size(); i++) {
            if (!marks[i]) {
                Pattern p1 = patternSet.get(i);
                for (int j = i + 1; j < patternSet.size(); j++) {
                    Pattern p2 = patternSet.get(j);
                    if (!marks[j] && p2.length() > p1.length()) {
                        // if p2 is a superset of pattern p1, compare growthrates
                        if (p1.covers(p2)) {
                            if (p1.getTraMeasure("GR") >= p2.getTraMeasure("GR")) {
                                // if gr(p1) >= gr(p2) it means that p2 is not an iEP.
                                marks[j] = true;
                            }
                        }
                    }
                }
            }
        }

        // retrieve those items marked
        for (int i = 0; i < marks.length; i++) {
            if (!marks[i]) {
                newSet.add(patternSet.get(i));
            }
        }

        /* System.out.println(newSet.size());
        
        // check in the reduced patterns set for patterns that accomplish with condition 3
         patternSet.clear();
        for (int i = 0; i < newSet.size(); i++) {
            HashSet<Pattern> set = new HashSet<>();
            powerSet(newSet.get(i), set);
            Iterator<Pattern> iterator = set.iterator();
            boolean mark = true;
            while (iterator.hasNext() && mark) {
                Pattern next = iterator.next();
                int[] counts = getCounts(next);
                double D1 = ((Integer) counts[0]).doubleValue();
                double D2 = ((Integer) counts[1]).doubleValue();
                double gr;
                if (D1 != 0.0 && D2 == 0.0) {
                    gr = Double.POSITIVE_INFINITY;
                } else if (D1 == 0.0 && D2 == 0.0) {
                    gr = 0;
                } else {
                    gr = D1 / D2;
                }
                if(newSet.get(i).getTraMeasure("GR") < gr){
                    mark = false;
                }
            }
            if(mark){
                patternSet.add(newSet.get(i));
            }
        }
         */
        //return patternSet;
        return newSet;
    }

    private boolean is_iEP(int supp, double gr, int[] Y, int[] X) {
        if (supp < minimumSupport) { // Condition 1
            return false;
        }
        if (gr < minimumGrowthRate) { // Condition 2
            return false;
        }
        // check condition 4, for each shadow pattern of gamma, checke if chi is greater than the threshold
        boolean next = true;
//        for (int i = 0; i < gamma.length() && next; i++) {
//            Pattern shadow = gamma.clone();
//            shadow.drop(i);
//            int[] counts = getCounts(shadow);
//            double x2 = chi(Y, counts);
//            if (x2 < minimumChiSquared) {
//                next = false;
//            }
//        }
        //return next;
        return chi(Y, X) >= minimumChiSquared; // return condition 2
    }

    public int[] getCounts(Pattern p) {
        int[] counts = new int[2];
        counts[0] = 0;
        counts[1] = 0;
        for (Pattern inst : trainingInstances) {
            if (p.covers(inst)) {
                counts[inst.getClase()]++;
            }
        }
        return counts;
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
}
