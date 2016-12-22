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
package algorithms.deletebestconditionbylevel;

import java.util.HashMap;
import keel.Dataset.InstanceSet;
import framework.GUI.Model;

/**
 *
 * @author Marcel Alvarez Espinosa
 * @version 1.0
 * @since JDK 1.8
 */
public class DeleteBestConditionByLevel_Wrapper extends Model
{

    DeleteBestConditionByLevel algorithm;

    @Override
    public void learn (InstanceSet training, HashMap<String, String> params)
    {
        algorithm = new DeleteBestConditionByLevel(training, params);
        algorithm.mine();
        super.setPatterns(algorithm.getPatterns());
    }

    @Override
    public String[][] predict (InstanceSet test)
    {
         String[][] result = new String[4][test.getNumInstances()];
        result[0] = super.getPredictions(super.patterns, test);
        result[1] = super.getPredictions(super.patternsFilteredMinimal, test);
        result[2] = super.getPredictions(super.patternsFilteredMaximal, test);
        result[3] = super.getPredictions(super.patternsFilteredByMeasure, test);
        return result;
    }
}
