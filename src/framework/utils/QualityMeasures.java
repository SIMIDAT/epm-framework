/*
 * The MIT License
 *
 * Copyright 2017 angel.
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

package framework.utils;

import java.util.HashMap;

/**
 * Class to store, add and define the quality measures 
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class QualityMeasures {
    
    /**
     * The hash map with the quality meaures
     */
    private HashMap<String, Double> measures;
    
    /**
     * Default constructor, initialises the {@code measures} hash map and add
     * entries for the confusion matrix, i.e., tp,fp,tn and fn
     * 
     */
    public QualityMeasures(){
        measures = new HashMap<>();
        measures.put("TP", 0d);
        measures.put("TN", 0d);
        measures.put("FP", 0d);
        measures.put("FN", 0d);
    }
    
    
    /**
     * It gets the value of the given quality measure
     * @param measure
     * @return 
     */
    public double getMeasure(String measure){
        return measures.get(measure);
    }
    
    /**
     * It adds/overwrites the value of a given quality measure by the given value;
     * @param measure
     * @param value 
     */
    public void addMeasure(String measure, double value){
        measures.put(measure, value);
    }
    
    /**
     * Gets a reference to {@code measures}
     * @return 
     */
    public HashMap<String, Double> getMeasures(){
        return measures;
    }
    
    
    /**
     * Adds the values of the quality measures in {@code this} with the ones in 
     * {@code other}, creatin in {@code this} new entries if necessary
     * @param other 
     */
    public void sum(QualityMeasures other){
       for(String key: other.measures.keySet()){
           measures.put(key, other.measures.get(key) + measures.getOrDefault(key, 0.0));
       }
    }
   
}
