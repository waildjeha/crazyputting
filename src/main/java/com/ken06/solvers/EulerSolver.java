package com.ken06.solvers;

public class EulerSolver {
    //when hit tree get penalty when hit water penalty
    //when in sand (set certain interval) increase friction
    private final static double GRAVITY = 9.81;
    private static double FRICTION;
    private static ODEFunction GREEN;
    //2d array where the first sub array holds the x-bounds and the second sub array holds the y-bounds
    private static double[][] SAND_INTERVAL;
    private final static double SAND_FRICTION = 1;

    //constructor to set variables
    public EulerSolver(double stepSize, ODEFunction green, double friction, double[][] sandInterval){
        this.GREEN = green;
        this.FRICTION = friction;
        this.SAND_INTERVAL = sandInterval;

    }
    /** This ODE-Solver estimates a new position based on velocity and prior position
     *it runs until the velocity of both x and y are small enough
     * @param initialVelocity speed of x and y
     * @param initialPosition the position of x and y
     * @return Array [0] X-position [1] Y-position
     */
    public double[] euler(double[] initialVelocity,double[] initialPosition,double stepSize){
        double time = 0;
        double[] position = initialPosition.clone();
        double[] velocity = initialVelocity.clone();

        //loop until the ball stops (velocity < 0.01)
        while(!(((Math.abs(velocity[0]) < 0.01) && Math.abs(velocity[1]) < 0.01))){
            double[] nextState = step(velocity,position,time,stepSize);
            time += stepSize;

            position[0] = nextState[0];
            position[1] = nextState[1];
            velocity[0] = nextState[2];
            velocity[1] = nextState[3];
        }

        return position;

    }

    /** HELPER METHOD EULER-SOLVER
     * computes the slope of the green (assuming the derivative function is given to us)
     *
     * @param t time positioning on the green
     * @param position x and y position on the green
     * @return the slope at the specific parameters
     */
    private static double[] getSlope(double t,double[] position){
        return GREEN.computeDerivatives(t,position);
    }

    /** HELPER METHOD EULER-SOLVER a single euler-step, computes the next x and y and the next velocity based on friction and gravity
     *
     * @param velocity speed of x and y
     * @param position the position of x and y
     * @param time positioning on the green to be able to call the getSlope()-method
     * @return new estimate of x,y vx and vy
     */
    private static double[] step(double[] velocity,double[] position,double time,double stepSize){
        double[] slope = getSlope(time, position);
        double speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);

        //acceleration formulas
        double ax = -GRAVITY * slope[0];
        double ay = -GRAVITY * slope[1];

        //Apply friction if moving
        if (speed > 0.01) {
            if (isInSand(position)){
                ax -= SAND_FRICTION * GRAVITY * (velocity[0] / speed);
                ay -= SAND_FRICTION * GRAVITY * (velocity[1] / speed);
            }else {
                ax -= FRICTION * GRAVITY * (velocity[0] / speed);
                ay -= FRICTION * GRAVITY * (velocity[1] / speed);
            }
        }


        double nextX = position[0] + stepSize * velocity[0];
        double nextY = position[1] + stepSize * velocity[1];
        double nextVx = velocity[0] + stepSize * ax;
        double nextVy = velocity[1] + stepSize * ay;

        return new double[]{nextX, nextY, nextVx, nextVy};
    }
    private static boolean isInSand(double[] position){
        double x = position[0];
        double y = position[1];

        if (SAND_INTERVAL != null) {
            if (x >= SAND_INTERVAL[0][0] && x <= SAND_INTERVAL[0][1]) {
                if (y >= SAND_INTERVAL[1][0] && y <= SAND_INTERVAL[1][1]) {
                    return true;
                }
            }
        }

        return false;
    }
}
