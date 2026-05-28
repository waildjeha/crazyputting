package bots;

import com.ken06.solvers.ODESolver;

public class MachineBot {

    private ODESolver solver;

    /**
     * Initializes the machine bot with a specific ODE solver.
     *
     * @param solver  the mathematical solver used to simulate the ball's trajectory
     */
    public MachineBot(ODESolver solver){
        this.solver = solver;
    }

    /**
     * Calculates the difference between the ball's final resting position and the target hole.
     *
     * @param velocity       the initial x and y speeds of the ball
     * @param finalPositions the x and y coordinates of the target hole
     * @param position       the starting x and y coordinates of the ball
     * @param stepSize       the time increment used for the simulation
     * @return               an array containing the error in the x and y directions
     */
    private double[] error(double[] velocity, double[] finalPositions, double[] position, double stepSize){
        double[] estimates = solver.solve(velocity, position, stepSize);
        double x = estimates[0] - finalPositions[0];
        double y = estimates[1] - finalPositions[1];
        return new double[]{x, y};
    }

    /**
     * Generates the 2x2 Jacobian matrix used in the Newton-Raphson method to estimate derivatives.
     *
     * @param velocity      the initial x and y speeds of the ball
     * @param finalPosition the x and y coordinates of the target hole
     * @param epsilon       a small value used to calculate the rate of change
     * @param position      the starting x and y coordinates of the ball
     * @param stepSize      the time increment used for the simulation
     * @return              a 2x2 matrix representing the change in position based on velocity adjustments
     */
    private double[][] populateJacobianMatrix(double[] velocity, double[] finalPosition, double epsilon, double[] position, double stepSize){
        double[] baseLine = error(velocity, finalPosition, position, stepSize);
        double[] changeX = error(new double[]{velocity[0] + epsilon, velocity[1]}, finalPosition, position, stepSize);
        double[] changeY = error(new double[]{velocity[0], velocity[1] + epsilon}, finalPosition, position, stepSize);

        return new double[][]{
                {(changeX[0] - baseLine[0]) / epsilon, (changeY[0] - baseLine[0]) / epsilon},
                {(changeX[1] - baseLine[1]) / epsilon, (changeY[1] - baseLine[1]) / epsilon}
        };
    }

    /**
     * Calculates the inverse of a 2x2 matrix.
     *
     * @param matrix  the original 2x2 Jacobian matrix
     * @return        the inverted 2x2 matrix
     */
    private static double[][] invertMatrix(double[][] matrix){
        double ad = matrix[0][0] * matrix[1][1];
        double bc = matrix[1][0] * matrix[0][1];

        if (ad - bc == 0){
            throw new RuntimeException("Matrix is singular (Determinant is 0). The bot got stuck in a valley.");
        }

        double factor = 1 / (ad - bc);

        double temp = factor * matrix[0][0];
        matrix[0][0] = factor * matrix[1][1];
        matrix[1][1] = temp;
        matrix[1][0] = factor * -matrix[1][0];
        matrix[0][1] = factor * -matrix[0][1];

        return matrix;
    }

    /**
     * Applies the Newton-Raphson formula to calculate a better initial velocity guess.
     *
     * @param velocity      the current x and y speeds being tested
     * @param finalPosition the x and y coordinates of the target hole
     * @param epsilon       a small value used for the Jacobian approximation
     * @param position      the starting x and y coordinates of the ball
     * @param stepSize      the time increment used for the simulation
     * @return              an updated array containing the improved x and y speeds
     */
    private double[] newton_raphson(double[] velocity, double[] finalPosition, double epsilon, double[] position, double stepSize){
        double[] error = error(velocity, finalPosition, position, stepSize);
        double[][] jacob = populateJacobianMatrix(velocity, finalPosition, epsilon, position, stepSize);
        double[][] invert = invertMatrix(jacob);

        double xRow = invert[0][0] * error[0] + invert[0][1] * error[1];
        double yRow = invert[1][0] * error[0] + invert[1][1] * error[1];

        return new double[]{velocity[0] - xRow, velocity[1] - yRow};
    }

    /**
     * Iteratively searches for the optimal initial velocity to score a hole-in-one.
     *
     * @param initialVelocity the starting guess for the x and y speeds
     * @param startPosition   the starting x and y coordinates of the ball
     * @param targetPosition  the x and y coordinates of the target hole
     * @param radius          the acceptable radius around the hole to count as a success
     * @param epsilon         a small value used for the Jacobian approximation
     * @param stepSize        the time increment used for the simulation
     * @return                an array containing the optimal x and y speeds to reach the hole
     */
    public double[] holeInOneMachine(double[] initialVelocity, double[] startPosition, double[] targetPosition, double radius, double epsilon, double stepSize) {
        double[] currentVelocity = initialVelocity.clone();
        int maxIterations = 500;
        int iterations = 0;

        while (iterations < maxIterations) {

            double[] finalRestingPlace = solver.solve(currentVelocity, startPosition, stepSize);

            double distanceToHole = Math.hypot(
                    finalRestingPlace[0] - targetPosition[0],
                    finalRestingPlace[1] - targetPosition[1]
            );

            if (distanceToHole <= radius) {
                break;
            }

            currentVelocity = newton_raphson(currentVelocity, targetPosition, epsilon, startPosition, stepSize);

            iterations++;
        }

        if (iterations == maxIterations){
            System.err.println("Bot could not find the best shot within the Safeguard");
        }
        if (currentVelocity[0] > 5 || currentVelocity[1] > 5){
            System.err.println("Bot calculated a too fast speed");
        }

        return currentVelocity;
    }
}
