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

import java.util.Objects;
import framework.utils.Fuzzy;

/**
 *
 * @author Ángel M. García-Vico (agvico@ujaen.es)
 * @version 1.0
 * @since JDK 1.8
 */
public class FuzzyItem extends Item {

    private Fuzzy value;
    private String label;

    /**
     * Constructor without label and growth rate
     *
     * @param variable
     * @param value
     */
    public FuzzyItem(String variable, Fuzzy value) {
        super.variable = variable;
        this.value = new Fuzzy();
        this.value.setVal(value.getX0(), value.getX1(), value.getX3(), 1);
    }

    /**
     * Constructor without growth rate
     *
     * @param variable
     * @param value
     * @param label
     */
    public FuzzyItem(String variable, Fuzzy value, String label) {
        super.variable = variable;
        this.value = new Fuzzy();
        this.value.setVal(value.getX0(), value.getX1(), value.getX3(), 1);
        this.label = label;
    }

    /**
     * Full FuzzyItem constructor.
     *
     * @param variable
     * @param value
     * @param label
     * @param growthRate
     */
    public FuzzyItem(String variable, Fuzzy value, String label, double growthRate) {
        super.variable = variable;
        this.value = new Fuzzy();
        this.value.setVal(value.getX0(), value.getX1(), value.getX3(), 1);
        this.label = label;
        this.growthRate = growthRate;
    }

    public FuzzyItem(FuzzyItem orig) {
        this.growthRate = orig.growthRate;
        this.label = orig.label;
        this.value = new Fuzzy();
        this.value.setVal(orig.value.getX0(), orig.value.getX1(), orig.value.getX3(), 1);
        this.variable = orig.variable;
    }

    @Override
    public int compareTo(Item o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean equals(Object other) {
        FuzzyItem o = (FuzzyItem) other;
        return this.getVariable().equals(o.getVariable()) && this.value.equals(o.value);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.value);
        hash = 17 * hash + Objects.hashCode(this.label);
        hash += super.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return this.getVariable() + " = " + this.label + " (" + value.getX0() + ", " + value.getX1() + ", " + value.getX3() + ")";
    }

    @Override
    public boolean covers(Item itemInstance) {
        if (itemInstance instanceof FuzzyItem) {
            FuzzyItem other = (FuzzyItem) itemInstance;
            
            // A fuzzy item, in this case, covers another item if the values that defines the fuzzy set are equal
            // (Maybe, I it is more correct to say if the borders of the fuzzy set are contained in other... )
            return this.value.getX0() == other.value.getX0() &&
                    this.value.getX1() == other.value.getX1() &&
                    this.value.getX3() == other.value.getX3();
        } else {
            return false;
        }
    }

}
