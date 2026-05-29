package bots;

import physicsHandler.Collision_Detector;
import com.ken06.solvers.function.ODEFunction;
import com.ken06.solvers.ODESolver;
import com.ken06.solvers.rk4.SolverResult;
import physicsHandler.PhysicsEngine;

import java.util.ArrayList;
import java.util.List;

public class RuleBasedBot {

    private static final double STEP_SIZE = 0.01;
    private static final double MAX_SPEED = 5.0;
    private static final double MIN_SPEED = 0.1;
    private static final double TARGET_TOLERANCE = 0.05;

    private final ODESolver solver;
    private final PhysicsEngine physics;

    public RuleBasedBot(ODESolver solver, PhysicsEngine physics) {
        if (solver == null) throw new IllegalArgumentException("Solver cannot be null");
        if (physics == null) throw new IllegalArgumentException("PhysicsEngine cannot be null");

        this.solver = solver;
        this.physics = physics;
    }
// assuming we do not use trees, can be changed later
    public double[] calculateVelocity(double[] ballPos,
                                      double[] targetPos,
                                      double miuK,
                                      double g,
                                      ODEFunction f) {
        return calculateVelocity(ballPos, targetPos, miuK, g, f, null);
    }

    public double[] calculateVelocity(double[] ballPos,
                                      double[] targetPos,
                                      double miuK,
                                      double g,
                                      ODEFunction f,
                                      List<double[]> trees) {
        if (f == null) throw new IllegalArgumentException("ODEFunction cannot be null.");

        double dx = targetPos[0] - ballPos[0];
        double dy = targetPos[1] - ballPos[1];
        double distance = Math.hypot(dx, dy);

        if (distance < 1e-6) {
            throw new IllegalArgumentException("Ball is already at the target");
        }

        double speed = Math.sqrt(2.0 * miuK * g * distance);

        if (physics.isInSand(ballPos) || physics.isInSand(targetPos)) {
            speed *= 1.6;
        }
        speed = clamp(speed, MIN_SPEED, MAX_SPEED);
        double[] bestVelocity = new double[]{
            speed * dx / distance,
            speed * dy / distance
        };

        double[] bestPosition = simulateClosestPosition(ballPos, targetPos, bestVelocity, f, trees);
        double bestError = distanceToTarget(bestPosition, targetPos);
        double step = 1.0;

        for (int i = 0; i < 80 && bestError > TARGET_TOLERANCE; i++) {
            boolean improved = false;

            double[][] candidates = {
                {bestVelocity[0] + step, bestVelocity[1]},
                {bestVelocity[0] - step, bestVelocity[1]},
                {bestVelocity[0], bestVelocity[1] + step},
                {bestVelocity[0], bestVelocity[1] - step},
                {bestVelocity[0] + step, bestVelocity[1] + step},
                {bestVelocity[0] + step, bestVelocity[1] - step},
                {bestVelocity[0] - step, bestVelocity[1] + step},
                {bestVelocity[0] - step, bestVelocity[1] - step}
            };

            for (double[] candidate : candidates) {
                candidate = limitSpeed(candidate);
                double[] closest = simulateClosestPosition(ballPos, targetPos, candidate, f, trees);
                double error = distanceToTarget(closest, targetPos);
                if (error < bestError) {
                    bestError = error;
                    bestVelocity = candidate;
                    improved = true;
                }
            }
            if (!improved) {
                step *= 0.5;
            }
            if (step < 0.001) {
                break;
            }
        }
        return bestVelocity;
    }

// we assume there are no trees, can be implemented later
    public double[] estimateTargetPosition(double[] ballPos,
                                           double[] targetPos,
                                           double miuK,
                                           double g,
                                           ODEFunction f) {
        return estimateTargetPosition(ballPos, targetPos, miuK, g, f, null);
    }

    public double[] estimateTargetPosition(double[] ballPos,
                                           double[] targetPos,
                                           double miuK,
                                           double g,
                                           ODEFunction f,
                                           List<double[]> trees) {
        double[] velocity = calculateVelocity(ballPos, targetPos, miuK, g, f, trees);
        return simulateClosestPosition(ballPos, targetPos, velocity, f, trees);
    }

    public SolverResult playGame(double[] ballPos,
                                 double[] targetPos,
                                 double miuK,
                                 double g,
                                 ODEFunction f) {
        return playGame(ballPos, targetPos, miuK, g, f, null);
    }

    public SolverResult playGame(double[] ballPos,
                                 double[] targetPos,
                                 double miuK,
                                 double g,
                                 ODEFunction f,
                                 List<double[]> trees) {
        double[] velocity = calculateVelocity(ballPos, targetPos, miuK, g, f, trees);

        double[] state = new double[]{
            ballPos[0],
            ballPos[1],
            velocity[0],
            velocity[1]
        };

        double tEnd = estimateTime(ballPos, targetPos, velocity);

        return simulateUntilStop(state, tEnd, f, trees);
    }

    private double[] simulateClosestPosition(double[] ballPos,
                                             double[] targetPos,
                                             double[] velocity,
                                             ODEFunction f,
                                             List<double[]> trees) {
        double[] state = new double[]{
            ballPos[0],
            ballPos[1],
            velocity[0],
            velocity[1]
        };

        double tEnd = estimateTime(ballPos, targetPos, velocity);
        SolverResult result = simulateUntilStop(state, tEnd, f, trees);

        return findClosestState(result, targetPos);
    }

    private SolverResult simulateUntilStop(double[] initialState,
                                           double tEnd,
                                           ODEFunction f,
                                           List<double[]> trees) {
        ArrayList<Double> times = new ArrayList<>();
        ArrayList<double[]> states = new ArrayList<>();

        double time = 0.0;
        double[] state = initialState.clone();

        times.add(time);
        states.add(state.clone());

        int steps = (int) Math.floor(tEnd / STEP_SIZE);

        for (int i = 0; i < steps; i++) {
            state = solver.step(f, time, state, STEP_SIZE);
            time += STEP_SIZE;

            times.add(time);
            states.add(state.clone());

            if (Collision_Detector.isInWater(state, f)) {
                break;
            }

            if (Collision_Detector.hitTree(state, trees)) {
                break;
            }

            double speed = Math.hypot(state[2], state[3]);
            if (speed < 0.01) {
                break;
            }
        }

        double[] timeArray = new double[times.size()];
        double[][] stateArray = new double[states.size()][];

        for (int i = 0; i < times.size(); i++) {
            timeArray[i] = times.get(i);
            stateArray[i] = states.get(i);
        }

        return new SolverResult(timeArray, stateArray);
    }

    private double[] findClosestState(SolverResult result, double[] targetPos) {
        double[] closest = result.states[0];
        double bestDistance = distanceToTarget(closest, targetPos);

        for (double[] state : result.states) {
            double currentDistance = distanceToTarget(state, targetPos);

            if (currentDistance < bestDistance) {
                bestDistance = currentDistance;
                closest = state;
            }
        }

        return new double[]{
            closest[0],
            closest[1]
        };
    }

    private double estimateTime(double[] ballPos, double[] targetPos, double[] velocity) {
        double distance = Math.hypot(
            targetPos[0] - ballPos[0],
            targetPos[1] - ballPos[1]
        );

        double speed = Math.hypot(velocity[0], velocity[1]);

        if (speed < 1e-6) {
            return 2.0;
        }

        return Math.max(1.0, Math.min((distance / speed) * 2.25, 15.0));
    }

    private double distanceToTarget(double[] state, double[] targetPos) {
        return Math.hypot(
            state[0] - targetPos[0],
            state[1] - targetPos[1]
        );
    }

    private double[] limitSpeed(double[] velocity) {
        double speed = Math.hypot(velocity[0], velocity[1]);
        if (speed > MAX_SPEED) {
            return new double[]{
                velocity[0] / speed * MAX_SPEED,
                velocity[1] / speed * MAX_SPEED
            };
        }
        if (speed < MIN_SPEED && speed > 1e-9) {
            return new double[]{
                velocity[0] / speed * MIN_SPEED,
                velocity[1] / speed * MIN_SPEED
            };
        }
        return velocity;
    }
    // helper method
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}