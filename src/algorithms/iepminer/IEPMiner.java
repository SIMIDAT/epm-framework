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
package algorithms.iepminer;

import framework.GUI.Model;
import framework.items.Item;
import framework.items.NominalItem;
import framework.items.Pattern;
import java.util.ArrayList;
import java.util.HashMap;
import keel.Dataset.Instance;
import keel.Dataset.InstanceSet;

/**
 *
 * @author Ángel M. García Vico <agvico@ujaen.es>
 * @version 1.0
 * @since JDK 1.8
 */
public class IEPMiner extends Model {

    private PTree root;
    private ArrayList<Pattern> trainingInstances;

    @Override
    public void learn(InstanceSet training, HashMap<String, String> params) {
        root = new PTree(null, true);
        PTree.headerTable = new ArrayList<>();
        int numClasses = training.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues();
        trainingInstances = new ArrayList<>();
        for (int i = 0; i < numClasses; i++) {
            // gets the training instances
            root = new PTree(null, true);
            for (Instance inst : training.getInstances()) {
                Pattern p = new Pattern(new ArrayList<Item>(), inst.getOutputNominalValuesInt(0) == i ? 0 : 1);
                for (int j = 0; j < training.getAttributeDefinitions().getInputNumAttributes(); j++) {
                    p.add(new NominalItem(training.getAttributeDefinitions().getAttribute(j).getName(), inst.getInputNominalValues(j)));
                }
                trainingInstances.add(p);
            }
            long t_ini = System.currentTimeMillis();
            for(Pattern p : trainingInstances){
                root.insertTree(p);
            }
            System.out.println("Time to build the tree: " + (System.currentTimeMillis() - t_ini) / 1000f);

        }
    }
}
