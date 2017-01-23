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
package framework.GUI;

import framework.utils.Utils;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import keel.Dataset.InstanceSet;
import framework.items.Pattern;
import keel.Dataset.Attribute;
import keel.Dataset.Instance;

/**
 * The {@code Model} class implements the neccesary methods to learn and predict
 * instances and also to read and save serialized objects. This <b> must be the
 * superclass </b> of each algorithm included in the package and the methods
 * {@code learn()} and {@code predict} must be overriden.
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 *
 */
public class Model implements Serializable {

    private String fullyQualifiedName;
    //private String minorityClass;

    protected ArrayList<Pattern> patterns;
    protected ArrayList<Pattern> patternsFilteredMinimal; // Minimal Patterns
    protected ArrayList<Pattern> patternsFilteredMaximal; // Maximal Patterns
    protected ArrayList<Pattern> patternsFilteredByMeasure; // Patterns filtered by a given quality measure

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
     * accessible to {@code predict()} and {@code test()} methods.
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
    public String[][] predict(InstanceSet test) {
        return null;
    }

    /**
     * The default method to predict an instance. It is based on the apportation
     * of support each pattern that covers an instance do
     *
     * @param patterns
     * @param test
     * @return
     */
    public String[] getPredictions(ArrayList<framework.items.Pattern> patterns, InstanceSet test) {
        Attribute[] attributes = test.getAttributeDefinitions().getInputAttributes();
        ArrayList<String> predictions = new ArrayList<>();
        float[] clasContrib = new float[test.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues()];
        ArrayList<ArrayList<Double>> contribs = new ArrayList<>();
        for (int i = 0; i < clasContrib.length; i++) {
            contribs.add(new ArrayList<>());
        }
        //For each test instance
        for (Instance inst : test.getInstances()) {
            for (int i = 0; i < clasContrib.length; i++) {
                clasContrib[i] = 0;
            }

            // Checks the patterns that covers the instance for each class, and sum its support
            for (framework.items.Pattern pat : patterns) {
                if (pat.covers(inst, attributes)) {
                    contribs.get(pat.getClase()).add(pat.getTraMeasure("SUPP"));
                    clasContrib[pat.getClase()] += pat.getTra_measures().get("SUPP");
                }
            }

            // Normalise the score by the median value of each contribution.
            for (int i = 0; i < clasContrib.length; i++) {
                if (!contribs.get(i).isEmpty()) {
                    double median = Utils.median(contribs.get(i));
                    if (median != 0) {
                        clasContrib[i] /= median;
                    }
                }
            }

            // The max value wins and it is the value predicted.
            predictions.add(test.getAttributeDefinitions().getOutputAttribute(0).getNominalValue(Utils.getIndexOfMaxValue(clasContrib)));
        }

        //return the array of predictions
        return predictions.toArray(new String[0]);
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
    public ArrayList<Pattern> getPatternsFilteredMinimal() {
        return patternsFilteredMinimal;
    }

    /**
     * @param patternsFilteredAllClass the patternsFilteredAllClass to set
     */
    public void setPatternsFilteredMinimal(ArrayList<Pattern> patternsFilteredAllClass) {
        this.patternsFilteredMinimal = patternsFilteredAllClass;
    }

    /**
     * @return the patternsFilteredMaximal
     */
    public ArrayList<Pattern> getPatternsFilteredMaximal() {
        return patternsFilteredMaximal;
    }

    /**
     * @param patternsFilteredMaximal the patternsFilteredMaximal to set
     */
    public void setPatternsFilteredMaximal(ArrayList<Pattern> patternsFilteredMaximal) {
        this.patternsFilteredMaximal = patternsFilteredMaximal;
    }

    /**
     * @return the patternsFilteredByMeasure
     */
    public ArrayList<Pattern> getPatternsFilteredByMeasure() {
        return patternsFilteredByMeasure;
    }

    /**
     * @param patternsFilteredByMeasure the patternsFilteredByMeasure to set
     */
    public void setPatternsFilteredByMeasure(ArrayList<Pattern> patternsFilteredByMeasure) {
        this.patternsFilteredByMeasure = patternsFilteredByMeasure;
    }

}
