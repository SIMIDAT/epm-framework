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
package algorithms.dgcp_tree;

import framework.items.Item;
import framework.items.NominalItem;
import framework.utils.bsc_tree.BSCTree;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that represents a node of the DGCP-Tree
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class Node {

    /**
     * Checks wheter the node is the root of a DGCP-Tree
     */
    private boolean root;

    /**
     * The item stored in this node
     */
    private Item item;

    /**
     * The BSC-Tree that contains the path codes arrays for the positive dataset
     */
    private BSCTree pcArrPos;

    /**
     * The BSC-Tree that contains the path codes arrays for the negative dataset
     */
    private BSCTree pcArrNeg;

    /**
     * The childs of this node
     */
    private ArrayList<Node> child;

    public Node() {
        child = new ArrayList<>();
        root = false;
        pcArrNeg = null;
        pcArrPos = null;
    }

    public int numChilds() {
        return getChilds().size();
    }

    /**
     * Gets the node at the {@code pos} position
     *
     * @param pos
     * @return
     */
    public Node getChild(int pos) {
        return getChilds().get(pos);
    }

    /**
     * Adds a child at the end of the children list
     *
     * @param node
     */
    public void addChild(Node node) {
        getChilds().add(node);
    }

    /**
     * Sorts the children nodes according to the given support ratio values of
     * each item. This support ratio is a HashMap with an Item as key and a
     * double as value which correspond to the support-ratio of the item in the
     * dataset
     *
     * @param supportRatioValues
     */
    public void sortChilds(HashMap<Item, Double> supportRatioValues) {
        this.getChilds().sort((i1, i2) -> {
            double gr1 = supportRatioValues.get(i1.getItem());
            double gr2 = supportRatioValues.get(i2.getItem());
            if (gr1 > gr2) {
                return 1;
            } else if (gr1 < gr2) {
                return -1;
            } else {
                return 1 * ((NominalItem) i1.getItem()).compareTo((NominalItem) i2.getItem());
            }
        });
    }

    /**
     * Checks wheter the node is the root of a DGCP-Tree
     *
     * @return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * Set this node as root
     *
     */
    public void asRoot() {
        this.root = true;
    }

    /**
     * The item stored in this node
     *
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * The item stored in this node
     *
     * @param item the item to set
     */
    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * The BSC-Tree that contains the path codes arrays for the positive dataset
     *
     * @return the pcArrPos
     */
    public BSCTree getPcArrPos() {
        return pcArrPos;
    }

    /**
     * The BSC-Tree that contains the path codes arrays for the positive dataset
     *
     * @param pcArrPos the pcArrPos to set
     */
    public void setPcArrPos(BSCTree pcArrPos) {
        this.pcArrPos = pcArrPos;
    }

    /**
     * The BSC-Tree that contains the path codes arrays for the negative dataset
     *
     * @return the pcArrNeg
     */
    public BSCTree getPcArrNeg() {
        return pcArrNeg;
    }

    /**
     * The BSC-Tree that contains the path codes arrays for the negative dataset
     *
     * @param pcArrNeg the pcArrNeg to set
     */
    public void setPcArrNeg(BSCTree pcArrNeg) {
        this.pcArrNeg = pcArrNeg;
    }

    /**
     * @return the child
     */
    public ArrayList<Node> getChilds() {
        return child;
    }

    /**
     * @param child the child to set
     */
    public void setChilds(ArrayList<Node> child) {
        this.child = child;
    }
    

 
    public Node clone(){
        Node a = new Node();
        a.root = this.root;
        a.item = this.item;
        a.pcArrNeg = this.pcArrNeg;
        a.pcArrPos = this.pcArrPos;
        
        //a.child = this.child;
        return a;
    }
}
