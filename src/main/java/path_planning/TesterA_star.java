package path_planning;

import com.ken06.solvers.function.ODEFunction;

public class TesterA_star {
    static void main() {
        ODEFunction green = new ODEFunction() {
            @Override
            public double[] computeDerivatives(double t, double[] x) {
                double x1 = x[0];
                double y = x[1];

                return new double[]{Math.cos(x1 + y),Math.cos(x1 + y)};
            }

            @Override
            public double evaluateHeight(double[] position) {
                double x = position[0];
                double y = position[1];

                return Math.sin(x + y);
            }
        };
        A_star aStar = new A_star(green,20,0.5);

        aStar.populateGrid();

        System.out.println("hi");
    }
}
