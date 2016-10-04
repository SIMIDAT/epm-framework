/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utils;

import java.io.Serializable;
import sjep_classifier.*;
import java.util.ArrayList;
import keel.Dataset.Attributes;
import keel.Dataset.Instance;
import keel.Dataset.InstanceSet;

/**
 *
 * @author angel
 */
public class Pattern implements Serializable{

    private ArrayList<Item> items;
    private int support; // support counts for SJEP-C
    private float growthRate;
    private float strength;
    private float supp; // support quality measure.
    private int clase; // the class of the pattern
    
    public Pattern clone(){
        Pattern result = new Pattern((ArrayList<Item>) this.items.clone(), this.support, this.clase);
        result.growthRate = this.growthRate;
        result.strength = this.strength;
        result.supp = this.supp;
        return result;
    }
    public Pattern(ArrayList<Item> items, int supp, int clase) {
        this.items = new ArrayList<Item>(items);
        support = supp;
        this.clase = clase;
    }

    /**
     * Checks if the current pattern covers a given instance
     *
     * @param instance
     * @return true if the pattern covers the instance
     */
    public boolean covers(ArrayList<Item> instance) {
        // for each item in the pattern
        for (Item item : getItems()) {
            // look for each item on instance to check if exists
            boolean exists = false;
            for (Item instanceItem : instance) {
                if (item.equals(instanceItem)) {
                    exists = true;
                }
            }
            if (!exists) {
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

    /**
     * Calculates the support, growth rate and strength of the given pattern for
     * the training set.
     *
     * @param training
     */
    public void calculateMeasures(InstanceSet training) {
        float tp = 0;
        float fp = 0;
        float tn = 0;
        float fn = 0;

        for (Instance inst : training.getInstances()) {
            boolean covers = true;
            for (Item it : this.items) {
                boolean exist = false;
                for (int i = 0; i < Attributes.getInputNumAttributes(); i++) {
                    if (it.getVariable().equals(Attributes.getInputAttribute(i).getName())) {
                        if (it.getValue().equals(inst.getInputNominalValues(i))) {
                            exist = true;
                        }
                    }
                }
                if (!exist) {
                    covers = false;
                    break;
                }
            }

            if (covers) {
                if (clase == inst.getOutputNominalValuesInt(0)) {
                    tp++;
                } else {
                    fp++;
                }
            } else if (clase != inst.getOutputNominalValuesInt(0)) {
                tn++;
            } else {
                fn++;
            }
        }

        // Compute measures
        if ((tp / (tp + fn)) != 0 && (fp / (fp + tn)) == 0) {
            this.growthRate = Float.POSITIVE_INFINITY;
        } else if ((tp / (tp + fn)) == 0 && (fp / (fp + tn)) == 0) {
            this.growthRate = 0;
        } else {
            this.growthRate = (tp / (tp + fn)) / (fp / (fp + tn));
        }

        this.supp = tp / (tp + tn + fp + fn);
        
        if (this.growthRate == Float.POSITIVE_INFINITY) {
            this.strength = Float.POSITIVE_INFINITY;
        } else if (this.growthRate == 0 || this.supp == 0) {
            this.strength = 0;
        } else {
            this.strength = (this.growthRate / (this.growthRate + 1)) * supp;
        }
    }

    @Override
    public String toString() {
        String result = "IF ";
        for (int i = 0; i < items.size() - 1; i++) {
            result += items.get(i).toString() + " AND ";
        }

        result += items.get(items.size() - 1).toString();
        return result + " THEN " + Attributes.getOutputAttribute(0).getNominalValue(clase);
    }

    /**
     * @param clase the clase to set
     */
    public void setClase(int clase) {
        this.clase = clase;
    }

    /**
     * @return the growthRate
     */
    public float getGrowthRate() {
        return growthRate;
    }

    /**
     * @return the strength
     */
    public float getStrength() {
        return strength;
    }

    /**
     * @return the supp
     */
    public float getSupp() {
        return supp;
    }

}
