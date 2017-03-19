/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

public abstract class Individual {

      public int tamano;      
      public boolean evaluado;
      public boolean cubre[]; 
      
      public float cubr;
      public int n_eval;             

      public QualityMeasures medidas;

    public Individual() {

    }

    public abstract void RndInitInd(TableVar Variables, float porcVar, int neje, String nFile);


    public abstract void BsdInitInd(TableVar Variables, TableDat Examples, float porcVar, String nFile);


    /**
     * <p>
     * Returns the position i of the array cubre
     * </p>
     * @param pos               Position of example
     * @return                  Value of the example
     */
    public boolean getIndivCovered (int pos) {
        return cubre[pos];
    }
    
    /**
     * <p>
     * Returns if the individual has been evaluated
     * </p>
     * @return                  Value of the example
     */
    public boolean getIndivEvaluated () {
        return evaluado;
    }

    /**
     * <p>
     * Sets that the individual has been evaluated
     * </p>
     * @param val               Value of the state of the individual
     */
    public void setIndivEvaluated (boolean val) {
        evaluado = val;
    }

    /**
     * <p>
     * Returns the number of evaluation when the individual was created
     * </p>
     * @return                  Number of evalution when the individual was created
     */
    public int getNEval (){
        return n_eval;
    }
    
    /**
     * <p>
     * Sets the number of evaluation when the individual was created
     * </p>
     * @param eval              Number of evaluation when the individual was created
     */
    public void setNEval (int eval){
        n_eval = eval;
    }

    /**
     * <p>
     * Return the quality measure of the individual
     * </p>
     * @return                  Quality measures of the individual
     */
    public QualityMeasures getMeasures(){
        return medidas;
    }

    public abstract int getCromElem(int pos);
    public abstract void setCromElem (int pos, int val);

    public abstract boolean getCromGeneElem(int pos, int elem);
    public abstract void setCromGeneElem(int pos, int elem, boolean val);

    public abstract CromCAN getIndivCromCAN();
    public abstract CromDNF getIndivCromDNF();

    public abstract void copyIndiv (Individual indi, int neje);

    public abstract void evalInd (Genetic AG, TableVar Variables, TableDat Examples);
    
    public abstract int NumInterv (float valor, int num_var, TableVar Variables);

    public abstract float getClassIndividual (TableVar Variables, TableDat Examples, int eje);
    
    public abstract void Print(String nFile);

    
}
