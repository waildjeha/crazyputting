package com.ken06.solvers;

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
        };

        // 3. Set up the test parameters
        double[] startPosition = {7.0, 8.0};
        double[] targetPosition = {14.0, 1.0};
        double radius = 0.1;
        double epsilon = 1e-5; // A small value for the Jacobian approximation
        double stepsize = 0.01;

        // 2. Initialize your machine
        // (Assuming step size = 0.01 and no sand intervals for this basic test)
        EulerSolver euler = new EulerSolver(stepsize,testGreen,0.08,null);
        MachineBot bot = new MachineBot(euler);


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
    }
}
