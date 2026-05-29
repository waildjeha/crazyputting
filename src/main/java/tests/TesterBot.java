package tests;

import bots.MachineBot;
import com.ken06.solvers.EulerSolver;
import com.ken06.solvers.function.ODEFunction;
import com.ken06.solvers.rk4.RK4Solver;
import physicsHandler.PhysicsEngine;

import java.util.Arrays;

public class TesterBot {
    public static void main(String[] args) {
        // 1. Define the course derivatives based on the manual's test case
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
        double[] startPosition = {7.0, 8.0};
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
        //Second example: testing water logic

        // 1. Define "The Cursed Bowl" course
        ODEFunction cursedBowl = new ODEFunction() {
            @Override
            public double[] computeDerivatives(double t, double[] position) {
                double x = position[0];
                double y = position[1];
                // Derivatives of the paraboloid
                double dhdx = 0.2 * (x - 10.0);
                double dhdy = 0.2 * (y - 5.0);
                return new double[]{dhdx, dhdy};
            }

            @Override
            public double evaluateHeight(double[] position) {
                double x = position[0];
                double y = position[1];
                // The height function. Drops below 0 near (10, 5)
                return 0.1 * (Math.pow(x - 10.0, 2) + Math.pow(y - 5.0, 2)) - 0.4;
            }
        };

        // 2. Set up the test parameters
        double[] startPosition2 = {5.0, 3.0};
        double[] targetPosition2 = {15.0, 7.0};
        double radius2 = 0.1;
        double epsilon2 = 1e-5;
        double stepsize2 = 0.01;
        double friction2 = 0.08;

        PhysicsEngine engine1 = new PhysicsEngine(0.08,0.104);
        // 3. Initialize your solvers
        RK4Solver rk4 = new RK4Solver(cursedBowl, engine1);
        MachineBot bot2 = new MachineBot(rk4);

        // 4. Provide a naive initial guess
        // This guess shoots directly at the hole, guaranteeing a water collision on iteration 1
        double[] initialVelocityGuess2 = {3.0, 1.2};

        System.out.println("Starting Newton-Raphson Optimization...");
        System.out.println("Target: " + targetPosition2[0] + ", " + targetPosition2[1]);
        System.out.println("-------------------------------------------------");

        // 5. Run the bot!
        double[] bestVelocity2 = bot2.holeInOneMachine(
                initialVelocityGuess2,
                startPosition2,
                targetPosition2,
                radius2,
                epsilon2,
                stepsize2
        );

        // 6. Print the results
        System.out.println("-------------------------------------------------");
        System.out.println("SOLUTION FOUND!");
        System.out.println("Optimal Initial Velocity X: " + bestVelocity2[0]);
        System.out.println("Optimal Initial Velocity Y: " + bestVelocity2[1]);

        double speed2 = Math.hypot(bestVelocity2[0], bestVelocity2[1]);
        System.out.println("Total Speed: " + speed2 + " m/s");

        if(speed2 > 5.0) {
            System.out.println("WARNING: Speed exceeds the maximum allowed 5 m/s!");
        }

        //Testing the User logic:

        //User user = new User(startPosition2,testGreen,friction2,null);
        //System.out.println(Arrays.toString(user.getPosition()));
        //user.move(bestVelocity2,0.01);
        //System.out.println(Arrays.toString(user.getPosition()));
        System.out.println(Arrays.toString(solver.solve(bestVelocity2,startPosition2,0.01)));


    }
}
