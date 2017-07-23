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
import framework.items.Pattern;
import java.util.HashMap;

/**
 * CP-Tree data structure that allows to store information about the counts of a
 * two-class problem, it needs the information about the support-ratio for each
 * item that appear in the dataset to perform the sorting of the nodes
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class CPTree {

    /**
     * The root of the CPTree
     */
    private Node root;

    /**
     * Default constructor. Creates an empty root node.
     */
    public CPTree() {
        root = new Node();
    }

    /**
     * Adds this pattern on the CP-Tree. Updating the tree and its counts.
     *
     * @param pattern
     * @param supportRatio The support-ratio values for each item in the dataset to perform sorting of nodes
     */
    public void insert(Pattern pattern, HashMap<Item, Double> supportRatio) {
        root.insert(pattern, root, supportRatio);
    }

    /**
     * @return the root
     */
    public Node getRoot() {
        return root;
    }
    
    public void clear(){
        root = new Node();
    }

}
