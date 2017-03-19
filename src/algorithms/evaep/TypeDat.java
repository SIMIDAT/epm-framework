/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

public class TypeDat {
    /**
     * <p>
     * Class defined to store the data characteristics
     * </p>
     */

    private float[] ejemplo;    // Example values for all of the variables
    private int clase;          // Class of the example for the target var
    private boolean cubierto;   // false if not covered by any rule; true otherwise

    /**
     * <p>
     * Creates a new instance of TypeDat
     * </p>
     */
    public TypeDat() {
    }


    /**
     * <p>
     * Initialise the structure for the examples
     * </p>
     * @param tama      Number of variables for an example
     */
    public void initDat (int tama) {
        ejemplo = new float[tama];
    }

    /**
     * <p>
     * Initialise a variable of an example
     * </p>
     * @param pos       Position of the variable
     * @param value     Value to initialise
     */
    public void setDat (int pos, float value) {
        ejemplo[pos] = value;
    }

    /**
     * <p>
     * Gets the value of a variable
     * </p>
     * @param pos       Position of the variable
     * @return          The float value of the variable
     */
    public float getDat (int pos) {
        return ejemplo[pos];
    }


    /**
     * <p>
     * Gets the class
     * </p>
     * @return      The value of the position of the class
     */
    public int getClas () {
        return clase;
    }

    /**
     * <p>
     * Sets the value of a class
     * </p>
     * @param val       Value of the position of the class
     */
    public void setClas (int val) {
        clase = val;
    }

    /**
     * <p>
     * Gets if the example is covered
     * </p>
     * @return      Value true if the example is covered
     */
    public boolean getCovered () {
        return cubierto;
    }

    /**
     * <p>
     * Sets the state of the example
     * </p>
     * @param val   Value correspondent to the state of the example
     */
    public void setCovered (boolean val) {
        cubierto = val;
    }

}
