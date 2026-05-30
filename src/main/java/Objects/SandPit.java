package Objects;

public class SandPit implements Obstacles{
    private double[][] interval;

    public SandPit(double[][] sand_interval){
        this.interval = sand_interval;
    }

    public double[][] getInterval() {
        return interval;
    }
}
