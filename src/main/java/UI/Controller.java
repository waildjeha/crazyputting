package UI;

import java.util.ArrayList;
import java.util.List;

import bots.MachineBot;
import com.ken06.solvers.function.FunctionEvaluator;
import com.ken06.solvers.function.ODEFunction;
import com.ken06.solvers.rk4.RK4Solver;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import physicsHandler.Collision_Detector;
import physicsHandler.PhysicsEngine;

import java.io.File;

public class Controller {

    private Stage mainWindow;

    @FXML private TextField heightFunctionInput;
    @FXML private TextField kineticInput;
    @FXML private TextField staticInput;
    @FXML private TextField startX;
    @FXML private TextField startY;
    @FXML private TextField targetX;
    @FXML private TextField targetY;
    @FXML private TextField targetRadius;
    @FXML private Button loadButton;
    @FXML private TextField velocityX;
    @FXML private TextField velocityY;
    @FXML private Button launchButton;
    @FXML private Label statsLabel;
    @FXML private TextField sandXMin;
    @FXML private TextField sandXMax;
    @FXML private TextField sandYMin;
    @FXML private TextField sandYMax;
    @FXML private TextField sandKineticInput;
    @FXML private TextField sandStaticInput;
    @FXML private TextArea treeTextArea;
    @FXML private Canvas terrainCanvas;
    @FXML private Canvas actionCanvas;

    private double scale = 20.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private static final double MAX_SPEED = 5.0;

    private Course currentCourse;
    private double currentBallX;
    private double currentBallY;
    private int shotCount = 0;

    private javafx.scene.image.Image treeImage = new javafx.scene.image.Image(getClass().getResource("/tree.png").toExternalForm());

    /**
     * Sets the main application window for this controller so it can manage the stage.
     * 
     * @param mainWindow the main stage of the JavaFX application
     */
    public void setMainWindow(Stage mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Runs automatically when the UI loads. Sets up a default course so the screen
     * isn't blank on startup.
     */
    @FXML
    public void initialize() {

        // Loading a basic golf course for the launch
        currentCourse = new Course("0.25*sin((x+y)/10)+1", 0.08, 0.2, 7.0, 8.0, 14.0, 1.0, 0.1, 0.2, 0.3, null, null);
        currentBallX = currentCourse.x0; 
        currentBallY = currentCourse.y0; 

        velocityX.setPromptText("Max total: 5.0");
        velocityY.setPromptText("Max total: 5.0");

        calculateDynamicScale(currentCourse);
        drawTerrain();
        drawActionLayer(currentBallX, currentBallY, currentCourse.xt, currentCourse.yt, currentCourse.r); 
    }

    /**
     * Reads a text field to see if the user entered a valid number.
     * 
     * @param field         the text field to validate
     * @param allowNegative whether negative numbers are acceptable for this specific input
     * @return              true if the field contains a valid number, false otherwise
     */
    private boolean validateDoubleField(TextField field, boolean allowNegative) {
        if (field == null) return false; 
        String text = field.getText().trim();
        
        try {
            double value = Double.parseDouble(text);
            if (!allowNegative && value < 0) {
                showFieldError(field, "Must be positive");
                return false;
            }
            field.setStyle(""); 
            return true;
        } catch (NumberFormatException e) {
            showFieldError(field, "Invalid number");
            return false;
        }
    }

    /**
     * Highlights a text box in red and shows a warning message when the user types something wrong.
     * 
     * @param field   the text field that contains the error
     * @param message the warning message to display inside the box
     */
    private void showFieldError(TextField field, String message) {
        field.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
        field.clear(); 
        field.setPromptText(message); 
        
        field.setOnMouseClicked(event -> {
            field.setStyle("");
            field.setPromptText("");
        });
    }

    /**
     * Gathers all the data typed into the left panel, validates it, and generates a new golf course.
     */
    @FXML
    public void handleLoadCourse() {
        boolean isValid = validateDoubleField(kineticInput, false)
                        & validateDoubleField(staticInput, false)
                        & validateDoubleField(startX, false)
                        & validateDoubleField(startY, false)
                        & validateDoubleField(targetX, false)
                        & validateDoubleField(targetY, false)
                        & validateDoubleField(targetRadius, false);

        if (isValid) {
            String hFunction = heightFunctionInput.getText();
            
            // Validate the math formula
            try {
                FunctionEvaluator.compile(hFunction);
            } catch (RuntimeException e) {
                showFieldError(heightFunctionInput, "Invalid Formula");
                return; 
            }

            double muK = Double.parseDouble(kineticInput.getText());
            double muS = Double.parseDouble(staticInput.getText());
            double x0 = Double.parseDouble(startX.getText());
            double y0 = Double.parseDouble(startY.getText());
            double xt = Double.parseDouble(targetX.getText());
            double yt = Double.parseDouble(targetY.getText());
            double r = Double.parseDouble(targetRadius.getText());

            // Check for restrictions
            boolean rulesPassed = true;

            if (muK < 0.05 || muK > 0.1) {
                showFieldError(kineticInput, "0.05 to 0.1");
                rulesPassed = false;
            }
            if (muS < 0.1 || muS > 0.2) {
                showFieldError(staticInput, "0.1 to 0.2");
                rulesPassed = false;
            }
            if (muS <= muK) {
                showFieldError(staticInput, "Must be > muK");
                rulesPassed = false;
            }
            if (r < 0.05 || r > 0.15) {
                showFieldError(targetRadius, "0.05 to 0.15");
                rulesPassed = false;
            }

            // Stop loading if basic rules failed
            if (!rulesPassed) return;

            // Sand variables and restrictions
            double[][] sandInterval = null; 
            double sandMuK = 0.2; 
            double sandMuS = 0.3;
            
            if (sandXMin != null && !sandXMin.getText().trim().isEmpty()) {
                try {
                    double xMin = Double.parseDouble(sandXMin.getText());
                    double xMax = Double.parseDouble(sandXMax.getText());
                    double yMin = Double.parseDouble(sandYMin.getText());
                    double yMax = Double.parseDouble(sandYMax.getText());
                    sandInterval = new double[][]{ {xMin, xMax}, {yMin, yMax} };

                    if (sandKineticInput != null && !sandKineticInput.getText().trim().isEmpty()) {
                        sandMuK = Double.parseDouble(sandKineticInput.getText());
                        if (sandMuK <= muK || sandMuK >= 1.0) {
                            showFieldError(sandKineticInput, "Must be > grass muK and < 1.0");
                            rulesPassed = false;
                        }
                    }
                    
                    if (sandStaticInput != null && !sandStaticInput.getText().trim().isEmpty()) {
                        sandMuS = Double.parseDouble(sandStaticInput.getText());
                        if (sandMuS <= sandMuK || sandMuS >= 1.0 || sandMuS <= muS) {
                            showFieldError(sandStaticInput, "Must be > sand muK, > grass muS, and < 1.0");
                            rulesPassed = false;
                        }
                    }

                } catch (NumberFormatException e) {
                    // Ignore empty or malformed optional fields
                }
            }

            // Stop loading if sand rules failed
            if (!rulesPassed) return;

            // Tree 0bstacles
            List<double[]> trees = new ArrayList<>(); 
            if (treeTextArea != null && !treeTextArea.getText().trim().isEmpty()) {
                String[] treeLines = treeTextArea.getText().trim().split("\n");
                for (String line : treeLines) {
                    String[] tParts = line.split(",");
                    if (tParts.length == 3) {
                        try {
                            double tx = Double.parseDouble(tParts[0].trim());
                            double ty = Double.parseDouble(tParts[1].trim());
                            double tr = Double.parseDouble(tParts[2].trim());
                            trees.add(new double[]{tx, ty, tr});
                        } catch (NumberFormatException e) {
                        }
                    }
                }
            }

            // Creating the course
            currentCourse = new Course(hFunction, muK, muS, x0, y0, xt, yt, r, sandMuK, sandMuS, sandInterval, trees);

            calculateDynamicScale(currentCourse);

            currentBallX = currentCourse.x0;
            currentBallY = currentCourse.y0;
            shotCount = 0;
            updateStats(currentBallX, currentBallY);

            drawTerrain(); 
            drawActionLayer(currentBallX, currentBallY, xt, yt, r);
        }
    }

    @FXML
    public void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Course Text File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        
        File selectedFile = fileChooser.showOpenDialog(mainWindow);

        if (selectedFile != null) {
            // Using CourseReader class to do the reading
            Course loadedCourse = CourseReader.readCourse(selectedFile.getAbsolutePath());

            if (loadedCourse != null) {
                currentCourse = loadedCourse;

                // Put the numbers into the text boxes 
                heightFunctionInput.setText(currentCourse.heightFunction);
                kineticInput.setText(String.valueOf(currentCourse.muK));
                staticInput.setText(String.valueOf(currentCourse.muS));
                startX.setText(String.valueOf(currentCourse.x0));
                startY.setText(String.valueOf(currentCourse.y0));
                targetX.setText(String.valueOf(currentCourse.xt));
                targetY.setText(String.valueOf(currentCourse.yt));
                targetRadius.setText(String.valueOf(currentCourse.r));

                // Clear any lingering red error styles from previous mistakes!
                heightFunctionInput.setStyle("");
                kineticInput.setStyle("");
                staticInput.setStyle("");
                startX.setStyle("");
                startY.setStyle("");
                targetX.setStyle("");
                targetY.setStyle("");
                targetRadius.setStyle("");

                // Reset the game state to the new start position
                currentBallX = currentCourse.x0;
                currentBallY = currentCourse.y0;
                shotCount = 0;
                updateStats(currentBallX, currentBallY);

                // Re-center the camera and draw the new map
                calculateDynamicScale(currentCourse);
                drawTerrain(); 
                drawActionLayer(currentBallX, currentBallY, currentCourse.xt, currentCourse.yt, currentCourse.r);
            }
        }
    }

    /**
     * Reads the chosen velocity, enforces game rules, simulates the physics over time,
     * and updates the ball's resting position on the canvas.
     */
    @FXML
    public void handleShoot() {

        // Checking if the inputs are correct
        boolean isValid = validateDoubleField(velocityX, true)
                        & validateDoubleField(velocityY, true);

        if (isValid) {
            double v0x = Double.parseDouble(velocityX.getText());
            double v0y = Double.parseDouble(velocityY.getText());

            double speed = Math.hypot(v0x, v0y);

            // Check for Maximum speed
            if (speed > MAX_SPEED) {
                String errorMsg = String.format("Total: %.1f > 5", speed);
                showFieldError(velocityX, errorMsg);
                showFieldError(velocityY, errorMsg);
                return;
            }

            launchButton.setDisable(true);

            ODEFunction courseODE = new ODEFunction() {
                
                @Override
                public double evaluateHeight(double[] position) {
                    return currentCourse.compiledHeight.eval(position[0], position[1]);
                }

                @Override
                public double[] computeDerivatives(double t, double[] position) {
                    double x = position[0];
                    double y = position[1];
                    double delta = 1e-5; 

                    double hxPlus = currentCourse.compiledHeight.eval(x + delta, y);
                    double hxMinus = currentCourse.compiledHeight.eval(x - delta, y);
                    double dhdx = (hxPlus - hxMinus) / (2 * delta);

                    double hyPlus = currentCourse.compiledHeight.eval(x, y + delta);
                    double hyMinus = currentCourse.compiledHeight.eval(x, y - delta);
                    double dhdy = (hyPlus - hyMinus) / (2 * delta);

                    return new double[]{dhdx, dhdy};
                }
            };

            PhysicsEngine engine = new PhysicsEngine(currentCourse.muK, currentCourse.muS, currentCourse.sandMuK, currentCourse.sandMuS, currentCourse.sandInterval);
            RK4Solver solver = new RK4Solver(courseODE, engine);

            final double[] state = {currentBallX, currentBallY, v0x, v0y};
            final double[] time = {0.0};
            final double stepSize = 0.01;

            double xt = Double.parseDouble(targetX.getText());
            double yt = Double.parseDouble(targetY.getText());
            double r = Double.parseDouble(targetRadius.getText());

            AnimationTimer timer = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Calculate the slope at the current position
                    double[] slope = courseODE.computeDerivatives(time[0], state);

                    // Check if the ball should keep moving
                    if (engine.isMoving(state, slope)) {
                        
                        // To speed up the visual animation, calculate a few physics steps per visual frame
                        for (int i = 0; i < 5; i++) {
                            double[] nextState = solver.step(courseODE, time[0], state, stepSize);
                            System.arraycopy(nextState, 0, state, 0, 4); // Update state array

                            double ballPhysicalRadius = 0.021; 
                            if (currentCourse.trees != null) {
                                for (double[] tree : currentCourse.trees) {
                                    double tx = tree[0];
                                    double ty = tree[1];
                                    double tr = tree[2];

                                    // Calculate distance between ball center and tree center
                                    double dx = state[0] - tx;
                                    double dy = state[1] - ty;
                                    double distance = Math.hypot(dx, dy);

                                    // If it hits the tree, penalize them and reset
                                    if (distance < ballPhysicalRadius + tr) {
                                        System.out.println("Bonk! Hit a tree.");
                                        
                                        this.stop(); // Stop the animation
                                        
                                        // 1 stroke for the penalty (matches water)
                                        shotCount += 1; 
                                        
                                        // Redraw the ball at the previous safe position (currentBallX/Y)
                                        updateStats(currentBallX, currentBallY); 
                                        drawActionLayer(currentBallX, currentBallY, xt, yt, r);
                                        
                                        launchButton.setDisable(false); // Re-enable the cannon
                                        return; // Break out of the physics loop completely
                                    }
                                }
                            }

                            time[0] += stepSize;
                            
                            // Check for water mid-step
                            if (Collision_Detector.isInWater(state)) {
                                this.stop();
                                System.out.println("Splash!");
                                shotCount += 1; // Penalty
                                updateStats(currentBallX, currentBallY); // Keep previous safe position
                                drawActionLayer(currentBallX, currentBallY, xt, yt, r);
                                launchButton.setDisable(false);
                                return;
                            }
                        }

                        keepBallInView(state[0], state[1]);
                    
                        // Redraw the frame
                        drawActionLayer(state[0],state[1], xt, yt, r);

                    } else {
                        // End the animation because the ball stopped.
                        this.stop();
                        shotCount++;

                        currentBallX = state[0];
                        currentBallY = state[1];


                        updateStats(currentBallX, currentBallY);
                        drawActionLayer(currentBallX, currentBallY, xt, yt, r);
                        launchButton.setDisable(false); // Let the user shoot again

                        double distance = Math.hypot(currentBallX - xt, currentBallY - yt);
                    
                        // If the distance is less than or equal to the target radius, they won
                        if (distance <= r) {
                            javafx.application.Platform.runLater(() -> {
                                javafx.scene.control.Alert winAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                                winAlert.setTitle("Hole in One!");
                                winAlert.setHeaderText("Congratulations! You hit the target!");
                                
                                if (shotCount == 1) {
                                    winAlert.setContentText("Incredible! You got a Hole in One!");
                                } else {
                                    winAlert.setContentText("Great job! You reached the target in " + shotCount + " shots.");
                                }
                                
                                // Show the popup
                                winAlert.showAndWait();
                            });
                        }
                    }
                }
            };

            timer.start();

        }
    }

    @FXML
    public void handleMLBotTest() {
        if (currentCourse == null) return;
// Create the ODE Function for the bot to read
        ODEFunction courseODE = new ODEFunction() {
            @Override
            public double evaluateHeight(double[] position) {
                return currentCourse.compiledHeight.eval(position[0], position[1]);
            }

            @Override
            public double[] computeDerivatives(double t, double[] position) {
                double x = position[0];
                double y = position[1];
                double delta = 1e-5;
                double dhdx = (currentCourse.compiledHeight.eval(x + delta, y) - currentCourse.compiledHeight.eval(x - delta, y)) / (2 * delta);
                double dhdy = (currentCourse.compiledHeight.eval(x, y + delta) - currentCourse.compiledHeight.eval(x, y - delta)) / (2 * delta);
                return new double[]{dhdx, dhdy};
            }
        };

// Setup the bot
        RK4Solver solver = new RK4Solver(courseODE, new PhysicsEngine(currentCourse.muK, currentCourse.muS, currentCourse.sandMuK, currentCourse.sandMuS, currentCourse.sandInterval));
        MachineBot bot = new MachineBot(solver);

        double[] startPos = {currentBallX, currentBallY};
        double[] targetPos = {currentCourse.xt, currentCourse.yt};

// Safe initial guess
        double dx = targetPos[0] - startPos[0];
        double dy = targetPos[1] - startPos[1];
        double distance = Math.hypot(dx, dy);
        if (distance == 0) return;
        double guessSpeed = Math.min(distance * 0.5, 5.0);
        double[] initialGuess = { (dx / distance) * guessSpeed, (dy / distance) * guessSpeed };

        // 1. Disable the button so the user doesn't spam it while the bot is thinking
        launchButton.setDisable(true);
        statsLabel.setText("Bot is calculating... please wait.");

        // 2. Create a Background Task for the heavy lifting
        javafx.concurrent.Task<double[]> mlTask = new javafx.concurrent.Task<>() {
            @Override
            protected double[] call() throws Exception {
                // This runs on a separate thread, safely away from the UI!
                return bot.holeInOneMachine(
                        initialGuess,
                        startPos,
                        targetPos,
                        currentCourse.r,
                        1e-5,
                        0.01
                );
            }
        };

        // 3. What to do when the bot succeeds
        mlTask.setOnSucceeded(event -> {
            launchButton.setDisable(false); // Turn button back on
            updateStats(currentBallX, currentBallY); // Reset label

            double[] bestVelocity = mlTask.getValue();
            double totalSpeed = Math.hypot(bestVelocity[0], bestVelocity[1]);

            if (totalSpeed > 5.0) {
                javafx.scene.control.Alert botAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                botAlert.setTitle("ML Bot Result");
                botAlert.setHeaderText("Hole-in-One Impossible!");
                botAlert.setContentText(String.format("The bot calculated that a hole-in-one requires a total speed of %.2f m/s, which exceeds the 5.0 m/s limit.", totalSpeed));
                botAlert.showAndWait();
            } else {
                // Success alert!
                javafx.scene.control.Alert botAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                botAlert.setTitle("ML Bot Result");
                botAlert.setHeaderText("Optimal Velocity Calculated!");
                botAlert.setContentText(String.format("X Velocity: %.3f\nY Velocity: %.3f\n\nI have pasted these into the input boxes for you!", bestVelocity[0], bestVelocity[1]));
                botAlert.showAndWait();

                velocityX.setText(String.format(java.util.Locale.US, "%.3f", bestVelocity[0]));
                velocityY.setText(String.format(java.util.Locale.US, "%.3f", bestVelocity[1]));
            }
        });

        // 4. What to do if the math blows up or throws an error
        mlTask.setOnFailed(event -> {
            launchButton.setDisable(false);
            updateStats(currentBallX, currentBallY);

            Throwable error = mlTask.getException();
            javafx.scene.control.Alert botAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            botAlert.setTitle("Bot Error");
            botAlert.setHeaderText("Calculation Failed");
            botAlert.setContentText("The ML bot crashed during calculation: " + error.getMessage());
            botAlert.showAndWait();

            error.printStackTrace(); // Print the actual error to the terminal
        });

        // 5. Fire up the background thread!
        new Thread(mlTask).start();
    }

    /**
     * Paints the background grid pixel by pixel, calculating heights to decide
     * if a coordinate should be grass, sand, or water.
     */
    private void drawTerrain() {
        if (terrainCanvas == null || currentCourse == null) return;
        
        GraphicsContext gc = terrainCanvas.getGraphicsContext2D();
        int width = (int) terrainCanvas.getWidth();
        int height = (int) terrainCanvas.getHeight();

        // Load textures
        Image grassTex = new Image(getClass().getResource("/grass.png").toExternalForm());
        Image sandTex = new Image(getClass().getResource("/sand.png").toExternalForm());
        Image waterTex = new Image(getClass().getResource("/water.png").toExternalForm());

        // Create PixelReaders for textures
        javafx.scene.image.PixelReader grassReader = grassTex.getPixelReader();
        javafx.scene.image.PixelReader sandReader = sandTex.getPixelReader();
        javafx.scene.image.PixelReader waterReader = waterTex.getPixelReader();

        int grassW = (int) grassTex.getWidth();
        int grassH = (int) grassTex.getHeight();
        int sandW = (int) sandTex.getWidth();
        int sandH = (int) sandTex.getHeight();
        int waterW = (int) waterTex.getWidth();
        int waterH = (int) waterTex.getHeight();

        javafx.scene.image.PixelWriter pw = gc.getPixelWriter();
        
        try {

            double texturePixelsPerMeter = 1000.0; //Scale for texture

            for (int px = 0; px < width; px++) {
                for (int py = 0; py < height; py++) {
                    
                    // Convert screen pixels back to math coordinates
                    double mathX = (px - (width / 2.0)) / scale + offsetX;
                    double mathY = (py - (height / 2.0)) / scale + offsetY;

                    double z = currentCourse.compiledHeight.eval(mathX, mathY);

                    int texXGrass = (int) (((mathX * texturePixelsPerMeter) % grassW + grassW) % grassW);
                    int texYGrass = (int) (((mathY * texturePixelsPerMeter) % grassH + grassH) % grassH);
                    
                    int texXSand = (int) (((mathX * texturePixelsPerMeter) % sandW + sandW) % sandW);
                    int texYSand = (int) (((mathY * texturePixelsPerMeter) % sandH + sandH) % sandH);
                    
                    int texXWater = (int) (((mathX * texturePixelsPerMeter) % waterW + waterW) % waterW);
                    int texYWater = (int) (((mathY * texturePixelsPerMeter) % waterH + waterH) % waterH);

                    if (z < 0) {
                        // Water
                        Color baseWaterColor = waterReader.getColor(texXWater, texYWater);
                        pw.setColor(px, py, baseWaterColor);
                        
                    } else {
                        // Grass or sand
                        Color baseColor;
                        
                        // Check if this specific coordinate falls inside the sand pit
                        boolean inSand = false;
                        if (currentCourse.sandInterval != null) {
                            double minX = currentCourse.sandInterval[0][0];
                            double maxX = currentCourse.sandInterval[0][1];
                            double minY = currentCourse.sandInterval[1][0];
                            double maxY = currentCourse.sandInterval[1][1];
                            
                            if (mathX >= minX && mathX <= maxX && mathY >= minY && mathY <= maxY) {
                                inSand = true;
                            }
                        }

                        // Pull the pixel from the correct texture
                        if (inSand) {
                            baseColor = sandReader.getColor(texXSand, texYSand);
                        } else {
                            baseColor = grassReader.getColor(texXGrass, texYGrass);
                        }

                        // Apply 3D shadowing
                        double brightness = 1.0; // 1.0 is flat lighting (z = 1)
                        if (z > 1.0) {
                            // Highlight the hills (caps at 40% brighter)
                            brightness = Math.min(1.4, 1.0 + (z - 1.0) * 0.15); 
                        } else if (z < 1.0) {
                            // Shadow the valleys (caps at 60% darker)
                            brightness = Math.max(0.4, 1.0 - (1.0 - z) * 0.3); 
                        }
                        
                        // Write the shaded texture pixel to the screen
                        pw.setColor(px, py, baseColor.deriveColor(0, 1.0, brightness, 1.0));
                    }
                }
            }
        } catch (RuntimeException e) {
            showFieldError(heightFunctionInput, "Invalid Formula");
        }
    }

    /**
     * Draws the dynamic elements like the golf ball, target hole, and tree obstacles.
     * 
     * @param ballX   the current mathematical x-coordinate of the ball
     * @param ballY   the current mathematical y-coordinate of the ball
     * @param targetX the mathematical x-coordinate of the hole
     * @param targetY the mathematical y-coordinate of the hole
     * @param targetR the mathematical radius of the hole
     */
    private void drawActionLayer(double ballX, double ballY, double targetX, double targetY, double targetR) {
        if (actionCanvas == null || currentCourse == null) return;

        GraphicsContext gc = actionCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, actionCanvas.getWidth(), actionCanvas.getHeight());

        double width = actionCanvas.getWidth();
        double height = actionCanvas.getHeight();

        double ballPhysicalRadius = 0.021; // reel size of a golf ball

        // Checking if target and/or ball is in water
        boolean targetInWater = isTouchingWater(currentCourse.compiledHeight, targetX, targetY, targetR);
        boolean ballInWater = isTouchingWater(currentCourse.compiledHeight, ballX, ballY, ballPhysicalRadius); 

        // Scalling target
        double screenTargetX = (targetX - offsetX) * scale + (width / 2.0);
        double screenTargetY = (targetY - offsetY) * scale + (height / 2.0);
        double screenTargetR = targetR * scale;
        double visualTargetR = Math.max(screenTargetR, 8.0); 

        // If target is in water -> turns red
        if (targetInWater) {
            gc.setFill(Color.RED);
        } else {
            gc.setFill(Color.BLACK);
        }
        gc.fillOval(screenTargetX - visualTargetR, screenTargetY - visualTargetR, visualTargetR * 2, visualTargetR * 2);

        // Scalling Ball
        double screenBallX = (ballX - offsetX) * scale + (width / 2.0);
        double screenBallY = (ballY - offsetY) * scale + (height / 2.0);

        double screenBallR = ballPhysicalRadius * scale;
        double visualBallR = Math.max(screenBallR * 2.5, 6.0); // Making the image a bit bigger than the "hitbox" of the ball

        // If ball is in water -> turns red
        if (ballInWater) {
            gc.setFill(Color.RED);
        } else {
            gc.setFill(Color.WHITE);
        }
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1.5);
        
        gc.fillOval(screenBallX - visualBallR, screenBallY - visualBallR, visualBallR * 2, visualBallR * 2);
        gc.strokeOval(screenBallX - visualBallR, screenBallY - visualBallR, visualBallR * 2, visualBallR * 2);

        // Creating trees
        if (currentCourse.trees != null) {
            for(double[] treeData : currentCourse.trees){

                double treeMathX = treeData[0];
                double treeMathY = treeData[1];
                double treeR = treeData[2];

                boolean thisTreeInWater = isTouchingWater(currentCourse.compiledHeight, treeMathX, treeMathY, treeR);

                double screenTreeX = (treeMathX - offsetX) * scale + (width / 2.0);
                double screenTreeY = (treeMathY - offsetY) * scale + (height / 2.0);
                double screenTreeR = (treeR * scale) * 3;
                
                if (thisTreeInWater) {
                    gc.setFill(new Color(1.0, 0, 0, 0.5)); // 50% transparent red
                    gc.fillOval(screenTreeX - screenTreeR, screenTreeY - screenTreeR, screenTreeR * 2, screenTreeR * 2);
                }

                if (treeImage != null && !treeImage.isError()) {
                    gc.drawImage(treeImage, 
                                 screenTreeX - screenTreeR, 
                                 screenTreeY - screenTreeR, 
                                 screenTreeR * 2, 
                                 screenTreeR * 2);
                } else {
                    gc.setFill(Color.DARKGREEN);
                    gc.fillOval(screenTreeX - screenTreeR, screenTreeY - screenTreeR, screenTreeR * 2, screenTreeR * 2);
                }
            }
        }
    }

    /**
     * Refreshes the score and position text at the bottom of the screen.
     * 
     * @param finalX the resting x-coordinate of the ball after a shot
     * @param finalY the resting y-coordinate of the ball after a shot
     */
    private void updateStats(double finalX, double finalY) {
        if (statsLabel != null) {
            statsLabel.setText(String.format("Shots: %d | Last Pos: (%.2f, %.2f)", shotCount, finalX, finalY));
        }
    }

    /**
     * Figures out how much to zoom in or out so the ball and hole are both visible on screen.
     * 
     * @param course the loaded course containing the start and target positions
     */
    private void calculateDynamicScale(Course course) {
        if (terrainCanvas == null) return;

        double canvasWidth = terrainCanvas.getWidth();
        double canvasHeight = terrainCanvas.getHeight();

        double padding = Math.max(course.r, 0.5); 

        double minX = Math.min(course.x0, course.xt) - padding;
        double maxX = Math.max(course.x0, course.xt) + padding;
        double minY = Math.min(course.y0, course.yt) - padding;
        double maxY = Math.max(course.y0, course.yt) + padding;

        double boxWidth = maxX - minX;
        double boxHeight = maxY - minY;

        if (boxWidth == 0) boxWidth = 10;
        if (boxHeight == 0) boxHeight = 10;

        boxWidth *= 1.2;
        boxHeight *= 1.2;

        double scaleX = canvasWidth / boxWidth;
        double scaleY = canvasHeight / boxHeight;
        scale = Math.min(scaleX, scaleY);

        offsetX = (minX + maxX) / 2.0;
        offsetY = (minY + maxY) / 2.0;
    }
    
    /**
     * Checks if the center or edges of an object dip below a z-height of 0.
     * 
     * @param hFunction the mathematical height formula of the course
     * @param centerX   the x-coordinate of the object's center
     * @param centerY   the y-coordinate of the object's center
     * @param radius    the physical size of the object
     * @return          true if any part of the object touches water (height < 0), false otherwise
     */
    private boolean isTouchingWater(FunctionEvaluator.CompiledFunction compiledHeight, double centerX, double centerY, double radius) {
        double[][] pointsToCheck = {{centerX, centerY}, {centerX, centerY + radius}, {centerX, centerY - radius}, {centerX + radius, centerY}, {centerX - radius, centerY}};

        for (double[] point : pointsToCheck) {
            try {
                // Evaluate using the compiled function
                double z = compiledHeight.eval(point[0], point[1]);
                if (z < 0) {
                    return true; 
                }
            } catch (RuntimeException e) {
                // ignore
            }
        }
        return false; 
    }

    /**
     * Checks if the ball has rolled off the screen. If it has, it zooms the camera out
     * and redraws the terrain to keep the ball in view.
     */
    private void keepBallInView(double ballX, double ballY) {
        if (actionCanvas == null) return;
        
        double width = actionCanvas.getWidth();
        double height = actionCanvas.getHeight();

        // Calculate where the ball currently is on the screen in pixels
        double screenBallX = (ballX - offsetX) * scale + (width / 2.0);
        double screenBallY = (ballY - offsetY) * scale + (height / 2.0);

        // If the ball gets within 30 pixels of the edge
        double margin = 30.0; 

        if (screenBallX < margin || screenBallX > width - margin ||
            screenBallY < margin || screenBallY > height - margin) {

            // Calculate the current visible math boundaries
            double currentMinX = offsetX - (width / 2.0) / scale;
            double currentMaxX = offsetX + (width / 2.0) / scale;
            double currentMinY = offsetY - (height / 2.0) / scale;
            double currentMaxY = offsetY + (height / 2.0) / scale;

            // Convert the 30 pixel margin back into mathematical distance
            double mathMargin = margin / scale;

            // Expand the boundaries to include the new ball position
            double newMinX = Math.min(currentMinX, ballX - mathMargin);
            double newMaxX = Math.max(currentMaxX, ballX + mathMargin);
            double newMinY = Math.min(currentMinY, ballY - mathMargin);
            double newMaxY = Math.max(currentMaxY, ballY + mathMargin);

            // Add 10% padding so the camera jumps out a bit rather than hugging the ball tightly
            double boxWidth = (newMaxX - newMinX) * 1.1; 
            double boxHeight = (newMaxY - newMinY) * 1.1;

            // Apply the new camera zoom and offset
            scale = Math.min(width / boxWidth, height / boxHeight);
            offsetX = (newMinX + newMaxX) / 2.0;
            offsetY = (newMinY + newMaxY) / 2.0;

            // Because the camera moved, we redraw the terrain background
            drawTerrain();
        }
    }
}