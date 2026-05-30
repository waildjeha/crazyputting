package Objects;

public class Tree implements Obstacles{
    private double xPosition;
    private double yPosition;

    private double size; //how big it is

    public Tree(double[] messurements){

        xPosition = messurements[0];
        yPosition = messurements[1];
        size = messurements[2];
    }

    public double getSize() {
        return size;
    }

    public double getX() {
        return xPosition;
    }

    public double getY() {
        return yPosition;
    }
}
