package UI;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseReader {

    /**
     * Reads a text file line by line and extracts the settings to build a golf course.
     *
     * @param filename  the path to the text file containing the course data (e.g. "course.txt")
     * @return          a fully initialized Course object ready for the simulation
     */
    public static Course readCourse(String filename) {
        String h = "0"; 
        double muK = 0.0, muS = 0.0;
        double x0 = 0.0, y0 = 0.0;
        double xt = 0.0, yt = 0.0;
        double r = 0.0;
        
        double sandMuK = 0.2, sandMuS = 0.3; 
        
        double[][] sandInterval = null; 
        List<double[]> trees = new ArrayList<>(); 

        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }

                String[] parts = line.split("=");
                if (parts.length != 2) continue; 

                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "h": h = value; break;
                    case "muK": muK = Double.parseDouble(value); break;
                    case "muS": muS = Double.parseDouble(value); break;
                    case "x0": x0 = Double.parseDouble(value); break;
                    case "y0": y0 = Double.parseDouble(value); break;
                    case "xt": xt = Double.parseDouble(value); break;
                    case "yt": yt = Double.parseDouble(value); break;
                    case "r": r = Double.parseDouble(value); break;
                    
                    case "sandMuK": sandMuK = Double.parseDouble(value); break;
                    case "sandMuS": sandMuS = Double.parseDouble(value); break;
                    
                    case "sand":
                        String[] sParts = value.split(",");
                        if (sParts.length == 4) {
                            double xMin = Double.parseDouble(sParts[0].trim());
                            double xMax = Double.parseDouble(sParts[1].trim());
                            double yMin = Double.parseDouble(sParts[2].trim());
                            double yMax = Double.parseDouble(sParts[3].trim());
                            sandInterval = new double[][]{ {xMin, xMax}, {yMin, yMax} };
                        }
                        break;
                        
                    case "tree":
                        String[] tParts = value.split(",");
                        if (tParts.length == 3) {
                            double tx = Double.parseDouble(tParts[0].trim());
                            double ty = Double.parseDouble(tParts[1].trim());
                            double tr = Double.parseDouble(tParts[2].trim());
                            trees.add(new double[]{tx, ty, tr});
                        }
                        break;
                }
            }

        } catch (IOException | NumberFormatException e) {
            System.err.println("Error reading the course file: " + e.getMessage());
            e.printStackTrace();
        }

        return new Course(h, muK, muS, x0, y0, xt, yt, r, sandMuK, sandMuS, sandInterval, trees);
    }
}