package path_planning;

import com.ken06.solvers.function.ODEFunction;

public class RiverTestCourse implements ODEFunction {

    @Override
    public double evaluateHeight(double[] position) {
        double x = position[0];
        double y = position[1];

        // Default flat ground level
        double height = 1.0;

        // Create a "river" of water (negative height) from x=4 to x=6.
        // However, leave a "bridge" (positive height) where y is greater than 8.
        if (x > 4.0 && x < 6.0 && y <= 8.0) {
            height = -1.0; // This is WATER
        }

        return height;
    }

    @Override
    public double[] computeDerivatives(double t, double[] state) {
        // Since you are only testing the Macro-Planner (A*) right now,
        // the physics ODE solver isn't being used.
        // We can just return an empty array to satisfy the interface.
        return new double[state.length];
    }
}