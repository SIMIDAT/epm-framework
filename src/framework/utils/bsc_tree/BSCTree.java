/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package framework.utils.bsc_tree;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import javafx.util.Pair;

/**
 * Class that represents a Bit-String Compression Tree (BSC-Tree) This class
 * represents the bit string that represents if an item i appears in transaction
 * k.
 *
 * @author angel
 */
public class BSCTree {

    /**
     * The root of the BSCTree
     */
    private BSCTreeNode root;
    private ArrayList<Pair<String, Integer>> pathCodes;

    /**
     * Construct a BSC-Tree with the given bit string
     *
     * @param bitString The bit string
     */
    public BSCTree(String bitString) {
        generateTree(bitString, 2);
        pathCodes = getPathCodes();
    }

    /**
     * Generates the BSC-Tree
     *
     * @param bitString
     * @param N
     */
    private void generateTree(String bitString, int N) {
        int L = 1;
        int BSL = (int) Math.pow(N, L - 1.0);
        char currentBitType;
        BSCTreeNode temporaryNode = new BSCTreeNode();
        Stack<BSCTreeNode> stack = new Stack<>();
        int pos = 0;
        //for(char bit : bitString.toCharArray()){
        while (pos < bitString.length()) {
            currentBitType = bitString.charAt(pos);
            if (temporaryNode.numBits == 0) {
                temporaryNode.setBitType(currentBitType);
            }
            if (temporaryNode.numBits < BSL) {
                if (temporaryNode.getBitType() != currentBitType) {
                    // Break the node down decreasing the level of L correspondingly
                    ArrayList<BSCTreeNode> breakDown = temporaryNode.breakDown(L, currentBitType);
                    for (BSCTreeNode node : breakDown) {
                        stack.push(node);
                    }
                    stack.push(new BSCTreeNode(1, currentBitType, currentBitType == '1' ? 1 : 0, 1));
                    temporaryNode.clean();
                } else if (currentBitType == '1') {
                    temporaryNode.setOneBitCount(temporaryNode.getOneBitCount() + 1);
                }
                temporaryNode.numBits++;
                pos++;
            } else {
                // The num of bits is equal to BSL
                BSCTreeNode newNode = new BSCTreeNode(temporaryNode);
                newNode.setNodeLevel(L);
                stack.push(newNode);
                temporaryNode.clean();
                while (checkSameVirtualLevelNodes(stack)) {
                    BSCTreeNode node1 = stack.pop();
                    BSCTreeNode node2 = stack.pop();
                    BSCTreeNode merged = null;
                    if (node1.getBitType() == node2.getBitType() && node1.getBitType() != 'm' && node2.getBitType() != 'm') {
                        // Merge the nodes
                        merged = new BSCTreeNode(node1.getNodeLevel() + 1, node1.getBitType(), node1.getOneBitCount() + node2.getOneBitCount(), node1.numBits + node2.numBits);
                        // Upgrade the level of L
                        //L++;
                    } else {
                        // Merge the nodes
                        merged = new BSCTreeNode(node1.getNodeLevel() + 1, 'm', node1.getOneBitCount() + node2.getOneBitCount(), node1.numBits + node2.numBits);
                        // keep the poped nodes as children.
                        merged.setLeft(node2);
                        merged.setRight(node1);
                    }
                    stack.push(merged);
                }
            }
        }
        // CATCH THE LAST TEMPORARY NODE BIT AND ADD IT INTO THE STACK
        BSCTreeNode newNode = new BSCTreeNode(temporaryNode);
        newNode.setNodeLevel(L);
        stack.push(newNode);
        temporaryNode.clean();
        while (checkSameVirtualLevelNodes(stack)) {
            BSCTreeNode node1 = stack.pop();
            BSCTreeNode node2 = stack.pop();
            BSCTreeNode merged = null;
            if (node1.getBitType() == node2.getBitType() && node1.getBitType() != 'm' && node2.getBitType() != 'm') {
                // Merge the nodes
                merged = new BSCTreeNode(node1.getNodeLevel() + 1, node1.getBitType(), node1.getOneBitCount() + node2.getOneBitCount(), node1.numBits + node2.numBits);
                // Upgrade the level of L
                //L++;
            } else {
                // Merge the nodes
                merged = new BSCTreeNode(node1.getNodeLevel() + 1, 'm', node1.getOneBitCount() + node2.getOneBitCount(), node1.numBits + node2.numBits);
                // keep the poped nodes as children.
                merged.setLeft(node2);
                merged.setRight(node1);
            }
            stack.push(merged);
        }

        // JOIN THE NODES AND CREATE THE TREE
        while (!stack.empty()) {
            BSCTreeNode node1 = stack.pop();
            if (stack.empty()) {
                // Set node1 as root node
                root = node1;
            } else {
                // more than one tree left
                BSCTreeNode node2 = stack.pop();
                if (node1.getNodeLevel() == node2.getNodeLevel()) {
                    BSCTreeNode merged = new BSCTreeNode(node1.getNodeLevel() + 1, node1.getBitType() == node2.getBitType() ? node1.getBitType() : 'm', node1.getOneBitCount() + node2.getOneBitCount(), node1.numBits + node2.numBits);
                    // keep nodes as chidren
                    if (merged.getBitType() == 'm') {
                        merged.setLeft(node2);
                        merged.setRight(node1);
                    }
                    stack.push(merged);
                } else {
                    if (node1.getNodeLevel() < node2.getNodeLevel()) {
                        node1.setNodeLevel(node1.getNodeLevel() + 1);
                    } else {
                        node2.setNodeLevel(node2.getNodeLevel() + 1);
                    }
                    stack.push(node2);
                    stack.push(node1);
                }
            }
        }
    }

    /**
     * It checks wheter the stack has two nodes in the same virtual level.
     *
     * @param stack
     * @return
     */
    private static boolean checkSameVirtualLevelNodes(Stack<BSCTreeNode> stack) {
        if (stack.size() <= 1) {
            return false;
        }
        BSCTreeNode node1 = stack.get(stack.size() - 1);
        BSCTreeNode node2 = stack.get(stack.size() - 2);
        return node1.getNodeLevel() == node2.getNodeLevel();
    }

    public ArrayList<Pair<String, Integer>> getPathCodes() {
        ArrayList<Pair<String, Integer>> result = new ArrayList<>();
        getPathCodes(root, result, "");
        return result;
    }

    private void getPathCodes(BSCTreeNode node, ArrayList<Pair<String, Integer>> results, String pathCode) {
        if (node.getLeft() != null && node.getRight() != null) {
            // The node is not a leaf node
            // Go to the left child
            getPathCodes(node.getLeft(), results, pathCode + "0");
            // Go to the right child
            getPathCodes(node.getRight(), results, pathCode + "1");
        } else // The node is a leaf, check if it is a 1-bit leaf
        {
            if (node.getBitType() == '1') { // If 1-bit leaf node, store as a result
                Pair pair = new Pair(pathCode, node.getOneBitCount());
                results.add(pair);
            }
        }
    }

    /**
     * Performs the and operation of {@code this} and {@code other} and return
     * an array of path codes that is the resulting path code of result of
     * applying the and operator to this BSC-Trees
     *
     * @param other
     * @return The path code of the "and" BSC-Tree
     */
    private ArrayList<Pair<String, Integer>> and(BSCTree other) {
        ArrayList<Pair<String, Integer>> array3 = new ArrayList<>();
        int index1, index2, index3;
        index1 = index2 = index3 = 0;

        while (index1 < this.pathCodes.size() && index2 < other.pathCodes.size()) {
            // If the path codes pointed by index1 and index2 are the same in both arrays 1 and 2
            if (this.pathCodes.get(index1).getKey().equals(other.pathCodes.get(index2).getKey())) {
                // Store the path code pointed by index 1 in array3
                array3.add(this.pathCodes.get(index1));
                index1++;
                index2++;
                index3++;
            } else if (isSubCode(this.pathCodes.get(index1).getKey(), other.pathCodes.get(index2).getKey())) {
                // if the path code in array1 is the subcode of the pathcode of array2
                array3.add(this.pathCodes.get(index1));
                index1++;
            } else if (isSubCode(other.pathCodes.get(index2).getKey(), this.pathCodes.get(index1).getKey())) {
                array3.add(other.pathCodes.get(index2));
                index2++;
            } else if (isLarger(this.pathCodes.get(index1).getKey(), other.pathCodes.get(index2).getKey())) {
                index1++;
            } else {
                index2++;
            }
        }
        return array3;
    }

    /**
     * Gets the total sum of the 1-bit counts in all the pathcodes. This gives
     * the support of this item.
     *
     * @return
     */
    public int getCounts() {
        int counts = 0;
        for (Pair<String, Integer> pathCode : pathCodes) {
            counts += pathCode.getValue();
        }
        return counts;
    }

    /**
     * It returns whether {@code pathCode1} is a subcode of {@code pathCode2}
     *
     * @param pathCode1
     * @param pathCode2
     * @return
     */
    private boolean isSubCode(String pathCode1, String pathCode2) {
        if (pathCode2.length() > pathCode1.length()) {
            return false;
        }
        String regex = "^" + pathCode2 + ".*";

        return pathCode1.matches(regex);
    }

    /**
     * It returns whether {@code pathCode2} is larger than {@code pathCode1}
     *
     * @param pathCode1
     * @param pathCode2
     * @return
     */
    private boolean isLarger(String pathCode1, String pathCode2) {
        int minor = pathCode1.length() < pathCode2.length() ? pathCode1.length() : pathCode2.length();

        for (int i = 0; i < minor; i++) {
            if (pathCode2.charAt(i) > pathCode1.charAt(i)) {
                return true;
            } else if (pathCode2.charAt(i) < pathCode1.charAt(i)) {
                return false;
            }
        }

        return pathCode2.length() > pathCode1.length();
    }

    /**
     * It performs the ANDing operation of {@code this} with an undefined set of
     * BSC-Trees and returns the final counts
     *
     * @param trees
     * @return
     */
    public int treeANDing(ArrayList<BSCTree> trees) {
        if (trees.isEmpty()) {
            return this.getCounts();
        }
        int result = 0;

        BSCTree aux = new BSCTree("0");
        aux.pathCodes = this.and(trees.get(0));
        for (int i = 1; i < trees.size(); i++) {
            aux.pathCodes = aux.and(trees.get(i));
        }

        for (Pair<String, Integer> pair : aux.pathCodes) {
            result += pair.getValue();
        }

        return result;
    }

    /**
     * It checks if the item that represents this BSC-Tree covers {@code other},
     * i.e., if the pathCode of {@code this.and(other)} is equal to
     * {@code other.pathCodes} and it means that {@code this} covers {@code other}
     *
     * @param other
     * @return
     */
    public boolean covers(BSCTree other) {
        // Perfoms the and
        ArrayList<Pair<String, Integer>> and = this.and(other);
        
        if (and.isEmpty() && !other.pathCodes.isEmpty()) {
            return false;
        }
        if (and.size() != other.pathCodes.size()) {
            return false;
        }
        
        // Checks if the and pathCode is equal to the path code of other
        for (Pair<String, Integer> pathCode : and) {
            if (!other.pathCodes.contains(pathCode)) {
                return false;
            }
        }

        return true;

    }

    public BSCTree And(BSCTree other) {
        BSCTree result = new BSCTree("");
        result.pathCodes = this.and(other);
        return result;
    }
}
