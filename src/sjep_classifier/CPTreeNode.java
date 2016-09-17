/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sjep_classifier;

import java.util.ArrayList;

/**
 *
 * @author angel
 */
public class CPTreeNode {
    
    public int itemNumber;                      // The number of items in the node
    public ArrayList<Item> items;               // The items of the node
    
    
 public CPTreeNode(int iNumber){
     itemNumber = iNumber;
     items = new ArrayList<>();
 }

    CPTreeNode(CPTreeNode other) {
        this.itemNumber = other.itemNumber;
        this.items = new ArrayList<>(other.items);
    }
    
}
