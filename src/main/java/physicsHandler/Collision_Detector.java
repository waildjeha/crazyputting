package physicsHandler;
import Objects.ObstacleContainer;
import Objects.SandPit;
import Objects.Tree;
import Objects.Wall;
import com.ken06.solvers.function.ODEFunction;

import java.util.List;
public class Collision_Detector{
    private final static ObstacleContainer container = ObstacleContainer.getInstance();
/**
 * Determines if the ball has fallen into a water hazard (height less than zero).
 *
 * @param state the current state array containing the ball's x and y position
 * @return      true if the ball's position results in a negative height, false otherwise
 */
public static boolean isInWater(double[] state) {
    ODEFunction green = container.getGreen();
    double currentZ = green.evaluateHeight(new double[]{state[0], state[1]});

    return currentZ < 0;
}
    /**
     * Checks if the ball has collided with any trees on the course.
     * trees a list of trees defined by their x-coordinate, y-coordinate, and radius
     * @param state the current state array containing the ball's x and y position
     * @return      true if the ball intersects with a tree's physical space, false otherwise
     */
    public static boolean hitTree(double[] state) {
        List<Tree> trees = container.getTrees();
        if (trees == null || trees.isEmpty()) return false;

        double ballX = state[0];
        double ballY = state[1];
        double ballRadius = 0.021;

        for (Tree tree : trees) {
            double treeX = tree.getX();
            double treeY = tree.getY();
            double treeRadius = tree.getSize();

            double distance = Math.hypot(ballX - treeX, ballY - treeY);

            if (distance <= (treeRadius + ballRadius)) {
                System.out.println("Thwack! Your ball hit a tree at (" + treeX + ", " + treeY + ")");
                return true;
            }
        }

        return false;
    }
    /**
     * Checks if the ball has collided with any wall on the course.
     * walls a list of walls defined by their start, end, width and anchorpoint
     * @param state the current state array containing the ball's x and y position
     * @return      true if the ball intersects with a wall's physical space, false otherwise
     */
    public static boolean hitWall(double[] state){
        List<Wall> walls = container.getWalls();

        if (walls == null || walls.isEmpty()) return false;

        double ballX = state[0];
        double ballY = state[1];
        double ballRadius = 0.021;

        for (Wall wall : walls){
            double width = wall.getWidth();
            double start = wall.getStart();
            double end = wall.getEnd();
            double anchorPoint = wall.getAnchorPoint();



            if (wall.getType().equals("Y")){
                if ((ballY - ballRadius) <= end && (ballY + ballRadius) >= start){
                    if ((ballX + ballRadius) >= (anchorPoint - width) && (ballX - ballRadius) <= (anchorPoint + width)){
                        return true;
                    }
                }
            }else{
                if ((ballX - ballRadius) <= end && (ballX + ballRadius) >= start){
                    if ((ballY + ballRadius) >= (anchorPoint - width) && (ballY - ballRadius) <= (anchorPoint + width)){
                        return true;
                    }
                }
            }
        }

        return false;
    }
    /**
     * Checks if the ball is currently within the boundaries of a sand pit.
     *
     * @param position the current x and y coordinates of the ball
     * @return         true if the ball is in the sand, false otherwise
     */
    public static boolean isInSand(double[] position){
        List<SandPit> sandPits = container.getSandPits();

        if (sandPits == null || sandPits.isEmpty()) return false;

        double x = position[0];
        double y = position[1];
        for(SandPit sandpit : sandPits) {
            double[][] sand_interval = sandpit.getInterval();

            if (sand_interval != null) {
                if (x >= sand_interval[0][0] && x <= sand_interval[0][1]) {
                    if (y >= sand_interval[1][0] && y <= sand_interval[1][1]) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
    public static boolean hitAnything(double[] position){
        return isInSand(position) || isInWater(position) || hitWall(position) || hitTree(position);
    }
}
