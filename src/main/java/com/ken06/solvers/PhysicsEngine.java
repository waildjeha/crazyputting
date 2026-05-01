package com.ken06.solvers;

public class PhysicsEngine {
    private final double GRAVITY = 9.81;
    private double FRICTION;
    private double SAND_FRICTION;
    private double[][] SAND_INTERVAL;

    public PhysicsEngine(double FRICTION, double SAND_FRICTION,double[][] sandInterval){
        this.FRICTION = FRICTION;
        this.SAND_FRICTION = SAND_FRICTION;
        this.SAND_INTERVAL = sandInterval;
    }
    public double[] applyPhysics(double[] position, double[] velocity,double[] slope){
        //acceleration formulas
        double ax = -GRAVITY * slope[0];
        double ay = -GRAVITY * slope[1];
        //calculate speed
        double speed = Math.hypot(velocity[0], velocity[1]);

        if (speed > 0.01) {
            if (isInSand(position)) {
                ax -= SAND_FRICTION * GRAVITY * (velocity[0] / speed);
                ay -= SAND_FRICTION * GRAVITY * (velocity[1] / speed);
            } else {
                ax -= FRICTION * GRAVITY * (velocity[0] / speed);
                ay -= FRICTION * GRAVITY * (velocity[1] / speed);
            }
        }
        return new double[]{ax,ay};
    }
    private boolean isInSand(double[] position){
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
    public boolean isInWater(double[] state,ODEFunction green) {
        //check if height function is negative (and by def. ball is in water)
        double currentZ = green.evaluateHeight(new double[]{state[0], state[1]});
        //is in the water
        if (currentZ < 0) {
            System.out.println("Your ball fell into the water");
            return true;
        }
        return false;
    }
    public boolean hitTree(){
        return false;
    }
}
