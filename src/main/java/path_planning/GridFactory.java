package path_planning;

import com.ken06.solvers.function.ODEFunction;
import physicsHandler.Collision_Detector;

public class GridFactory {
    private double[][] grid;
    protected final  double cellSize;

    private final double minX;
    private final double minY;

    /** constructor of the gridFactory
     *
     * @param boundaries the end of the grid for x and y (symmetrical grid)
     * @param cellSize how big each cell is on the grid
     * @param minX the start of the grid for x
     * @param minY the start of the grid for y
     */
    public GridFactory(int boundaries,double cellSize, double minX, double minY){
        this.cellSize = cellSize;

        this.minX = minX;
        this.minY = minY;

        int amountOfGrid = (int) Math.ceil(boundaries / cellSize);
        grid = new double[amountOfGrid][amountOfGrid];
    }

    /** Populates the internally stored grid
     */
    public void populateGrid(){
        for (int i = 0;i < grid.length;i++) {
            for (int j = 0; j < grid.length; j++) {

                //formula for calculating the actual position of the grid
                double x = minX + (i * cellSize) + (cellSize / 2);
                double y = minY + (j * cellSize) + (cellSize / 2);

                double[] position = {x,y};

                if (Collision_Detector.hitAnything(position)) {
                    grid[i][j] = Double.POSITIVE_INFINITY;
                } else {
                    double cost = evaluateCost(position);
                    grid[i][j] = cost;
                }
            }
        }
    }

    /**
     * Retrieves the grid index based on the given X
     * @param actualX the actual value of the state
     * @return        the index on the grid corresponding to the actualX
     */
    public int getGridIndexX(double actualX) {
        return (int) ((actualX - minX) / cellSize);
    }

    /**
     * Retrieves the grid index based on the given X
     * @param actualY the actual value of the state
     * @return        the index on the grid corresponding to the actualY
     */
    public int getGridIndexY(double actualY) {
        return (int) ((actualY - minY) / cellSize);
    }

    /** evaluates the cost of each grid discarding steepness
     *
     * @param position the position of the current grid
     * @return
     */
    private double evaluateCost(double[] position){


        double cost = 1;
/*
        double epsilon = 0.001;
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
 */
        if (Collision_Detector.isInSand(position)){
            cost += 4;
        }
        return cost;
    }
    public double[][] getGrid() {
        return grid;
    }
}
