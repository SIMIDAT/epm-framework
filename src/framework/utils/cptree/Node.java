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
package framework.utils.cptree;

import algorithms.topk.*;
import framework.items.Item;
import framework.items.NominalItem;
import framework.items.Pattern;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Class that represent a node of the CP-Tree
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class Node {

    /**
     * The number of items in the node
     */
    protected int itemNumber;

    /**
     * The set of node entry. Each entry contains an item, counts for D1 and D2,
     * and a child Node.
     */
    private ArrayList<Entry> items;
    
    public boolean visited = false;
    
    /**
     * Creates a node with the given item, and start to 1 the counts of the
     * given class
     *
     * @param item
     * @param clas
     */
    public Node(Item item, int clas) {
        this.items = new ArrayList<>();
        this.items.add(new Entry(item, clas));
        this.itemNumber = 1;
    }

    /**
     * Creates a new node with the specified item inside it with its counts equal to zero.
     * @param item 
     */
    public Node(Item item){
        this.items = new ArrayList<>();
        this.items.add(new Entry(item));
        this.itemNumber = 1;
    }
    /**
     * Default constructor creates an empty node
     */
    public Node() {
        this.items = new ArrayList<>();
        this.itemNumber = 0;
    }

    public Object clone() {
        Node a = new Node();
        a.items = new ArrayList<>();
        for (int i = 0; i < this.itemNumber; i++) {
            a.items.add((Entry) this.items.get(i).clone());
        }
        a.setItemNumber(this.getItemNumber());
        return a;
    }

    /* @Override
    public boolean equals(Object other) {
        Node n = (Node) other;
        return n.item.equals(this.item);
    }*/
    /**
     * Adds the given pattern recursively on the tree and sum its counts
     *
     * @param p The pattern ordered in inverse order than the ordering given by
     * < operator. @pa ram n The node where pattern will be inserted
     */
    public void insert(Pattern p, Node n, HashMap<Item, Double> supportRatio) {

        Item it = p.get(p.length() - 1);
        Entry check = new Entry(it, 0);
        int index = 0;
        if (n.getItems().contains(check)) {
            // get the node and sum it counts
            index = n.getItems().indexOf(check);
            // sum counts in D1 or D2
            if (p.getClase() == 0) {
                n.getItems().get(index).setCountD1(n.getItems().get(index).getCountD1() + 1);
            } else {
                n.getItems().get(index).setCountD2(n.getItems().get(index).getCountD2() + 1);
            }
        } else {
            // Insert the item
            n.getItems().add(new Entry(it, p.getClase()));
            // incremen itemNumber
            n.itemNumber++;
            // sorts items and counts by support-ratio descending order
            n.getItems().sort(Collections.reverseOrder((o1, o2) -> {
                double gr1 = supportRatio.get(o1.getItem());
                double gr2 = supportRatio.get(o2.getItem());
                if (gr1 > gr2) {
                    return 1;
                } else if (gr1 < gr2) {
                    return -1;
                } else {
                    return -1 * ((NominalItem) o1.getItem()).compareTo(o2.getItem());
                }
            }));
            index = n.getItems().indexOf(check);
        }

        // Go down recursively
        Pattern a = p.clone();
        a.getItems().remove(a.length() - 1);
        if (!a.getItems().isEmpty()) {
            if (n.getItems().get(index).getChild() == null) {
                // create a new node if subtree is empty
                n.getItems().get(index).setChild(new Node());
            }
            insert(a, n.getItems().get(index).getChild(), supportRatio);
        }

    }

    /**
     * Merges T1's nodes into {@code this}. {@code this} is updated (including new-node generation
     * and existing-node changes, but no nodes deletion), while T1 remains
     * unchanged. The merge must be done T1 is the subtree and T2 is T1's
     * parent. Else, the function would cause an stack overflow.
     *
     * @param T1
     * @param supportRatio
     */
    public void merge(Node T1, HashMap<Item, Double> supportRatio) {
        // for each T1.item do
        for (int i = 0; i < T1.itemNumber; i++) {
            Entry item = T1.getItems().get(i);
            //item.merged = true;
            T1.getItems().get(i).merged =true;
            int pos = -1;
            // search T2 for T2.items[j] = T1.items[i]
            if (this.getItems().contains(item)) {
                // is T2.items[j] found
                pos = this.getItems().indexOf(item);
                this.getItems().get(pos).setCountD1(this.getItems().get(pos).getCountD1() + item.getCountD1());
                this.getItems().get(pos).setCountD2(this.getItems().get(pos).getCountD2() + item.getCountD2());
            } else {
                // Insert T1.items[i] with it counts and child at the appropiate place of T2 obeyin the order
                this.getItems().add((Entry) item.clone());
                this.getItems().sort(Collections.reverseOrder((o1, o2) -> {
                    double gr1 = supportRatio.get(o1.getItem());
                    double gr2 = supportRatio.get(o2.getItem());
                    if (gr1 > gr2) {
                        return 1;
                    } else if (gr1 < gr2) {
                        return 1;
                    } else {
                        return -1*((NominalItem) o1.getItem()).compareTo(o2.getItem());
                    }
                }));
                this.itemNumber++;
                pos = this.getItems().indexOf(item);
            }
            // If T1.items subtree is not empty
            if (item.getChild() != null) {
                // If T2.items[j] subtree is empty
                if (this.getItems().get(pos).getChild() == null) {
                    // create a new node as this.items[j] subtree
                    this.getItems().get(pos).setChild(new Node());
                }
                // recursive call
                this.items.get(pos).getChild().merge(item.getChild(), supportRatio);
            }
        }
    }

    /**
     * @return the itemNumber
     */
    public int getItemNumber() {
        return itemNumber;
    }

    /**
     * @return the items
     */
    public ArrayList<Entry> getItems() {
        return items;
    }

    /**
     * @param itemNumber the itemNumber to set
     */
    public void setItemNumber(int itemNumber) {
        this.itemNumber = itemNumber;
    }
}
