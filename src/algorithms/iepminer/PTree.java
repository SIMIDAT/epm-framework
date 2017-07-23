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

import framework.items.*;
import java.util.ArrayList;

/**
 * A class to represent the P-Tree structure to efficiently mine emmerging
 * patterns.
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class PTree {

    /**
     * Checks if the node is the root node
     */
    protected boolean root;

    /**
     * The item associated with this node. In fact, Item must be always an
     * instance of NominalItem.
     */
    protected Item item;

    /**
     * The number of transactions of D1 represented by the portion of the path
     * reaching the node
     */
    protected int countD1;

    /**
     * The number of transactions of D2 represented by the portion of the path
     * reaching the node
     */
    protected int countD2;

    /**
     * The children nodes of this one.
     */
    private ArrayList<PTree> childrens;

    /**
     * The next node in the tree that contains the same item than this one.
     */
    protected PTree node_link;

    /**
     * The header table of node-links
     */
    public static ArrayList<HeaderTableEntry> headerTable;

    /**
     * Default constructor
     *
     * @param item
     * @param root
     */
    public PTree(Item item, boolean root) {
        this.childrens = new ArrayList<>();
        this.countD1 = 0;
        this.countD2 = 0;
        this.item = item;
        this.node_link = null;
        this.root = root;
    }

    @Override
    public boolean equals(Object other) {
        PTree o = (PTree) other;
        return this.item.equals(o.item);
    }

    /**
     * Inserts the given instance (viewed as a pattern itself) on the P-Tree
     *
     * @param pat The pattern to insert
     */
    public void insertTree(Pattern pat) {
        // Sorts the items in the pattern by lexycographicall order 
        // Rememeber that all items are of class NominalItem.
        pat.getItems().sort((o1, o2) -> {
            return -1 * o1.compareTo(o2);
        });
        insertTree(pat, this);
    }

    /**
     * Private and recursive function to insert a pattern into the P-Tree
     *
     * @param pat The pattern to insert
     * @param node The node to introduce an item
     */
    private void insertTree(Pattern pat, PTree node) {
        if (!pat.getItems().isEmpty()) {
            Pattern p = (Pattern) pat.clone();
            Item actual = p.get(p.length()-1);
            int index = -1;
            boolean newNodeInserted = false;
            PTree newNode = null;
            boolean exist = false;
            // Look on node's children for this item
            for (int i = 0; i < node.childrens.size() && !exist; i++) {
                if (node.childrens.get(i).equals(new PTree(actual, false))) {
                    exist = true;
                    index = i;
                }
            }
            if (!exist) {
                // If not exists, adds the node
                newNode = new PTree(actual, false);
                //Look the class of the pattern and increment counts
                if (p.getClase() == 0) {
                    newNode.countD1++;
                } else {
                    newNode.countD2++;
                }
                // Adds the node in the tree
                node.childrens.add(newNode);
                // Sort the childrens according to the order
                node.childrens.sort((PTree o1, PTree o2) -> {
                    return o1.getItem().compareTo(o2.getItem());
                });
                newNodeInserted = true;
                index = node.childrens.indexOf(newNode);
            } else {
                // The item exist, increments its counts
                //index = node.childrens.indexOf(new PTree(actual, false));
                if (p.getClase() == 0) {
                    node.childrens.get(index).countD1++;
                } else {
                    node.childrens.get(index).countD2++;
                }
            }

            // look if the item exist in the header table
            if (headerTable.contains(new HeaderTableEntry(actual, null))) {
                // if exists, increment the global counts
                int ind = headerTable.indexOf(new HeaderTableEntry(actual, null));
                if (p.getClase() == 0) {
                    headerTable.get(ind).count1++;
                } else {
                    headerTable.get(ind).count2++;
                }
                // and if it is a new node inserted, insert as the last node-link
                if (newNodeInserted) {
                    headerTable.get(ind).addNodeLink(newNode);
                }
            } else {
                HeaderTableEntry entry;
                if(newNodeInserted){
                    entry = new HeaderTableEntry(actual, newNode);
                } else {
                    entry = new HeaderTableEntry(actual, node);
                }
               entry.count1 += newNode.countD1;
               entry.count2 += newNode.countD2;
                // Add the entry in the table
                headerTable.add(entry);
                //Sort the table
                headerTable.sort((o1, o2) -> {
                    return o1.item.compareTo(o2.item);
                });
            }

            // Performs the recursive call removing the item of the pattern
            p.getItems().remove(p.length() - 1);
            insertTree(p, node.childrens.get(index));
        }
    }

    /**
     * @return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * @return the countD1
     */
    public int getCountD1() {
        return countD1;
    }

    /**
     * @return the countD2
     */
    public int getCountD2() {
        return countD2;
    }

    /**
     * @return the node_link
     */
    public PTree getNode_link() {
        return node_link;
    }

    /**
     * @param node_link the node_link to set
     */
    public void setNode_link(PTree node_link) {
        this.node_link = node_link;
    }

    
    
    /**
     * Removes all subtree of this given node.
     */
    public void clear() {
        clear(this);
    }
    
    
    /**
     * Recursive function to remove all the subtree of this given node
     * @param node 
     */
    private void clear(PTree node) {
        // Go down to a leaf node.
        if (!node.childrens.isEmpty()) {
           for(int i = 0; i < node.childrens.size(); i++){
               clear(node.childrens.get(i));
           }
        }
        node.childrens.clear();
    }

    public PTree getChildren(int pos){
        return childrens.get(pos);
    }
    
    public int numChildren(){
        return childrens.size();
    }
    
    /**
     * Sets the nodes from {@code startIndex +1 } untill the end of the header table with counts equal to 0 and null node-links
     * @param startIndex 
     */
    public static void cleanLinks(int startIndex){
        if(startIndex + 1 < headerTable.size()){
            for(int i = startIndex + 1; i < headerTable.size(); i++){
                // remove all node links for this item
                PTree aux = headerTable.get(i).headNodeLink;
                while(aux != null){
                    PTree aux2 = aux.node_link;
                    aux.node_link = null;
                    aux = aux2;
                }
                headerTable.set(i, new HeaderTableEntry(headerTable.get(i).item, null));
            }
        }
    }
    
}
