package tests;

import bots.MachineBot;
import com.ken06.solvers.EulerSolver;
import com.ken06.solvers.function.ODEFunction;
import com.ken06.solvers.rk4.RK4Solver;
import physicsHandler.PhysicsEngine;

import java.util.Arrays;

public class NoiseTester {
    private static double[] test_flat(){


        double noiseA = Math.random() * 10;
        double noiseB = Math.random() * 10;
        System.out.println(noiseB);
        System.out.println(noiseA);

        ODEFunction testGreen = new ODEFunction() {
            @Override
            public double[] computeDerivatives(double t, double[] position) {
                double x = position[0];
                double y = position[1];

                double dhdx = 0.025 * Math.cos((x + y) / 10.0);
                double dhdy = 0.025 * Math.cos((x + y) / 10.0);

                return new double[]{dhdx, dhdy};
            }
            @Override
            public double evaluateHeight(double[] position){
                double x = position[0];
                double y = position[1];
                // The height function h(x,y)
                return 0.25 * Math.sin((x + y) / 10.0) + 1.0;
            }
        };
// 3. Set up the test parameters
        double[] startPosition = {
                7.0 + noiseA,
                8.0 + noiseB};
        double[] targetPosition = {14.0, 1.0};
        double radius = 0.1;
        double epsilon = 1e-5; // A small value for the Jacobian approximation
        double stepsize = 0.01;

        // 2. Initialize your machine
        // (Assuming step size = 0.01 and no sand intervals for this basic test)
        PhysicsEngine engine = new PhysicsEngine(0.08,0.104);
        EulerSolver euler = new EulerSolver(testGreen,engine);
        RK4Solver solver = new RK4Solver(testGreen,engine);

        //choose which bot to take
        // MachineBot bot = new MachineBot(euler);
        MachineBot bot = new MachineBot(solver);


        // 4. Provide an initial guess for the velocity
        // Just a basic guess pointing roughly towards the hole
        double[] initialVelocityGuess = {2.0, -2.0};

        // 5. Run the bot!
        double[] bestVelocity = bot.holeInOneMachine(
                initialVelocityGuess,
                startPosition,
                targetPosition,
                radius,
                epsilon,
                stepsize
        );

        // 6. Print the results
        System.out.println("Optimal Initial Velocity X: " + bestVelocity[0]);
        System.out.println("Optimal Initial Velocity Y: " + bestVelocity[1]);

        double speed = Math.hypot(bestVelocity[0], bestVelocity[1]);
        System.out.println("Total Speed: " + speed + " m/s");

        if(speed > 5.0) {
            System.out.println("WARNING: Speed exceeds the maximum allowed 5 m/s!");
        }
        double[] result = solver.solve(bestVelocity,startPosition,stepsize);
        System.out.println(Arrays.toString(result));
        result[0] = Math.abs(result[0] - targetPosition[0]);
        result[1] = Math.abs(result[1] - targetPosition[1]);

        System.out.println("This is the result of the flat test: "+Arrays.toString(result)+ " with a noise of: " + noiseA + " and " + noiseB);

        return result;
    }
    private static double[] test_complex(){

        double noiseA = Math.random() * 10;
        double noiseB = Math.random() * 10;

        ODEFunction steep = new ODEFunction() {
            @Override
            public double[] computeDerivatives(double t, double[] x) {
                return new double[]{0.05,0};
            }

            @Override
            public double evaluateHeight(double[] position) {
                double x = position[0];
                double y = position[1];

                return 0.05* x;
            }
        };
        // 3. Set up the test parameters
        double[] startPosition = {
                2.0 + noiseA,
                2.0 + noiseB};
        double[] targetPosition = {4.0, 4.0};
        double radius = 0.1;
        double epsilon = 1e-5; // A small value for the Jacobian approximation
        double stepsize = 0.01;

        double[] initialVelocity = {2, 1.3};

        PhysicsEngine engine = new PhysicsEngine(0.08,0.104);
        RK4Solver rk4 = new RK4Solver(steep,engine);
        MachineBot bot = new MachineBot(rk4);

        System.out.println(Arrays.toString(rk4.solve(initialVelocity,startPosition,stepsize)));
        double[] estimateVelocity = bot.holeInOneMachine(initialVelocity,startPosition,targetPosition,radius,epsilon,stepsize);
        System.out.println(Arrays.toString(estimateVelocity));
       double[] result = rk4.solve(estimateVelocity,startPosition,stepsize);

        result[0] = Math.abs(result[0] - targetPosition[0]);
        result[1] = Math.abs(result[1] - targetPosition[1]);

        System.out.println("This is the result of the complex test: "+Arrays.toString(result)+ " with a noise of: " + noiseA + " and " + noiseB);

        return result;
    }

    static void main() {
        test_flat();
        test_complex();
    }
}
