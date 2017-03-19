/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

import keel.Dataset.*;


public class TableDat {
    /**
     * <p>
     * Class defined to store the information of the complete dataset
     * </p>
     */

    private int n_eje;		    // Number of examples in dataset
    private TypeDat[] dat;          // Dataset instances
    private int[] EjClase;          // Number of examples of each class
    private int ej_clase_obj;	    // Number of examples of the target class
    private int total_ej_cubiertos;         // Total covered examples
    private int total_ej_cubiertos_clase;   // Total covered examples for class

    /**
     * <p>
     * Returns the number of examples of the DataSet
     * </p>
     * @return      Number of examples
     **/
    public int getNEx () {
        return n_eje;
    }


    /**
     * <p>
     * Returns the number of examples belonging to the class specified
     * </p>
     * @param clas         A value of the target variable
     * @return              Number of examples of the class
     **/
    public int getExamplesClass (int clas) {
        return EjClase[clas];
    }


    /**
     * <p>
     * Stores and gets in "ej_clase_obj" the number of examples of the target class
     * </p>
     * @param clas         A value of the target variable
     * @return              Number of examples of the class
     **/
    public int setExamplesClassObj (int clas) {
        ej_clase_obj = EjClase[clas];
        return ej_clase_obj;
    }

    /**
     * <p>
     * Returns the number of examples of the target class
     * </p>
     * @return              Number of examples of the objective class
     **/
    public int getExamplesClassObj () {
        return ej_clase_obj;
    }

    /**
     * <p>
     * Sets the value of the gen of an example as an lost value
     * lost = max value of the variable + 1
     * </p>
     * @param Variables         Structure of the variables for the dataset
     * @param example           Position of the examples
     * @param pos               Position of the variable
     **/
    public void setLost (TableVar Variables, int example, int pos) {
        dat[example].setDat(pos, Variables.getMax(pos)+1);
    }


    /**
     * <p>
     * Returns if the value of the gen of an example is a lost value or not
     * lost = max value of the variable + 1
     * </p>
     * @param Variables         Structure of the variables for the dataset
     * @param example           Position of the examples
     * @param pos               Position of the variable
     * @return                  If is a lost value
     **/
    public boolean getLost (TableVar Variables, int example, int pos) {
        if (dat[example].getDat(pos) ==  Variables.getMax(pos)+1)
            return true;
        else
            return false;
    }


    /**
     * <p>
     * Creates and fill TableDat with the examples of the dataset
     * </p>
     * @param Data          Data structure of the dataset
     * @param Variables     Variables structure of the dataset
     */
    public void Load(InstanceSet Data, TableVar Variables) {
        int num_vars = Variables.getNVars();       // Set the number of variables of the dataset
        n_eje = Data.getNumInstances();            // Set Sthe number of examples (instances) of the dataset
        EjClase = new int[Variables.getNClass()];  // Creates space to store the number of examples of each class
        dat = new TypeDat[n_eje];                  // Creates the structure to store the data

        // Initializes to 0 the number of examples of each class
        for (int i=0; i<Variables.getNClass(); i++)
            EjClase[i]=0;

         // For each example of the dataset
         for (int i=0; i<n_eje; i++) {
            dat[i] = new TypeDat();       // Creates space for the example
            dat[i].initDat(num_vars);     // Creates space for each value of the example
            dat[i].setCovered (false);    // Set example to not covered

	    // Stores de values for all the input variables
	    Instance inst = Data.getInstance(i);
	    double instValues[] = new double[num_vars];
            instValues = inst.getAllInputValues();
            // Gets all the input attributes of the instance, converting enumerated to consecutive integers

            for (int j=0; j<num_vars; j++) {
                if (inst.getInputMissingValues(j))
                    // If the value is a lost one, sets as max value + 1
                    setLost (Variables, i, j);
                else
                    // Set the value. Automatic translation from enum to integer for nominal values
                    dat[i].setDat (j, (float)instValues[j]);
            }

            // Set the value for the target variable of the example
            double classValue[] = new double[1];
            classValue = inst.getAllOutputValues();
            dat[i].setClas((int)classValue[0]);

            // Increments the number of examples of the class
            EjClase[(int)classValue[0]]++;
        }

        for (int i=0; i<Variables.getNClass(); i++)
            System.out.println ("Class " + i + ": " + EjClase[i] + " examples");
        System.out.println ("Total examples: " + n_eje);

    }


    /**
     * <p>
     * Returns the class of the example in position pos
     * </p>
     * @param pos       Position of the example
     */
    public int getClass (int pos) {
        return dat[pos].getClas();
    }

    /**
     * <p>
     * Sets the class of the example in position pos
     * </p>
     * @param pos       Position of the example
     * @param val       Value of the class
     */
    public void setClass (int pos, int val) {
        dat[pos].setClas(val);
    }


    /**
     * <p>
     * Returns if the example in position pos is yet covered or not
     * </p>
     * @param pos       Position of the example
     * @return          State of the example
     */
    public boolean getCovered (int pos) {
        return dat[pos].getCovered();
    }

    /**
     * <p>
     * Sets to covered or not the the example in position pos
     * </p>
     * @param pos       Position of the example
     * @param val       Value of the state of the example
     */
    public void setCovered (int pos, boolean val) {
        dat[pos].setCovered(val);
    }


    /**
     * <p>
     * Returns the number of examples covered
     * </p>
     * @return          Number of examples covered
     */
    public int getExamplesCovered ( ) {
        return total_ej_cubiertos;
    }

    /**
     * <p>
     * Sets the number of examples covered
     * </p>
     * @param val       Number of examples covered
     */
    public void setExamplesCovered (int val) {
        total_ej_cubiertos = val;
    }


    /**
     * <p>
     * Returns the number of examples covered for class
     * </p>
     * @return          Number of examples covered for class
     */
    public int getExamplesCoveredClass ( ) {
        return total_ej_cubiertos_clase;
    }

    /**
     * <p>
     * Sets the number of examples covered for class
     * </p>
     * @param val       Number of examples covered for class
     */
    public void setExamplesCoveredClass (int val) {
        total_ej_cubiertos_clase = val;
    }


    /**
     * <p>
     * Get the value of the variable "pos" of the example "numEj"
     * </p>
     * @param numEj     Position of the example
     * @param pos       Position of the variable
     */
    public float getDat (int numEj, int pos) {
        return dat[numEj].getDat(pos);
    }


    /**
     * <p>
     * Creates a new instance of TableDat
     * </p>
     */
    public TableDat() {
    }

}
