package Objects;

import java.util.ArrayList;
import java.util.List;

public class ObstacleContainer {
    private static List<SandPit> sandPits;
    private static List<Tree> trees;
    private static List<Wall> walls;
    private static ObstacleContainer instance;

    /** Singleton class so only one object of this class is accessible in the whole program
     *
     */
    private ObstacleContainer(){
    }

    /** gets the singleton instance of the ObstacleContainer class
     *
     * @return      the instance of this class object
     */
    public static ObstacleContainer getInstance(){
        //if object hasn't been instantiated create it
        if (instance == null){
            instance = new ObstacleContainer();
            sandPits = new ArrayList<>();
            trees = new ArrayList<>();
            walls = new ArrayList<>();
        }
        return instance;
    }

    public List<SandPit> getSandPits() {
        return sandPits;
    }

    public List<Tree> getTrees() {
        return trees;
    }

    public List<Wall> getWalls() {
        return walls;
    }
    public void addSandPit(SandPit sandPit){
        sandPits.add(sandPit);
    }
    public void addTree(Tree tree){
        trees.add(tree);
    }
    public void addWall(Wall wall){
        walls.add(wall);
    }

    /**
     * resets all the list of objects stored
     * this method is called whenever a new course is created
     */
    public void reset(){
        sandPits = new ArrayList<>();
        trees = new ArrayList<>();
        walls = new ArrayList<>();
    }
}
