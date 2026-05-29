package path_planning;

public class Node implements Comparable<Node>{
    public int x, y;          // Grid array indices [i][j]
    public double startCost;      // Cost from start to this node
    public double endCost;      // Estimated cost from this node to the target
    public double totalCost;      // Total cost (g + h)
    public Node parent;       // The node we came from

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        this.endCost = Double.POSITIVE_INFINITY;
        this.totalCost = Double.POSITIVE_INFINITY;
        this.parent = null;
    }

    @Override
    public int compareTo(Node other) {
        return Double.compare(this.totalCost, other.totalCost);
    }
}
