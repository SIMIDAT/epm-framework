/**
 * <p>
 * EvAEP
 * Non-dominated Multi-objective Evolutionary algorithm for Extracting Fuzzy rules in Subgroup Discovery
 * </p>
 * <p>
 * Algorithm for the discovery of rules describing subgroups
 * @author Cristobal J. Carmona
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

//import keel.Algorithms.Subgroup_Discovery.EvAEP.Calculate.Calculate;

import framework.GUI.Model;
import framework.items.*;
import keel.Dataset.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EvAEP extends Model {

    private static int seed;                // Seed for the random generator
    private static String nombre_alg;       // Algorithm Name

    private static String input_file_tra;   // Input mandatory file training
    private static String input_file_ref;   // Input mandatory file training
    private static String input_file_tst;   // Input mandatory file test
    private static String output_file_tra;  // Output mandatory file training
    private static String output_file_tst;  // Output mandatory file test
    private static String rule_file;        // Auxiliary output file for rules
    private static String seg_file;         // Auxiliary output file for tracking
    private static String qmeasure_file;    // Output quality measure file

    // Structures
    static InstanceSet Data;
    static TableVar Variables;     // Set of variables of the dataset and their characteristics
    static TableDat Examples;      // Set of instances of the dataset
    static Genetic AG;             // Genetic Algorithm

    /**
     * <p>
     * Auxiliar Gets the name for the output files, eliminating "" and skiping "="
     * </p>
     * @param s                 String of the output files
     */
    private static void GetOutputFiles(StringTokenizer s) {
        String val   = s.nextToken();
        
        output_file_tra = s.nextToken().replace('"',' ').trim();
        output_file_tst = s.nextToken().replace('"',' ').trim();
        rule_file    = s.nextToken().replace('"',' ').trim();
        qmeasure_file= s.nextToken().replace('"',' ').trim();
        seg_file     = s.nextToken().replace('"',' ').trim();
    }

    /**
     * <p>
     * Auxiliar Gets the name for the input files, eliminating "" and skiping "="
     * </p>
     * @param s                 String of the input files
     */
    private static void GetInputFiles(StringTokenizer s) {
        String val   = s.nextToken(); // skip "="
        input_file_tra = s.nextToken().replace('"',' ').trim();
        input_file_ref = s.nextToken().replace('"',' ').trim();
        input_file_tst = s.nextToken().replace('"',' ').trim();
    }

    /**
     * <p>
     * Reads the parameters from the file specified and stores the values
     * </p>
     * @param nFile      File of parameters
     */
    public static void ReadParameters(HashMap<String, String> params) {
        nombre_alg = "NMEEFSD";
        seed = Integer.parseInt(params.get("Seed"));
        AG.setRulesRep(params.get("Rule Representation"));
        Variables.setNLabel(Integer.parseInt(params.get("Number of Fuzzy Labels")));
        AG.setNEval(Integer.parseInt(params.get("Number of Evaluations")));
        AG.setLengthPopulation(Integer.parseInt(params.get("Population Length")));
        AG.setProbCross(Float.parseFloat(params.get("Crossover Probability")));
        AG.setProbMutation(Float.parseFloat(params.get("Mutation Probability")));
        AG.setFitness("MEDGEO");
        AG.setInitialisation("BIASED");
        AG.setRoundRobin(false);
    }

    /**
    * <p>
    * Read the dataset and stores the values
    * </p>
    */
    public static void CaptureDatasetTraining (InstanceSet training) throws IOException   {

        try {

        // Declaration of the dataset and load in memory
        Data = training;

        // Check that there is only one output variable
        if (Data.getAttributeDefinitions().getOutputNumAttributes() > 1) {
  		System.out.println("This algorithm can not process MIMO datasets");
  		System.out.println("All outputs but the first one will be removed");
	}
	boolean noOutputs=false;
	if (Data.getAttributeDefinitions().getOutputNumAttributes() < 1) {
  		System.out.println("This algorithm can not process datasets without outputs");
  		System.out.println("Zero-valued output generated");
  		noOutputs=true;
	}

        // Chek that the output variable is nominal
        if (Data.getAttributeDefinitions().getOutputAttribute(0).getType()!=Attribute.NOMINAL) {
            // If the output variables is not enumeratad, the algorithm can not be run
            try {
                throw new IllegalAccessException("Finish");
            } catch( IllegalAccessException term) {
                System.err.println("Target variable is not a discrete one.");
                System.err.println("Algorithm can not be run.");
                System.out.println("Program aborted.");
                System.exit(-1);
            }
        }

        // Set the number of classes of the output attribute - this attribute must be nominal
        Variables.setNClass(Data.getAttributeDefinitions().getOutputAttribute(0).getNumNominalValues());

        // Screen output of the output variable and selected class
        System.out.println ( "Output variable: " + Data.getAttributeDefinitions().getOutputAttribute(0).getName());

        // Creates the space for the variables and load the values.
        Variables.Load (Data.getAttributeDefinitions().getInputNumAttributes());

        // Setting and file writing of fuzzy sets characteristics for continuous variables
        String nombreF = seg_file;
        Variables.InitSemantics (nombreF);

        // Creates the space for the examples and load the values
        Examples.Load(Data,Variables);

        } catch (Exception e) {
            System.out.println("DBG: Exception in readSet");
            e.printStackTrace();
        }

    }

    /**
    * <p>
    * Read the dataset and stores the values
    * </p>
    */
    public static void CaptureDatasetTest () throws IOException   {

        try {

        // Declaration of the dataset and load in memory
        Data = new InstanceSet();
        Data.readSet(input_file_tst,false);

        // Check that there is only one output variable
        if (Attributes.getOutputNumAttributes()>1) {
  		System.out.println("This algorithm can not process MIMO datasets");
  		System.out.println("All outputs but the first one will be removed");
	}
	boolean noOutputs=false;
	if (Attributes.getOutputNumAttributes()<1) {
  		System.out.println("This algorithm can not process datasets without outputs");
  		System.out.println("Zero-valued output generated");
  		noOutputs=true;
	}

        // Chek that the output variable is nominal
        if (Attributes.getOutputAttribute(0).getType()!=Attribute.NOMINAL) {
            // If the output variables is not enumeratad, the algorithm can not be run
            try {
                throw new IllegalAccessException("Finish");
            } catch( IllegalAccessException term) {
                System.err.println("Target variable is not a discrete one.");
                System.err.println("Algorithm can not be run.");
                System.out.println("Program aborted.");
                System.exit(-1);
            }
        }

        // Set the number of classes of the output attribute - this attribute must be nominal
        Variables.setNClass(Attributes.getOutputAttribute(0).getNumNominalValues());

        // Screen output of the output variable and selected class
        System.out.println ( "Output variable: " + Attributes.getOutputAttribute(0).getName());

        // Creates the space for the variables and load the values.
        Variables.Load (Attributes.getInputNumAttributes());

        // Setting and file writing of fuzzy sets characteristics for continuous variables
        String nombreF = seg_file;
        Variables.InitSemantics (nombreF);

        // Creates the space for the examples and load the values
        Examples.Load(Data,Variables);

        } catch (Exception e) {
            System.out.println("DBG: Exception in readSet");
            e.printStackTrace();
        }

    }

    /**
    * <p>
    * Dataset file writting to output file
    * </p>
    * @param filename           Output file
    * @param pop                Population with final emerging patterns
    * @param nrules             Number of rules generated
    */
    public static void CalculateOutDataCAN (String filename, int tipo, Population pop, int[] classFinal, int nrules) {

        float pertenencia, pert;
        float disparo = 1;
        int numVarNoInterv = 0;
        float[] compatibility;
        float[] normsum;
        String contents = "";
        int acierto=0, fallo=0;

        compatibility = new float[nrules];
        normsum = new float[Variables.getNClass()];

        int max;
        double maximo;

        //NECESITO SABER LA CLASE MAYORITARIA POR SI ACASO NO SE CUBRE EL EJEMPLO
        int majority = Integer.MIN_VALUE;
        int pos = 0;
        for(int i=0; i<Variables.getNClass(); i++){
            if(majority < Examples.getExamplesClass(i)){
                pos=i;
                majority = Examples.getExamplesClass(i);
            }
        }
        majority = pos;

        for (int i=0; i<Examples.getNEx(); i++) { // For each example of the dataset
            
            for (int indi=0; indi<nrules; indi++){
                
                Individual ind = new IndCAN();
                ind = pop.getIndiv(indi);
                disparo = 1;
                numVarNoInterv = 0;
                
                // Compute all chromosome values
                for (int j=0; j<Variables.getNVars(); j++) {
                    if (!Variables.getContinuous(j)) {  // Discrete Variable
                        if (ind.getCromElem(j)<=Variables.getMax(j)){
                            // Variable j takes part in the rule
                            if ((Examples.getDat(i,j) != ind.getCromElem(j)) && (!Examples.getLost(Variables,i,j))) {
                                // If chromosome value <> example value, and example value is not a lost value
                                disparo = 0;    
                            }
                        }
                        else
                            numVarNoInterv++;  // Variable does not take part
                    }
                    else {	// Continuous variable
                        if (ind.getCromElem(j)<Variables.getNLabelVar(j)) {
                            // Variable takes part in the rule
                            if (!Examples.getLost(Variables,i,j))
                                if (NumInterv (Examples.getDat(i,j),j, Variables)!= ind.getCromElem(j))
                                    disparo = 0;
                        }
                        else
                            numVarNoInterv++;  // Variable does not take part
                    }
                } // End FOR all chromosome values
                if(Variables.getNVars() <= numVarNoInterv) disparo=0;
                compatibility[indi] = disparo;
            }
            for(int j=0; j<Variables.getNClass(); j++)
                normsum[j]=0;

            for(int j=0; j<nrules; j++){
                normsum[classFinal[j]] += compatibility[j];
            }

            float maximum = 0;
            pos = -1;

//            for(int j=0; j<nrules; j++){
//                if(compatibility[j] > maximum){
//                    maximum = compatibility[j];
//                    pos = j;
////                } else if (compatibility[j]==maximum){
////                    if (Attributes.getOutputAttribute(0).getNominalValue(classFinal[j])
////                         == Attributes.getOutputAttribute(0).getNominalValue(Examples.getClass(i))){
////                        maximum = compatibility[j];
////                        pos = j;
////                    }
//                }
//            }
            for(int j=0; j<Variables.getNClass(); j++){
                if(normsum[j] >= maximum){
                    maximum = normsum[j];
                    pos = j;
                }
            }
            if(maximum==0){
                contents = Attributes.getOutputAttribute(0).getNominalValue(Examples.getClass(i))+" "+Attributes.getOutputAttribute(0).getNominalValue(majority)+"\n";
                if (Examples.getClass(i) == majority)
                    acierto++;
                else fallo++;
            } else {
                contents = Attributes.getOutputAttribute(0).getNominalValue(Examples.getClass(i))+" "+Attributes.getOutputAttribute(0).getNominalValue(pos)+"\n";
                if (Examples.getClass(i) == pos)
                    acierto++;
                else fallo++;
            }
            Files.addToFile(filename, contents);
       }
        float resultado = (float) acierto/ (float)(acierto+fallo);
        if(tipo==0){
            contents = "TRAINING ACCURACY: "+resultado+"\n\n\n";
            Files.addToFile(seg_file, contents);
            System.out.println(contents);
        } else {
            contents = "\nTEST ACCURACY: "+resultado;
            Files.addToFile(seg_file, contents);
            System.out.println(contents);
        }
    }    
            
    /**
    * <p>
    * Dataset file writting to output file
    * </p>
    * @param filename           Output file
    * @param pop                Population with final emerging patterns
    * @param nrules             Number of rules generated
    */
    /*
    public static void CalculateOutDataAUC (String filename, Population pop, int[] classFinal, int nrules) {

        float pertenencia, pert;
        float disparo = 1;
        int numVarNoInterv = 0;
        float[] compatibility;
        float[] normsum;
        String contents = "";

        compatibility = new float[nrules];
        normsum = new float[Variables.getNClass()];

        int max;
        double maximo;

        int majority = Integer.MIN_VALUE;
        int pos = 0;
        for(int i=0; i<Variables.getNClass(); i++){
            if(Attributes.getOutputAttribute(0).getNominalValue(i).compareTo("negative")==0){
                pos=i;
            }
        }
        majority = pos;

        for (int i=0; i<Examples.getNEx(); i++) { // For each example of the dataset
            
            for (int indi=0; indi<nrules; indi++){
                
                Individual ind = new IndCAN();
                ind = pop.getIndiv(indi);
                disparo = 1;
                numVarNoInterv = 0;
                
                // Compute all chromosome values
                for (int j=0; j<Variables.getNVars(); j++) {
                    if (!Variables.getContinuous(j)) {  // Discrete Variable
                        if (ind.getCromElem(j)<=Variables.getMax(j)){
                            // Variable j takes part in the rule
                            if ((Examples.getDat(i,j) != ind.getCromElem(j)) && (!Examples.getLost(Variables,i,j))) {
                                // If chromosome value <> example value, and example value is not a lost value
                                disparo = 0;    
                            }
                        }
                        else
                            numVarNoInterv++;  // Variable does not take part
                    }
                    else {	// Continuous variable
                        if (ind.getCromElem(j)<Variables.getNLabelVar(j)) {
                            // Variable takes part in the rule
                            if (!Examples.getLost(Variables,i,j))
                                if (NumInterv (Examples.getDat(i,j),j, Variables)!= ind.getCromElem(j))
                                    disparo = 0;
                        }
                        else
                            numVarNoInterv++;  // Variable does not take part
                    }
                } // End FOR all chromosome values
                if(Variables.getNVars() <= numVarNoInterv) disparo=0;
                compatibility[indi] = disparo;
            }
            for(int j=0; j<Variables.getNClass(); j++)
                normsum[j]=0;

            for(int j=0; j<nrules; j++){
                normsum[classFinal[j]] += compatibility[j];
            }

            float maximum = 0;
            pos = -1;

            for(int j=0; j<nrules; j++){
                if(compatibility[j]>maximum){
                    maximum = compatibility[j];
                    pos = j;
//                } else if (compatibility[j]==maximum){
//                    if (Attributes.getOutputAttribute(0).getNominalValue(classFinal[j])
//                         == Attributes.getOutputAttribute(0).getNominalValue(Examples.getClass(i))){
//                        maximum = compatibility[j];
//                        pos = j;
//                    }
                }
            }
            if(maximum==0){
                contents = Attributes.getOutputAttribute(0).getNominalValue(Examples.getClass(i))+" "+Attributes.getOutputAttribute(0).getNominalValue(majority)+"\n";
            } else {
                contents = Attributes.getOutputAttribute(0).getNominalValue(Examples.getClass(i))+" "+Attributes.getOutputAttribute(0).getNominalValue(classFinal[pos])+"\n";
            }
            Files.addToFile(filename, contents);
       }
                
    }*/  
    
    /**
    * <p>
    * Quality measures obtained for test files for EPs
    * </p>
    * @param pop                Population with final emerging patterns
    * @param nrules             Number of rules generated
    */
    public static void CalculateQMeasures (Population pop, int nrules, String filename, int[] classFinal) {

        int ejCompAntFuzzy=0;                // Number of compatible examples with the antecedent of any class - fuzzy version --- unused
        int ejAntCrisp=0;                // Number of compatible examples with the antecedent of any class - crisp version
        int ejCompAntClassFuzzy=0;           // Number of compatible examples (antecedent and class) - fuzzy version
        int ejAntClassCrisp=0;           // Number of compatible examples (antecedent and class) - crisp version
        int ejAntNoClassCrisp=0;
        int ejAntClassNewCrisp=0;
        int ejCompAntClassNewFuzzy=0;        // Number of new covered compatible examples (antec and class) - fuzzy version
        int tp, fp, tn, fn;
        float tpr, fpr;

        float gradoCompAntFuzzy=0;           // Total compatibility degree with the antecedent - fuzzy version
        float gradoCompAntClassFuzzy=0;      // Tot compatibility degree with antecedent and class - fuzzy version

        float pertenencia, pert;
        float disparoFuzzy = 1;
        float disparoCrisp = 1;
        int numVarNoInterv = 0;
        String contents = "";

        float AvNVAR = 0;
        float AvLENG = 0;
        float AvUNUS = 0;
        float AvGAIN = 0;
        float AvSENS = 0;
        float AvDIFS = 0;
        float AvFCNF = 0;
        float AvTPr = 0;
        float AvFPr = 0;
        float AvDH = 0;
        float AvTF = 0;
        float AvGR = 0;

        DecimalFormat sixDecimals = new DecimalFormat("0.000000");
        DecimalFormat threeInts   = new DecimalFormat("000");

        contents = "Number \tClass \tSize \tNVar \tLength \tUnusualness \tGain" +
                     "\tSensitivity \tSupportDif \tFConfidence \tGrowthRate \tTEFisher \tHellinger \tTPr \tFPr \n";
        Files.writeFile(filename, contents);  
        int rule = 0;

        for (int indi=0; indi<nrules; indi++){
        
            Individual ind = new IndCAN();
            ind = pop.getIndiv(indi);
             
//            if(ind.getMeasures().getConf()>=0.5){
            
                ejCompAntFuzzy = ejAntCrisp = ejAntCrisp = ejCompAntClassFuzzy = 0;
                ejAntClassCrisp = ejAntNoClassCrisp = ejAntClassNewCrisp = ejCompAntClassNewFuzzy=0;
                gradoCompAntFuzzy = gradoCompAntClassFuzzy=0;
                tp = fp = tn = fn = 0;
                tpr = fpr = 0;


                float nvar = 0;
                float leng = 0;
                float unus = 0;
                float gain = 0;
                float sens = 0;
                float difs = 0;
                float fcnf = 0;
                float grat = 0;
                float cove = 0;

                for (int i=0; i<Examples.getNEx(); i++) { // For each example of the dataset

                    disparoFuzzy = 1;
                    disparoCrisp = 1;
                    numVarNoInterv = 0;

                    // Compute all chromosome values
                    for (int j=0; j<Variables.getNVars(); j++) {
                        if (!Variables.getContinuous(j)) {  // Discrete Variable
                            if (ind.getCromElem(j)<=Variables.getMax(j)){
                                // Variable j takes part in the rule
                                if ((Examples.getDat(i,j) != ind.getCromElem(j)) && (!Examples.getLost(Variables,i,j))) {
                                    // If chromosome value <> example value, and example value is not a lost value
                                    disparoFuzzy = 0;    
                                    disparoCrisp = 0;    
                                }
                            }
                            else
                                numVarNoInterv++;  // Variable does not take part
                        }
                        else {	// Continuous variable
                            if (ind.getCromElem(j)<Variables.getNLabelVar(j)) {
                                // Variable takes part in the rule
                                // Fuzzy computation
                                if (!Examples.getLost(Variables,i,j)) {
                                    // If the value is not a lost value
                                    pertenencia = Variables.Fuzzy(j, ind.getCromElem(j), Examples.getDat(i,j));
                                    disparoFuzzy = Utils.Minimum (disparoFuzzy, pertenencia);
                                }
                                // Crisp computation
                                if (!Examples.getLost(Variables,i,j))
                                    if (NumInterv (Examples.getDat(i,j),j, Variables)!= ind.getCromElem(j))
                                        disparoCrisp = 0;
                            }
                            else
                                numVarNoInterv++;  // Variable does not take part
                        }
                    } // End FOR all chromosome values

                    // Update counters and mark example if needed
                    gradoCompAntFuzzy += disparoFuzzy;
                    if (disparoFuzzy>0) {
                        ejCompAntFuzzy++;
                        if (Examples.getClass(i) == classFinal[indi]) {
                            gradoCompAntClassFuzzy +=disparoFuzzy;
                            ejCompAntClassFuzzy ++;
                            Examples.setCovered(i, true);
                        }
                        if ((!Examples.getCovered(i)) &&  (Examples.getClass(i) == classFinal[indi])) {
                            // If example was not previusly covered and belongs to the target class increments the number of covered examples
                            ejCompAntClassNewFuzzy++;
                        }
                    }
                    if (disparoCrisp>0) {
                        ejAntCrisp++;
                        if (Examples.getClass(i) == classFinal[indi]) {
                            ejAntClassCrisp ++;
                            tp++;
                            Examples.setCovered(i, true);
                        } else {
                            ejAntNoClassCrisp ++;
                            fp++;
                        }
                        if ((!Examples.getCovered(i)) &&  (Examples.getClass(i) == classFinal[indi])) {
                            // If example was not previusly covered and belongs to the target class increments the number of covered examples
                            ejAntClassNewCrisp++;
                        }
                    } else {
                        if (Examples.getClass(i) == classFinal[indi]) {
                            fn++;
                        } else {
                            tn++;
                        }
                    }

                }
                
                // Compute the measures
                
                float ejT = 0;
                float ejM = 0;
                for(int k=0; k<  Variables.getNClass(); k++){
                    ejT += Examples.getExamplesClass(k); 
                    if(Examples.getExamplesClass(k)>ejM)
                        ejM = Examples.getExamplesClass(k);
                }
                
                //VARIABLES
                nvar = Variables.getNVars() - numVarNoInterv; 
                //SENS
                if (Examples.getExamplesClass(classFinal[indi]) != 0)
                    sens = ((float)ejAntClassCrisp/Examples.getExamplesClass(classFinal[indi]));
                else sens = 0;
                if (numVarNoInterv >= Variables.getNVars())
                    sens = 0;

                //LENGTH
                if (ejAntCrisp != 0)
                    leng = 1/(float)ejAntCrisp;
                else leng = 0;
                //COVE
                if (ejAntCrisp != 0)
                    cove = ((float)ejAntCrisp/Examples.getNEx());
                else cove = 0;
                //UNUS
                if (ejAntCrisp==0)
                    unus = 0;
                else unus =  cove * ((float)ejAntClassCrisp/ejAntCrisp - (float)Examples.getExamplesClass(classFinal[indi])/Examples.getNEx());
                if (numVarNoInterv >= Variables.getNVars())
                    unus = 0;
                float minUnus = (1-(ejM/ejT)) * (0-(ejM/ejT));
                float maxUnus = (ejM/ejT) * (1-(ejM/ejT));

                unus = (unus - minUnus) / (maxUnus - minUnus);
                
                //GAIN
                if ((ejAntCrisp==0)||(sens==0)) {
                    if (Examples.getExamplesClass(classFinal[indi])!=0)
                        gain = sens * (0-((float)Math.log10((float)Examples.getExamplesClass(classFinal[indi])/Examples.getNEx())));
                    else gain = 0;
                } else {
                    gain = sens * (((float)Math.log10(sens/cove)) - ((float)Math.log10((float)Examples.getExamplesClass(classFinal[indi])/Examples.getNEx())));
                }

                //CONF
                if (gradoCompAntFuzzy != 0)
                    fcnf = (float)gradoCompAntClassFuzzy/gradoCompAntFuzzy;
                else
                    fcnf = 0;
                
                // TPR
                if(Examples.getExamplesClass(classFinal[indi]) != 0){
                tpr = (float) ejAntClassCrisp / (float) Examples.getExamplesClass(classFinal[indi]);
                } else {
                    tpr = 0;
                }
                // FPr
                if(Examples.getExamplesClass(classFinal[indi]) != 0){
                fpr = (float) ejAntNoClassCrisp / (float) (ejT - Examples.getExamplesClass(classFinal[indi]));
                } else {
                    fpr = 0;
                }
                
                difs = Math.abs(tpr - fpr);
                //GRAT
                if(tpr!=0 && fpr!=0) grat = tpr/fpr;
                else if(tpr!=0 && fpr==0) grat = Float.POSITIVE_INFINITY;
                else grat = 0;
                
                // FISHER
                    FisherExact fe = new FisherExact((int)ejT);
                    float fisher = (float) fe.getTwoTailedP((int)tp, (int)fp, 
                                (int) fn, (int) tn);
                    
                // Hellinger
                 float parte1 = 0, parte2 = 0;
                 if(Examples.getExamplesClass(classFinal[indi]) != 0){
                     parte1 = (float) (Math.sqrt((float)(ejAntClassCrisp/Examples.getExamplesClass(classFinal[indi]))) - 
                                Math.sqrt((float)(fn/Examples.getExamplesClass(classFinal[indi]))));
                 }
                 
                 if((ejT - Examples.getExamplesClass(classFinal[indi])) != 0){
                     parte2 = (float)(Math.sqrt((float)(ejAntNoClassCrisp)/(ejT-Examples.getExamplesClass(classFinal[indi]))) - 
                                Math.sqrt((float)tn/(ejT-Examples.getExamplesClass(classFinal[indi]))));
                 }
                 float  dh = (float) Math.sqrt(
                             ((Math.pow(parte1,2)) +
                             (Math.pow(parte2,2))));
                
                contents= "" + threeInts.format(rule) + "   ";
                contents+= "\t" + threeInts.format(classFinal[indi]);
                contents+= "\t-";
                contents+= "\t" + sixDecimals.format(nvar);
                contents+= "\t" + sixDecimals.format(leng);
                contents+= "\t" + sixDecimals.format(unus);
                if(gain == Float.POSITIVE_INFINITY) contents+= "\tINFINITY" + sixDecimals.format(gain);
                else contents+= "\t" + sixDecimals.format(gain);
                contents+= "\t" + sixDecimals.format(sens);
                contents+= "\t" + sixDecimals.format(difs);
                contents+= "\t" + sixDecimals.format(fcnf);
                if(grat == Float.POSITIVE_INFINITY) contents+= "\tINFINITY" + sixDecimals.format(grat);
                else contents+= "\t" + sixDecimals.format(grat);
                contents += "\t" + sixDecimals.format(fisher);
                contents += "\t" + sixDecimals.format(dh);
                contents += "\t" + sixDecimals.format(tpr);
                contents += "\t" + sixDecimals.format(fpr);
                contents+= "\n";

                // Add values of quality measures for each rule
                Files.addToFile(filename, contents);  

                AvNVAR += nvar;
                AvLENG += leng;
                AvUNUS += unus;
                AvGAIN += gain;
                AvSENS += sens;
                AvDIFS += difs;
                AvFCNF += fcnf;
                AvTPr += tpr;
                AvFPr += fpr;
                AvDH += dh;
                if(fisher<0.1) AvTF++;
                if(grat>1) AvGR++;
                rule++;
                
//            }
       }
       
       //Calcular soporte con Examples
       float exCov = 0;
       for(int i=0; i<Examples.getNEx(); i++){
           if(Examples.getCovered(i)) exCov++;
       }
        
       contents= "---\t";
       contents+= "---";
       contents+= "\t" + nrules;
       contents+= "\t" + sixDecimals.format(AvNVAR/nrules);
       contents+= "\t" + sixDecimals.format(AvLENG/nrules);
       contents+= "\t" + sixDecimals.format(AvUNUS/nrules);
       contents+= "\t" + sixDecimals.format(AvGAIN/nrules);
       contents+= "\t" + sixDecimals.format(AvSENS/nrules);
       contents+= "\t" + sixDecimals.format(AvDIFS/nrules);
       contents+= "\t" + sixDecimals.format(AvFCNF/nrules);
       contents+= "\t" + sixDecimals.format(AvGR/nrules);
       contents+= "\t" + sixDecimals.format(AvTF/nrules);
       contents+= "\t" + sixDecimals.format(AvDH/nrules);
       contents+= "\t" + sixDecimals.format(AvTPr/nrules);
       contents+= "\t" + sixDecimals.format(AvFPr/nrules);
       contents+= "\n";

       // Add average values of quality measures for each rule
       Files.addToFile(filename, contents);
    }    
        
    /**
    * <p>
    * Dataset file writting to output file
    * </p>
    * @param filename           Output file
    */
    public static void WriteOutDataset (String filename) {
        String contents;
        contents = Data.getHeader();
        contents+= Attributes.getInputHeader() + "\n";
        contents+= Attributes.getOutputHeader() + "\n\n";
        contents+= "@data \n";
        File.writeFile(filename, contents);
    }

    /**
    * <p>
    * Dataset file writting to tracking file
    * </p>
    * @param filename           Tracking file
    */
    public static void WriteSegDataset (String filename) {
        String contents="\n";
        contents+= "--------------------------------------------\n";
        contents+= "|               Dataset Echo               |\n";
        contents+= "--------------------------------------------\n";
        contents+= "Number of examples: " + Examples.getNEx() + "\n";
        contents+= "Number of variables: " + Variables.getNVars()+ "\n";
        contents+= Data.getHeader() + "\n";
        if (filename!="")
            File.AddtoFile(filename, contents);
    }

    /**
     * <p>
     * Writes the rule and the quality measures
     * </p>
     * @param pob                       Final population with EP
     * @param nrules                    Number of rules
     */
    public static void WriteRules (Population pop, int nrules, int[] classFinal) {

          String contents;

          File.writeFile(rule_file, "");
          
          for(int aux=0; aux<nrules; aux++){
            // Write the quality measures of the rule in "measure_file"
            //QualityMeasures Result = new QualityMeasures( );
            //Result = pop.getIndiv(aux).getMeasures();

            // Rule File
            contents = "GENERATED RULE " + aux + "\n";
            contents+= "\tAntecedent\n";

            //Canonical rules
            if(AG.getRulesRep().compareTo("CAN")==0){
               CromCAN regla = pop.getIndivCromCAN(aux);
               for (int auxi=0; auxi<Variables.getNVars(); auxi++) {
                   if (!Variables.getContinuous(auxi)) {    // Discrete variable
                       if (regla.getCromElem(auxi)<Variables.getNLabelVar(auxi)) {
                           contents+= "\t\tVariable " + Attributes.getInputAttribute(auxi).getName() + " = " ;
                           contents+= Attributes.getInputAttribute(auxi).getNominalValue(regla.getCromElem(auxi)) + "\n";
                       }
                   }
                   else {  // Continuous variable
                       if (regla.getCromElem(auxi)<Variables.getNLabelVar(auxi)) {
                           contents+= "\t\tVariable " + Attributes.getInputAttribute(auxi).getName() + " = ";
                           contents+= "Label " + regla.getCromElem(auxi);
                           contents+= " \t (" + Variables.getX0(auxi,(int) regla.getCromElem(auxi));
                           contents+= " " + Variables.getX1(auxi,(int) regla.getCromElem(auxi));
                           contents+= " " + Variables.getX3(auxi,(int) regla.getCromElem(auxi)) +")\n";
                       }
                   }
               }
            } else {
               //DNF rules
                CromDNF regla = pop.getIndivCromDNF(aux);

                for (int i=0; i<Variables.getNVars(); i++) {
                    if (regla.getCromGeneElem(i,Variables.getNLabelVar(i))==true){
                        if (!Variables.getContinuous(i)) {    // Discrete variable
                            contents+= "\tVariable " + Attributes.getInputAttribute(i).getName() + " = " ;
                            for (int j=0; j<Variables.getNLabelVar(i); j++) {
                                if (regla.getCromGeneElem(i, j)==true)
                                    contents+= Attributes.getInputAttribute(i).getNominalValue(j) + " ";
                            }
                            contents+= "\n";
                        }
                        else {  // Continuous variable
                            contents+= "\tVariable " + Attributes.getInputAttribute(i).getName() + " = ";
                            for (int j=0; j<Variables.getNLabelVar(i); j++) {
                                if (regla.getCromGeneElem(i, j)==true) {
                                    contents+= "Label " + j;
                                    contents+= " (" + Variables.getX0(i,j);
                                    contents+= " " + Variables.getX1(i,j);
                                    contents+= " " + Variables.getX3(i,j) +")\t";
                                }
                            }
                            contents+= "\n";
                        }
                    }
               }

             }
             
            contents+= "\tConsecuent: " + Attributes.getOutputAttribute(0).getNominalValue(classFinal[aux])+"\n\n";
            File.AddtoFile(rule_file, contents);

          }
    }

    /**
     * <p>
     * Main method of the algorithm
     * </p>
     **/
    @Override
    public void learn(InstanceSet training, HashMap<String, String> params){
        
        String contents;                    // String for the file contents
        String NameRule, NameMeasure;       // String containing de original names for the rules and measures files
        boolean terminar = false;           // Indicates no more repetition for the rule generation of diferent classes
        int NumRulesGenerated = 0;          // Number of rules generated
        
        int clase;                          // Store the value of the class to analyse
        
        // Initial echo
        System.out.println("\nEvAEP implementation");

        Variables = new TableVar();
        Examples = new TableDat();
        AG = new Genetic();

        // Read parameter file and initialize parameters
        ReadParameters (params);
        NameRule = rule_file;

        try {
            // Read the dataset, store values and echo to output and seg files
            CaptureDatasetTraining (training);
        } catch (IOException ex) {
            Logger.getLogger(EvAEP.class.getName()).log(Level.SEVERE, null, ex);
        }
        //WriteOutDataset(output_file_tra); // Creates and writes the exit files 
        //WriteOutDataset(output_file_tst); // Creates and writes the exit files 
        //WriteSegDataset (seg_file);

        // Create and initilize gain information array
        //Variables.GainInit(Examples, seg_file);

        // Screen output of same parameters
        System.out.println ("\nSeed: " + seed);    // Random Seed
        System.out.println ("\nOutput variable: " + Attributes.getOutputAttribute(0).getName() ); // Output variable

        // Initialize measure file
        /*String cab_measure_file = "";
        cab_measure_file = "--------------------------------------------\n";
        cab_measure_file+= "|              Measures file               |\n";
        cab_measure_file+= "--------------------------------------------\n\n";
        cab_measure_file+= "\n\nCLASS\tFITNESS";*/
        
        // Initialization of random generator seed. Done after load param values
        if (seed!=0) Randomize.setSeed (seed);  
        Population popFinal = new Population(100, Variables.getNVars(), Examples.getNEx(), AG.getRulesRep(), Variables, AG.getTrials());
        int classFinal[] = new int[100];
        
        long t_ini = System.currentTimeMillis();
        
        if(!AG.getRoundRobin()) {
            //--------------------
            //ONE VERSUS ALL STUDY
            //--------------------
            System.out.println("One Vs. All STUDY");
            //Algorithm is executed for all classes
            for(clase=0; clase< Attributes.getOutputAttribute(0).getNumNominalValues(); clase++){
                
                Variables.setNumClassObj(clase);
                Variables.setNameClassObj(Attributes.getOutputAttribute(0).getNominalValue(clase));
                System.out.println ("Generate rules for class: "+clase);

                // Set all the examples as not covered
                for (int ej=0; ej<Examples.getNEx(); ej++)
                    Examples.setCovered (ej,false);  // Set example to not covered

                // Load the number of examples of the target class
                Examples.setExamplesClassObj(Variables.getNumClassObj());

                // Variables Initialization
                Examples.setExamplesCovered(0);
                Examples.setExamplesCoveredClass(0);
                terminar = false;

                // Tracking to file and "seg" file
                System.out.println ("\nTarget class number: " + Variables.getNumClassObj() + " (value " + Variables.getNameClassObj() + ")");

                contents = "\n";
                contents+= "--------------------------------------------\n";
                contents+= "|                 Class "+Variables.getNumClassObj()+"                  |\n";
                contents+= "--------------------------------------------\n\n";

                //File.AddtoFile(seg_file, contents);
                System.out.println(contents);

                System.out.println("Number of rule: \n");

                //File.AddtoFile(seg_file, "Number of rule: \n");

                boolean rulesClass = false;
                
                do {        

                    Individual result = AG.GeneticAlgorithm(Variables,Examples,seg_file);
                 
                    if((result.getMeasures().getGRat() < 1) ||
                        (Examples.getExamplesCoveredClass()==Examples.getExamplesClassObj()) ||
                        result.getMeasures().getNSup()==0) {
                            terminar = true;
                    }

                    if((rulesClass == false) || (terminar==false)){
                    
                        System.out.print("#"+NumRulesGenerated+":\n");
                       // File.AddtoFile(seg_file, "#"+NumRulesGenerated+":\n");
                       
                       // Here is where the translation of CAN or DNF rules to Pattern is done              
                        this.patterns.add(toPattern(result));
                        
                       
                        result.Print("");
                        result.getMeasures().Print("", AG);
                        
                        // Duplicate the size of the result population if neccesary
                        /*if(NumRulesGenerated==popFinal.getNumIndiv()-1){
                            Population aux_popFinal = new Population(popFinal.getNumIndiv()*2, Variables.getNVars(), Examples.getNEx(), AG.getRulesRep(), Variables, AG.getTrials());
                            int[] aux_classFinal = new int[classFinal.length*2];
                            for(int i=0; i<classFinal.length; i++)
                                aux_classFinal[i] = classFinal[i];
                            aux_popFinal.CopyPopulation(popFinal, Examples.getNEx());
                            popFinal = new Population(popFinal.getNumIndiv()*2, Variables.getNVars(), Examples.getNEx(), AG.getRulesRep(), Variables, AG.getTrials());
                            popFinal.CopyPopulation(aux_popFinal, Examples.getNEx());
                            classFinal = new int[aux_classFinal.length];
                            for(int i=0; i<classFinal.length; i++)
                                classFinal[i] = aux_classFinal[i];
                        }
                        popFinal.CopyIndiv(NumRulesGenerated, Examples.getNEx(), result);
                        classFinal[NumRulesGenerated] = clase;
                        */
                        NumRulesGenerated++; 

                        //Update Examples Structure
                        for(int j=0; j<Examples.getNEx(); j++){
                            if(result.getIndivCovered(j)==true){
                                if(Examples.getCovered(j)==false) {
                                    Examples.setCovered(j, true);
                                    Examples.setExamplesCovered(Examples.getExamplesCovered()+1);
                                if(Examples.getClass(j) == Variables.getNumClassObj())
                                    Examples.setExamplesCoveredClass(Examples.getExamplesCoveredClass()+1);
                                }
                            }
                        }
                    }
                    
                    rulesClass = true;

                } while (terminar==false);            
            }
        } 
        
        System.out.println("Algorithm terminated\n\n");

        //CALCULAR FICHERO .TRA
        //CalculateOutDataCAN(output_file_tra,0,popFinal,classFinal,NumRulesGenerated);
        //LEER FICHERO .TST
//        CaptureDatasetTest();
        //CALCULAR FICHERO .TST
        //CalculateOutDataCAN(output_file_tst,1,popFinal,classFinal,NumRulesGenerated);
        //CALCULAR FICHERO DE MEDIDAS
        //CalculateQMeasures(popFinal,NumRulesGenerated,qmeasure_file,classFinal);
        //ESCRIBIR LAS REGLAS
        //WriteRules(popFinal, NumRulesGenerated, classFinal);
        
        long t_end = System.currentTimeMillis();
        
        
        System.out.println("EXECUTION TIME: " + (t_end - t_ini) / 1000d + " seconds.");
  }

    
  @Override
  public String[][] predict(InstanceSet test){
        String[][] result = new String[4][test.getNumInstances()];
        result[0] = super.getPredictions(super.patterns, test);
        result[1] = super.getPredictions(super.patternsFilteredMinimal, test);
        result[2] = super.getPredictions(super.patternsFilteredMaximal, test);
        result[3] = super.getPredictions(super.patternsFilteredByMeasure, test);
        return result;
  }
    

  private static int NumInterv (float value, int num_var, TableVar Variables) {
        float pertenencia=0, new_pert=0;
        int interv = -1;

        for (int i=0; i<Variables.getNLabelVar(num_var); i++) {
            new_pert = Variables.Fuzzy(num_var, i, value);
            if (new_pert>pertenencia) {
                interv = i;
                pertenencia = new_pert;
            }
        }
        return interv;

  }
  
  
  /**
   * Convert a Rule of class Individual into a Pattern of the framework
   * @param ind
   * @return 
   */
  public Pattern toPattern(Individual ind){
      ArrayList<Item> items = new ArrayList<>();
      if(AG.getRulesRep().equalsIgnoreCase("CAN")){
          // CAN RULE
          CromCAN crom = ind.getIndivCromCAN();
         
          for(int i = 0; i < crom.getCromLength(); i++){
              if(!Variables.getContinuous(i)){
                  // Nominal variable
                  if(crom.getCromElem(i) < Variables.getNLabelVar(i)){
                      // Variable takes part in the rule, add it to the Pattern
                      int j = crom.getCromElem(i);
                      NominalItem it = new NominalItem(Attributes.getAttribute(i).getName(), Attributes.getInputAttribute(i).getNominalValue(j));
                      items.add(it);
                  }
              } else {
                  // Numeric variable -> Fuzzy Item
                  if(crom.getCromElem(i) < Variables.getNLabelVar(i)){
                      framework.utils.Fuzzy fuz = new framework.utils.Fuzzy();
                      int j = crom.getCromElem(i);
                      fuz.setVal(Variables.getX0(i, j), Variables.getX1(i, j), Variables.getX3(i, j), 1);
                      FuzzyItem it = new FuzzyItem(Attributes.getAttribute(i).getName(), fuz, "Label " + j);
                      items.add(it);
                  }
              }
          }
      } else{
          // DNF RULE
      }
      
      return new Pattern(items, Variables.getNumClassObj());
  }

    
}
