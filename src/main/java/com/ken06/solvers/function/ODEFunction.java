package com.ken06.solvers.function;
/* Represents a system of first-order ODEs of the form: 
     dx/dt = f(t, x)*/
public interface ODEFunction {
    double[] computeDerivatives(double t, double[] x);

    double evaluateHeight(double[] position);
}
