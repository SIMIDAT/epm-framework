/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

import org.core.*;

public class CromCAN {
    /**
     * <p>
     * Defines the structure and manage the contents of a rule
     * This implementation uses only integer values to store the gens.
     * So, variables values must be discretized (if they are continuous)
     * or translated into integers (if they are enumerated)
     * </p>
     */

      private int num_genes;      // Number of genes
      private int cromosoma [];   // Individual content - integer representation

      
    /**
     * <p>
     * Creates new instance of chromosome, no initialization
     * </p>
     * @param lenght          Length of the chromosome
     */
    public CromCAN(int lenght) {
      num_genes = lenght;
      cromosoma = new int [lenght];
    }


    /**
     * <p>
     * Random initialization of an existing chromosome
     * </p>
     * @param Variables     Structure of variables of the dataset
     * @param porcVar       Participating variables in the chromosom
     */
    public void RndInitCrom(TableVar Variables, float porcVar) {

        int num_var;

        // This array indicates if every chromosome has been initialised
        boolean crom_inic[] = new boolean[num_genes];
        for (int i=0; i<num_genes; i++)
           crom_inic[i] = false;
        
        // Firtly, we obtain the numbero of variable which are in the chromosome
        int numInterv = Randomize.Randint (1, Math.round(porcVar*Variables.getNVars()));
        numInterv = Math.round(porcVar*Variables.getNVars());
        
        int var=0;
        while (var<numInterv) {
            num_var = Randomize.Randint (0, num_genes-1);
            // If the variable is not in the chromosome
            if (crom_inic[num_var] == false) {
                cromosoma[num_var] = Randomize.Randint (0, Variables.getNLabelVar(num_var)-1);
                crom_inic[num_var] = true;
                var++;
            }
        }
        
        // Initialise the rest variables
        for (int i=0; i<num_genes; i++)  {
            if (crom_inic[i]==false) {
                cromosoma[i] = Variables.getNLabelVar(i);
            }
        }
    }


    /**
     * <p>
     * Initialization based on coverage
     * </p>
     * @param Variables		Contents the type of the variable, and the number of labels.
     * @param Examples          Dataset
     * @param porcVar           Percentage of participating variables
     */
    public void BsdInitCrom(TableVar Variables, TableDat Examples, float porcVar) {

        int num_var;

        boolean crom_inic[] = new boolean[num_genes];
        for (int i=0; i<num_genes; i++)
           crom_inic[i] = false;

        // Number of participating variables in the chromosome
        int numInterv = Randomize.Randint (1, Math.round(porcVar*Variables.getNVars()));
        numInterv = Math.round(porcVar*Variables.getNVars());
        
        
        boolean centi = false;
        int aleatorio = 0;
        int ii=0;
        //Search an example not covered and for the objective class
        while((!centi)&&(ii<Examples.getNEx())){
            aleatorio = Randomize.Randint(0, Examples.getNEx()-1);
            if((Examples.getCovered(aleatorio)==false)&&(Examples.getClass(aleatorio)==Variables.getNumClassObj()))
                centi = true;
            ii++;
        }

        //In aleatorio we store the example to initiate the chromosome
        int var=0;
        while (var<numInterv) {
            num_var = Randomize.Randint (0, num_genes-1);
            // If the variable is not in the chromosome
            if (crom_inic[num_var]==false) {
                if (Variables.getContinuous(num_var)) { //Continuous variable
                    // Put in the correspondent interval 
                    float pertenencia=0, new_pert=0;
                    int interv = Variables.getNLabelVar(num_var)-1;
                    for (int i=0; i < Variables.getNLabelVar(num_var); i++) {
                        new_pert = Variables.Fuzzy(num_var,i,(int) Examples.getDat(aleatorio, num_var));
                        if (new_pert>pertenencia) {
                            interv = i;
                            pertenencia = new_pert;
                        }
                    }
                    cromosoma[num_var] = interv;
                } else { //Discrete variable
                    // Put in the correspondent value //
                    cromosoma[num_var] = (int) Examples.getDat(aleatorio, num_var);
                }
                crom_inic[num_var]=true;
                var++;
            }
        }

        // Initialise the rest variables
        for (int i=0; i<num_genes; i++)  {
            if (crom_inic[i]==false) {
//                if(Variables.getContinuous(i)) cromosoma[i] = Variables.getNLabelVar(i);
//                else cromosoma[i] = (int) Variables.getMax(i)+1;
                cromosoma[i] = Variables.getNLabelVar(i);
            }
        }
    }

    
    /**
     * <p>
     * Retuns the value of the gene indicated
     * </p>
     * @param pos      Position of the gene
     * @return              Value of the gene
     */
    public int getCromElem (int pos) {
      return cromosoma[pos];
    }
    
    
    /**
     * <p>
     * Sets the value of the indicated gene of the chromosome
     * </p>
     * @param pos      Position of the gene
     * @param value         Value of the gene
     */
    public void setCromElem (int pos, int value) {
      cromosoma[pos] = value;
    }
    
    
    /**
     * <p>
     * Retuns the gene lenght of the chromosome
     * </p>
     * @return          Gets the lenght of the chromosome
     */
    public int getCromLength () {
      return num_genes;
    }
   
   
    /**
     * <p>
     * Prints the chromosome genes
     * </p>
     * @param nFile         File to write the cromosome
     */
    public void Print(String nFile) {
        String contents;
        contents = "Chromosome: ";
        for(int i=0; i<num_genes; i++)
            contents+= cromosoma[i] + " ";
        contents+= "\n";
        if (nFile=="")
            System.out.print (contents);
        else
           File.AddtoFile(nFile, contents);
    }
    
}
