package path_planning;

import com.ken06.solvers.function.ODEFunction;
import physicsHandler.Collision_Detector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class A_star {
    private final ODEFunction green;
    private double[][] grid;
    private final double cellSize;

    private double minX = 0;
    private double minY = 0;

    public A_star(ODEFunction green,int boundaries,double cellSize){
        this.green = green;
        this.cellSize = cellSize;

        int amountOfGrid = (int) Math.ceil(boundaries / cellSize);
        grid = new double[amountOfGrid][amountOfGrid];
    }

    public void populateGrid(){
        for (int i = 0;i < grid.length;i++) {
            for (int j = 0; j < grid.length; j++) {

                //formula for calculating the actual position of the grid
                double x = minX + (i * cellSize) + (cellSize / 2);
                double y = minY + (j * cellSize) + (cellSize / 2);

                double[] position = {x,y};

                if (Collision_Detector.isInWater(position ,green)
                || Collision_Detector.hitTree(position ,null)
                || Collision_Detector.hitWall(position ,null)) {
                        grid[i][j] = Double.POSITIVE_INFINITY;
                } else {
                        double cost = evaluateCost(position);
                        grid[i][j] = cost;
                }
            }
        }
    }
    public int getGridIndexX(double actualX) {
        return (int) ((actualX - minX) / cellSize);
    }

    public int getGridIndexY(double actualY) {
        return (int) ((actualY - minY) / cellSize);
    }
    private double evaluateCost(double[] position){

        double epsilon = 0.001;
        double cost = 1;

        double x = position[0];
        double y = position[1];

        // Evaluate heights
        double height = green.evaluateHeight(position);
        double alteredHeightX = green.evaluateHeight(new double[]{x + epsilon, y});
        double alteredHeightY = green.evaluateHeight(new double[]{x, y + epsilon});

        double slopeX = Math.abs(alteredHeightX - height) / epsilon;
        double slopeY = Math.abs(alteredHeightY - height) / epsilon;

        // Take the steepest slope at this point
        double maxSlope = Math.max(slopeX, slopeY);

        // Add the slope to the cost.
        // You can multiply maxSlope by a tuning factor (e.g., 2.0) if you want A* to avoid hills more aggressively.
        cost += maxSlope;

        if (Collision_Detector.isInSand(position , null)){
            cost += 4;
        }
        return cost;
    }
    private double calculateDirection(int currentX, int currentY, int targetX, int targetY) {

        double distantX = currentX - targetX;
        double distantY = currentY - targetY;

        return Math.sqrt(distantX * distantX + distantY * distantY) * cellSize;
    }
    public List<Node> findPath(int startX, int startY, int targetX, int targetY){
        //setting objects
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        boolean[][] closedSet = new boolean[grid.length][grid.length];
        Node[][] allNodes = new Node[grid.length][grid.length];

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

            for (int[] direction : directions){
                int neighborX = direction[0];
                int neighborY = direction[1];

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

                // new starting cost = current starting Cost + terrain penalty + base distance
                double newStartingCost = current.startCost + grid[neighborX][neighborY] + (distanceToNeighbor * cellSize);

                if (newStartingCost < neighbor.startCost) {
                    neighbor.parent = current;
                    neighbor.startCost = newStartingCost;
                    neighbor.endCost = calculateDirection(neighborX, neighborY, targetX, targetY);
                    neighbor.totalCost = neighbor.startCost + neighbor.endCost;

                    if (!openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return null;
    }
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
}
