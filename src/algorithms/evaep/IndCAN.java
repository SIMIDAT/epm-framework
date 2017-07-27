/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */
 
package algorithms.evaep;

import org.core.File;
import org.core.Randomize;

public class IndCAN extends Individual {
    /**
     * <p>
     * Defines the individual of the population
     * </p>
     */

    public CromCAN cromosoma;   // Individual contents

    /**
     * <p>
     * Creates new instance of Canonical individual
     * </p>
     */
    public IndCAN(){
    }

    /**
     * </p>
     * Creates new instance of Canonical individual
     * </p>
     * @param lenght          Lenght of the individual
     * @param neje              Number of examples
     */
   public IndCAN(int lenght, int neje, int trials) {

        tamano = lenght;
        cromosoma = new CromCAN(lenght);
        medidas = new QualityMeasures( );

        evaluado = false;
        cubre = new boolean [neje];

        n_eval = trials;

    }


    /**
     * <p>
     * Creates random instance of Canonical individual
     * </p>
     * @param Variables             Variables structure
     * @param porcVar               Percentage of variables initialised in the population
     * @param neje                  Number of exaples
     * @param nFile                 File to write the individual
     */
    public void RndInitInd(TableVar Variables, float porcVar, int neje, String nFile) {
        cromosoma.RndInitCrom(Variables, porcVar);  // Random initialization method
        evaluado = false;                           // Individual not evaluated
        for (int i=0; i<neje; i++){
            cubre[i] = false;
        }

        n_eval = 0;
    }


    /**
     * <p>
     * Creates instance of Canonical individual based on coverage
     * </p>
     * @param Variables     Variables structure
     * @param Examples      Examples structure
     * @param porcVar       Percentage of variables to form the individual
     * @param nFile         File to write the individual
     */
    public void BsdInitInd(TableVar Variables, TableDat Examples, float porcVar, String nFile) {

        cromosoma.BsdInitCrom(Variables, Examples, porcVar);
  
        evaluado = false;

        for (int i=0; i<Examples.getNEx(); i++){
            cubre[i] = false;
        }

        n_eval = 0;
    }


    /**
     * <p>
     * Returns the Chromosome
     * </p>
     * @return              Chromosome
     */
    public CromCAN getIndivCrom () {
        return cromosoma;
    }


    /**
     * <p>
     * Returns the indicated gene of the Chromosome
     * </p>
     * @param pos               Position of the gene
     * @return                  Value of the gene
     */
    public int getCromElem (int pos) {
        return cromosoma.getCromElem (pos);
    }


    /**
     * <p>
     * Returns the value of the indicated gene for the variable
     * </p>
     * @param pos               Position of the variable
     * @param elem              Position of the gene
     * @return                  Value of the gene
     */
    public boolean getCromGeneElem(int pos, int elem){
        return false;
    }


    /**
     * <p>
     * Sets the value of the indicated gene of the Chromosome
     * </p>
     * @param pos               Position of the variable
     * @param val               Value of the variable
     */
    public void setCromElem (int pos, int val) {
        cromosoma.setCromElem(pos, val);
    }

    /**
     * <p>
     * Sets the value of the indicated gene of the Chromosome
     * </p>
     * @param pos               Position of the variable
     * @param elem              Position of the gene
     * @param val               Value of the variable
     */
    public void setCromGeneElem (int pos, int elem, boolean val){}


    /**
     * <p>
     * Returns the indicated Chromosome
     * </p>
     * @return                  The canonical Chromosome
     */
    public CromCAN getIndivCromCAN(){
        return cromosoma;
    }


    /**
     * <p>
     * Returns the indicated Chromosome
     * </p>
     * @return                  The DNF Chromosome
     */
    public CromDNF getIndivCromDNF(){
        return null;
    }

    /**
     * <p>
     * Copy the indicaded individual in "this" individual
     * </p>
     * @param a              The individual to Copy
     * @param neje              Number of examples
     */
    public void copyIndiv (Individual a, int neje) {
        for (int i=0;i<this.tamano;i++)
            this.setCromElem(i, a.getCromElem(i));

        this.setIndivEvaluated(a.getIndivEvaluated());
        for (int i=0;i<neje;i++)
           this.cubre[i] = a.cubre[i];

        this.setNEval(a.getNEval());
        
        this.medidas.Copy(a.getMeasures());
        
    }


    /**
     * <p>
     * Evaluate a individual. This function evaluates an individual.
     * NOTE: FUZZY COVERAGE IS <strong>DISABLED</strong>
     * </p>
     * @param AG                Genetic algorithm
     * @param Variables         Variables structure
     * @param Examples          Examples structure
     */
    public void evalInd (Genetic AG, TableVar Variables, TableDat Examples) {

//        int ejAntFuzzy=0;                // Number of compatible examples with the antecedent of any class - fuzzy version --- unused
//        int ejAntClassFuzzy=0;           // Number of compatible examples (antecedent and class) - fuzzy version
//        int ejAntNoClassFuzzy=0;           // Number of compatible examples (antecedent and class) - fuzzy version
//        int ejAntClassNewFuzzy=0;        // Number of new covered compatible examples (antec and class) - fuzzy version
//        float gradoAntFuzzy=0;           // Total compatibility degree with the antecedent - fuzzy version
//        float gradoAntClassFuzzy=0;      // Tot compatibility degree with antecedent and class - fuzzy version
//        float gradoAntClassNewEjFuzzy=0; // Tot compatibility degree with antecedent and class of new covered examples - fuzzy version
//        float disparoFuzzy;    // Final compatibility degree of the example with the individual - fuzzy version

        int ejAntCrisp=0;                // Number of compatible examples with the antecedent of any class - crisp version
        int ejAntClassCrisp=0;           // Number of compatible examples (antecedent and class) - crisp version
        int ejAntNoClassCrisp=0;         // Number of compatible examples (antecedent and NOT class) - crisp version
        int ejAntClassNewCrisp=0;        // Number of new covered compatible examples (antec and class) - crisp version
        float disparoCrisp;    // Final compatibility or not of the example with the individual - crisp version

        float tp=0;
        float tn=0;
        float fp=0;
        float fn=0;     
        float tpr=0;
        float fpr=0;
        float tnr=0;
        
        float leng, supM, supm, unus, gain, difs, sens, nsup, conf, medgeo;

        int ejClase[] = new int[Variables.getNClass()];
        int cubreClase[] = new int[Variables.getNClass()];
        for (int i=0; i<Variables.getNClass(); i++) {
            cubreClase[i]=0;
            ejClase[i] = Examples.getExamplesClass (i);
        }

        int numVarNoInterv=0;  // Number of variables not taking part in the individual


        for (int i=0; i<Examples.getNEx(); i++) { // For each example of the dataset
            // Initialization
//            disparoFuzzy = 1;
            disparoCrisp = 1;
            numVarNoInterv = 0;

            // Compute all chromosome values
            for (int j=0; j<Variables.getNVars(); j++) {
                if (!Variables.getContinuous(j)) {  // Discrete Variable
                    if (cromosoma.getCromElem(j)<=Variables.getMax(j)){
                        // Variable j takes part in the rule
                        if ((Examples.getDat(i,j) != cromosoma.getCromElem(j)) && (!Examples.getLost(Variables,i,j))) {
                            // If chromosome value <> example value, and example value is not a lost value
//                            disparoFuzzy = 0;    
                            disparoCrisp = 0;    
                        }
                    }
                    else
                        numVarNoInterv++;  // Variable does not take part
                }
                else {	// Continuous variable
                    if (cromosoma.getCromElem(j)<Variables.getNLabelVar(j)) {
                        // Variable takes part in the rule
                        // Fuzzy computation
//                        if (!Examples.getLost(Variables,i,j)) {
//                            // If the value is not a lost value
//                            float pertenencia = Variables.Fuzzy(j, cromosoma.getCromElem(j), Examples.getDat(i,j));
//                            disparoFuzzy = Utils.Minimum (disparoFuzzy, pertenencia);
//                        }
                        // Crisp computation
                        if (!Examples.getLost(Variables,i,j))
                            if (NumInterv (Examples.getDat(i,j),j, Variables)!= cromosoma.getCromElem(j))
                                disparoCrisp = 0;
                    }
                    else
                        numVarNoInterv++;  // Variable does not take part
                }
            } // End FOR all chromosome values

            // Update counters and mark example if needed
//            gradoAntFuzzy += disparoFuzzy;
            if (disparoCrisp>0) {
            	ejAntCrisp++;
                cubre[i]=true;
                if (Examples.getClass(i) == Variables.getNumClassObj()) {
//                    gradoAntClassFuzzy +=disparoFuzzy;
                    ejAntClassCrisp ++;
                    tp++;
                } else {
                    ejAntNoClassCrisp ++;
                    fp++;
                }
                cubreClase[Examples.getClass(i)]++;
                if ((!Examples.getCovered(i)) &&  (Examples.getClass(i) == Variables.getNumClassObj())) {
                    // If example was not previusly covered and belongs to the target class increments the number of covered examples
                    ejAntClassNewCrisp++;
//                    gradoAntClassNewEjFuzzy += disparoFuzzy;
                }
            } else {
                cubre[i]=false;
                if (Examples.getClass(i) == Variables.getNumClassObj()) {
                    fn++;
                } else {
                    tn++;
                }
            }
            
        } // End of cycle for each example


        // Compute the measures
        //LENGTH
        if (ejAntClassCrisp != 0)
            leng = ((float)1/ejAntClassCrisp);
        else leng = 0;
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setLength(0);
        else medidas.setLength(leng);
        //SENS
        if (Examples.getExamplesClassObj() != 0)
            sens = ((float)ejAntClassCrisp/Examples.getExamplesClassObj());
        else sens = 0;
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setSens(0);
        else medidas.setSens(sens);
        //CONF
        if (ejAntCrisp != 0)
            conf = (float)ejAntClassCrisp/ejAntCrisp;
        else conf = 0;
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setConf(0);
        else medidas.setConf(conf);
        //UNUS
        float coverage = ((float)ejAntCrisp/Examples.getNEx());
        if (ejAntCrisp==0)
            unus = 0;
        else unus =  coverage * ((float)ejAntClassCrisp/ejAntCrisp - (float)Examples.getExamplesClassObj()/Examples.getNEx());
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setUnus(0);
        else medidas.setUnus(unus);
        //NSUP
        if (Examples.getExamplesClassObj()-Examples.getExamplesCoveredClass()!=0)
            nsup = ((float) (ejAntClassNewCrisp) / (Examples.getExamplesClassObj()-Examples.getExamplesCoveredClass()));
        else nsup = 0;
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setNSup(0);
        else medidas.setNSup(nsup);
        //SUPM
        /*
        if (Examples.getNEx() != 0)
            supM = ((float)ejAntNoClassCrisp/Examples.getNEx());
        else supM = 0;
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setSupM(0);
        else medidas.setSupM(supM);
        //SUPm
        if (Examples.getNEx() != 0)
            supm = ((float)ejAntClassCrisp/Examples.getNEx());
        else supm = 0;
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setSupm(0);
        else medidas.setSupm(supm);
        //DIFF SUPPORT
        difs = supm - supM;
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setDifS(0);
        else medidas.setDifS(difs);
        */
        //GAIN
        if ((ejAntCrisp==0)||(sens==0))
            gain = sens * (0-((float)Math.log10((float)Examples.getExamplesClassObj()/Examples.getNEx())));
        else {
            gain = sens * (((float)Math.log10(sens/coverage)) - ((float)Math.log10((float)Examples.getExamplesClassObj()/Examples.getNEx())));
        }
        if (numVarNoInterv >= Variables.getNVars())
            medidas.setGain(0);
        else medidas.setGain(gain);
        //TPr
        if (numVarNoInterv >= Variables.getNVars()){
            tpr=0;
        } else {
            if ((tp+fn)!=0)
                tpr=tp/(tp+fn);
            else tpr=0;
        }
        //FPr
        if (numVarNoInterv >= Variables.getNVars()){
            fpr=0;
        } else {
            if ((fp+tn)!=0)
                fpr=fp/(fp+tn);
            else fpr=0;
        }
        //TNr
        if (numVarNoInterv >= Variables.getNVars()){
            tnr=0;
        } else {
            if ((fp+tn)!=0)
                tnr=tn/(fp+tn);
            else tnr=0;
        }
        
        supM = fpr; 
        medidas.setSupM(supM);
        supm = tpr; 
        medidas.setSupm(supm);
        difs = Math.abs(supm - supM); 
        medidas.setDifS(difs);
        
        /*
        AUC
        if (numVarNoInterv >= Variables.getNVars()){
            auc = 0;
        } else auc = (1 + tpr - fpr) / 2;
        medidas.setAUC(auc);
        */
        //MedGeo
        if (numVarNoInterv >= Variables.getNVars()){
            medgeo = 0;
        } else medgeo = (float) Math.sqrt(tpr*tnr);
        medidas.setMedGeo(medgeo);
        //GRAT
        float grat;
        if(tpr!=0 && fpr!=0) grat = tpr/fpr;
        else if(tpr!=0 && fpr==0) grat = Float.POSITIVE_INFINITY;
        else grat = 0;
        
        medidas.setGRat(grat);
        
        //Introduce in fitness the correspondent value
        if (AG.getFitness().compareTo("NSUP")==0) medidas.setFitness(nsup);
        if (AG.getFitness().compareTo("SENS")==0) medidas.setFitness(sens);
        if (AG.getFitness().compareTo("SUPMA")==0) medidas.setFitness(supM);
        if (AG.getFitness().compareTo("SUPMI")==0) medidas.setFitness(supm);
        if (AG.getFitness().compareTo("UNUS")==0) medidas.setFitness(unus);
        if (AG.getFitness().compareTo("CONF")==0) medidas.setFitness(conf);
        if (AG.getFitness().compareTo("MEDGEO")==0) medidas.setFitness(medgeo);
        if (AG.getFitness().compareTo("GRAT")==0) medidas.setFitness(grat);
        if (AG.getFitness().compareTo("GAIN")==0) medidas.setFitness(gain);
        
        // Set the individual as evaluated
        evaluado = true;

    }

    /**
     * <p>
     * Returns the number of the interval of the indicated variable to which belongs
     * the value. It is performed seeking the greater belonging degree of the
     * value to the fuzzy sets defined for the variable
     * </p>
     * @param value                 Value to calculate
     * @param num_var               Number of the variable
     * @param Variables             Variables structure
     * @return                      Number of the interval
     */
    public int NumInterv (float value, int num_var, TableVar Variables) {
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

    public float getClassIndividual (TableVar Variables, TableDat Examples, int eje) {
    
        float disparo = 1;
        
        for (int j=0; j<Variables.getNVars(); j++) {
            if (!Variables.getContinuous(j)) {  // Discrete Variable
                if (cromosoma.getCromElem(j)<=Variables.getMax(j)){
                    // Variable j takes part in the rule
                    if ((Examples.getDat(eje,j) != cromosoma.getCromElem(j)) && (!Examples.getLost(Variables,eje,j))) {
                        // If chromosome value <> example value, and example value is not a lost value
                        disparo = 0;    
                    }
                }
            } else {	// Continuous variable
                if (cromosoma.getCromElem(j)<Variables.getNLabelVar(j)) {
                    // Variable takes part in the rule
                    // Crisp computation
                    if (!Examples.getLost(Variables,eje,j)) {
                        if (NumInterv (Examples.getDat(eje,j),j, Variables)!= cromosoma.getCromElem(j))
                            disparo = 0;
                    }
                    // Fuzzy computation
//                    if (!Examples.getLost(Variables,eje,j)) {
//                        // If the value is not a lost value
//                        float pertenencia = Variables.Fuzzy(j, cromosoma.getCromElem(j), Examples.getDat(eje,j));
//                        disparo = Utils.Minimum (disparo, pertenencia);
//                    }
                }
            }
        } // End FOR all chromosome values

        return disparo;
    
    }
    
    
    /**
     * <p>
     * Method to Print the contents of the individual
     * </p>
     * @param nFile             File to write the individual
     */
    public void Print(String nFile) {
        String contents;
        cromosoma.Print(nFile);

        contents = "Evaluated - " + evaluado + "\n";
        contents+= "Evaluation generated " + n_eval + "\n";
        contents+= "Fitness: " + getMeasures().getFitness() +"\n";
        contents+= "Growth Rate: " + getMeasures().getGRat() +"\n";
        if (nFile=="")
            System.out.print (contents);
        else
           File.AddtoFile(nFile, contents);
    }


}
