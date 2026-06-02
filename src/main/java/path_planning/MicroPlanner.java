package path_planning;

import Objects.ObstacleContainer;
import com.ken06.solvers.function.ODEFunction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class MicroPlanner {
    private final double[][] grid;
    private final double cellSize;
    private final double minX;
    private final double minY;


    public MicroPlanner(double[][] grid, double cellSize, double min){
        this.cellSize = cellSize;
        this.grid = grid;

        this.minX = min;
        this.minY = min;
    }

    /** compass inside the findPath method to guide the loop into the right direction
     *
     * @param currentX the x-index on the grid where the ball currently sits
     * @param currentY the y-index on the grid where the ball starts
     * @param targetX the x-index on the grid where the ball needs to go
     * @param targetY the y-index on the grid where the ball needs to go
     * @return        the distance from the current position to the hole
     */
    private double calculateDirection(int currentX, int currentY, int targetX, int targetY) {

        double distantX = currentX - targetX;
        double distantY = currentY - targetY;

        return Math.sqrt(distantX * distantX + distantY * distantY) * cellSize;
    }

    /** finds the path following each grid cell to determine the quickest way / the lowest cost to reach the target
     *
     * @param startX the x-index on the grid where the ball starts
     * @param startY the y-index on the grid where the ball starts
     * @param targetX the x-index on the grid where the ball needs to go
     * @param targetY the y-index on the grid where the ball needs to go
     * @return        a list of nodes which are sorted in order form the starting node to the last node (where we reach the target)
     */
    public List<Node> findPath(int startX, int startY, int targetX, int targetY){
        //setting objects
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[grid.length][grid.length];
        Node[][] allNodes = new Node[grid.length][grid.length];

        ObstacleContainer container = ObstacleContainer.getInstance();
        ODEFunction green = container.getGreen();

        if (startX == targetX && startY == targetY) {
            return new ArrayList<>();
        }

        //populating allNodes-object
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                allNodes[i][j] = new Node(i, j);
            }
        }
        //getting the first node and setting its values right
        Node startNode = allNodes[startX][startY];
        startNode.startCost = 0;
        startNode.endCost = calculateDirection(startX, startY, targetX, targetY);
        startNode.totalCost = startNode.startCost + startNode.endCost;
        openSet.add(startNode);

        //list of all possible directions
        int[][] directions = { {0,1}, {1,0}, {0,-1}, {-1,0}, {1,1}, {1,-1}, {-1,1}, {-1,-1} };

        while(!openSet.isEmpty()){

            Node current = openSet.poll();

            if(current.x == targetX && current.y == targetY){
                return retracePath(startNode, current);
            }

            closedSet[current.x][current.y] = true;

            //loop through all directions
            for (int[] direction : directions){
                int neighborX = current.x + direction[0];
                int neighborY = current.y + direction[1];

                if (neighborX < 0 || neighborX >= grid.length
                        || neighborY < 0 || neighborY >= grid.length){
                    continue;
                }
                if(grid[neighborX][neighborY] == Double.POSITIVE_INFINITY
                        || closedSet[neighborX][neighborY]){
                    continue;
                }

                Node neighbor = allNodes[neighborX][neighborY];

                double distanceToNeighbor;
                if (direction[0] != 0 && direction[1] != 0){
                    //move 1 x and 1 y
                    distanceToNeighbor = 1.414;
                } else{
                    //move on a line
                    distanceToNeighbor = 1.0;
                }
                //evaluate cost based on steepness

                //Get the heights of the two cells
                double currentHeight = green.evaluateHeight(new double[]{
                        calculateRealValueX(current), calculateRealValueY(current)
                });
                double neighborHeight = green.evaluateHeight(new double[]{
                        calculateRealValueX(neighbor), calculateRealValueY(neighbor)
                });

                //Calculate the directional height difference
                double heightDifference = neighborHeight - currentHeight;

                //Evaluate the slope penalty dynamically
                double slopePenalty = 0;

                if (heightDifference > 0) {
                    // Going UPHILL: penalize the cost based on steepness.
                    slopePenalty = heightDifference * 10.0;

                    if (heightDifference > 1.25) {
                        continue; // Too steep to climb, skip this neighbor.
                    }
                } else if (heightDifference < 0) {
                    // Going DOWNHILL: Gravity does the work.
                    slopePenalty = 0.0;
                }

                //Calculate the final new starting cost
                double newStartingCost = current.startCost
                        + grid[neighborX][neighborY] // Sand penalty from GridFactory
                        + (distanceToNeighbor * cellSize)
                        + slopePenalty;

                if (newStartingCost < neighbor.startCost) {
                    neighbor.parent = current;
                    neighbor.startCost = newStartingCost;
                    neighbor.endCost = calculateDirection(neighborX, neighborY, targetX, targetY);
                    neighbor.totalCost = neighbor.startCost + neighbor.endCost;

                    // Force PriorityQueue to re-sort by removing and re-adding
                    if (openSet.contains(neighbor)) {
                        openSet.remove(neighbor);
                    }
                    openSet.add(neighbor);
                }
            }
        }
        return null;
    }

    /** retraces all the steps found in the findPath method
     *
     * @param startNode the first Node (starting node)
     * @param endNode the last Node (target node)
     * @return  a list of all the Nodes that are connected from the start to the end node
     */
    private List<Node> retracePath(Node startNode, Node endNode){
        List<Node> path = new ArrayList<>();
        Node current = endNode;

        while (current != startNode) {
            path.add(current);
            current = current.parent;
        }
        Collections.reverse(path);

        return path;
    }
    /** retrieves the real value on the course based on the indices given
     *
     * @param value indexes on the grid (i, j)
     * @return      the real value on the course of X
     */
    private double calculateRealValueX(Node value){
        return minX + (value.x * cellSize) + (cellSize / 2.0);
    }

    /**
     * retrieves the real value on the course based on the indices given
     *
     * @param value indexes on the grid (i, j)
     * @return      the real value on the course of Y
     */
    private double calculateRealValueY(Node value){
        return minY + (value.y * cellSize) + (cellSize / 2.0);
    }
}
