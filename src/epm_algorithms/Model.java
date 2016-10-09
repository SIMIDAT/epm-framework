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
package epm_algorithms;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import keel.Dataset.InstanceSet;
import utils.Pattern;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class Model implements Serializable {

    private String fullyQualifiedName;
    //private String minorityClass;
    
    protected ArrayList<Pattern> patterns;
    protected ArrayList<Pattern> patternsFilteredAllClasses; // Patterns filtered by the best n rules of a given qm
    protected ArrayList<Pattern> patternsFilteredByClass; // Patterns filtered by the best n rules of each class

    /**
     * Saves the current model to a .ser file extension.
     *
     * @param path The path to save the file (without .ser extension)
     *
     * @throws IOException
     */
    public void saveModel(String path) throws IOException {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(path));
            stream.writeObject(this);
            stream.close();
            System.out.println("Model saved Correctly in " + path);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads a previous saved model in .ser extension
     *
     * @param path The path to the model
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object readModel(String path) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(new FileInputStream(path));
        return stream.readObject();
    }

    /**
     * Learns a model from training data, and saves it to a file. The patterns
     * obtained in the model must be saved on a class variable to make it
     * accessible to "predict" and "test" methods.
     *
     * @param training the training data
     * @param params the parameters of the algorithms
     */
    public void learn(InstanceSet training, HashMap<String, String> params) {
    }

    /**
     * Predict the class of the new unseen instances.
     *
     * @param test The set of instances to predict the class
     * @return An array with the class predicted for each test instance for
     * unfiltered patterns, filtered global and filtered by class respectively.
     */
    public String[][] predict(InstanceSet test)  {
        return null;
    }

   

    /**
     * @return the fullyQualifiedName
     */
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    /**
     * @param fullyQualifiedName the fullyQualifiedName to set
     */
    protected void setFullyQualifiedName(String fullyQualifiedName) {
        this.fullyQualifiedName = fullyQualifiedName;
    }

    /**
     * @return the minorityClass
     */
//    public String getMinorityClass() {
//        return minorityClass;
//    }
//
//    /**
//     * @param minorityClass the minorityClass to set
//     */
//    public void setMinorityClass(String minorityClass) {
//        this.minorityClass = minorityClass;
//    }

    /**
     * @return the patterns
     */
    public ArrayList<Pattern> getPatterns() {
        return patterns;
    }

    /**
     * @param patterns the patterns to set
     */
    public void setPatterns(ArrayList<Pattern> patterns) {
        this.patterns = patterns;
    }

    /**
     * @return the patternsFilteredAllClass
     */
    public ArrayList<Pattern> getPatternsFilteredAllClass() {
        return patternsFilteredAllClasses;
    }

    /**
     * @param patternsFilteredAllClass the patternsFilteredAllClass to set
     */
    public void setPatternsFilteredAllClass(ArrayList<Pattern> patternsFilteredAllClass) {
        this.patternsFilteredAllClasses = patternsFilteredAllClass;
    }

    /**
     * @return the patternsFilteredByClass
     */
    public ArrayList<Pattern> getPatternsFilteredByClass() {
        return patternsFilteredByClass;
    }

    /**
     * @param patternsFilteredByClass the patternsFilteredByClass to set
     */
    public void setPatternsFilteredByClass(ArrayList<Pattern> patternsFilteredByClass) {
        this.patternsFilteredByClass = patternsFilteredByClass;
    }

    

    
    
    

}
