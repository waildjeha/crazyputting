package path_planning;

import com.ken06.solvers.function.ODEFunction;
import physicsHandler.Collision_Detector;

import java.util.ArrayList;
import java.util.List;

public class MacroPlanner {
    private final ODEFunction green;
    private final double cellSize;
    private final double minX;
    private final double minY;

    public MacroPlanner(ODEFunction green,double cellSize, double minX){
        this.green = green;
        this.cellSize = cellSize;
        this.minY = minX;
        this.minX = minX;

    }

    /** retrieves only the necessary Nodes instead of all the nodes connected on the path
     * (since some nodes can be skipped in a shot)
     * @param allPathPoints all the nodes found previously which connect the starting node to the end node
     * @return          the smallest amount of nodes that connect the start to the end
     */
    public List<Node> findWayPoints(List<Node> allPathPoints){
        List<Node> waypoints = new ArrayList<>();

        // Safety check
        if (allPathPoints == null || allPathPoints.size() < 2) {
            return waypoints;
        }
        Node currentShot = allPathPoints.getFirst();

        //loop
        for (int i = 1; i < allPathPoints.size(); i++) {
            Node nextShot = allPathPoints.get(i);

            // If the line is blocked, the FURTHEST safe shot was the previous node
            if (anyObstacles(currentShot, nextShot)) {

                // Using allPathPoints.get(i - 1) is safer than nextShot.parent
                // because it guarantees list sequence order.
                Node safeWaypoint = allPathPoints.get(i - 1);
                waypoints.add(safeWaypoint);

                // Advance our starting position to this new waypoint
                currentShot = safeWaypoint;
            }
        }

        waypoints.add(allPathPoints.getLast());

        return waypoints;
    }

    /** Determines if in the line of sight from the current node to the target node are any obstacles
     *
     * @param start node to start the search from
     * @param target node to go to from the start
     * @return      if an obstacles is in the line from start to target
     */
    private boolean anyObstacles(Node start, Node target) {
        //convert into real values
        double startX = minX + (start.x * cellSize) + (cellSize / 2.0);
        double startY = minY + (start.y * cellSize) + (cellSize / 2.0);
        double targetX = minX + (target.x * cellSize) + (cellSize / 2.0);
        double targetY = minY + (target.y * cellSize) + (cellSize / 2.0);

        // Find the total distance of the line
        double distanceX = targetX - startX;
        double distanceY = targetY - startY;
        double distance = Math.sqrt(distanceX * distanceX + distanceY * distanceY);

        // Calculate how many samples to take.
        int amount = (int) Math.ceil(distance / (cellSize / 2.0));

        if (amount <= 1) return false; // Adjacent cells are already assumed safe by A*

        double xStep = distanceX / amount;
        double yStep = distanceY / amount;

        for (int j = 1; j < amount; j++) {

            double[] position = {startX + (xStep * j), startY + (yStep * j)};

            if (Collision_Detector.hitAnything(position, green)) {
                return true; // Line of sight broken
            }
        }

        return false; // Clear line of sight
    }
}
