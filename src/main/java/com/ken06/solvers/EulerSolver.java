package com.ken06.solvers;

public class EulerSolver extends ODESolver{
    //when hit tree get penalty when hit water penalty
    //when in sand (set certain interval) increase friction
    private ODEFunction green;
    //2d array where the first sub array holds the x-bounds and the second sub array holds the y-bounds
    private PhysicsEngine engine;

    //constructor to set variables
    public EulerSolver(double stepSize, ODEFunction green, double friction, double[][] sandInterval){
        this.green = green;
        double sandFriction = 1.0;
        this.engine = new PhysicsEngine(friction, sandFriction,sandInterval);

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
        double[] values = new double[]{initialPosition[0],initialPosition[1],initialVelocity[0],initialVelocity[1]};

        double[] state = new double[]{
                initialPosition[0],
                initialPosition[1],
                initialVelocity[0],
                initialVelocity[1]
        };

        //loop until the ball stops (velocity < 0.01)
        while (Math.abs(state[2]) >= 0.01 || Math.abs(state[3]) >= 0.01) {
            double[] nextState = step(this.green,time,state,stepSize);
            time += stepSize;

            state = nextState;

        }

        return new double[]{state[0],state[1]};

    }

    /** HELPER METHOD EULER-SOLVER a single euler-step, computes the next x and y and the next velocity based on friction and gravity
     *
     * @param values the position of x and y and the velocity of x and y
     * @param time positioning on the green to be able to call the getSlope()-method
     * @return new estimate of x,y vx and vy
     */
    @Override
    public double[] step(ODEFunction f,double time,double[] values,double stepSize){
        double[] velocity = new double[]{values[2],values[3]};
        double[] slope = f.computeDerivatives(time,values);

        double speed = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);

        double[] result = engine.applyPhysics(values,velocity,slope,speed);
        double ax = result[0];
        double ay = result[1];


        double nextX = values[0] + stepSize * velocity[0];
        double nextY = values[1] + stepSize * velocity[1];
        double nextVx = velocity[0] + stepSize * ax;
        double nextVy = velocity[1] + stepSize * ay;

        return new double[]{nextX, nextY, nextVx, nextVy};
    }
}
