package framework.utils;

import PRFramework.Core.Common.DoubleFeature;
import PRFramework.Core.Common.Feature;
import PRFramework.Core.Common.FeatureType;
import PRFramework.Core.Common.FeatureValue;
import PRFramework.Core.Common.Helpers.ArrayHelper;
import PRFramework.Core.Common.Instance;
import PRFramework.Core.Common.InstanceModel;
import PRFramework.Core.Common.IntegerFeature;
import PRFramework.Core.Common.NominalFeature;
import PRFramework.Core.Common.RefObject;
import PRFramework.Core.IO.ARFFSerializer;
import PRFramework.Core.IO.BaseSerializer;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.DifferentThanItem;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.GreatherThanItem;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.IEmergingPattern;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.LessOrEqualThanItem;
import PRFramework.Core.SupervisedClassifiers.EmergingPatterns.SingleValueItem;
import static PRFramework.Core.SupervisedClassifiers.InstanceModelHelper.classValues;
import java.util.ArrayList;
import java.util.StringTokenizer;
import keel.Dataset.Attribute;
import keel.Dataset.InstanceSet;
import framework.items.*;

/**
 *
 * @author Marcel Alvarez Espinosa
 * @version 1.0
 * @since JDK 1.8
 */
public class Base
{

    public static void ConvertKeelInstancesToPRFInstances (InstanceSet train, 
            ArrayList<Instance> instances, InstanceModel model, RefObject<Feature> classFeature)
    {
        
        ArrayList<Feature> featureDescriptions = new ArrayList<>();

        //StringTokenizer tokens = new StringTokenizer(train.getHeader(), " \n\r");
        //tokens.nextToken();

        Attribute classAttr = train.getAttributeDefinitions().getOutputAttribute(0);
        
        model.setRelationName("Relation");

        Attribute[] attributes = train.getAttributeDefinitions().getAttributes();

        int index = 0;
        for (Attribute a : attributes) { 
            String featName = a.getName();    
            Feature featToAdd = null;
            if (a.getType() == Attribute.NOMINAL) {
                NominalFeature nominalFeature = null;
                featToAdd = nominalFeature = new NominalFeature(featName, index++);                
                nominalFeature.setValues((String[]) a.getNominalValuesList().stream().toArray(String[]::new));
            }
            if (a.getType() == Attribute.INTEGER) {
                IntegerFeature integerFeature = null;
                featToAdd = integerFeature = new IntegerFeature(featName, index++);
                integerFeature.setMinValue(a.getMinAttribute());
                integerFeature.setMaxValue(a.getMaxAttribute());
            }
            if (a.getType() == Attribute.REAL) {
                DoubleFeature b = new DoubleFeature(featName, index++);
                b.setMaxValue(a.getMaxAttribute());
                b.setMinValue(a.getMinAttribute());
                featToAdd = b;
            }
            featureDescriptions.add(featToAdd);
            if (a == classAttr)
                classFeature.argValue = featToAdd;
        }

        model.setFeatures(featureDescriptions.toArray(new Feature[0]));

        keel.Dataset.Instance[] KInstances = train.getInstances();

        for (keel.Dataset.Instance i : KInstances) {
            Instance obj = model.CreateInstance();
            int idx = 0;
            for (String s : i.toString().split(",")) {
                String value = s.trim();
                if (value.equals("?")) {
                    obj.set(model.getFeatures()[idx], FeatureValue.Missing);
                } else {
                    obj.set(model.getFeatures()[idx], model.getFeatures()[idx].Parse(value));
                }
                idx++;
            }
            instances.add(obj);
        }

        BaseSerializer.LoadInstancesInformation(model, instances);
    }

    public static void convertPRFPatternsToKeelPatterns (ArrayList<IEmergingPattern> patterns, ArrayList<Pattern> keelPatterns)
    {
        for (IEmergingPattern ep : patterns) {
            Item it;
            ArrayList<Item> items = new ArrayList<>();

            for (PRFramework.Core.SupervisedClassifiers.EmergingPatterns.Item i : ep.getItems()) {
                System.out.println(i.getFeature().getFeatureType());
                if (i.getFeature().getFeatureType() == FeatureType.Integer || i.getFeature().getFeatureType() == FeatureType.Double) {
                    // IF VARIABLE IS REAL, CREATE A REAL ITEM.
                    String operator = " = ";
                    if (i instanceof DifferentThanItem) {
                        operator = " != ";
                    } else if (i instanceof GreatherThanItem) {
                        operator = " > ";
                    } else if (i instanceof LessOrEqualThanItem) {
                        operator = " <= ";
                    }

                    it = new NumericItem(i.getFeature().getName(), ((SingleValueItem) i).getValue(), operator);
                } else {
                    // IF NOMINAL, CREATE THE ITEM NOMINAL. NOTE THAT WE MADE THE CONVERSION OF REAL TO NOMINAL ATTRIBUTE
                    double aux = 1.0 / (classValues(i.getFeature()).length - 1.0);
                    int valueVal = ((Double) (((SingleValueItem) i).getValue() / aux)).intValue();
                    String valor = i.getFeature().valueToString(((SingleValueItem) i).getValue());
                    it = new NominalItem(i.getFeature().getName(), valor.substring(1, valor.length() - 1));
                }
                items.add(it);
            }

            Pattern p = new Pattern(items, ArrayHelper.argMax(ep.getCounts()));
            keelPatterns.add(p);
        }
    }
}
