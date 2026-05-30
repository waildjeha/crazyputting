package path_planning;

import com.ken06.solvers.function.ODEFunction;

import java.util.Arrays;
import java.util.List;

public class TesterA_star {
    static void main() {
        ODEFunction green = new ODEFunction() {
            @Override
            public double[] computeDerivatives(double t, double[] x) {
                double x1 = x[0];
                double y = x[1];

                return new double[]{Math.cos(x1 ),Math.cos(x1 )};
            }

            @Override
            public double evaluateHeight(double[] position) {
                double x = position[0];
                double y = position[1];

                return Math.sin(x );
            }
        };
        double cellSize = 0.5;
        double min = 0;

       GridFactory factory = new GridFactory(green,20,cellSize, min, min);
       factory.populateGrid();
       MicroPlanner micro = new MicroPlanner(factory.getGrid(), cellSize,min);
       List<Node> list = micro.findPath(green,0,0 ,0, 8);
       MacroPlanner macro = new MacroPlanner(green, cellSize, min);
       List<Node> list1 = macro.findWayPoints(list);

       A_star a_Star = new A_star(green,20,cellSize,min);
       double[][] shots = a_Star.calculateShots(new double[]{0,0},new double[]{0, 8});

        ODEFunction testGreen = new RiverTestCourse();
// e.g., 10x10 boundary, 0.5 cell size, starting at 0,0
        A_star aStar = new A_star(testGreen, 10, 0.5, 0.0);

        double[][] shots1 = aStar.calculateShots(new double[]{1.0,5.0},new double[]{9.9, 5.0});

        System.out.println(Arrays.deepToString(shots1));
    }
}
