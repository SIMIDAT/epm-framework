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
import java.util.Objects;
import keel.Dataset.Attribute;
import keel.Dataset.Instance;

/**
 *
 * @author Ángel M. García-Vico (agvico@ujaen.es)
 * @version 2.0
 * @since JDK 1.8
 */
public abstract class Item implements Comparable<Item>, Serializable {

    protected String variable;
    protected double growthRate;

    @Override
    public abstract int compareTo(Item o);

    @Override
    public abstract boolean equals(Object other);

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.getVariable());
        //hash = 17 * hash + (int) (Double.doubleToLongBits(this.growthRate) ^ (Double.doubleToLongBits(this.growthRate) >>> 32));
        return hash;
    }

    @Override
    public abstract String toString();

    /**
     * It returns whether the item covers the given Instance Item
     *
     * @return
     */
    public abstract boolean covers(Item itemInstance);

   

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
     * @return the variable
     */
    public String getVariable() {
        return variable;
    }

}
