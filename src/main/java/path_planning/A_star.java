package path_planning;

import Objects.ObstacleContainer;
import com.ken06.solvers.function.ODEFunction;

import java.util.List;

public class A_star {
    private final GridFactory gridFactory;
    private final MacroPlanner macro;
    private final MicroPlanner micro;

    private final double cellSize;
    private final double minX;
    private final double minY;

    /** path planning algorithm called A*
     *
     * @param boundaries the edges of the grid to produce
     * @param cellSize the resolution of the grid
     * @param min holds min value of the grid always symmetric
     */
    public A_star(int boundaries,double cellSize, double min){

        this.minY = min;
        this.minX = min;
        this.cellSize = cellSize;
        this.gridFactory = new GridFactory(boundaries,cellSize,minX,minY);
        gridFactory.populateGrid();
        this.macro = new MacroPlanner(cellSize, minX);
        this.micro = new MicroPlanner(gridFactory.getGrid(),cellSize,minX);


    }

    /** combines gridFactory, MacroPlanner and MicroPlanner to get the calculated shots necessary to score a hole
     *
     * @param start the real values of the starting coordinate of the ball
     * @param target the real values of the target location
     * @return      the intermediate shots necessary to take to score a hole in one
     */
    public double[][] calculateShots(double[] start, double[] target){

        //check if course has been initialized
        if (!ObstacleContainer.getInstance().isReady()) {
            throw new IllegalStateException("Cannot calculate shots: ObstacleContainer is not fully initialized. Create a Course first.");
        }

        //get grid indices
        int startIndexX =  gridFactory.getGridIndexX(start[0]);
        int startIndexY =  gridFactory.getGridIndexY(start[1]);

        int targetIndexX =  gridFactory.getGridIndexX(target[0]);
        int targetIndexY =  gridFactory.getGridIndexY(target[1]);
        //getting the path of the lowest grids connecting the start to the target
        List<Node> longPath = micro.findPath(startIndexX, startIndexY, targetIndexX, targetIndexY);
        //prunning the longPath to only the waypoints necessary
        List<Node> shortPath = macro.findWayPoints(longPath);

        //if target is on the start
        if (shortPath.isEmpty()) {
            return new double[][] { {target[0], target[1]} };
        }

        double[][] intermediateTargets = new double[shortPath.size()][2];

        int length = shortPath.size()-1;
        //transforming the waypoints into real values usable by the ODE-solvers
        for (int i = 0; i < shortPath.size(); i++) {
            intermediateTargets[i][0] = calculateRealValueX(shortPath.get(i));
            intermediateTargets[i][1] = calculateRealValueY(shortPath.get(i));

        }
        //setting the target to be located directly at the end
        /*
        this is necessary since the calculateRealValue methods return the center of each cell,
        and we want the exact position the target not the center.
         */
        intermediateTargets[length][0] = target[0];
        intermediateTargets[length][1] = target[1];

        return intermediateTargets;
    }

    /** Retrieves the velocity of the intermediate targets
     *
     * @param intermediateTargets the waypoints of the A* search
     * @param start the starting position
     * @return      a 2D array with the estimated guess of the velocity to reach each intermediate target
     */
    public double[][] calculateVelocityBasedOnShots(double[] start,double[][] intermediateTargets){
        double[][] velocity = new double[intermediateTargets.length][intermediateTargets[0].length];

        double xStart = start[0];
        double yStart = start[1];

        for (int i = 0; i < intermediateTargets.length; i++) {
            double[] position = intermediateTargets[i];

            double x = position[0];
            double y = position[1];

            double velocityX = x - xStart;
            double velocityY = y - yStart;

            //safety check
            double speed = Math.sqrt(velocityY * velocityY + velocityX * velocityX);


            if (speed > 5){
                velocityX = velocityX / (speed);
                velocityY = velocityY / (speed);
            }
            velocity[i][0] = velocityX;
            velocity[i][1] = velocityY;

            //update starting location to look at the latest position
            xStart = x;
            yStart = y;
        }

        return velocity;
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
