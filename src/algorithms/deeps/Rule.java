/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms.deeps;
/**
 *
 * @author admin
 */
public class Rule implements Comparable<Rule>{
    
    double[] variables;
    int nVar;
    int number;
    int ruleClass;
    double score;
    
    public Rule(int numV, int clase){
    
        number = 0;
        nVar = numV;
        ruleClass = clase;
        score = 0;
        variables = new double[nVar];
        for(int i=0; i<nVar; i++) variables[i]=-1;
        
    }
    
    public int getNVar(){return nVar;}
    public void setNVar(int a){nVar = a;}
    
    public int getNumber(){return number;}
    public void setNumber(int a){number = a;}
    
    public int getRuleClass(){return ruleClass;}
    public void setRuleClass(int a){ruleClass = a;}
    
    public double getScore(){return score;}
    public void setScore (double a){score = a;}
   
    public double getVar(int pos){return variables[pos];}
    public void setVar(int pos, double val){variables[pos]=val;}
    
    @Override
    public int compareTo(Rule o) {
        return new Double(o.score).compareTo(score);
    }
    @Override
    public String toString() {
        return String.valueOf(score);
    }
}
