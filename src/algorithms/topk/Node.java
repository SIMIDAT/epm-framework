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

import framework.items.Item;
import framework.items.Pattern;
import java.util.ArrayList;

/**
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class Node {

    /**
     * The item that represents this node
     */
    private Item item;

    /**
     * The number of instances for class 1 reaching this node from the root
     */
    private int countD1;

    /**
     * The number of instances for class 2 reaching this node from the root
     */
    private int countD2;

    /**
     * The node subtrees
     */
    private ArrayList<Node> children;

    
    /**
     * Creates a node with the given item, and start to 1 the counts of the
     * given class
     *
     * @param item
     * @param clas
     */
    public Node(Item item, int clas) {
        this.item = item;
        if (clas == 0) {
            this.countD1 = 1;
            this.countD2 = 0;
        } else {
            this.countD1 = 0;
            this.countD2 = 1;
        }
        this.children = new ArrayList<>();
    }

    @Override
    public boolean equals(Object other) {
        Node n = (Node) other;
        return n.item.equals(this.item);
    }

    
    /**
     * Adds the given pattern recursively on the tree and sum its counts
     *
     * @param p The pattern ordered in inverse order than the ordering given by
     * < operator. 
     * @param n The node where pattern will be inserted
     */
    public void insert(Pattern p, Node n) {
        if (!p.getItems().isEmpty()) {
            Item it = p.get(p.length() - 1);

            if (n.children.contains(n)) {
                // get the node and sum it counts
                if (p.getClase() == 0) {
                    n.children.get(n.children.indexOf(it)).countD1++;
                } else {
                    n.children.get(n.children.indexOf(it)).countD1++;
                }
            } else {
                // Create the new node
                Node newNode = new Node(it, p.getClase());
                // Insert in the children of this node
                n.children.add(newNode);
                // sort by support-ratio descending order (TO-DO)
                n.children.sort(null);
            }

            // Go down recursively
            Pattern a = p.clone();
            a.getItems().remove(a.length() - 1);
            insert(a, n.children.get(n.children.indexOf(it)));
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
    public void merge(Node T1, Node T2){
         /**/ // TO-DO
    }
    
}
