/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework.utils.bsc_tree;

import java.util.ArrayList;

/**
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class BSCTreeNode {

    private int nodeLevel;
    private char bitType;
    private int oneBitCount;
    public int numBits = 0;
    private BSCTreeNode right;
    private BSCTreeNode left;

    public BSCTreeNode() {

    }

    public BSCTreeNode(int level, char type, int oneBitCount, int numBits) {
        this.nodeLevel = level;
        this.bitType = type;
        this.oneBitCount = oneBitCount;
        this.numBits = numBits;
        this.left = this.right = null;
    }

    /**
     * Copy constructor
     *
     * @param other The original object
     */
    public BSCTreeNode(BSCTreeNode other) {
        this.bitType = other.bitType;
        this.nodeLevel = other.nodeLevel;
        this.numBits = other.numBits;
        this.oneBitCount = other.oneBitCount;
        this.left = this.right = null;
    }

    /**
     * @return the nodeLevel
     */
    public int getNodeLevel() {
        return nodeLevel;
    }

    /**
     * @param nodeLevel the nodeLevel to set
     */
    public void setNodeLevel(int nodeLevel) {
        this.nodeLevel = nodeLevel;
    }

    /**
     * @return the bitType
     */
    public char getBitType() {
        return bitType;
    }

    /**
     * @param bitType the bitType to set
     */
    public void setBitType(char bitType) {
        this.bitType = bitType;
    }

    /**
     * @return the oneBitCount
     */
    public int getOneBitCount() {
        return oneBitCount;
    }

    /**
     * @param oneBitCount the oneBitCount to set
     */
    public void setOneBitCount(int oneBitCount) {
        this.oneBitCount = oneBitCount;
    }

    public ArrayList<BSCTreeNode> breakDown(int L, char bitType) {
        ArrayList<BSCTreeNode> result = new ArrayList<>();
        breakDown(this, L, bitType, result);
        return result;
    }

    private void breakDown(BSCTreeNode node, int L, char bitType, ArrayList<BSCTreeNode> results) {
        // If the number of bits in the node is greater than 1 and is not a power of two, go recurive
        if (node.numBits % Math.pow(2, L - 1.0) > 1 && !((node.numBits & (node.numBits - 1)) == 0)) {
            results.add(new BSCTreeNode(L, bitType, bitType == '1' ? (int) Math.pow(2, L - 2.0) : 0, (int) Math.pow(2, L - 2.0)));
            breakDown(new BSCTreeNode(L - 1, bitType, bitType == '1' ? node.oneBitCount - (int) Math.pow(2, L - 2.0) : 0, node.numBits - (int) Math.pow(2, L - 2.0)), L - 1, bitType, results);
        } else {
            // Base case, split into two nodes of the same type
            results.add(new BSCTreeNode(L, bitType, bitType == '1' ? node.numBits : 0, node.numBits));
        }
    }

    /**
     * @return the left
     */
    public BSCTreeNode getLeft() {
        return left;
    }

    /**
     * @param left the left to set
     */
    public void setLeft(BSCTreeNode left) {
        this.left = left;
    }

    /**
     * @return the right
     */
    public BSCTreeNode getRight() {
        return right;
    }

    /**
     * @param right the right to set
     */
    public void setRight(BSCTreeNode right) {
        this.right = right;
    }

    
    public void clean(){
        this.bitType = ' ';
        this.left = this.right = null;
        this.nodeLevel = 1;
        this.numBits = this.oneBitCount = 0;
    }
}
