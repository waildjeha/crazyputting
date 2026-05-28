package physicsHandler;
public class PhysicsEngine {
    private final double GRAVITY = 9.81;
    private double FRICTION;
    private double SAND_FRICTION;
    private double[][] SAND_INTERVAL;
    private double STATIC_FRICTION;
    private double STATIC_SAND_FRICTION;

    /**
     * Initializes the physics engine with full friction parameters, including sand.
     *
     * @param muK          the kinetic friction coefficient for standard grass
     * @param muS          the static friction coefficient for standard grass
     * @param sandMuK      the kinetic friction coefficient for sand
     * @param sandMuS      the static friction coefficient for sand
     * @param sandInterval the coordinate boundaries defining the sand pit
     */
    public PhysicsEngine(double muK, double muS, double sandMuK, double sandMuS, double[][] sandInterval) {
        this.FRICTION = muK;
        this.STATIC_FRICTION = muS;
        this.SAND_FRICTION = sandMuK;
        this.STATIC_SAND_FRICTION = sandMuS;
        this.SAND_INTERVAL = sandInterval;
    }

    /**
     * Initializes the physics engine with only standard grass friction parameters.
     *
     * @param muK the kinetic friction coefficient for standard grass
     * @param muS the static friction coefficient for standard grass
     */
    public PhysicsEngine(double muK, double muS) {
        this.FRICTION = muK;
        this.STATIC_FRICTION = muS;
        this.SAND_FRICTION = 0.0;
        this.STATIC_SAND_FRICTION = 0.0;
        this.SAND_INTERVAL = null;
    }

    /**
     * Calculates the acceleration of the ball based on gravity, terrain slope, and friction.
     *
     * @param position the current x and y coordinates of the ball
     * @param velocity the current x and y speeds of the ball
     * @param slope    the gradient of the terrain at the ball's current position
     * @return         an array containing the calculated x and y accelerations
     */
    public double[] applyPhysics(double[] position, double[] velocity, double[] slope) {
        double ax_grav = -GRAVITY * slope[0];
        double ay_grav = -GRAVITY * slope[1];

        double gravityForce = Math.hypot(ax_grav, ay_grav);

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

        double speed = Math.hypot(velocity[0], velocity[1]);

        double ax_fric = 0.0;
        double ay_fric = 0.0;

        if (speed > 0.01) {
            ax_fric = -kineticFriction * GRAVITY * (velocity[0] / speed);
            ay_fric = -kineticFriction * GRAVITY * (velocity[1] / speed);
        } else {
            double maxStaticFriction = staticFriction * GRAVITY;

            if (gravityForce > maxStaticFriction) {
                double dir_x = ax_grav / gravityForce;
                double dir_y = ay_grav / gravityForce;

                ax_fric = -kineticFriction * GRAVITY * dir_x;
                ay_fric = -kineticFriction * GRAVITY * dir_y;
            } else {
                return new double[]{0.0, 0.0};
            }
        }
        return new double[]{ax_grav + ax_fric, ay_grav + ay_fric};
    }

    /**
     * Checks if the ball is currently within the boundaries of a sand pit.
     *
     * @param position the current x and y coordinates of the ball
     * @return         true if the ball is in the sand, false otherwise
     */
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
    /**
     * Evaluates whether the ball is currently in motion or if static friction has brought it to a stop.
     *
     * @param state the current state array containing position and velocity
     * @param slope the gradient of the terrain at the ball's current position
     * @return      true if the ball is moving or accelerating by gravity, false if it is completely stationary
     */
    public boolean isMoving(double[] state, double[] slope) {
        double speed = Math.hypot(state[2], state[3]);

        if (speed > 0.01) {
            return true;
        }

        double gravityForce = Math.hypot(GRAVITY * slope[0], GRAVITY * slope[1]);
        double currentFriction;

        if (isInSand(state)) {
            currentFriction = STATIC_SAND_FRICTION;
        } else {
            currentFriction = STATIC_FRICTION;
        }

        double staticFrictionForce = currentFriction * GRAVITY;

        return gravityForce > staticFrictionForce;
    }
}
