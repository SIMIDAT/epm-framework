/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.sjep_classifier;

import java.util.*;
import javafx.util.Pair;
import keel.Dataset.Attribute;
import keel.Dataset.Attributes;
import keel.Dataset.InstanceSet;

public class Utils {
    /**
     * <p>
     * Assorted methods to manage several topics
     * </p>
     */


    /**
     * <p>
     * Gets an integer from param file, skiping "="
     * </p>
     * @param s     Token
     * @return      Integer value of the token
     */
    public static int getParamInt (StringTokenizer s) {
        String val = s.nextToken(); // skip "="
        val = s.nextToken();
        return Integer.parseInt(val);
    }

    /**
     * <p>
     * Gets an float from param file, skiping "="
     * </p>
     * @param s     Token
     * @return      Float value of the token
     */
    public static float getParamFloat (StringTokenizer s) {
        String val = s.nextToken(); // skip "="
        val = s.nextToken();
        return Float.parseFloat(val);
    }

    /**
     * <p>
     * Gets an String from param file, skiping "="
     * </p>
     * @param s     Token
     * @return      String value of the token
     */
    public static String getParamString(StringTokenizer s) {
        String contenido = "";
        String val = s.nextToken(); // skip "="
        do {
            if (!s.hasMoreTokens()) break;
            contenido += s.nextToken() + " ";
        } while(true);
        contenido = contenido.trim();
        return contenido;
    }

    /**
     * <p>
     * Gets the name for the file, eliminating "" and skiping "="
     * </p>
     * @param s     Token
     * @return      The name of the file
     */
    public static String getFileName(StringTokenizer s) {
        String val = s.nextToken(); // skip "="
        val = s.nextToken();
        val = val.replace('"',' ').trim();
        return val;  // Only takes first name, second is ignored
    }


    /**
     * <p>
     * Returns the position of the element at the vector, -1 if does not appear
     * </p>
     * @param vect_valores  Vector of values
     * @param valor         Value to seek
     * @return              Position of the value searched
     */
    public static int getposString (Vector vect_valores, String valor ) {
        for (int i=0;  i<vect_valores.size(); i++)
            if (vect_valores.elementAt(i).equals(valor))
                return (i);
        return (-1);
    }


    /**
     * <p>
     * Returns the minimum of two float values
     * </p>
     * @param x         A float
     * @param y         A float
     * @return          The minimum float in the comparison
     */
    public static float Minimum (float x, float y) {
        if (x<y)
            return (x);
        else
            return (y);
    }

    /**
     * <p>
     * Returns the maximum of two float values
     * </p>
     * @param x         A float
     * @param y         A float
     * @return          The maximum float in the comparison
     */
    public static float Maximum (float x, float y) {
        if (x>y)
            return (x);
        else
            return (y);
    }


    /**
     * <p>
     * Returns if the first float argument is better than the second
     * </p>
     * @param X     Some float
     * @param Y     Some float
     * @return      True if X is better than Y and false in other way
     */
    public static boolean BETTER (float X, float Y) {
        if (X > Y) return true;
        else return false;
    }

    /**
     * <p>
     * Returns if the first integer argument is better than the second
     * </p>
     * @param X     Some integer
     * @param Y     Some integer
     * @return      True if X is better than Y and false in other way
     */
    public static boolean BETTER (int X, int Y) {
        if (X > Y) return true;
        else return false;
    }

    /**
     * <p>
     * Returns if the first double argument is better than the second
     * </p>
     * @param X     Some double
     * @param Y     Some double
     * @return      True if X is better than Y and false in other way
     */
    public static boolean BETTER (double X, double Y) {
        if (X > Y) return true;
        else return false;
    }


    /**
     * <p>
     * C.A.R, Hoare Quick sort. Based on sort by interchange. Decreasing sort.
     * </p>
     * @param v             Vector to be sorted
     * @param izqinitial    Position to sort
     * @param right           Final position to sort
     * @param index        The indexes of the original vector
     */
    public static void OrDecIndex (double v[], int left, int right, int index[])  {
        int i,j,aux;
        double x,y;

        i = left;
        j = right;
        x = v[(left+right)/2];
        do {
            while (v[i]>x && i<right)
                i++;
            while (x>v[j] && j>left)
                j--;
            if (i<=j) {
                y = v[i];
                v[i] = v[j];
                v[j] = y;
                aux = index[i];
                index[i] = index[j];
                index[j] = aux;
                i++;
                j--;
            }
        } while(i<=j);
        if (left<j)
            OrDecIndex (v,left,j,index);
        if (i<right)
            OrDecIndex (v,i,right,index);

    }


    /**
     * <p>
     * C.A.R, Hoare Quick sort. Based on sort by interchange. Incresing sort.
     * </p>
     * @param v		Vector to be sorted
     * @param left	Initial position to sort
     * @param right	Final position to sort
     * @param index	The indexes of the original vector
     */
    public static void OrCrecIndex (double v[], int left, int right, int index[])  {
        int i,j,aux;
        double x,y;

        i = left;
        j = right;
        x = v[(left+right)/2];
        do {
            while (v[i]<x && i<right)
                i++;
            while (x<v[j] && j>left)
                j--;
            if (i<=j) {
                y = v[i];
                v[i] = v[j];
                v[j] = y;
                aux = index[i];
                index[i] = index[j];
                index[j] = aux;
                i++;
                j--;
            }
        } while(i<=j);
        if (left<j)
            OrCrecIndex (v,left,j,index);
        if (i<right)
            OrCrecIndex (v,i,right,index);
    }


    /**
     * <p>
     * Rounds the generated value for the semantics when necesary
     * </p>
     * @param val       The value to round
     * @param tope
     */
    public static float Assigned (float val, float tope) {
        if (val>-0.0001 && val<0.0001)
            return (0);
        if (val>tope-0.0001 && val<tope+0.0001)
            return (tope);
        return (val);
    }

       /**
     * Gets simple itemsets with a support higher than a threshold
     *
     * @param a
     * @param minSupp
     * @param positiveClass - The class to consider as positive. For multiclass
     * problems, the others classes are considered as negative.
     * @return
     */
    public static ArrayList<Item> getSimpleItems(InstanceSet a, double minSupp, int positiveClass) {
        // Reads the KEEL instance set.

        int countD1 = 0;   // counts of examples belonging to class D1 and D2.
        int countD2 = 0;
        ArrayList<Item> simpleItems = new ArrayList<>();
        // get classes
        ArrayList<String> classes;
        try {
            classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());
        } catch (NullPointerException ex) {
            classes = new ArrayList<>(a.getAttributeDefinitions().getOutputAttribute(0).getNominalValuesList());
        }
        // Gets the count of examples for each class to calculate the growth rate.
        for (int i = 0; i < a.getNumInstances(); i++) {
            if (a.getInstance(i).getOutputNominalValuesInt(0) == positiveClass) {
                countD1++;
            } else {
                countD2++;
            }
        }

        // Get the attributes
        Attribute[] attributes = Attributes.getInputAttributes();
        int countId = 0;
        // for each attribute
        for (int i = 0; i < attributes.length; i++) {
            // get nominal values of the attribute
            ArrayList<String> nominalValues = new ArrayList<>(attributes[i].getNominalValuesList());
            //for each nominal value
            for (String value : nominalValues) {
                int countValueInD1 = 0;
                int countValueInD2 = 0;
                // counts the times the value appear for each class
                for (int j = 0; j < a.getNumInstances(); j++) {
                    String p = a.getInputNominalValue(j, i);
                    if (value.equals(p)) {
                        // If are equals, check the class and increment counters
                        if (a.getInstance(j).getOutputNominalValuesInt(0) == positiveClass) {
                            countValueInD1++;
                        } else {
                            countValueInD2++;
                        }
                    }
                }
                double suppD1 = (double) countValueInD1 / (double) countD1;
                double suppD2 = (double) countValueInD2 / (double) countD2;
                // now calculate the growth rate of the item.
                double gr;
                if (suppD1 < minSupp && suppD2 < minSupp) {
                    gr = 0;
                } else if ((suppD1 == 0 && suppD2 >= minSupp) || (suppD1 >= minSupp && suppD2 == 0)) {
                    gr = Double.POSITIVE_INFINITY;
                } else {
                    gr = Math.max(suppD2 / suppD1, suppD1 / suppD2);
                }

                // Add the item to the list of simple items
                Item it = new Item(countId, value, attributes[i].getName(), gr);
                it.setD1count(countValueInD1);
                it.setD2count(countValueInD2);
                simpleItems.add(it);
                countId++;

            }
        }

        return simpleItems;
    }

    /**
     * Gets the instances of a dataset as set of Item class
     *
     * @param a
     * @param simpleItems
     * @return
     */
    public static ArrayList<Pair<ArrayList<Item>, Integer>> getInstances(InstanceSet a, ArrayList<Item> simpleItems, int positiveClass) {
        String[] att_names = new String[Attributes.getInputAttributes().length];
        ArrayList<Pair<ArrayList<Item>, Integer>> result = new ArrayList<>();
        ArrayList<String> classes = new ArrayList<>(Attributes.getOutputAttribute(0).getNominalValuesList());

        for (int i = 0; i < att_names.length; i++) {
            att_names[i] = Attributes.getAttribute(i).getName();
        }

        for (int i = 0; i < a.getNumInstances(); i++) {
            ArrayList<Item> list = new ArrayList<>();
            for (int j = 0; j < Attributes.getInputNumAttributes(); j++) {
                // Add the item into the pattern
                Item it = Item.find(simpleItems, att_names[j], a.getInputNominalValue(i, j));
                if (it != null) {
                    list.add(it);
                }
            }
            // Add into the set of instances, the second element is the class
            int clas = 0;
            if (a.getInstance(i).getOutputNominalValuesInt(0) != positiveClass) {
                clas = 1;
            }
            result.add(new Pair(list, clas));
        }

        return result;
    }

}
