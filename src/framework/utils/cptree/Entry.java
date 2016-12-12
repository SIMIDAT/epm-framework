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

/**
 * Class to represent an entry on a CP-Tree node.
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class Entry {

    private Item item;
    private int countD1;
    private int countD2;
    private Node child;
    public boolean merged = false;
    public boolean visited = false;

    public Entry(Item item, int clas) {
        this.item = item;
        countD1 = countD2 = 0;
        child = null;
        if (clas == 0) {
            countD1++;
        } else {
            countD2++;
        }
    }

    public Entry(Item item) {
        this.item = item;
        countD1 = countD2 = 0;
        child = null;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Entry) {
            return this.item.equals(((Entry) other).item);
        } else if (other instanceof Item) {
            return this.item.equals(other);
        }

        return false;
    }

    @Override
    public Object clone() {
        Entry a = new Entry(this.item);
        a.countD1 = this.countD1;
        a.countD2 = this.countD2;
        a.merged = false;
        /*
        if (child != null) {
            a.child = (Node) this.child.clone();
        } else {
            this.child = null;
        }*/
        if (this.child != null) {
            // copy the child node, but setting counts for child.items[i] to 0 
            a.child = new Node();
            for(Entry entry : this.child.getItems()){
                Entry copy = new Entry(entry.item);
                a.child.getItems().add(copy);
            }
            a.child.setItemNumber(this.child.itemNumber);
        } else {
            a.child = null;
        }

        return a;
    }

    /**
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * @param item the item to set
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * @return the countD1
     */
    public int getCountD1() {
        return countD1;
    }

    /**
     * @param countD1 the countD1 to set
     */
    public void setCountD1(int countD1) {
        this.countD1 = countD1;
    }

    /**
     * @return the countD2
     */
    public int getCountD2() {
        return countD2;
    }

    /**
     * @param countD2 the countD2 to set
     */
    public void setCountD2(int countD2) {
        this.countD2 = countD2;
    }

    /**
     * @return the child
     */
    public Node getChild() {
        return child;
    }

    /**
     * @param child the child to set
     */
    public void setChild(Node child) {
        this.child = child;
    }

}
