package Objects;

import java.util.List;

public class Wall implements Obstacles {
    private double start;   //starting point of the wall
    private double end;     //stoping point of the wall
    private double width; //means in both directions (width = 2 -> -2 & +2)
    private String XorY;    //decides if the wall is parallel to the x-axis or the y-axis
    private double anchorPoint; //the coordinate of the x, y value that walks parallel to the axis

    public Wall(double start, double end,double width, String XorY,double anchorPoint){
        this.start = start;
        this.end = end;
        this.width = width;
        this.XorY = XorY.toUpperCase();
        this.anchorPoint = anchorPoint;
    }

    public double getEnd() {
        return end;
    }

    public double getStart() {
        return start;
    }

    public double getWidth() {
        return width;
    }
    public String getType(){
        return XorY;
    }

    public double getAnchorPoint() {
        return anchorPoint;
    }
}
