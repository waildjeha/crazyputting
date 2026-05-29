package tests;

import bots.RuleBasedBot;
import com.ken06.solvers.function.ODEFunction;
import com.ken06.solvers.ODESolver;
import com.ken06.solvers.rk4.RK4Solver;
import com.ken06.solvers.rk4.SolverResult;
import physicsHandler.PhysicsEngine;

import java.util.Arrays;

public class RuleBasedBotTest {

    private static final double MU_K = 0.05;
    private static final double MU_S = 0.10;
    private static final double SAND_MU_K = 0.20;
    private static final double SAND_MU_S = 0.30;
    private static final double G = 9.81;

    public static void main(String[] args) {
        testF1();
        System.out.println();
        testF2();
    }

    private static void testF1() {
        ODEFunction cosineFunction = new ODEFunction() {
            @Override
            public double[] computeDerivatives(double t, double[] state) {
                double x = state[0];
                double y = state[1];

                double dx = (Math.cos(x + y) * 0.025) / 10.0;
                double dy = (Math.cos(x + y) * 0.025) / 10.0;

                return new double[]{dx, dy};
            }
// setting the height to 1, so there is no body of water
            @Override
            public double evaluateHeight(double[] position) {
                return 1.0;
            }
        };

        double[] ballPos = {0.0, 0.0};
        double[] targetPos = {14.0, 1.0};

        runTest("Function 1 - Cosine", cosineFunction, ballPos, targetPos);
    }

    private static void testF2() {
        ODEFunction linearFunction = new ODEFunction() {
            @Override
            public double[] computeDerivatives(double t, double[] state) {
                double x = state[0];
                double y = state[1];

                double dx = 0.2 * (x - 10);
                double dy = 0.2 * (y - 5);

                return new double[]{dx, dy};
            }
// setting the height to 1, so there is no body of water
            @Override
            public double evaluateHeight(double[] position) {
                return 1.0;
            }
        };

        double[] ballPos = {0.0, 0.0};
        double[] targetPos = {15.0, 7.0};

        runTest("Function 2 - Linear", linearFunction, ballPos, targetPos);
    }

    private static void runTest(String testName,
                                ODEFunction function,
                                double[] ballPos,
                                double[] targetPos) {

        double[][] sandInterval = null;

        PhysicsEngine physics = new PhysicsEngine(
            MU_K,
            MU_S,
            SAND_MU_K,
            SAND_MU_S,
            sandInterval
        );
        ODESolver solver = new RK4Solver(function,physics);
        RuleBasedBot bot = new RuleBasedBot(solver, physics);

        double[] velocity = bot.calculateVelocity(
            ballPos,
            targetPos,
            MU_K,
            G,
            function
        );

        double[] estimatedPosition = bot.estimateTargetPosition(
            ballPos,
            targetPos,
            MU_K,
            G,
            function
        );

        SolverResult result = bot.playGame(
            ballPos,
            targetPos,
            MU_K,
            G,
            function
        );

        double[] lastState = result.states[result.states.length - 1];
        double[] finalPosition = {lastState[0], lastState[1]};

        double distanceToTarget = Math.hypot(
            estimatedPosition[0] - targetPos[0],
            estimatedPosition[1] - targetPos[1]
        );

        System.out.println(testName);
        System.out.println("Ball position: " + Arrays.toString(ballPos));
        System.out.println("Target position: " + Arrays.toString(targetPos));
        System.out.println("Estimated velocity: " + Arrays.toString(velocity));
        System.out.println("Estimated speed: " + Math.hypot(velocity[0], velocity[1]));
        System.out.println("Closest simulated position: " + Arrays.toString(estimatedPosition));
        System.out.println("Final position: " + Arrays.toString(finalPosition));
        System.out.println("Distance to target: " + distanceToTarget);
        System.out.println("Steps simulated: " + result.size());
    }
}