/**
 * <p>
 * @author Written by Cristobal J. Carmona (University of Jaen) 11/08/2008
 * @version 1.0
 * @since JDK1.5
 * </p>
 */
package algorithms.evaep;

import org.core.*;
import java.util.*;

public class Genetic {

    /**
     * <p>
     * Methods to define the genetic algorithm and to apply operators and
     * reproduction schema
     * </p>
     */

    private Population poblac;     // Main Population
    private Population auxiliar;   // Auxiliar population

    private int long_poblacion;   // Number of individuals of the population
    private int n_eval;           // Number of evaluations per ejecution
    private float prob_cruce;     // Cross probability
    private float prob_mutacion;  // Mutation probability
    private int Gen;		  // Number of generations performed by the GA
    private int Trials;		  // Number of evaluated chromosomes

    private int best_guy;         // Position of the best buy
    private String fitness;       // Name of the fitness function

    private String RulesRep = "CAN";
    private String Initialisation;      // Type of initialisation random or biased
    private boolean unballanced = true;
    private boolean roundrobin = false;

    /**
     * <p>
     * Sets the lenght of the population
     * </p>
     *
     * @param value Lenght of the population
     */
    public void setLengthPopulation(int value) {
        long_poblacion = value;
    }

    /**
     * <p>
     * Gets the lenght of the population
     * </p>
     *
     * @return Lenght of the population
     */
    public int getLengthPopulation() {
        return long_poblacion;
    }

    /**
     * <p>
     * Sets the number of evaluations of the algorithm
     * </p>
     *
     * @param value Number of evaluations
     */
    public void setNEval(int value) {
        n_eval = value;
    }

    /**
     * <p>
     * Gets the number of evalutions of the algorithms
     * </p>
     *
     * @return Number of evaluations
     */
    public int getNEval() {
        return n_eval;
    }

    /**
     * <p>
     * Sets the position of the best guy of population
     * </p>
     *
     * @param value Position
     */
    public void setBestGuy(int value) {
        best_guy = value;
    }

    /**
     * <p>
     * Gets the position of the best guy in population
     * </p>
     *
     * @return Number of evaluations
     */
    public int getBestGuy() {
        return best_guy;
    }

    /**
     * <p>
     * Sets the cross probability in the algorithm
     * </p>
     *
     * @param value Cross probability
     */
    public void setProbCross(float value) {
        prob_cruce = value;
    }

    /**
     * <p>
     * Gets the cross probability
     * </p>
     *
     * @return Cross probability
     */
    public float getProbCross() {
        return prob_cruce;
    }

    /**
     * <p>
     * Sets the mutation probability
     * </p>
     *
     * @param value Mutation probability
     */
    public void setProbMutation(float value) {
        prob_mutacion = value;
    }

    /**
     * <p>
     * Gets the mutation probability
     * </p>
     *
     * @return Mutation probability
     */
    public float getProbMutation() {
        return prob_mutacion;
    }

    /**
     * <p>
     * Sets the value of a gene
     * </p>
     *
     * @param value Value of the gene
     */
    public void setGen(int value) {
        Gen = value;
    }

    /**
     * <p>
     * Gets the value of a gene
     * </p>
     *
     * @return Value of the gene
     */
    public int getGen() {
        return Gen;
    }

    /**
     * <p>
     * Sets the number of trials in the algorithm
     * </p>
     *
     * @param value Number of trials
     */
    public void setTrials(int value) {
        Trials = value;
    }

    /**
     * <p>
     * Gets the number of trials in the algorithm
     * </p>
     *
     * @return Number of trials
     */
    public int getTrials() {
        return Trials;
    }

    /**
     * <p>
     * Gets the name of fitness function
     * </p>
     *
     * @return
     */
    public String getFitness() {
        return fitness;
    }

    /**
     * <p>
     * Sets the value of the fitness function
     * </p>
     *
     * @param value
     */
    public void setFitness(String value) {
        fitness = value;
    }

    /**
     * <p>
     * Gets the name of round robin
     * </p>
     *
     * @return
     */
    public boolean getRoundRobin() {
        return roundrobin;
    }

    /**
     * <p>
     * Sets the value of round robin
     * </p>
     *
     * @param value
     */
    public void setRoundRobin(boolean value) {
        roundrobin = value;
    }

    /**
     * <p>
     * Gets if the algorithm uses re-initialisation based on coverage
     * </p>
     *
     * @return The uses of re-initialisation based on coverage
     */
    public String getInitialisation() {
        return Initialisation;
    }

    /**
     * <p>
     * Sets the value of re-initialisation based on coverage
     * </p>
     *
     * @param value Value of the re-inisitalisation based on coverage
     */
    public void setInitialisation(String value) {
        Initialisation = value;
    }

    /**
     * <p>
     * Gets the rules representation of the algorithm
     * </p>
     *
     * @return Representation of the rules
     */
    public String getRulesRep() {
        return RulesRep;
    }

    /**
     * <p>
     * Sets the rules representation of the algorithm
     * </p>
     *
     * @param value Representation of the rule
     */
    public void setRulesRep(String value) {
        RulesRep = value;
    }

    /**
     * <p>
     * Applies the selection schema of the genetic algorithm. k-Tournament
     * selection
     * </p>
     *
     * @param k Number of tournament selection performed
     * @return Position of the individual selected
     */
    public int Select() {

        int winner = 0;

        int opponent1 = Randomize.Randint(0, long_poblacion - 1);
        int opponent2 = opponent1;
        while (opponent2 == opponent1) {
            opponent2 = Randomize.Randint(0, long_poblacion - 1);
        }

        if ((poblac.getIndiv(opponent1).getMeasures().getNSup() * 0.5
                + poblac.getIndiv(opponent1).getMeasures().getFitness() * 0.5)
                > (poblac.getIndiv(opponent2).getMeasures().getNSup() * 0.5
                + poblac.getIndiv(opponent2).getMeasures().getFitness() * 0.5)) {
            winner = opponent1;
        } else if ((poblac.getIndiv(opponent1).getMeasures().getNSup() * 0.5
                + poblac.getIndiv(opponent1).getMeasures().getFitness() * 0.5)
                == (poblac.getIndiv(opponent2).getMeasures().getNSup() * 0.5
                + poblac.getIndiv(opponent2).getMeasures().getFitness() * 0.5)) {
            if (poblac.getIndiv(opponent1).getMeasures().getNSup() > poblac.getIndiv(opponent2).getMeasures().getNSup()) {
                winner = opponent1;
            } else {
                winner = opponent2;
            }
        } else {
            winner = opponent2;
        }

        return winner;

    }

    /**
     * <p>
     * Cross operator for the genetic algorithm
     * </p>
     *
     * @param Variables Variables structure
     * @param dad Position of the daddy
     * @param mom Position of the mummy
     * @param contador Position to insert the son
     * @param neje Number of examples
     */
    public void CrossMultipoint(TableVar Variables, int dad, int mom, int contador, int neje) {

        int i, xpoint1, xpoint2;
        double cruce;

        // Copy the individuals to cross
        for (i = 0; i < Variables.getNVars(); i++) {
            if (RulesRep.compareTo("CAN") == 0) {
                auxiliar.setCromElem((contador * 2) - 1, i, 0, poblac.getCromElem(mom, i, 0, RulesRep), RulesRep);
                auxiliar.setCromElem((contador * 2), i, 0, poblac.getCromElem(dad, i, 0, RulesRep), RulesRep);
            } else {
                int number = auxiliar.getIndivCromDNF(contador * 2).getCromGeneLenght(i);
                for (int ii = 0; ii <= number; ii++) {
                    auxiliar.setCromElem((contador * 2) - 1, i, ii, poblac.getCromElem(mom, i, ii, RulesRep), RulesRep);
                    auxiliar.setCromElem((contador * 2), i, ii, poblac.getCromElem(dad, i, ii, RulesRep), RulesRep);
                }
            }
        }

        cruce = Randomize.Randdouble(0.0, 1.0);

        if (cruce <= getProbCross()) {
            // Generation of the two point of cross
            xpoint1 = Randomize.Randint(0, (Variables.getNVars() - 1));
            if (xpoint1 != Variables.getNVars() - 1) {
                xpoint2 = Randomize.Randint((xpoint1 + 1), (Variables.getNVars() - 1));
            } else {
                xpoint2 = Variables.getNVars() - 1;
            }

            // Cross the parts between both points
            for (i = xpoint1; i <= xpoint2; i++) {
                if (RulesRep.compareTo("CAN") == 0) {
                    auxiliar.setCromElem((contador * 2) - 1, i, 0, poblac.getCromElem(dad, i, 0, RulesRep), RulesRep);
                    auxiliar.setCromElem((contador * 2), i, 0, poblac.getCromElem(mom, i, 0, RulesRep), RulesRep);
                } else {
                    int number = auxiliar.getIndivCromDNF(contador * 2).getCromGeneLenght(i);
                    for (int ii = 0; ii <= number; ii++) {
                        auxiliar.setCromElem((contador * 2) - 1, i, ii, poblac.getCromElem(dad, i, ii, RulesRep), RulesRep);
                        auxiliar.setCromElem((contador * 2), i, ii, poblac.getCromElem(mom, i, ii, RulesRep), RulesRep);
                    }
                    int aux1 = 0;
                    int aux2 = 0;
                    for (int ii = 0; ii < number; ii++) {
                        if (auxiliar.getCromElem((contador * 2) - 1, i, ii, RulesRep) == 1) {
                            aux1++;
                        }
                        if (auxiliar.getCromElem((contador * 2), i, ii, RulesRep) == 1) {
                            aux2++;
                        }
                    }
                    if ((aux1 == number) || (aux1 == 0)) {
                        auxiliar.setCromElem((contador * 2) - 1, i, number, 0, RulesRep);
                    } else {
                        auxiliar.setCromElem((contador * 2) - 1, i, number, 1, RulesRep);
                    }
                    if ((aux2 == number) || (aux2 == 0)) {
                        auxiliar.setCromElem((contador * 2), i, number, 0, RulesRep);
                    } else {
                        auxiliar.setCromElem((contador * 2), i, number, 1, RulesRep);
                    }
                }
            }
        } else {
            auxiliar.CopyIndiv((contador * 2) - 1, neje, poblac.getIndiv(dad));
            auxiliar.CopyIndiv((contador * 2), neje, poblac.getIndiv(mom));
        }

    }

    /**
     * <p>
     * Mutates an individual
     * </p>
     *
     * @param Variables Variables structure
     * @param pos Position of the individual to mutate
     */
    public void Mutation(TableVar Variables, int pos) {

        double mutar;
        int posiciones, eliminar;

        posiciones = Variables.getNVars();

        if (getProbMutation() > 0) {
            for (int i = 0; i < posiciones; i++) {
                mutar = Randomize.Randdouble(0.00, 1.00);
                if (mutar <= getProbMutation()) {
                    eliminar = Randomize.Randint(0, 10);
                    if (eliminar <= 5) {
                        if (!Variables.getContinuous(i)) {
                            if (RulesRep.compareTo("CAN") == 0) {
                                auxiliar.setCromElem(pos, i, 0, (int) Variables.getMax(i) + 1, RulesRep);
                            } else {
                                int number = Variables.getNLabelVar(i);
                                for (int l = 0; l <= number; l++) {
                                    auxiliar.setCromElem(pos, i, l, 0, RulesRep);
                                }
                            }
                        } else if (RulesRep.compareTo("CAN") == 0) {
                            auxiliar.setCromElem(pos, i, 0, Variables.getNLabelVar(i), RulesRep);
                        } else {
                            int number = Variables.getNLabelVar(i);
                            for (int l = 0; l <= number; l++) {
                                auxiliar.setCromElem(pos, i, l, 0, RulesRep);
                            }
                        }
                    } else {
                        if (!Variables.getContinuous(i)) {
                            if (RulesRep.compareTo("CAN") == 0) {
                                auxiliar.setCromElem(pos, i, 0, Randomize.Randint(0, (int) Variables.getMax(i)), RulesRep);
                            } else {
                                int number = Variables.getNLabelVar(i);
                                int cambio = Randomize.Randint(0, number - 1);
                                if (auxiliar.getCromElem(pos, i, cambio, RulesRep) == 0) {
                                    auxiliar.setCromElem(pos, i, cambio, 1, RulesRep);
                                    int aux1 = 0;
                                    for (int ii = 0; ii < number; ii++) {
                                        if (auxiliar.getCromElem(pos, i, ii, RulesRep) == 1) {
                                            aux1++;
                                        }
                                    }
                                    if ((aux1 == number) || (aux1 == 0)) {
                                        auxiliar.setCromElem(pos, i, number, 0, RulesRep);
                                    } else {
                                        auxiliar.setCromElem(pos, i, number, 1, RulesRep);
                                    }
                                } else {
                                    for (int k = 0; k <= number; k++) {
                                        auxiliar.setCromElem(pos, i, k, 0, RulesRep);
                                    }
                                }
                            }
                        } else if (RulesRep.compareTo("CAN") == 0) {
                            auxiliar.setCromElem(pos, i, 0, Randomize.Randint(0, Variables.getNLabelVar(i) - 1), RulesRep);
                        } else {
                            int number = Variables.getNLabelVar(i);
                            int cambio = Randomize.Randint(0, number - 1); 
                            if (auxiliar.getCromElem(pos, i, cambio, RulesRep) == 0) {
                                auxiliar.setCromElem(pos, i, cambio, 1, RulesRep);
                                int aux1 = 0;
                                for (int ii = 0; ii < number; ii++) {
                                    if (auxiliar.getCromElem(pos, i, ii, RulesRep) == 1) {
                                        aux1++;
                                    }
                                }
                                if ((aux1 == number) || (aux1 == 0)) {
                                    auxiliar.setCromElem(pos, i, number, 0, RulesRep);
                                } else {
                                    auxiliar.setCromElem(pos, i, number, 1, RulesRep);
                                }
                            } else {
                                for (int k = 0; k <= number; k++) {
                                    auxiliar.setCromElem(pos, i, k, 0, RulesRep);
                                }
                            }
                        }
                    }

                    // Marks the chromosome as not evaluated
                    auxiliar.setIndivEvaluated(pos, false);
                }
            }
        }

    }

    /**
     * <p>
     * Composes the genetic algorithm applying the operators
     * </p>
     *
     * @param Variables Variables structure
     * @param Examples Examples structure
     * @param nFile File to write the process
     * @return Final Pareto population
     */
    public Individual GeneticAlgorithm(TableVar Variables, TableDat Examples, String nFile) {

        String contents;
        float porcPob = (float) 0.5;  //Percentage of population: biased and random
        float porcVar = (float) 0.8;  //Maximum number of variables in the individuals
        int best_guy = 0;             //Position of the best guy
        int[] ten_best_guy;

        poblac = new Population(long_poblacion, Variables.getNVars(), Examples.getNEx(), RulesRep, Variables, Trials);
        poblac.BsdInitPob(Variables, Examples, porcVar, porcPob, Examples.getNEx(), nFile);
        System.out.println(Randomize.calls);
        Trials = 0;
        Gen = 0;

        //Evaluates the population
        Trials += poblac.evalPop(this, Variables, Examples);
        //poblac.Print("");

        do { // GA General cycle

            Gen++;
            if(Gen == 143){
                System.out.println("Eooo");
            }
            // Initialise auxiliar 
            auxiliar = new Population(long_poblacion, Variables.getNVars(), Examples.getNEx(), RulesRep, Variables, Trials);
            // Introduce the best individual of population in auxiliar

            best_guy = BestIndividual(poblac);
//            System.out.println("GEN: " + Gen);
//            poblac.getIndiv(best_guy).Print("");
            //System.out.println(best_guy);
            auxiliar.CopyIndiv(0, Examples.getNEx(), poblac.getIndiv(best_guy));

            for (int conta = 1; conta < long_poblacion / 2; conta++) {
                
                int dad = 0;
                int mum = 0;
                // Select the daddy and mummy
                dad = Select();
                mum = Select();

                while (mum == dad) {
                    mum = Select();
                }

                // Crosses
                CrossMultipoint(Variables, dad, mum, conta, Examples.getNEx());
                // Mutates
                Mutation(Variables, (conta * 2) - 1);
                Mutation(Variables, (conta * 2));
            }

            int dad = 0;
            if (long_poblacion % 2 == 0) {
                dad = Select();
                auxiliar.CopyIndiv(long_poblacion - 1, Examples.getNEx(), poblac.getIndiv(dad));
            }

            //Copy auxiliar population in poblac population
            poblac.CopyPopulation(auxiliar, Examples.getNEx());

            Trials += poblac.evalPop(this, Variables, Examples);
         
            
        } while (Trials <= n_eval);

//        contents = "\tNumber of Generations = " + Gen + "\n";
//        contents+= "\tNumber of Evaluations = " + Trials + "\n";
//        File.AddtoFile(nFile, contents);
        return poblac.getIndiv(best_guy);

    }

    private int BestIndividual(Population p) {

        int best = 0;
        int pos_best = 0;
        double maximo = Double.NEGATIVE_INFINITY;
        double pos_maximo = Double.NEGATIVE_INFINITY;
        boolean centi = false;

        for (int i = 0; i < p.getNumIndiv(); i++) {

            if (p.getIndiv(i).getMeasures().getGRat() > 1) {
                if (((p.getIndiv(i).getMeasures().getNSup() * 0.5
                        + p.getIndiv(i).getMeasures().getFitness() * 0.5) >= maximo)
                        && (p.getIndiv(i).getMeasures().getConf() >= 0.6)) {
                    best = i;
                    maximo = (p.getIndiv(i).getMeasures().getNSup() * 0.5
                            + p.getIndiv(i).getMeasures().getFitness() * 0.5);
                }
            }

        }

        return best;
    }

}
