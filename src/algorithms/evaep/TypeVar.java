/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

import java.util.Vector;

public class TypeVar {
    /**
     * <p>
     * Class defined to store the attributes characteristics
     * </p>
     */

    private String nombre;        // Name of the variable stored in the dataset
    private char tipoDato;        // 'i': integer, 'r':real, 'e':enumerated
    private boolean continua;     // true: continuous, false: discrete
    private Vector valores;       // type "i" or "r": range of real values
                                  // type "e": list of valid values
    private int n_etiq;           // Number of labels (continuous vars) or
                                  //    values (discrete vars)
    private float min, max;       // Values for the min and max valid values.

    /**
     * <p>
     * Creates a new instance of TypeVar
     * </p>
     */
    public TypeVar() {
    }

    public String getValue(int pos){
        return valores.get(pos).toString();
    }

    /**
     * <p>
     * Initialize private "valores" with the values of the vector
     * </p>
     * @param data     Vector with the type of variables used
     */
    public void initValues (Vector data) {
        valores = new Vector (data);
    }

    /**
     * <p>
     * Gets the name of the variable
     * </p>
     * @return      Name of the variable
     */
    public String getName () {
        return nombre;
    }

    /**
     * <p>
     * Sets the name of the variable
     * </p>
     * @param val       Name of the variable
     */
    public void setName (String val) {
        nombre = val;
    }


    /**
     * <p>
     * Gets the char with the type used in the variable
     * </p>
     * @return      The char with the type used in the variable: 'i' 'r' 'e'
     */
    public char getType () {
        return tipoDato;
    }

    /**
     * <p>
     * Sets the char with the type used in the variable
     * </p>
     * @param val   The char with the type used in the variable: 'i' 'r' 'e'
     */
    public void setType (char val) {
        tipoDato = val;
    }


    /**
     * <p>
     * Gets if the variable is continuous or discrete
     * </p>
     * @return      True for continuous variable and false for discrete
     */
    public boolean getContinuous () {
        return continua;
    }

    /**
     * <p>
     * Sets the type of the variable
     * </p>
     * @param val   Value true if variable is continuos and false otherwise
     */
    public void setContinuous (boolean val) {
        continua = val;
    }

    /**
     * <p>
     * Gets the number of labels used by the continuous variable
     * </p>
     * @return      Number of labels in continuous variable
     */
    public int getNLabels () {
        return n_etiq;
    }

    /**
     * <p>
     * Sets the number of labels used by the continuous variable
     * </p>
     * @param val   Number of labels in continuous variable
     */
    public void setNLabels (int val) {
        n_etiq = val;
    }

    /**
     * <p>
     * Gets the minimum value for the variable
     * </p>
     * @return      Minimum value for the variable
     */
    public float getMin () {
        return min;
    }

    /**
     * <p>
     * Sets the minimum value for the variable
     * </p>
     * @param val      Minimum value for the variable
     */
    public void setMin (float val) {
        min = val;
    }

    /**
     * <p>
     * Gets the maximum value for the variable
     * </p>
     * @return      Maximum value for the variable
     */
    public float getMax () {
        return max;
    }

    /**
     * <p>
     * Sets the maximum value for the variable
     * </p>
     * @param val    Minimum value for the variable
     */
    public void setMax (float val) {
        max = val;
    }


}

