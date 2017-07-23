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

import framework.items.Item;

/**
 * A class to Define an entry in the header table of the P-Tree
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class HeaderTableEntry {

    /**
     * The item of the table
     */
    public Item item;

    /**
     * The number of times the item appear for D1
     */
    public int count1;

    /**
     * The number of times the item appear for D2
     */
    public int count2;

    /**
     * The first node in the tree that contains the item.
     */
    public PTree headNodeLink;
    
    /**
     * The last node in the tree that contains the item
     */
    public PTree tailNodeLink;
    
    /** 
     * Constructor of an entry of the header table with its counts set to 0
     * @param item The item to add
     * @param node The first node that contains this item
     */
    public HeaderTableEntry(Item item, PTree node) {
        this.item = item;
        this.count1 = 0;
        this.count2 = 0;
        this.headNodeLink = node;
        this.tailNodeLink = node;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof HeaderTableEntry)) {
            return false;
        }

        HeaderTableEntry o = (HeaderTableEntry) other;
        return this.item.equals(o.item);
    }

    /**
     * Search for the last item following node-links in this header table entry,
     * i.e., it finds the node whose {@code node_link} attribute is null, and
     * then assign the value of that {@code node_link} to {@code node}
     *
     * @param node
     */
    public void addNodeLink(PTree node) {  
        PTree aux = headNodeLink;
        if (aux != null) {
            /*
            while (aux.getNode_link() != null) {
                aux = aux.getNode_link();
            }

            aux.setNode_link(node);*/
            tailNodeLink.setNode_link(node);
            tailNodeLink = node;
        } else {
            headNodeLink = node;
            tailNodeLink = node;
        }
    }
}
