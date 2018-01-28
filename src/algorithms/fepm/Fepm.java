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
package algorithms.fepm;

import PRFramework.Core.Common.Feature;
import PRFramework.Core.Common.FeatureType;
import PRFramework.Core.Common.Instance;
import PRFramework.Core.Common.InstanceModel;
import PRFramework.Core.Common.RefObject;
import PRFramework.Core.Fuzzy.Fuzzifiers.EqualWidthTriangleBasedFuzzifier;
import PRFramework.Core.SupervisedClassifiers.DecisionTrees.Builder.DecisionTreeBuilder;
import PRFramework.Core.SupervisedClassifiers.DecisionTrees.DistributionTesters.PureNodeStopCondition;
import PRFramework.Core.SupervisedClassifiers.DecisionTrees.PruneTesters.PessimisticError;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Classifiers.EmergingPatternClassifier;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Classifiers.Normalizers.AvgSumSupportOnTrainingNormalizer;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Classifiers.PatternSelectionPolicies.AllPatternsPolicy;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Classifiers.VotesAggregators.SumOfSupportAggregator;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.IEmergingPattern;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Miners.RandomForestMiner;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.PatternTests.QualityBasedPatternTester;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Qualities.Statistical.GrowthRateQuality;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.SubsetRelation;
import framework.utils.Base;
import java.util.ArrayList;
import java.util.HashMap;
import keel.Dataset.InstanceSet;
import framework.items.Pattern;
import framework.GUI.Model;
import java.util.logging.Level;
import java.util.logging.Logger;
import keel.Dataset.DatasetException;
import keel.Dataset.HeaderFormatException;

public class Fepm extends Fepm_Wrapper {

    public String name = "FEPM";

    public InstanceSet train;

    public InstanceSet test;

    public double growthRate = 10;

    public SubsetRelation subsetRelation = SubsetRelation.Superset;

    public int maxDepth = 10;

    public int treeCount = 100;

    public int fuzzyfierCount = 4;

    //Timing
    protected long initialTime;

    protected double modelTime;

    protected double trainingTime;

    protected double testTime;

    public Fepm(InstanceSet train, HashMap<String, String> params) {
        this.train = train;
        try {
            this.test = new InstanceSet();
            this.test.readSet(params.getOrDefault("testData", ""), false);
            this.test.setAttributesAsNonStatic();
        } catch (DatasetException ex) {
            Logger.getLogger(Fepm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (HeaderFormatException ex) {
            Logger.getLogger(Fepm.class.getName()).log(Level.SEVERE, null, ex);
        }

        growthRate = Double.parseDouble(params.getOrDefault("growthRate", "10"));
        if ("superset".equals(params.getOrDefault("subsetRelation", "superset"))) {
            subsetRelation = SubsetRelation.Superset;
        } else {
            subsetRelation = SubsetRelation.Equal;
        }
        maxDepth = Integer.parseInt(params.getOrDefault("maxDepth", "10"));
        treeCount = Integer.parseInt(params.getOrDefault("treeCount", "100"));
        fuzzyfierCount = Integer.parseInt(params.getOrDefault("fuzzyfierCount", "4"));
    }

    public void mine() {
        ArrayList<Instance> prfInstances = new ArrayList<>();
        InstanceModel model = new InstanceModel();

        RefObject<Feature> classFeature = new RefObject<Feature>(null);
        Base.ConvertKeelInstancesToPRFInstances(train, prfInstances, model, classFeature);

        EqualWidthTriangleBasedFuzzifier fuzzifier = new EqualWidthTriangleBasedFuzzifier();
        Feature[] fuzFeats = new Feature[model.getFeatures().length];

        for (int i = 0; i < model.getFeatures().length; i++) {
            Feature feature = model.getFeature(i);
            if (feature.getFeatureType() == FeatureType.Double
                    || feature.getFeatureType() == FeatureType.Integer) {
                fuzFeats[i] = fuzzifier.Fuzzify(feature, fuzzyfierCount);
            } else {
                fuzFeats[i] = feature;
            }
        }

        model.setFeatures(fuzFeats);

//{ classFeature }
        //Check  time		
        setInitialTime();

        RandomForestMiner miner = new RandomForestMiner();
        DecisionTreeBuilder builder = new DecisionTreeBuilder();
        builder.setMaxDepth(maxDepth);
        builder.setPruneResult(true);
        builder.setPruneTester(new PessimisticError());
        builder.setMinimalInstanceMembership(0.05);
        builder.setStopCondition(new PureNodeStopCondition());

        miner.setDecisionTreeBuilder(builder);
        miner.setFilterRelation(subsetRelation);
        miner.setTreeCount(treeCount);
        miner.setEPTester(new QualityBasedPatternTester(new GrowthRateQuality(), growthRate));

        ArrayList<IEmergingPattern> prfPatterns = miner.mine(model, prfInstances, classFeature.argValue);
        ArrayList<Pattern> keelPatterns = new ArrayList<>();

        /**
         * Here we need to re-design the whole framework in order to convert the
         * fuzzy set returned by FEPM to EPM-Framework.
         *
         * This is due to the fuzzy sets of are more complex, and contains fuzzy
         * hedges.
         *
         * SO THE FUNCTION IS NOT AVAILABLE
         */
        //Base.convertPRFPatternsToKeelPatterns(prfPatterns, keelPatterns);
        trainingTime = ((double) System.currentTimeMillis() - initialTime) / 1000.0;
        System.out.println(name + " " + model.getRelationName() + " Training " + trainingTime + "s");

        /**
         *
         * THIS IS A PATCH IN ORDER TO GET THE ACCURACY OF THE MODEL EXTRACTED
         * USING THE CAEP AGGREGATION OF SUPPORTS METHOD
         *
         */
        prfInstances = new ArrayList<>();
        classFeature = new RefObject<Feature>(null);
        Base.ConvertKeelInstancesToPRFInstances(test, prfInstances, model, classFeature);

        EmergingPatternClassifier.ClassifierData data = new EmergingPatternClassifier.ClassifierData();
        IEmergingPattern[] patts = new IEmergingPattern[prfPatterns.size()];
        for (int i = 0; i < patts.length; i++) {
            patts[i] = prfPatterns.get(i);
        }
        data.setAllPatterns(patts);
        data.setTrainingInstances(prfInstances);
        data.setClassFeature(classFeature.argValue);

        EmergingPatternClassifier classifier = new EmergingPatternClassifier();
        SumOfSupportAggregator suppAggr = new SumOfSupportAggregator();
        AvgSumSupportOnTrainingNormalizer normalizer = new AvgSumSupportOnTrainingNormalizer();
        AllPatternsPolicy policy = new AllPatternsPolicy();

        normalizer.setData(data);
        suppAggr.setData(data);
        policy.setData(data);

        classifier.setSelectionPolicy(policy);
        classifier.setVotesNormalizer(normalizer);
        classifier.setVotesAggregator(suppAggr);
        classifier.setPatterns(prfPatterns);

        // Perform the classification of the testing instances
        float aciertos = 0;
        for (Instance inst : prfInstances) {
            double[] classify = classifier.Classify(inst);
            double max = -1;
            int predictedClass = -1;
            if (classify != null) {
                for (int i = 0; i < classify.length; i++) {
                    if (classify[i] > max) {
                        max = classify[i];
                        predictedClass = i;
                    }
                }
            }
            if (predictedClass == inst.get(classFeature.argValue)) {
                aciertos++;
            }
        }

        System.out.println("TEST ACCURACY: " + aciertos / (float) prfInstances.size());
        // End of the classification

        // SETS THE ARRAYLIST OF PATTERNS OF THE MODEL CLASS
        //super.setPatterns(keelPatterns);
        super.setPatterns(new ArrayList<>());
    }

    /**
     * Sets the time counter
     *
     */
    protected void setInitialTime() {
        initialTime = System.currentTimeMillis();
    }//end-method
}
