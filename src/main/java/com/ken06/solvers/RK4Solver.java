package com.ken06.solvers;


/* 4th-order Runge-Kutta solver*/
public class RK4Solver extends ODESolver {
    final ODEFunction green;
    private PhysicsEngine engine;
    private final String user; //to find out who is using it to apply different punishment when hitting water/tree
    public RK4Solver(ODEFunction green,double friction, double[][] sandInterval){
        this.green = green;
        double sandFriction = 1.0;
        this.user = null;
        this.engine = new PhysicsEngine(friction, sandFriction,sandInterval);
    }
    public RK4Solver(ODEFunction green,double friction, double[][] sandInterval,String user){
        this.green = green;
        double sandFriction = 1.0;
        this.user = user;
        this.engine = new PhysicsEngine(friction, sandFriction,sandInterval);
    }

    
    @Override
    public double[] step(ODEFunction f, double t, double[] x, double h) {
        int n = x.length;
        double[] xNew = new double[n];
        //new n = 2 since x y are in position 1 and 2

        double[] k1 = getStateDerivatives(f, t, x);

        //evaluate k2
        double[] x2 = add(x, scale(k1, h / 2.0));
        double[] k2 = getStateDerivatives(f, t+ h / 2.0, x2);

        //evaluate k3
        double[] x3 = add(x, scale(k2, h / 2.0));
        double[] k3 = getStateDerivatives(f, t + h / 2.0, x3);

        //evaluate k4
        double[] x4 = add(x, scale(k3, h));
        double[] k4 = getStateDerivatives(f, t + h, x4);

        // Combine to get the final new state:
        for (int i = 0; i < n; i++) {
            xNew[i] = x[i] + (h / 6.0) * (k1[i] + 2.0 * k2[i] + 2.0 * k3[i] + k4[i]);
        }

        return xNew;
    }
    @Override
    public double[] solve(double[] initialVelocity,double[] initialPosition,double stepSize){
        double time = 0;

        double[] state = new double[]{
                initialPosition[0],
                initialPosition[1],
                initialVelocity[0],
                initialVelocity[1]
        };
        double[] slope = this.green.computeDerivatives(time,state);

        //loop until the ball is not moving
        while (engine.isMoving(state,slope)) {
            double[] nextState = step(this.green,time,state,stepSize);
            time += stepSize;

            state = nextState;
            slope = this.green.computeDerivatives(time, state);
            //is in the water
            if (engine.isInWater(state,green)){
                if(user == null) { //not a user
                    return new double[]{state[0], state[1]};
                }else { //user
                    return initialPosition;
                }
            }
        }

        return new double[]{state[0],state[1]};

    }

    /**
     * Helper method to evaluate the full state derivative for RK4.
     * Takes the current 4D state [x, y, vx, vy] and returns the rates of change [vx, vy, ax, ay]
     */
    private double[] getStateDerivatives(ODEFunction f, double t, double[] state) {
        double[] velocity = new double[]{state[2], state[3]};
        double[] slope = f.computeDerivatives(t, state);



        // Apply physics to get accelerations
        double[] accels = engine.applyPhysics(new double[]{state[0], state[1]},velocity, slope);

        // Return [dx/dt, dy/dt, dvx/dt, dvy/dt]
        return new double[]{state[2], state[3], accels[0], accels[1]};
    }
    /*Main with one of the suggested test systems: LOtka Volterra*/

    public static void main(String[] args) {
/*


        System.out.println("=== Lotka-Volterra ===");
        double alpha = 1.0, beta = 0.1, delta = 0.075, gamma = 1.5;

        ODEFunction lotkaVolterra = (t, x) -> new double[]{
                alpha * x[0] - beta  * x[0] * x[1],
                delta * x[0] * x[1] - gamma * x[1]
        };

        ODESolver solver = new RK4Solver(lotkaVolterra);

        SolverResult lv = (SolverResult) solver.solve(lotkaVolterra, new double[]{10.0, 5.0}, 0.0, 20.0, 0.01);

        double[] prey      = lv.getVariable(0);
        double[] predators = lv.getVariable(1);

        for (int i = 0; i < lv.size(); i += 100) {
            System.out.printf("t = %5.2f  prey = %7.4f  predators = %7.4f%n",
            lv.times[i], prey[i], predators[i]);
        }

       */
    }
}
