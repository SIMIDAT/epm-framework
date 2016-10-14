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
package tree_based_jep;

import java.util.ArrayList;
import utils.Item;

/**
 *
 * @author Ángel M. García-Vico (agvico@ujaen.es)
 * @version 1.0
 * @since JDK 1.8
 */
public class Tree {

    /**
     * @param aSimpleItems the simpleItems to set
     */
    public static void setSimpleItems(ArrayList<Item> aSimpleItems) {
        simpleItems = aSimpleItems;
    }

    /**
     * @return the node_link
     */
    public static ArrayList<Tree> getNode_link() {
        return node_link;
    }

    private boolean root;
    private ArrayList<Tree> children;
    private Item item;
    private int[] count;
    private Tree parent;  // link to the parent node.
    private Tree nextEqual;
    private boolean visited;

    private static ArrayList<Tree> node_link;
    private static ArrayList<Item> simpleItems;

    public Tree(Item item, int clas, int nClasses, Tree parent) {
        visited = false;
        root = false;
        children = new ArrayList<>();
        this.item = item;
        count = new int[nClasses];
        for (int i = 0; i < nClasses; i++) {
            count[i] = 0;
        }
        count[clas]++;
        this.parent = parent;
    }

    public static void initializeNodeLinks(int size) {
        node_link = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            getNode_link().add(null);
        }
    }

    public static boolean addInNodeLink(Tree node) {
        int index = simpleItems.indexOf(node.item);
        if (getNode_link().get(index) == null) {
            getNode_link().set(index, node);
            return true;
        }
        return false;
    }

    public static int getIndexOf(Item a) {
        return simpleItems.indexOf(a);
    }

    /**
     * @return the root
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * @param root the root to set
     */
    public void setRoot(boolean root) {
        this.root = root;
    }

    /**
     * @return the children
     */
    public ArrayList<Tree> getChildren() {
        return children;
    }

    /**
     * @return the item
     */
    public Item getItem() {
        return item;
    }

    /**
     * @return the count
     */
    public int getCount(int pos) {
        return count[pos];
    }

    /**
     * @param count the count to set
     */
    public void setCount(int pos, int value) {
        this.count[pos] = value;
    }

    /**
     * Inserts an instance into the tree
     *
     * @param it The instances
     * @param clas The class of the instance
     */
    public void insert_tree(ArrayList<Item> it, int clas) {
        insert_tree(it, clas, this);
    }

    /**
     * Private function to insert an instance into the tree
     *
     * @param it the instance
     * @param clas the class of the instance
     * @param node The node to insert the item.
     */
    private void insert_tree(ArrayList<Item> it, int clas, Tree node) {
        this.nextEqual = null;
        // if "it" contains a child
        if (!it.isEmpty()) {
            ArrayList<Item> clone = (ArrayList<Item>) it.clone();
            boolean exist = false;
            int children_index = -1;
            // search if the item exist in node's children.
            for (int i = 0; i < node.children.size() && !exist; i++) {
                if (node.children.get(i).item.equals(it.get(0))) {
                    exist = true;
                    children_index = i;
                }
            }

            if (!exist) {
                // add new children
                Tree t = new Tree(it.get(0), clas, 2, node);
                node.children.add(t);
                children_index = node.children.size() - 1;
                // Add node the node-link structure
                if(!Tree.addInNodeLink(t)){
                    // if there are an elemente on the node-head list, get the last element and set the nextEqual to this one.
                    int index = simpleItems.indexOf(t.item);
                    Tree first = node_link.get(index);
                    while(first.getNextEqual() != null){
                        first = first.getNextEqual();
                    }
                    first.nextEqual = t;
                }
                // TODO
            } else {
                // node exists, incremets count of the children.
                node.children.get(children_index).count[clas]++;
            }
            clone.remove(0);
            insert_tree(clone, clas, node.children.get(children_index));
        }
    }

    /**
     * @return the visited
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * @param visited the visited to set
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * @return the nextEqual
     */
    public Tree getNextEqual() {
        return nextEqual;
    }

    /**
     * @return the parent
     */
    public Tree getParent() {
        return parent;
    }

}
