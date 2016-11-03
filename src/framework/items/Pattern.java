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
package framework.items;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import keel.Dataset.Attribute;
import keel.Dataset.Attributes;
import keel.Dataset.Instance;

/**
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 2.0
 * @since JDK 1.8
 */
public class Pattern implements Serializable {

    protected ArrayList<Item> items;
    protected int clase;
    protected HashMap<String, Double> tra_measures;
    protected HashMap<String, Double> tst_measures;

    @Override
    public Pattern clone() {
        Pattern p = new Pattern((ArrayList<Item>) this.items.clone(), clase);
        p.setTra_measures(this.tra_measures);
        p.setTst_measures(this.tst_measures);
        return p;
    }

    /**
     * Default constructor
     *
     * @param items
     * @param clase
     */
    public Pattern(ArrayList<Item> items, int clase) {
        this.items = new ArrayList<>();
        this.items.addAll(items);
        this.clase = clase;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (!(other instanceof Pattern)) {
            return false;
        }

        Pattern pat = (Pattern) other;
        if (this.clase != pat.clase) {
            return false;
        }

        // If contains this pattern all items of other, is equal.
        for (Item it : this.items) {
            if (!pat.items.contains(it)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash;
        for (Item it : items) {
            hash += it.hashCode();
        }
        return hash;
    }

    /**
     * Returns whether a pattern covers an example (viewed as a Pattern itself)
     *
     * @param instance
     * @return
     */
    public boolean covers(Pattern instance) {
        for (Item it : items) {
            boolean covered = false;
            for (Item it2 : instance.items) {
                if (it.covers(it2)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns whether a pattern covers an example (Direct InstanceSet version)
     *
     * @param instance
     * @param inputAttrs
     * @param alpha
     * @return
     */
    public boolean covers(Instance instance, Attribute[] inputAttrs) {
        for (Item item : this.items) {
            boolean covered = false;

            for (int i = 0; i < inputAttrs.length; i++) {
                Item it;
                if (inputAttrs[i].getType() == Attribute.NOMINAL) {
                    it = new NominalItem(inputAttrs[i].getName(), instance.getInputNominalValues(i));
                } else {
                    it = new NumericItem(inputAttrs[i].getName(), instance.getInputRealValues(i), 0);
                }

                if (item.covers(it)) {
                    covered = true;
                    break;
                }
            }
            if (!covered) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds an item to the pattern (without duplicates)
     *
     * @param it The item to be added
     * @return {@code true} if the item has been inserted, {@code false}
     * elsewhere.
     */
    public boolean add(Item it) {
        if (!items.contains(it)) {
            items.add(it);
            return true;
        }

        return false;
    }

    /**
     * Performs the Union of this pattern with other
     *
     * @param other
     * @param clas
     * @return
     */
    public Pattern merge(Pattern other, int clas) {
        Pattern result = new Pattern(new ArrayList<Item>(), clas);
        for (int i = 0; i < items.size(); i++) {
            result.add(this.getItems().get(i));
        }

        for (int i = 0; i < other.getItems().size(); i++) {
            result.add(other.getItems().get(i));
        }

        return result;
    }

    /**
     * Perform the difference of this pattern with other, i.e., it return a
     * pattern which elements are in this but not in other
     *
     * @param other
     * @param clas
     * @return
     */
    public Pattern difference(Pattern other) {
        Pattern result = new Pattern(new ArrayList<Item>(), other.clase);
        for (int i = 0; i < items.size(); i++) {
            if (!other.items.contains(items.get(i))) {
                result.items.add(items.get(i));
            }
        }

        return result;
    }

    /**
     * Reverse the pattern
     *
     * @return
     */
    public Pattern reverse() {
        ArrayList<Item> its = new ArrayList<>();
        for (int i = items.size() - 1; i >= 0; i--) {
            its.add(items.get(i));
        }

        return new Pattern(its, clase);
    }

    /**
     * Drops an item of the pattern
     *
     * @param it The item to be removed
     * @return {@code  true} if the item is removed succesfully, {@code false}
     * elsewhere.
     */
    public boolean drop(Item it) {
        return items.remove(it);
    }
    public void drop(int i) {
        items.remove(i);
    }

    /**
     * Returns the item at the {@code index} position
     *
     * @param index
     * @return
     */
    public Item get(int index) {
        return items.get(index);
    }

    /**
     * @return the clase
     */
    public int getClase() {
        return clase;
    }

    /**
     * @param clase the clase to set
     */
    public void setClase(int clase) {
        this.clase = clase;
    }

    /**
     * @return the tra_measures
     */
    public HashMap<String, Double> getTra_measures() {
        return tra_measures;
    }

    /**
     * @param tra_measures the tra_measures to set
     */
    public void setTra_measures(HashMap<String, Double> tra_measures) {
        this.tra_measures = tra_measures;
    }

    /**
     * @return the tst_measures
     */
    public HashMap<String, Double> getTst_measures() {
        return tst_measures;
    }

    /**
     * @param tst_measures the tst_measures to set
     */
    public void setTst_measures(HashMap<String, Double> tst_measures) {
        this.tst_measures = tst_measures;
    }

    /**
     * @return the items
     */
    public ArrayList<Item> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    /**
     * Returns the number of items in the pattern
     *
     * @return
     */
    public int length() {
        return items.size();
    }

    @Override
    public String toString() {
        if(items.isEmpty()) return "Empty pattern";
        String result = "IF ";
        for (int i = 0; i < items.size() - 1; i++) {
            result += items.get(i).toString() + " AND ";
        }

        result += items.get(items.size() - 1).toString() + " THEN " + Attributes.getOutputAttribute(0).getNominalValue(clase);
        return result;
    }
    
    /**
     * Gets the given training measure
     * @param value
     * @return 
     */
    public double getTraMeasure(String value) {
        return tra_measures.get(value);
    }

}
