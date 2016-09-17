/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sjep_classifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author angel
 */
public class CPTree {

    public CPTreeNode root;      // The root of the tree
    private int D1;              // The number of instances belonging to class D1
    private int D2;              // The number of instances belonging to class D2

    public CPTree(int D1, int D2) {
        root = new CPTreeNode(0);
        this.D1 = D1;
        this.D2 = D2;
    }

    /**
     * Inserts a pattern on the CP-Tree
     *
     * @param pattern
     * @param clas
     */
    public void insertTree(ArrayList<Item> pattern, int clas) {
        ArrayList<Item> p = new ArrayList<>(pattern);
        addInstance(p, root, clas);
    }

    /**
     * Adds an instance to the CP-Tree recursively
     *
     * @param pattern The pattern to add
     * @param node The node where the pattern is inserted (initially, the root)
     * @param clas The class of the instance. 0 for class 1 and 1 for class 2
     */
    private void addInstance(ArrayList<Item> pattern, CPTreeNode node, int clas) {

        // End of the recursivity
        if (pattern.size() <= 0) {
            return;
        }

        // Search for the first item in the item list of the actual node.
        int value = node.items.indexOf(pattern.get(0));
        Item p = new Item(pattern.get(0));
        if (value == -1) {
            // If item does not exist in the node:
            // D1 and D2 counts are 0
            p.setD1count(0);
            p.setD2count(0);
            p.child = null;
            // Inserts p in the appropiate place according to the order
            node.items.add(p);
            node.items.sort(null);

            // Increments itemNumer by 1
            node.itemNumber++;
        }

        if (clas == 0) {
            // Increments the number of counts in D1 by 1:
            node.items.get(node.items.indexOf(p)).incrementsD1();
        } else {
            // Increments the number of counts in D2 by 1:
            node.items.get(node.items.indexOf(p)).incrementsD2();
        }

        // Now, if pattern is not empty
        if (pattern.size() > 0) {
            // if item subtree is empty
            if (p.child == null || p.child.items.size() == 0) {
                // create a new child node as subtree
                p.child = new CPTreeNode(0);
            }

            // recursive call with the next element of the pattern
            pattern.remove(0);
            addInstance(pattern, node.items.get(node.items.indexOf(p)).child, clas);
        }
    }

    /**
     * Merges T1's nodes into T2. T2 is updated(including new-node generation
     * and existing-node changes, but no nodes deletion), while T1 remains
     * unchanged. The merge must be done T1 is the subtree and T2 is T1's
     * parent. Else, the function would cause an stack overflow.
     *
     * @param T1
     * @param T2
     */
    private void mergeTree(CPTreeNode T1, CPTreeNode T2) {
        // For each item in T1
        for (Item item : T1.items) {
            // Search 'item' in T2
            int value = T2.items.indexOf(item);

            if (value != -1) {
                // If 'item' is found in T2:
                int sumD1 = T2.items.get(value).getD1count() + item.getD1count();
                int sumD2 = T2.items.get(value).getD2count() + item.getD2count();
                // Update values of D1 and D2
                T2.items.get(value).setD1count(sumD1);
                T2.items.get(value).setD2count(sumD2);
            } else {
               // if 'item' is not found in T2:
                // insert the whole 'item' (including subtree) following the order
                T2.items.add(item);
                T2.items.sort(null);
                // increments itemNumber
                T2.itemNumber++;
            }

            // if 'item' subtree is not empty:
            if (item.child != null && item.child.items.size() > 0) {
                // if T2.items[value] subtree is empty:
                value = T2.items.indexOf(item);
                if (T2.items.get(value).child == null || T2.items.get(value).child.items.size() == 0) {
                    // create a new node as subtree
                    T2.items.get(value).child = new CPTreeNode(0);
                } 
                // make the recursive call 
                mergeTree(item.child, T2.items.get(value).child);
            }

        }
    }
    
    
    /**
     * Mines the tree to look for SJEPs of both classes
     * @param minSupp The minimum support threshold. A number in [0,1]
     * @return 
     */
    public ArrayList<Pattern> mineTree(double minSupp) {
        // Crete the hash map to return
        ArrayList<Pattern> result = new ArrayList<Pattern>();
        // Create an empty pattern
        ArrayList<Item> pattern = new ArrayList<>();

        // call mine_tree recursive function
        mine_tree(root, pattern, minSupp, result);

        return result;
    }

    /**
     *  Recursive function to mine the CP-Tree for SJEPs
     * @param node the node to mine
     * @param pattern The candidate pattern to be a SJEP
     * @param minSupp The minimum support threshold
     * @param result The data structure to store SJEPs obtained.
     */
    private void mine_tree(CPTreeNode node, ArrayList<Item> pattern, double minSupp, ArrayList<Pattern> result) {

        // for each item in 'node'
        for (int i = 0; i < node.items.size(); i++) {
            // Go to next item
            Item item = node.items.get(i);
            //make a copy of 'pattern' to avoid pass by reference:
            ArrayList<Item> p = new ArrayList<>(pattern);
            // If 'item' subtree is not empty
            if (item.child != null && item.child.items.size() > 0) {
                // merge the subtree with his parent
                
                mergeTree(item.child, node);
            }

            // Add 'item' to the resulting 'p'
            p.add(new Item(item));
            int threshold_D1 = (int) (D1 * minSupp);
            int threshold_D2 = (int) (D2 * minSupp);
            // Generate a SJEP of D2 if the patternes match the conditions
            if (item.getD1count() == 0 && item.getD2count() >= threshold_D2) {
                // Generate the SJEP
                result.add(new Pattern(p, item.getD2count(), 1));

            } else if (item.getD2count() == 0 && item.getD1count() >= threshold_D1) {
                // Generate the SJEP for class D1
                result.add(new Pattern(p, item.getD1count(), 0));

            } else if ((item.child != null && item.child.items.size() > 0) && (item.getD1count() >= (int) (D1 * minSupp) || item.getD2count() >= (int) (D2 * minSupp))) {
               // If subtree of 'item' is not null and the node pass the support threshold
                // then, go deeper searching for longer SJEPs

                // recursive call
                mine_tree(item.child, p, minSupp, result);
            }
            
            // remove the item subtree to allow large datasets load
            item.child = null;

        }

    }

}
