package com.ken06.solvers;

public class MachineBot {
    //when hit tree get penalty when hit water penalty
    //when in sand (set certain interval) increase friction
    //2d array where the first sub array holds the x-bounds and the second sub array holds the y-bounds
    private EulerSolver solver;

    //constructor to set variables
    public MachineBot(EulerSolver eulerSolver){
        this.solver = eulerSolver;
    }

    /** HELPER METHOD
     *  Calculates how far away we are from the hole
     * helper method of @newton_raphson (F(v_old) in the formula: v_old - J^-1(v_old) * F(v_old))
     * @param velocity speed of x and y
     * @param finalPositions the position of the hole
     * @param position current position of x and y
     * @return the distance away from the hole
     */
    private double[] error(double[] velocity,double[] finalPositions,double[] position, double stepSize){
        double[] estimates = solver.euler(velocity,position,stepSize);
        double x = estimates[0] - finalPositions[0];
        double y = estimates[1] - finalPositions[1];
        return new double[]{x,y};
    }

    /** HELPER METHOD
     * Generate a 2x2 Matrix called J
     * Part of the Formula: v_old - J^-1(v_old) * F(v_old)
     * @param velocity speed of x and y
     * @param finalPosition the position of the hole
     * @param epsilon small value to calculate the change at certain points
     * @param position current position of x and y
     * @return a 2x2 Matrix holding the change of x based on x and y and change of y based on x and y
     */
    private double[][] populateJacobianMatrix(double[] velocity, double[] finalPosition, double epsilon,double[] position,double stepSize){
        double[] baseLine =  error(velocity, finalPosition,position,stepSize);
        double[] changeX = error(new double[]{velocity[0]+ epsilon, velocity[1]},finalPosition,position,stepSize);
        double[] changeY = error(new double[]{velocity[0], velocity[1]+ epsilon},finalPosition,position,stepSize);

        return new double[][]{{(changeX[0] - baseLine[0])/epsilon,(changeY[0] -baseLine[0])/epsilon},{(changeX[1] - baseLine[1])/epsilon,(changeY[1] -baseLine[1])/epsilon}};
    }

    /** HELPER METHOD
     * Invert a 2x2 Matrix
     * used to invert the output of the @populateJacobianMatrix to J^-1 so it fits in the formula: v_old - J^-1(v_old) * F(v_old)
     * @param matrix Jacobian Matrix obtained in the previous method
     * @return the inverse of the Jacobian Matrix J^-1
     */
    private static double[][] invertMatrix(double[][] matrix){
        double ad = matrix[0][0] * matrix[1][1];
        double bc = matrix[1][0] * matrix[0][1];
        if (ad - bc == 0){
            System.exit(0);
        }
        double factor = 1/(ad - bc);

        double temp =factor* matrix[0][0];
        matrix[0][0] =factor* matrix[1][1];
        matrix[1][1] =temp;
        matrix[1][0] =factor* -matrix[1][0];
        matrix[0][1] =factor* -matrix[0][1];

        return matrix;
    }

    /** The final puzzle piece, following the formula: v_old - J^-1(v_old) * F(v_old) to get the new better velocity
     *
     * @param velocity current speed of x and y
     * @param finalPosition position of the hole
     * @param epsilon small value to calculate the change at certain points
     * @param position current position of x and y
     * @return the new better speed to get closer to the hole
     */
    private double[] newton_raphson(double[] velocity,double[] finalPosition,double epsilon,double[] position,double stepSize){
        double[] error = error(velocity,finalPosition,position,stepSize);
        double[][] jacob = populateJacobianMatrix(velocity,finalPosition,epsilon,position,stepSize);
        double[][] invert = invertMatrix(jacob);

        double xRow =invert[0][0] * error[0] + invert[0][1] * error[1];
        double yRow = invert[1][0] * error[0] + invert[1][1] * error[1];

        return new double[]{velocity[0]-xRow,velocity[1]-yRow};
    }

    /**
     *
     * @param initialVelocity current speed of x and y
     * @param startPosition current position of x and y
     * @param targetPosition position of hole
     * @param radius radius around the hole where it counts as a goal too
     * @param epsilon small value to calculate the change at certain points
     * @return the best velocity to shot with, in order to get a hole in one
     */
    public double[] holeInOneMachine(double[] initialVelocity, double[] startPosition, double[] targetPosition,double radius,double epsilon,double stepSize) {
        double[] currentVelocity = initialVelocity.clone();
        int maxIterations = 500; // Safeguard
        int iterations = 0;

        while (iterations < maxIterations) {

            double[] finalRestingPlace = solver.euler(currentVelocity, startPosition,stepSize);

            double distanceToHole = Math.hypot(
                    finalRestingPlace[0] - targetPosition[0],
                    finalRestingPlace[1] - targetPosition[1]
            );


            if (distanceToHole <= radius) {
                break;
            }

            currentVelocity = newton_raphson(currentVelocity, targetPosition, epsilon, startPosition,stepSize);

            iterations++;
        }
        if (iterations == maxIterations){
            System.err.println("Bot could not find the best shot within the Safeguard");
        }
        if (currentVelocity[0] > 5 || currentVelocity[1] > 5){
            System.err.println("Bot calculated a to fast speed");
        }

        return currentVelocity;
    }
}
