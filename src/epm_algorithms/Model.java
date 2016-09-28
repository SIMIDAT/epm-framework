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
import java.util.HashMap;
import keel.Dataset.InstanceSet;

/**
 *
 * @author Ángel M. García-Vico
 * @version 1.0
 * @since JDK 1.8
 */
public class Model implements Serializable{
    
    private String fullyQualifiedName;
    
    /**
     * Saves the current model to a .ser file extension.
     * @param path The path to save the file (without .ser extension)
     * 
     * @throws IOException 
     */
    public void saveModel(String path) throws IOException{
        ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(path));
        stream.writeObject(this);
        stream.close();
        System.out.println("Model saved Correctly in " + path);
    }
    
    
    
    /**
     * Reads a previous saved model in .ser extension
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
     * Learns a model from training data, and saves it to a file
     * 
     * @param training the training data
     * @param params the parameters of the algorithms
     */
    public void learn(InstanceSet training, HashMap<String, String> params){
        
    }
    
    
    /**
     *  Predict the class of the new unseen instances 
     * @param test The set of instances to predict the class
     */
    public void predict(InstanceSet test){
        
    }
    
    
    /**
     * This method perform the test phase of a classifier.
     * first, it predicts the label of the instances and then check if the class predicted math with the actual class.
     * 
     * @param test  the test set.
     * @param batch it is executed for a batch execution or not?
     */
    public HashMap<String, Double> test(InstanceSet test){
        
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
     
}
