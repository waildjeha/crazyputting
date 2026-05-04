package com.ken06.solvers;

public class PhysicsEngine {
    private final double GRAVITY = 9.81;
    private double FRICTION;
    private double SAND_FRICTION;
    private double[][] SAND_INTERVAL;
    private double STATIC_FRICTION;
    private double STATIC_SAND_FRICTION;

    public PhysicsEngine(double FRICTION, double SAND_FRICTION,double[][] sandInterval){
        this.FRICTION = FRICTION;
        this.STATIC_FRICTION = FRICTION * 1.3;
        this.SAND_FRICTION = SAND_FRICTION;
        this.STATIC_SAND_FRICTION = SAND_FRICTION * 1.3;
        this.SAND_INTERVAL = sandInterval;
    }
    public PhysicsEngine(double FRICTION){
        this.FRICTION = FRICTION;
        this.SAND_FRICTION = 0.0;
        this.STATIC_SAND_FRICTION = 0.0;
        this.SAND_INTERVAL = null;
    }
    public double[] applyPhysics(double[] position, double[] velocity,double[] slope) {
        //acceleration formulas
        double ax_grav = -GRAVITY * slope[0];
        double ay_grav = -GRAVITY * slope[1];

        // Magnitude of gravity pulling down the slope
        double gravityForce = Math.hypot(ax_grav, ay_grav);

        //Determine terrain coefficients
        boolean inSand = isInSand(position);
        double kineticFriction;
        double staticFriction;
        if (inSand) {
            kineticFriction = SAND_FRICTION;
            staticFriction = STATIC_SAND_FRICTION;
        } else {
            kineticFriction = FRICTION;
            staticFriction = STATIC_FRICTION;
        }

        //Calculate current speed
        double speed = Math.hypot(velocity[0], velocity[1]);

        double ax_fric = 0.0;
        double ay_fric = 0.0;

        // if ball is moving apply kineticFriction
        if (speed > 0.01) {
            ax_fric = -kineticFriction * GRAVITY * (velocity[0] / speed);
            ay_fric = -kineticFriction * GRAVITY * (velocity[1] / speed);
        }
        // if stationary apply static friction
        else {
            double maxStaticFriction = staticFriction * GRAVITY;

            if (gravityForce > maxStaticFriction) {
                // Gravity broke static friction!
                double dir_x = ax_grav / gravityForce;
                double dir_y = ay_grav / gravityForce;

                ax_fric = -kineticFriction * GRAVITY * dir_x;
                ay_fric = -kineticFriction * GRAVITY * dir_y;
            } else {
                // Static friction holds the ball firmly in place.
                // The net acceleration is 0, so we can exit early.
                return new double[]{0.0, 0.0};
            }
        }
        return new double[]{ax_grav + ax_fric, ay_grav + ay_fric};
    }
    public boolean isInSand(double[] position){
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

    public boolean isMoving(double[] state, double[] slope) {
        double speed = Math.hypot(state[2], state[3]);

        // If it's moving fast enough, keep going
        if (speed >= 0.01) {
            return true;
        }

        // If it's barely moving, check if gravity overcomes static friction
        // Force of gravity pulling down the slope
        double gravityForce = Math.hypot(GRAVITY * slope[0], GRAVITY * slope[1]);

        // Determine the current friction coefficient based on terrain
        double currentFriction;

        if (isInSand(state)){
            currentFriction = SAND_FRICTION;
        }else {
            currentFriction = FRICTION;
        }

        // The maximum force static friction can apply to hold the ball still
        // Note: Static friction is usually slightly higher than kinetic friction
        double staticFrictionForce = currentFriction * GRAVITY;

        // If gravity is stronger than friction, it keeps moving!
        return gravityForce > staticFrictionForce;
    }
    public boolean hitTree(){
        return false;
    }
}
