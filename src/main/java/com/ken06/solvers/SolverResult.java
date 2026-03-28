package com.ken06.solvers;

/* Holds the full output of an ODE solver run (the output of the solve() method), which is useful for the gui plotting*/
public class SolverResult {

    public final double[] times;
    public final double[][] states;

    public SolverResult(double[] times, double[][] states) {
        this.times  = times;
        this.states = states;
    }



    /**
     * Returns the full time series of a single variable across all steps.
    
     *
     * @param variableIndex  index of the variable 
     * @return               array of length nSteps+1 with values of that variable over time
     */
    public double[] getVariable(int variableIndex) {
        double[] series = new double[states.length];
        for (int i = 0; i < states.length; i++) {
            series[i] = states[i][variableIndex];
        }
        return series;
    }

    /* number of recorded time steps  */
    public int size() {
        return times.length;
    }

    /* dimension of the state vector */
    public int dimension() {
        return states[0].length;
    }
}
