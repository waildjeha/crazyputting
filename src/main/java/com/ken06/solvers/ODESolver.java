package com.ken06.solvers;

import com.ken06.solvers.function.ODEFunction;
import com.ken06.solvers.rk4.SolverResult;

/**
 * Abstract base class for all ODE solvers which defines the shared contract and provides the full trajectory solve() method
   that every solver inherits. RK4 solver only need to implement
   the single-step advance: step().
 */
public abstract class ODESolver {

    
    public abstract double[] step(ODEFunction f, double t, double[] x, double h);

    /**
     * Concrete solve() — inherited by all solvers, returns full trajectory
     * The step method used inside is that makes the difference betwen the solvers
     */
    
    public SolverResult solve(ODEFunction f, double[] x0, double t0, double tEnd, double h) {
        if (h <= 0)     throw new IllegalArgumentException("Step size h must be positive.");
        if (tEnd <= t0) throw new IllegalArgumentException("tEnd must be greater than t0.");

        int nSteps = (int) Math.floor((tEnd - t0) / h);
        int n      = x0.length;

        double[]   times  = new double[nSteps + 1];
        double[][] states = new double[nSteps + 1][n];

        times[0]  = t0;
        states[0] = x0.clone();

        double   t = t0;
        double[] x = x0.clone();

        for (int i = 1; i <= nSteps; i++) {
            x = step(f, t, x, h);
            t += h;
            times[i]  = t;
            states[i] = x.clone();
        }

        return new SolverResult(times, states);
    }
       public abstract double[] solve(double[] initialVelocity,double[] initialPosition,double stepSize);


    // Vector operations methods to avoid clunking the RK4 code with basic arithmetic

    /* Returns a new vector: v * scalar */
    protected static double[] scale(double[] v, double scalar) {
        double[] result = new double[v.length];
        for (int i = 0; i < v.length; i++) result[i] = v[i] * scalar;
        return result;
    }

    /* Returns a new vector: a + b  */
    protected static double[] add(double[] a, double[] b) {
        double[] result = new double[a.length];
        for (int i = 0; i < a.length; i++) result[i] = a[i] + b[i];
        return result;
    }
}
