/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */

package algorithms.evaep;

import org.core.*;

public class QualityMeasures {
    /**
     * <p>
     * Defines the quality measures of the individual
     * </p>
     */
    
    private double leng;
    private double unus;
    private double gain;
    private double sens;
    private double supM;
    private double supm;
    private double difs;
    private double conf;
    private double grat;
    private double nsup;    //Support based on examples pending to cover
    private double medgeo;
    private double fitness;
    
    


    /**
     * <p>
     * Creates a new instance of QualityMeasures
     * </p>
     */
    public QualityMeasures() {

    }


    /**
     * <p>
     * Gets the value of the confidence
     * </p>
     * @return                  Value of the confidence
     */
    public double getConf (){
        return conf;
    }

    /**
     * <p>
     * Sets the value of the confidence
     * </p>
     * @param aconf              Value of the confidence
     */
    public void setConf (double aconf){
        conf = aconf;
    }


    /**
     * <p>
     * Gets the value of the sensitivity
     * </p>
     * @return                  Value of the sensitivity
     */
    public double getSens (){
        return sens;
    }

    /**
     * <p>
     * Sets the value of the confidence
     * </p>
     * @param asens              Value of the sensitivity
     */
    public void setSens (double asens){
        sens = asens;
    }


    /**
     * <p>
     * Gets the value of the new support
     * </p>
     * @return                  Value of the new support
     */
    public double getNSup (){
        return nsup;
    }

    /**
     * <p>
     * Sets the value of the new support
     * </p>
     * @param asupp              Value of the new support
     */
    public void setNSup (double asupp){
        nsup = asupp;
    }
    
    
    /**
     * <p>
     * Gets the value of the support for majority class
     * </p>
     * @return                  Value of the support for majority class
     */
    public double getSupM (){
        return supM;
    }

    /**
     * <p>
     * Sets the value of the support for majority class
     * </p>
     * @param asupp              Value of the support for majority class
     */
    public void setSupM (double asupp){
        supM = asupp;
    }
    
    
    /**
     * <p>
     * Gets the value of the support for minority class
     * </p>
     * @return                  Value of the support for minority class
     */
    public double getSupm (){
        return supm;
    }

    /**
     * <p>
     * Sets the value of the support for minority class
     * </p>
     * @param asupp              Value of the support for minority class
     */
    public void setSupm (double asupp){
        supm = asupp;
    }


    /**
     * <p>
     * Gets the value of the unusualness
     * </p>
     * @return                  Value of the unusualness
     */
    public double getUnus (){
        return unus;
    }

    /**
     * <p>
     * Sets the value of the unusualness
     * </p>
     * @param aunus              Value of the unusualness
     */
    public void setUnus (double aunus){
        unus = aunus;
    }

    /**
     * <p>
     * Gets the value of the different support 
     * </p>
     * @return                  Value of the difference support
     */
    public double getDifS (){
        return difs;
    }

    /**
     * <p>
     * Sets the value of the difference support 
     * </p>
     * @param asupp              Value of the support for majority class
     */
    public void setDifS (double asupp){
        difs = asupp;
    }
    
    /**
     * <p>
     * Gets the value of the growth rate
     * </p>
     * @return                  Value of the growth rate
     */
    public double getGRat (){
        return grat;
    }

    /**
     * <p>
     * Sets the value of the growth rate
     * </p>
     * @param grate              Value of the growth rate
     */
    public void setGRat (double grate){
        grat = grate;
    }

    
    /**
     * <p>
     * Gets the value of the med geo
     * </p>
     * @return                  Value of the med geo
     */
    public double getMedGeo (){
        return medgeo;
    }

    /**
     * <p>
     * Sets the value of the med geo
     * </p>
     * @param amedgeo              Value of the med geo
     */
    public void setMedGeo (double amedgeo){
        medgeo = amedgeo;
    }

    /**
     * <p>
     * Gets the value of the gain
     * </p>
     * @return                  Value of the gain
     */
    public double getGain (){
        return gain;
    }

    /**
     * <p>
     * Sets the value of the gain
     * </p>
     * @param again              Value of the gain
     */
    public void setGain (double again){
        gain = again;
    }
    
    
    /**
     * <p>
     * Gets the value of the length
     * </p>
     * @return                  Value of the length
     */
    public double getLength(){
        return leng;
    }

    /**
     * <p>
     * Sets the value of the lenght
     * </p>
     * @param alen              Value of the length
     */
    public void setLength (double alen){
        leng = alen;
    }

    
    /**
     * <p>
     * Gets the value of the fitness
     * </p>
     * @return                  Value of the fitness
     */
    public double getFitness (){
        return fitness;
    }

    /**
     * <p>
     * Sets the value of the fitness
     * </p>
     * @param afitness              Value of the fitness
     */
    public void setFitness (double afitness){
        fitness = afitness;
    }

    /**
     * <p>
     * Copy in this object the values of qmeasures
     * </p>
     * @param qmeasures           Quality measures
     */
    public void Copy (QualityMeasures qmeasures) {
        this.setLength(qmeasures.getLength());
        this.setUnus(qmeasures.getUnus());
        this.setGain(qmeasures.getGain());
        this.setNSup(qmeasures.getNSup());
        this.setSens(qmeasures.getSens());
        this.setSupM(qmeasures.getSupM());
        this.setSupm(qmeasures.getSupm());
        this.setDifS(qmeasures.getDifS());
        this.setConf(qmeasures.getConf());
        this.setGRat(qmeasures.getGRat());
        this.setMedGeo(qmeasures.getMedGeo());
        this.setFitness(qmeasures.getFitness());
    }


    /**
     * <p>
     * Prints the measures
     * </p>
     * @param nFile             File to write the quality measures
     * @param AG                Genetic algorithm
     */
    public void Print(String nFile, Genetic AG) {
        String contents;
        contents = "\tLength: "+this.getLength()+"\n"
                + "\tUnus: "+this.getUnus()+"\n"
                + "\tGain: "+this.getGain()+"\n"
                + "\tNSup: "+this.getNSup()+"\n"
                + "\tSupM: "+this.getSupM()+"\n"
                + "\tSupm: "+this.getSupm()+"\n"
                + "\tSens: "+this.getSens()+"\n"
                + "\tMedGeo: "+this.getMedGeo()+"\n"
                + "\tConf: "+this.getConf();       
        contents += "\n";
        if (nFile=="")
            System.out.print (contents);
        else
           File.AddtoFile(nFile, contents);
    }



}
