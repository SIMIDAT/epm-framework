/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sjep_classifier;

import java.util.ArrayList;
import keel.Dataset.Attributes;

/**
 *
 * @author angel
 */
public class Pattern {
    private ArrayList<Item> items;
    private int support;
    private int clase;
    
    public Pattern(ArrayList<Item> items, int supp, int clase){
        this.items = new ArrayList<Item>(items);
        support = supp;
        this.clase = clase;
    }
    
    /**
     * Checks if the current pattern covers a given instance
     * @param instance
     * @return true if the pattern covers the instance
     */
    public boolean covers(ArrayList<Item> instance){
        // for each item in the pattern
        for(Item item : getItems()){
            // look for each item on instance to check if exists
            boolean exists = false;
            for(Item instanceItem : instance){
                if(item.equals(instanceItem)){
                    exists = true;
                }
            }
            if(! exists){
                return false;
            }
        }
        // Return true, the pattern match the instance.
        return true;
    }

    /**
     * @return the items
     */
    public ArrayList<Item> getItems() {
        return items;
    }

    /**
     * @return the support
     */
    public int getSupport() {
        return support;
    }

    /**
     * @return the clase
     */
    public int getClase() {
        return clase;
    }
    
    
    
    @Override
    public String toString(){
        String result = "IF ";
        for(int i = 0; i < items.size() - 1; i++){
            result += items.get(i).toString() + " AND ";
        }
        
        result += items.get(items.size()-1).toString();
        return result + " THEN " + Attributes.getOutputAttribute(0).getNominalValue(clase);
    }

    /**
     * @param clase the clase to set
     */
    public void setClase(int clase) {
        this.clase = clase;
    }
}
