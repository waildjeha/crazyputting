package com.ken06.solvers;

public class Collision_Detector {
    public static boolean isInWater(double[] state,ODEFunction green) {
        //check if height function is negative (and by def. ball is in water)
        double currentZ = green.evaluateHeight(new double[]{state[0], state[1]});
        //is in the water
        if (currentZ < 0) {
            System.out.println("Your ball fell into the water");
            return true;
        }
        return false;
    }
    public static boolean hitTree(){
        return false;
    }
}
