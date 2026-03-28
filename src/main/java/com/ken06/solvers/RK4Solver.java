package com.ken06.solvers;


/* 4th-order Runge-Kutta solver*/
public class RK4Solver extends ODESolver {

    
    @Override
    public double[] step(ODEFunction f, double t, double[] x, double h) {
        int n = x.length;

        double[] k1 = f.computeDerivatives(t, x);
        double[] k2 = f.computeDerivatives(t + h / 2.0, add(x, scale(k1, h / 2.0)));
        double[] k3 = f.computeDerivatives(t + h / 2.0, add(x, scale(k2, h / 2.0)));
        double[] k4 = f.computeDerivatives(t + h,        add(x, scale(k3, h)));

        double[] xNew = new double[n];
        for (int i = 0; i < n; i++) {
            xNew[i] = x[i] + (h / 6.0) * (k1[i] + 2.0 * k2[i] + 2.0 * k3[i] + k4[i]);
        }
        return xNew;
    }

    /*Main with one of the suggested test systems: LOtka Volterra*/

    public static void main(String[] args) {

        ODESolver solver = new RK4Solver();

        System.out.println("=== Lotka-Volterra ===");
        double alpha = 1.0, beta = 0.1, delta = 0.075, gamma = 1.5;

        ODEFunction lotkaVolterra = (t, x) -> new double[]{
                alpha * x[0] - beta  * x[0] * x[1],
                delta * x[0] * x[1] - gamma * x[1]
        };

        SolverResult lv = (SolverResult) solver.solve(lotkaVolterra, new double[]{10.0, 5.0}, 0.0, 20.0, 0.01);

        double[] prey      = lv.getVariable(0);
        double[] predators = lv.getVariable(1);

        for (int i = 0; i < lv.size(); i += 100) {
            System.out.printf("t = %5.2f  prey = %7.4f  predators = %7.4f%n",
            lv.times[i], prey[i], predators[i]);
        }

        
    }
}
