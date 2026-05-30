package UI;

import Objects.ObstacleContainer;
import Objects.SandPit;
import Objects.Tree;
import com.ken06.solvers.function.FunctionEvaluator;

import java.util.List;

public class Course {
    public String heightFunction;
    public FunctionEvaluator.CompiledFunction compiledHeight;
    public double muK, muS;
    public double x0, y0;
    public double xt, yt;
    public double r;
    public double sandMuK, sandMuS;
    public double[][] sandInterval; 
    public List<double[]> trees;

    /**
     * Creates a Course object containing the physical and geographical properties of the golf hole.
     *
     * @param heightFunction the mathematical expression defining the terrain's height (e.g. "0.25*sin((x+y)/10)+1")
     * @param muK            the kinetic friction coefficient for the standard grass
     * @param muS            the static friction coefficient for the standard grass
     * @param x0             the starting x-coordinate of the golf ball
     * @param y0             the starting y-coordinate of the golf ball
     * @param xt             the x-coordinate of the target hole
     * @param yt             the y-coordinate of the target hole
     * @param r              the radius of the target hole
     * @param sandMuK        the kinetic friction coefficient applied when the ball is in a sand pit
     * @param sandMuS        the static friction coefficient applied when the ball is in a sand pit
     * @param sandInterval   a 2D array defining the rectangular boundaries of the sand pit
     * @param trees          a list of arrays containing the x-coordinate, y-coordinate, and radius of each tree obstacle
     */
    public Course(String heightFunction, double muK, double muS,
                  double x0, double y0,
                  double xt, double yt, double r, double sandMuK, double sandMuS, double[][] sandInterval, List<double[]> trees) {

        this.heightFunction = heightFunction;
        this.compiledHeight = FunctionEvaluator.compile(heightFunction);
        this.muK = muK;
        this.muS = muS;
        this.x0 = x0;
        this.y0 = y0;
        this.xt = xt;
        this.yt = yt;
        this.r = r;
        this.sandMuK = sandMuK;
        this.sandMuS = sandMuS;
        this.sandInterval = sandInterval;
        this.trees = trees;
        ObstacleContainer container = ObstacleContainer.getInstance();
        container.reset();
        //populate obstacle container

        for(double[] tree : trees){
            container.addTree(new Tree(tree));
        }
        //maybe add a list of sandIntervals
        container.addSandPit(new SandPit(sandInterval));

        //add wall loop
        /*
        for(double[] wall : walls){
            container.addWall(new Wall(wall));
        }
         */

    }
}