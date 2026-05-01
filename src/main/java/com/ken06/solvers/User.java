package com.ken06.solvers;

public class User {
  private double[] position;
  private final ODESolver solver;
  private final ODEFunction green;
  private int moveCount;
  public User(double[] startingPosition,ODEFunction green,double friction, double[][] sandInterval){
      this.position = startingPosition;
      this.green = green;
      this.moveCount = 0;
      this.solver = new RK4Solver(green,friction,sandInterval,"User"); //creates the RK4 solver to give the specified penalty stroke
  }

    /** move the ball based on given velocity to a new position
     *  this also takes into account when the ball falls into water given an extra penalty stroke and resetting the position
     *  also increment the moveCount for each shot
     * @param velocity the speed of the x and y coordinate
     * @param stepsize how accurate the new position should be evaluated (can later be replaced with a hard coded small stepsize)
     */
    public void move(double[] velocity,double stepsize) {
        double[] newPosition = solver.solve(velocity,position,stepsize);
        if (newPosition == position) moveCount++; //receive an extra penalty if it falls into the water
        this.position = newPosition;
        moveCount++;

    }

    /** checks if the ball is in the hole based on the target position and its radius
     *
     * @param targetPosition position of the hole (x,y)
     * @param radius values around the hole
     * @return whether ball is in the hole
     */
    public boolean isInHole(double[] targetPosition, double radius){
        double distanceToHole = Math.hypot(
                position[0] - targetPosition[0],
                position[1] - targetPosition[1]
        );
        if (distanceToHole <= radius){
            return true;
        }
        return false;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public double[] getPosition() {
        return position;
    }

}
