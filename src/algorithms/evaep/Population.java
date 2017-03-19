/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

import java.util.Vector;
import org.core.Randomize;

public class Population {
    /**
     * <p>
     * Population of candidate rules
     * </p>
     */

      private Individual indivi [];     // Population individuals
      private int num_indiv;           // Max number of individuals in the population
      public boolean ej_cubiertos[];   // Covered examples of the population

      /**
       * <p>
       * Constructor
       * </p>
       */
      public Population(){

      }

      /**
       * <p>
       * Creates a population of Individual
       * </p>
       * @param numind          Number of individuals
       * @param numgen          Number of variables
       * @param neje            Number of examples
       * @param RulRep          Rules representation
       * @param Variables       Variables structure
       */
      public Population(int numind, int numgen, int neje, String RulRep, TableVar Variables, int trials) {

          indivi = new Individual[numind];
          num_indiv = numind;
          for(int i=0; i<numind; i++){
              if(RulRep.compareTo("CAN")==0){
                indivi[i] = new IndCAN(numgen, neje, trials);
              } else {
                indivi[i] = new IndDNF(numgen, neje, Variables, trials);
              }
          }
          ej_cubiertos = new boolean[neje];
          for(int i=0; i<neje; i++)
            ej_cubiertos[i] = false;          
      }

      
      /**
       * <p>
       * Biased random population initialization
       * </p>
       * @param Variables       Variables structure
       * @param Examples        Examples structure
       * @param porcVar         Percentage of variables to form the rules
       * @param porcPob         Percentage of population with biased initialisation
       * @param neje            Number of examples
       * @param nFile           File to write the population
       */
      public void BsdInitPob (TableVar Variables, TableDat Examples, float porcVar, float porcPob, int neje, String nFile) {

          float parteSesg = porcPob * num_indiv;
          int i,j;

          for(i=0; i<parteSesg; i++) {
              indivi[i].BsdInitInd(Variables, Examples, porcVar, nFile);
          }
          
          for(j=i; j<num_indiv; j++) {
              indivi[j].RndInitInd(Variables, porcVar, neje, nFile);
          }

          for(i=0; i<neje; i++)
            ej_cubiertos[i] = false;
          
          
      }


     /**
      * <p>
      * Evaluates non-evaluated individuals
      * </p>
      * @param AG                   Genetic algorithm
      * @param Variables            Variables structure
      * @param Examples             Examples structure
      * @return                     Number of evaluations performed
      */
      public int evalPop (Genetic AG, TableVar Variables, TableDat Examples) {

          int trials = 0;

          for (int i=0; i<AG.getLengthPopulation(); i++) {
              if (!getIndivEvaluated(i)) {     // Not evaluated
                  indivi[i].evalInd (AG, Variables, Examples);
                  setIndivEvaluated(i,true);   /* Now it is evaluated */
                  indivi[i].setNEval(AG.getTrials()+trials);
                  trials++;
              }
              for (int j=0; j<Examples.getNEx(); j++){
                  if(indivi[i].getIndivCovered(j)==true){
                      ej_cubiertos[j]=true;
                  }
              }
          }
          //Update examples covered in the population
                  
          return trials;
      }

      
      /**
       * <p>
       * Returns the indicated individual of the population
       * </p>
       * @param pos             Position of the individual
       * @return                Individual
       */
      public Individual getIndiv (int pos) {
          return indivi[pos];
      }
      
      /**
       * <p>
       * Return the number of individuals of the population
       * </p>
       * @return                Number of individuals of the population
       */
      public int getNumIndiv(){
        return num_indiv;
      }

      /**
       * <p>
       * Sets the number of individuals of the population
       * </p>
       */
      public void setNumIndiv(int nindiv){
        num_indiv = nindiv;
      }

      
      /**
       * <p>
       * Copy the population
       * </p>
       * @param poblacion      Population to copy
       * @param neje           Number of examples
       */
      public void CopyPopulation (Population poblacion, int neje){
          
          //private Individual indivi [];     // Population individuals
          //private int num_indiv;           // Max number of individuals in the population
          //public boolean ej_cubiertos[];   // Covered examples of the population
          
          this.setNumIndiv(poblacion.getNumIndiv());
          for(int i=0; i<neje; i++){
              this.ej_cubiertos[i] = poblacion.ej_cubiertos[i];
          }
          for(int i=0; i<getNumIndiv(); i++){
              indivi[i].copyIndiv(poblacion.getIndiv(i), neje);
          }
          
      }
      
      /**
       * <p>
       * Copy the individual in the Individual otro
       * </p>
       * @param pos             Position of the individual to copy
       * @param neje            Number of examples
       * @param a               Individual to copy
       */
      public void CopyIndiv (int pos, int neje, Individual a) {
          indivi[pos].copyIndiv(a, neje);
      }


      /**
       * <p>
       * Returns the indicated gene of the Chromosome
       * </p>
       * @param num_indiv               Position of the individual
       * @param pos                     Position of the variable
       * @param elem                    Position of the gene of the variable
       * @param RulesRep                Rules representation
       * @return                        Gene of the chromosome
       */
      public int getCromElem (int num_indiv, int pos, int elem, String RulRep) {

          if(RulRep.compareTo("CAN")==0){
               return indivi[num_indiv].getCromElem(pos);
          } else {
               if(indivi[num_indiv].getCromGeneElem(pos, elem)==true)
                   return 1;
               else return 0;
          }
          
      }


      /**
       * <p>
       * Sets the value of the indicated gene of the Chromosome
       * </p>
       * @param num_indiv               Position of the individual
       * @param pos                     Position of the variable
       * @param elem                    Position of the gene of the variable
       * @param val                     Value for the gene
       * @param RulesRep                Rules representation
       */
      public void setCromElem (int num_indiv, int pos, int elem, int val, String RulRep) {

          if(RulRep.compareTo("CAN")==0){
               indivi[num_indiv].setCromElem(pos, val);
          } else {
              if(val==0)
                indivi[num_indiv].setCromGeneElem(pos, elem, false);
              else indivi[num_indiv].setCromGeneElem(pos, elem, true);
          }
          
      }

      
      /**
       * <p>
       * Returns if the individual of the population has been evaluated
       * </p>
       * @param num_indiv               Position of the individual
       */
      public boolean getIndivEvaluated (int num_indiv) {
          return indivi[num_indiv].getIndivEvaluated ();
      }


      /**
       * <p>
       * Sets the value for de evaluated attribute of the individual
       * </p>
       * @param num_indiv           Position of the individual
       * @param val                 Value of the individual
       */
      public void setIndivEvaluated (int num_indiv, boolean val) {
          indivi[num_indiv].setIndivEvaluated (val);
      }
      

      /**
       * <p>
       * Returns de hole cromosoma of the selected individual
       * </p>
       * @param num_indiv           Position of the individual
       * @return                    Canonical chromosome
       */
      public CromCAN getIndivCromCAN (int num_indiv) {
          return indivi[num_indiv].getIndivCromCAN();
      }

      /**
       * <p>
       * Returns de hole cromosoma of the selected individual
       * </p>
       * @param num_indiv           Position of the individual
       * @return                    DNF chromosome
       */
      public CromDNF getIndivCromDNF (int num_indiv) {
          return indivi[num_indiv].getIndivCromDNF();
      }
      
      /**
       * <p>
       * Prints population individuals
       * </p>
       * @param nFile           File to write the population
       */
      public void Print(String nFile) {

          for(int i=0; i<num_indiv; i++) {
            indivi[i].Print(nFile);
          }
      }
      
}


