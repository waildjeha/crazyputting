package physicsHandler;
import com.ken06.solvers.function.ODEFunction;

import java.util.List;
public class Collision_Detector{
/**
 * Determines if the ball has fallen into a water hazard (height less than zero).
 *
 * @param state the current state array containing the ball's x and y position
 * @param green the mathematical function defining the course terrain
 * @return      true if the ball's position results in a negative height, false otherwise
 */
public static boolean isInWater(double[] state, ODEFunction green) {
    double currentZ = green.evaluateHeight(new double[]{state[0], state[1]});

    if (currentZ < 0) {
        System.out.println("Your ball fell into the water");
        return true;
    }
    return false;
}
    /**
     * Checks if the ball has collided with any trees on the course.
     *
     * @param state the current state array containing the ball's x and y position
     * @param trees a list of trees defined by their x-coordinate, y-coordinate, and radius
     * @return      true if the ball intersects with a tree's physical space, false otherwise
     */
    public static boolean hitTree(double[] state, List<double[]> trees) {
        if (trees == null || trees.isEmpty()) return false;

        double ballX = state[0];
        double ballY = state[1];
        double ballRadius = 0.021;

        for (double[] treeData : trees) {
            double treeX = treeData[0];
            double treeY = treeData[1];
            double treeRadius = treeData[2];

            double distance = Math.hypot(ballX - treeX, ballY - treeY);

            if (distance <= (treeRadius + ballRadius)) {
                System.out.println("Thwack! Your ball hit a tree at (" + treeX + ", " + treeY + ")");
                return true;
            }
        }

        return false;
    }
}
