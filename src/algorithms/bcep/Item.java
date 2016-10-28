/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.bcep;

import framework.exceptions.InvalidProbabilityException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import keel.Dataset.Attributes;
import keel.Dataset.Instance;
import keel.Dataset.InstanceSet;
import framework.utils.Fuzzy;

/**
 *
 * @author angel
 */
public class Item implements Comparable<Item>, Serializable {

    private final int itemID;              // The ID of the item
    private final String value;            // the value of the variable (nominal) 
    private float valueNum;            // the value of the variable (numeric)
    private Fuzzy valueFuzzy;            // the value of the variable (fuzzy)
    private String variable;         // The name of the variable to represent
    private double growthRate;       // The growthRate of the item
    private int type;                // The type of the item (nominal, real or fuzzy)

    private final ArrayList<Float> probabilitiesPerClass; // The probability of each item for each class for BCEP algorithm

    private int D1count;             // Counts of the actual item in the node  for class 1
    private int D2count;             // Counts of the actual item in the node  for class 2

    public static int NOMINAL_ITEM = 0;
    public static int REAL_ITEM = 1;
    public static int FUZZY_ITEM = 2;

    public CPTreeNode child;         // the child of the node

    /**
     * Copy constructor
     *
     * @param other The Item to get the copy.
     */
    public Item(Item other) {
        this.D1count = other.D1count;
        this.D2count = other.D2count;
        this.value = other.value;
        this.variable = other.variable;
        this.growthRate = other.growthRate;
        this.itemID = other.itemID;
        if (this.child == null) {
            this.child = null;
        } else {
            this.child = new CPTreeNode(other.child);
        }
        this.probabilitiesPerClass = (ArrayList<Float>) other.probabilitiesPerClass.clone();
        this.valueNum = other.valueNum;
        this.valueFuzzy = other.valueFuzzy;
        this.type = other.type;
    }

    public Item(int id, String value, String variable, double gr) {
        itemID = id;
        this.value = value;
        this.variable = variable;
        growthRate = gr;
        D1count = 0;
        D2count = 0;
        this.probabilitiesPerClass = new ArrayList<>();
        this.valueNum = Float.NaN;
        this.valueFuzzy = null;
        this.type = NOMINAL_ITEM;
    }

    public Item(String variable, String value) {
        itemID = 0;
        this.value = value;
        this.variable = variable;
        growthRate = 0;
        D1count = 0;
        D2count = 0;
        this.probabilitiesPerClass = new ArrayList<>();
        this.valueNum = Float.NaN;
        this.valueFuzzy = null;
        this.type = NOMINAL_ITEM;
    }

    /**
     * Constructor for numeric variable
     *
     * @param variable
     * @param value
     */
    public Item(String variable, float value) {
        this.variable = variable;
        this.valueNum = value;
        this.value = null;
        this.valueFuzzy = null;
        this.D1count = 0;
        this.D2count = 0;
        this.probabilitiesPerClass = new ArrayList<>();
        this.growthRate = 0;
        this.itemID = 0;
        this.type = REAL_ITEM;
    }

    /**
     * Constructor for Fuzzy Item
     *
     * @param variable
     * @param x0
     * @param x1
     * @param x2
     * @param y
     */
    public Item(String variable, float x0, float x1, float x2, float y) {
        this.variable = variable;
        this.valueNum = Float.NaN;
        this.value = null;
        this.valueFuzzy = new Fuzzy();
        this.valueFuzzy.setVal(x0, x1, x2, y);
        this.D1count = 0;
        this.D2count = 0;
        this.probabilitiesPerClass = new ArrayList<>();
        this.growthRate = 0;
        this.itemID = 0;
        this.type = FUZZY_ITEM;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof Item)) {
            return false;
        }

        Item obj = (Item) other;

        //boolean c3 = this.growthRate == obj.growthRate;
        //boolean c4 = this.getItemID() == obj.getItemID();
        boolean c5;
        if (type == NOMINAL_ITEM) {
            c5 = this.value.equals(obj.value);
        } else if (type == REAL_ITEM) {
            c5 = this.valueNum == obj.valueNum;
        } else {
            c5 = this.valueFuzzy.equals(obj.valueFuzzy);
        }
        boolean c6 = this.variable.equals(obj.variable);
        return /*c3 && c4 &&*/ c5 && c6;
    }

    @Override
    public int compareTo(Item o) {
        if (this.getGrowthRate() > o.getGrowthRate()) {
            return -1;
        }

        if (this.getGrowthRate() < o.getGrowthRate()) {
            return 1;
        }

        return 0;
    }

    @Override
    public String toString() {
        if (type == NOMINAL_ITEM) {
            return getVariable() + " = " + getValue();
        } else if (type == FUZZY_ITEM) {
            return variable + " in (" + valueFuzzy.getX0() + ", " + valueFuzzy.getX1() + ", " + valueFuzzy.getX3() + ")";
        } else if (type == REAL_ITEM) {
            return variable + " = " + valueNum;
        }
        return "";
    }

    public void incrementsD1() {
        D1count++;
    }

    public void incrementsD2() {
        D2count++;
    }

    /**
     * @return the D1count
     */
    public int getD1count() {
        return D1count;
    }

    /**
     * @return the D2count
     */
    public int getD2count() {
        return D2count;
    }

    /**
     * @param D1count the D1count to set
     */
    public void setD1count(int D1count) {
        this.D1count = D1count;
    }

    /**
     * @param D2count the D2count to set
     */
    public void setD2count(int D2count) {
        this.D2count = D2count;
    }

    /**
     * @return the itemID
     */
    public int getItemID() {
        return itemID;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the variable
     */
    public String getVariable() {
        return variable;
    }

    public float getProbabilityForClass(int clas) {
        return this.getProbabilitiesPerClass().get(clas);
    }

    public void addProbability(float prob) {
        try {
            if (prob <= 1 && prob >= 0) {
                this.getProbabilitiesPerClass().add(prob);
            } else {
                throw new framework.exceptions.InvalidProbabilityException();
            }
        } catch (InvalidProbabilityException ex) {
            System.out.println("FATAL ERROR: PROBILITY IS NOT IN [0,1]. ABORTING...");
            System.exit(-1);
        }
    }

    /**
     * Finds an item into an ArrayList of items by variable - value pairs
     *
     * @param items
     * @param variable
     * @param value
     * @return
     */
    public static Item find(ArrayList<Item> items, String variable, String value) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).value.equals(value) && items.get(i).variable.equals(variable)) {
                return items.get(i);
            }
        }
        return null;
    }

    public void calculateProbabilities(InstanceSet data, String correction) {
        float k = 1; // For Laplace-estimate
        float n0 = 5; // For M-estimate
        // Examples that contains the itemset for each class
        int[] counts_classItemset = new int[Attributes.getOutputAttribute(0).getNumNominalValues()];
        // The number of examples for each class
        int[] counts_class = new int[Attributes.getOutputAttribute(0).getNumNominalValues()];
        // the number of examples that have got the itemset.
        int count_itemset = 0;
        for (int i = 0; i < counts_classItemset.length; i++) {
            counts_classItemset[i] = 0;
            counts_class[i] = 0;
        }

        // for each instance
        for (Instance inst : data.getInstances()) {
            // find if the Item exists in the instance.
            // update the number of examples per class counter;

            counts_class[inst.getOutputNominalValuesInt(0)]++;

            for (int i = 0; i < Attributes.getNumAttributes(); i++) {
                // Check the counts itemset and itemset per class
                if (Attributes.getAttribute(i).getName().equals(this.variable)) {
                    if (inst.getInputNominalValues(i).equals(this.value)) {
                        // Match found, get the class and update the counts
                        counts_classItemset[inst.getOutputNominalValuesInt(0)]++;
                        count_itemset++;
                    }
                }
            }
        }

        // Compute the probability with the given estimator.
        switch (correction) {
            case "Laplace":
                for (int i = 0; i < counts_class.length; i++) {
                    float prob = ((float) counts_class[i] + k) / ((float) data.getNumInstances() + (float) counts_class.length * (float) k);
                    this.getProbabilitiesPerClass().add(prob);
                }
                break;
            case "M":
                for (int i = 0; i < counts_classItemset.length; i++) {
                    float prob = ((float) counts_classItemset[i] + n0 * ((float) count_itemset / (float) data.getNumInstances())) / (counts_class[i] + n0);
                    this.getProbabilitiesPerClass().add(prob);
                }
                break;
        }

    }

    /**
     * @return the valueNum
     */
    public float getValueNum() {
        return valueNum;
    }

    /**
     * @param valueNum the valueNum to set
     */
    public void setValueNum(float valueNum) {
        this.valueNum = valueNum;
    }

    /**
     * @return the valueFuzzy
     */
    public Fuzzy getValueFuzzy() {
        return valueFuzzy;
    }

    /**
     * @param valueFuzzy the valueFuzzy to set
     */
    public void setValueFuzzy(Fuzzy valueFuzzy) {
        this.valueFuzzy = valueFuzzy;
    }

    /**
     * @param variable the variable to set
     */
    public void setVariable(String variable) {
        this.variable = variable;
    }

    /**
     * @return the growthRate
     */
    public double getGrowthRate() {
        return growthRate;
    }

    /**
     * @param growthRate the growthRate to set
     */
    public void setGrowthRate(double growthRate) {
        this.growthRate = growthRate;
    }

    /**
     * @return the probabilitiesPerClass
     */
    public ArrayList<Float> getProbabilitiesPerClass() {
        return probabilitiesPerClass;
    }

    /**
     * @return the type
     */
    public int getType() {
        return type;
    }

}
