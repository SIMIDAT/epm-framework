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

/**
 *
 * @author Ángel M. García-Vico (agvico@ujaen.es)
 * @version 1.0
 * @since JDK 1.8
 */
public class NominalItem extends Item {

    protected String value;

    /**
     * NominalItem constructor that it does not set the growthrate
     *
     * @param variable
     * @param value
     */
    public NominalItem(String variable, String value) {
        this.variable = variable;
        this.value = value;
        this.growthRate = 0;
    }

    /**
     * NominalItem constructor that allows to set the growth rate of an item
     *
     * @param variable
     * @param value
     * @param growthRate
     */
    public NominalItem(String variable, String value, double growthRate) {
        this.variable = variable;
        this.value = value;
        this.growthRate = growthRate;
    }

    /**
     * Copy constructor of NominalItem
     *
     * @param orig
     */
    public NominalItem(NominalItem orig) {
        this.growthRate = orig.growthRate;
        this.value = orig.value;
        this.variable = orig.variable;
    }

    @Override
    public int compareTo(Item o) {
        // lexicographicall order
        NominalItem a = (NominalItem) o;
        if (this.getVariable().compareTo(a.getVariable()) == 0) {
            return this.getValue().compareTo(a.getValue());
        } else {
            return this.getVariable().compareTo(a.getVariable());
        }
    }

    @Override

    public boolean equals(Object other) {
        NominalItem o = (NominalItem) other;
        return this.getVariable().equals(o.getVariable()) && this.getValue().equals(o.getValue());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.getValue());
        hash = hash + super.hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return this.getVariable() + " = " + this.getValue();
    }

    @Override
    public boolean covers(Item itemInstance) {
        if (itemInstance instanceof NominalItem) {
            return this.equals(itemInstance);
        } else {
            return false;
        }
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

}
