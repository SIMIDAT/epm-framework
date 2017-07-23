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

/**
 *
 * @author Ángel M. García-Vico (agvico@ujaen.es)
 * @version 1.0
 * @since JDK 1.8
 */
public class NumericItem extends Item
{

    protected double value;

    protected double alpha;

    protected String operator = " = ";

    /**
     * Constructor of NumericItem without growthrate and alpha
     *
     * @param variable
     * @param value
     * @param op
     */
    public NumericItem (String variable, double value, String op)
    {
        super.variable = variable;
        this.value = value;
        this.alpha = 0;
        this.operator = op;
        super.growthRate = 0;
    }

    /**
     * Constructor of NumericItem without growthrate
     *
     * @param variable
     * @param value
     * @param alpha
     */
    public NumericItem (String variable, double value, double alpha)
    {
        super.variable = variable;
        this.value = value;
        this.alpha = alpha;
        super.growthRate = 0;
    }

    /**
     * Constructor of NumericItem with growthRate
     *
     * @param variable
     * @param value
     * @param alpha
     * @param growthRate
     */
    public NumericItem (String variable, double value, double alpha, double growthRate)
    {
        super.variable = variable;
        this.value = value;
        this.alpha = alpha;
        super.growthRate = growthRate;
    }

    /**
     * Copy constructor
     *
     * @param orig
     */
    public NumericItem (NumericItem orig)
    {
        super.variable = orig.variable;
        super.growthRate = orig.growthRate;
        this.value = orig.value;
        this.alpha = orig.alpha;
        this.operator = orig.operator;
    }

    @Override
    public int compareTo (Item o)
    {
        NumericItem other = (NumericItem) o;
        if (other.getVariable().equals(this.getVariable())) {
            return ((Double) this.getValue()).compareTo(other.getValue());
        } else {
            return this.getVariable().compareTo(o.getVariable());
        }
    }

    @Override
    public boolean equals (Object other)
    {
        NumericItem o = (NumericItem) other;
        return this.getVariable().equals(o.getVariable()) && this.getValue() == o.getValue();
    }

    @Override
    public int hashCode ()
    {
        int hash = 5;
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
        hash = 37 * hash + (int) (Double.doubleToLongBits(this.alpha) ^ (Double.doubleToLongBits(this.alpha) >>> 32));
        hash += super.hashCode();
        return hash;
    }

    @Override
    public String toString ()
    {   
        return this.getVariable() + operator + this.getValue();
    }

    @Override
    public boolean covers (Item itemInstance)
    {
        if (!(itemInstance instanceof NumericItem)) {
            return false;
        }

        NumericItem other = (NumericItem) itemInstance;
        if(!this.variable.equals(other.variable))
            return false;
        
        double minBound = this.getValue() - getAlpha();
        double maxBound = this.getValue() + getAlpha();
        if(alpha > 0){
            return other.getValue() >= minBound && other.getValue() <= maxBound;
        } else {
            switch(operator){
                case " = ":
                    return other.getValue() == this.value;
                case " != ":
                    return other.getValue() != this.value;
                case " > ":
                    return other.getValue() > this.value;
                case " <= ":
                        return other.getValue() <= this.value;   
                default: 
                    return false;
            }
        }
    }

    /**
     * @return the value
     */
    public double getValue ()
    {
        return value;
    }

    /**
     * @return the alpha
     */
    public double getAlpha ()
    {
        return alpha;
    }
}