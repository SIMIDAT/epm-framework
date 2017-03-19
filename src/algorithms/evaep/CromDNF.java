/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

import org.core.*;

public class CromDNF {
     /**
      * Defines the structure and manage the contents of a rule
      * This implementation uses disjunctive formal norm to store the gens.
      * So, variables are codified in binary genes
      */

      private int num_genes;      // Number of genes
      private Gene cromosoma [];   // Individual content - integer representation

    /**
     * <p>
     * Creates new instance of chromosome, no initialization
     * </p>
     * @param lenght      Length of the chromosome
     * @param Variables     Structure of variables of the dataset
     */
    public CromDNF(int lenght, TableVar Variables) {
      num_genes = lenght;
      cromosoma = new Gene[lenght];
      for(int i=0; i<num_genes; i++){
        cromosoma[i] = new Gene(Variables.getNLabelVar(i));
      }
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
        boolean crom_inic[]= new boolean[num_genes];
        for (int i=0; i<num_genes; i++)
           crom_inic[i] = false;

        // Firtly, we obtain the numbero of variable which are in the chromosome
        int numInterv = Randomize.Randint (1, Math.round(porcVar*Variables.getNVars()));

        int var=0;
        while (var<numInterv) {
            num_var = Randomize.Randint (0, num_genes-1);
            // If the variable is not in the chromosome
            if (crom_inic[num_var]==false) {
                cromosoma[num_var].RndInitGene();
                crom_inic[num_var]=true;
                var++;
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
                    // Put in the correspondent interval //
                    float pertenencia=0, new_pert=0;
                    int interv = Variables.getNLabelVar(num_var);
                    for (int i=0; i < Variables.getNLabelVar(num_var); i++) {
                        new_pert = Variables.Fuzzy(num_var, i, (int) Examples.getDat(aleatorio, num_var));
                        if (new_pert>pertenencia) {
                            interv = i;
                            pertenencia = new_pert;
                        }
                    }
                    int number = Variables.getNLabelVar(num_var);
                    for(int l=0; l<=number; l++){
                        if(l!=num_var){
                            setCromGeneElem(num_var, l, false);
                        }
                    }

                    setCromGeneElem(num_var, interv, true);
                    setCromGeneElem(num_var, number, true);
                } else { //Discrete variable
                    // Put in the correspondent value //
                    int number = Variables.getNLabelVar(num_var);
                    for(int l=0; l<=number; l++){
                        if(l!=num_var){
                            setCromGeneElem(num_var, l, false);
                        }
                    }
                    setCromGeneElem(num_var, (int) Examples.getDat(aleatorio, num_var), true);
                    setCromGeneElem(num_var, number, true);
                }
                crom_inic[num_var]=true;
                var++;
            }
        }

        // Initialise the rest variables
        for (int i=0; i<num_genes; i++)  {
            if (crom_inic[i]==false) {
                int number = Variables.getNLabelVar(i);
                for(int l=0; l<=number; l++){
                    this.setCromGeneElem(i, l, false);
                }
            }
        }
    }

    /**
     * <p>
     * Retuns the lenght of the chromosome
     * </p>
     * @return          Lenght of the chromosome
     */
    public int getCromLenght () {
      return num_genes;
    }


    /**
     * <p>
     * Retuns the gene lenght of the chromosome
     * </p>
     * @return          Lenght of the gene
     */
    public int getCromGeneLenght (int pos) {
      return cromosoma[pos].getGeneLenght();
    }


    /**
     * <p>
     * Retuns the value of the gene indicated
     * </p>
     * @param pos      Position of the variable
     * @param elem          Position of the gene
     */
    public boolean getCromGeneElem (int pos, int elem) {
      return cromosoma[pos].getGeneElem(elem);
    }


   /**
    * <p>
    * Sets the value of the indicated gene of the Chromosome
    * </p>
    * @param pos            Position of the variable
    * @param elem           Position of the gene
    * @param val            Value to insert
    */
    public void setCromGeneElem (int pos, int elem, boolean val) {
        cromosoma[pos].setGeneElem(elem, val);
    }

    /**
     * <p>
     * Prints the chromosome genes
     * </p>
     * @param nFile         File to write the chromosome
     */
    public void Print(String nFile) {
        String contents;
        contents = "Chromosome: \n";
        for(int i=0; i<num_genes; i++){
            contents += "Var "+i+": ";
            int neti = getCromGeneLenght(i);
            for(int l=0; l<=neti; l++){
                contents += this.getCromGeneElem(i, l) + " ";
            }
            contents+="\n";
        }
        if (nFile=="")
            System.out.print (contents);
        else
           File.AddtoFile(nFile, contents);
    }

}
