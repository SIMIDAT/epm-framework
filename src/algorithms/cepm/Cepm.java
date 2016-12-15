/**
 * *********************************************************************
 *
 * This file is part of KEEL-software, the Data Mining tool for regression,
 * classification, clustering, pattern mining and so on.
 *
 * Copyright (C) 2004-2010
 *
 * F. Herrera (herrera@decsai.ugr.es) L. Sanchez (luciano@uniovi.es) J.
 * Alcal�-Fdez (jalcala@decsai.ugr.es) S. Garc�a (sglopez@ujaen.es) A. Fern�ndez
 * (alberto.fernandez@ujaen.es) J. Luengo (julianlm@decsai.ugr.es)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses/
 *
 *********************************************************************
 */
package algorithms.cepm;

import PRFramework.Core.Common.Instance;
import PRFramework.Core.Common.InstanceModel;
import PRFramework.Core.SupervisedClassifiers.DecisionTrees.Builder.DecisionTreeBuilder;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.IEmergingPattern;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Miners.CepmMiner;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.PatternTests.QualityBasedPatternTester;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Qualities.Statistical.ConfidenceQuality;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.SubsetRelation;
import static PRFramework.Core.SupervisedClassifiers.InstanceModelHelper.classFeature;
import framework.utils.Base;
import java.util.ArrayList;
import java.util.HashMap;
import keel.Dataset.InstanceSet;
import framework.items.Pattern;
//import main.Pattern;

public class Cepm extends Cepm_Wrapper
{

    public String name = "Cepm";

    public InstanceSet train;

    public double confidence = 0.9;

    public int maxOfItems = 5;

    public SubsetRelation filterRelation = SubsetRelation.Different;

    public int maxDepth = 5;

    public int maxIterations = 100;

    //Timing
    protected long initialTime;

    protected double modelTime;

    protected double trainingTime;

    protected double testTime;

    public Cepm (InstanceSet train, HashMap<String, String> params)
    {
        this.train = train;

        confidence = Double.parseDouble(params.get("confidence"));
        if ("Different".equals(params.get("filterRelation"))) {
            filterRelation = SubsetRelation.Different;
        } else {
            filterRelation = SubsetRelation.Equal;
        }
        maxDepth = Integer.parseInt(params.get("maxDepth"));
        maxIterations = Integer.parseInt(params.get("maxIterations"));
    }

    public void mine ()
    {
        ArrayList<Instance> prfInstances = new ArrayList<>();
        InstanceModel model = new InstanceModel();

        Base.ConvertKeelInstancesToPRFInstances(train, prfInstances, model);

        //Check  time		
        setInitialTime();

        CepmMiner miner = new CepmMiner();
        DecisionTreeBuilder builder = new DecisionTreeBuilder();
        builder.setMaxDepth(maxDepth);

        miner.setDecisionTreeBuilder(builder);
        miner.setFilterRelation(filterRelation);
        miner.setMaxIterations(maxIterations);
        miner.setEPTester(new QualityBasedPatternTester(new ConfidenceQuality(), confidence));

        ArrayList<IEmergingPattern> prfPatterns = miner.mine(model, prfInstances, classFeature(model));
        ArrayList<Pattern> keelPatterns = new ArrayList<>();

        Base.convertPRFPatternsToKeelPatterns(prfPatterns, keelPatterns);

        trainingTime = ((double) System.currentTimeMillis() - initialTime) / 1000.0;
        System.out.println(name + " " + model.getRelationName() + " Training " + trainingTime + "s");

        // SETS THE ARRAYLIST OF PATTERNS OF THE MODEL CLASS
        super.setPatterns(keelPatterns);
    }

    /**
     * Sets the time counter
     *
     */
    protected void setInitialTime ()
    {
        initialTime = System.currentTimeMillis();
    }//end-method
}
