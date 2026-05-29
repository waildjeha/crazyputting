package com.ken06.solvers;

public class User {

    private double[] position;
    private int moveCount;

    /**
     * Initializes a new user player, setting the starting position.
     * Notice we removed the solvers and physics engine from here!
     *
     * @param startingPosition the initial x and y coordinates of the golf ball
     */
    public User(double[] startingPosition){
        this.position = startingPosition.clone();
        this.moveCount = 0;
    }

    /**
     * Updates the ball's position. The physics math is now handled externally.
     */
    public void setPosition(double[] newPosition) {
        this.position = newPosition.clone();
    }

    /**
     * Adds a standard stroke to the score.
     */
    public void addStroke() {
        this.moveCount++;
    }

    /**
     * Adds a penalty for hitting water.
     */
    public void addWaterPenalty() {
        System.out.println("Splash! Penalty stroke added.");
        this.moveCount += 2;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public double[] getPosition() {
        return position;
    }
}