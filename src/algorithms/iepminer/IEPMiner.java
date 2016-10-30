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

import framework.GUI.Model;
import framework.items.Item;
import framework.items.NominalItem;
import framework.items.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import keel.Dataset.Instance;
import keel.Dataset.InstanceSet;

/**
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class IEPMiner extends Model {

    private PTree root;
    private ArrayList<Pattern> trainingInstances;
    private int minimumSupport;
    private double minimumGrowthRate;
    private double minimumChiSquared;

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        PTree.headerTable = new ArrayList<>();
        int numClasses = training.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();
        trainingInstances = new ArrayList<>();

        // Mine patterns for each class
        for (int i = 0; i < numClasses; i++) {
            // gets the training instances
            trainingInstances.clear();
            root = new PTree(null, true);

            for (Instance inst : training.getInstances()) {
                Pattern p = new Pattern(new ArrayList<Item>(), inst.getOutputNominalValuesInt(0) == i ? 0 : 1);
                for (int j = 0; j < training.getAttributeDefinitions().getInputNumAttributes(); j++) {
                    p.add(new NominalItem(training.getAttributeDefinitions().getAttribute(j).getName(), inst.getInputNominalValues(j)));
                }
                trainingInstances.add(p);
            }
            long t_ini = System.currentTimeMillis();
            for (Pattern p : trainingInstances) {
                root.insertTree(p);
            }
            System.out.println("Time to build the tree: " + (System.currentTimeMillis() - t_ini) / 1000f + " seconds.");

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
     * Calculates the chi-squared value and compares if the value obtained is greater than the given chi-squeared threshold.
     * @param Y A vector with the counts in D1 and D2, respectively.
     * @param X A vector with the counts in D1 and D2
     * @return {@code true} is passes the test, {@code false} elsewhere.
     */
    public boolean chi(int[] Y, int[] X){
        if(Y.length != 2 || X.length != 2) return false;
        
        float observedTable[][] = new float[2][2];
        float expectedTable[][] = new float[2][2];
        float totalSum = Y[0] + Y[1] + X[0] + X[1];
        observedTable[0][0] = Y[0];
        observedTable[0][1] = X[0];
        observedTable[1][0] = Y[1];
        observedTable[1][1] = X[1];
        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 2; j++){
                expectedTable[i][j] = Math.round(((observedTable[0][j] + observedTable[1][j]) * (observedTable[i][0] + observedTable[i][1])) / totalSum);
            }
        }
        
        float chiValue = 0;
        for(int i = 0; i < 2; i++){
            for(int j = 0; j < 2; j++){
                chiValue += Math.pow(observedTable[i][j] - expectedTable[i][j], 2) / expectedTable[i][j];
            }
        }
        
        return chiValue > minimumChiSquared;
    }
    
    
    /**
     * Public function to mine root tree
     * @param root 
     */
    public void mineTree(PTree root, int clase){
        ArrayList<Pattern> patternSet = new ArrayList<>();
        for(int i = PTree.headerTable.size() - 1; i >= 0; i--){
            ArrayList<Item> its = new ArrayList<>();
            its.add(PTree.headerTable.get(i).item);
            Pattern beta = new Pattern(its, clase);
            // gets the counts for D1 and D2 for the header table to get support and gr.
            double D1 = ((Integer) PTree.headerTable.get(i).count1).doubleValue();
            double D2 = ((Integer) PTree.headerTable.get(i).count2).doubleValue();
            if(is_iEP(PTree.headerTable.get(i).count1, D1 / D2)){
                patternSet.add(beta);
            }
            
            mineSubTree(beta);
        }
    }
    
    
    /**
     * Private and recursive function to mine the tree
     * @param node 
     */
    private void mineSubTree(Pattern beta){
        Item k = beta.get(beta.length() -1);
        int positionItem = PTree.headerTable.indexOf(new HeaderTableEntry(k, null));
        // Adjust the node links of k's subtrees and accumulate counts.
        //PTree.headerTable.get(positionItem).headNodeLink;
    }
}
