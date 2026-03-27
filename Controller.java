import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Controller {

    private Stage mainWindow;

    public void setMainWindow(Stage mainWindow){
        this.mainWindow = mainWindow;
    }

    @FXML private ChoiceBox<String> solverChoiceBox;

    @FXML private TextField stepSizeTextField;
    @FXML private TextField integrationTimeTextField;

    @FXML private ChoiceBox<String> systemChoiceBox;

    @FXML private VBox lotkaVBox;

    @FXML private TextField x0TextField;
    @FXML private TextField y0TextField;
    @FXML private TextField alphaTextField;
    @FXML private TextField betaTextField;
    @FXML private TextField gammaTextField;
    @FXML private TextField deltaTextField;

    @FXML private VBox fitzVBox;

    @FXML private TextField v0TextField;
    @FXML private TextField w0TextField;
    @FXML private TextField iextTextField;
    @FXML private TextField aTextField;
    @FXML private TextField bTextField;
    @FXML private TextField epsilonTextField;

    @FXML private VBox sirVBox;

    @FXML private TextField s0TextField;
    @FXML private TextField i0TextField;
    @FXML private TextField r0TextField;
    @FXML private TextField kTextField;
    @FXML private TextField gamma2TextField;
    @FXML private TextField muTextField;

    @FXML private ImageView eulerImageView;
    @FXML private ImageView rungeKuttaImageView;
    @FXML private ImageView lotkaVolterraImageView;
    @FXML private ImageView fitzHughImageView;
    @FXML private ImageView sirImageView;

    @FXML private Label stepSizeError;
    @FXML private Label integTimeError;
    @FXML private Label x0Error;
    @FXML private Label y0Error;
    @FXML private Label alphaError;
    @FXML private Label betaError;
    @FXML private Label gammaError;
    @FXML private Label deltaError;
    @FXML private Label v0Error;
    @FXML private Label w0Error;
    @FXML private Label iextError;
    @FXML private Label aError;
    @FXML private Label bError;
    @FXML private Label epsilonError;
    @FXML private Label s0Error;
    @FXML private Label i0Error;
    @FXML private Label r0Error;
    @FXML private Label kError;
    @FXML private Label gamma2Error;
    @FXML private Label muError;
    
    @FXML
    public void initialize() {

        stepSizeError.setVisible(false);
        integTimeError.setVisible(false);
        x0Error.setVisible(false);
        y0Error.setVisible(false);
        alphaError.setVisible(false);
        betaError.setVisible(false);
        gammaError.setVisible(false);
        deltaError.setVisible(false);
        v0Error.setVisible(false);
        w0Error.setVisible(false);
        iextError.setVisible(false);
        aError.setVisible(false);
        bError.setVisible(false);
        epsilonError.setVisible(false);
        s0Error.setVisible(false);
        i0Error.setVisible(false);
        r0Error.setVisible(false);
        kError.setVisible(false);
        gamma2Error.setVisible(false);
        muError.setVisible(false);

        solverChoiceBox.getItems().addAll("Euler", "RK4");
        solverChoiceBox.getSelectionModel().selectFirst();

        eulerImageView.setVisible(true);
        rungeKuttaImageView.setVisible(false);

        solverChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            eulerImageView.setVisible(false);
            rungeKuttaImageView.setVisible(false);

            if (newVal.equals("Euler")){
                eulerImageView.setVisible(true);
            } else if (newVal.equals("RK4")){
                rungeKuttaImageView.setVisible(true);
            }

        });

        systemChoiceBox.getItems().addAll("Lotka-Volterra", "FitzHugh-Nagumo", "SIR");
        systemChoiceBox.getSelectionModel().selectFirst();

        lotkaVBox.setVisible(true);
        fitzVBox.setVisible(false);
        sirVBox.setVisible(false);

        lotkaVBox.setManaged(true);
        fitzVBox.setManaged(false);
        sirVBox.setManaged(false);

        lotkaVolterraImageView.setVisible(true);
        fitzHughImageView.setVisible(false);
        sirImageView.setVisible(false);

        systemChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {

            lotkaVBox.setVisible(false);
            fitzVBox.setVisible(false);
            sirVBox.setVisible(false);

            lotkaVBox.setManaged(false);
            fitzVBox.setManaged(false);
            sirVBox.setManaged(false);

            lotkaVolterraImageView.setVisible(false);
            fitzHughImageView.setVisible(false);
            sirImageView.setVisible(false);

            if (newVal.equals("Lotka-Volterra")) {
                lotkaVBox.setVisible(true);
                lotkaVBox.setManaged(true);
                lotkaVolterraImageView.setVisible(true);
            } else if (newVal.equals("FitzHugh-Nagumo")) {
                fitzVBox.setVisible(true);
                fitzVBox.setManaged(true);
                fitzHughImageView.setVisible(true);
            } else if (newVal.equals("SIR")) {
                sirVBox.setVisible(true);
                sirVBox.setManaged(true);
                sirImageView.setVisible(true);
            }

        });

        
    }

    /**
     * Checks if a TextField has a valid number. 
     * Formats it to a double and handles the red error styling.
     * * @return true if valid, false if invalid
     */
    private boolean validateDoubleField(TextField field, Label errorLabel, boolean allowNegative) {
        String text = field.getText().trim();

        try {
            // Try parsing. If they typed "5", it becomes 5.0 in memory.
            double value = Double.parseDouble(text);


            if(!allowNegative && value < 0){
                field.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                errorLabel.setText("Must be positive");
                errorLabel.setVisible(true);
                return false;
            }

            // Visually update the field so "5" shows as "5.0"
            if (!text.contains(".")) {
                field.setText(text + ".0");
            }

            // Input is good! Clear any red error styles.
            field.setStyle(""); 
            errorLabel.setText("");
            errorLabel.setVisible(false);
            return true;

        } catch (NumberFormatException e) {
            // Input is bad! Turn the field red.
            field.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
            errorLabel.setText("Invalid number");
            errorLabel.setVisible(true);
            return false;
        }
    }

    @FXML
    public void handleRunButtonClick() {

        boolean formatValid = validateDoubleField(stepSizeTextField, stepSizeError, false) & validateDoubleField(integrationTimeTextField, integTimeError, false);

        String selectedSystem = systemChoiceBox.getValue();
        String selectedSolver = solverChoiceBox.getValue();

        boolean logicValid = true;

        if (formatValid){

            double h = Double.parseDouble(stepSizeTextField.getText());
            double time = Double.parseDouble(integrationTimeTextField.getText());

            double error = 1e-9; 
            double remainder = time % h;

            if(remainder > error && Math.abs(remainder - h) > error){
                stepSizeTextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                stepSizeError.setText("Must be a divider of (t)");
                stepSizeError.setVisible(true);
                logicValid = false;
            } else if(h > time){
                stepSizeTextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                stepSizeError.setText("Must be smaller than (t)");
                stepSizeError.setVisible(true);
                logicValid = false;
            }

        }

        boolean systemValid = false;

        if (selectedSystem.equals("Lotka-Volterra")) {
            systemValid = validateDoubleField(x0TextField, x0Error, false)
                      & validateDoubleField(y0TextField, y0Error,false)
                      & validateDoubleField(alphaTextField, alphaError, false) 
                      & validateDoubleField(betaTextField, betaError, false) 
                      & validateDoubleField(gammaTextField, gammaError, false) 
                      & validateDoubleField(deltaTextField, deltaError, false);
            if (systemValid){
                double x0 = Double.parseDouble(x0TextField.getText());
                double y0 = Double.parseDouble(y0TextField.getText());
                double alpha = Double.parseDouble(alphaTextField.getText());
                double beta = Double.parseDouble(betaTextField.getText());
                double gamma = Double.parseDouble(gammaTextField.getText());
                double delta = Double.parseDouble(deltaTextField.getText());
            }

        } else if (selectedSystem.equals("FitzHugh-Nagumo")) {
            systemValid = validateDoubleField(v0TextField, v0Error, true)
                      & validateDoubleField(w0TextField, w0Error, true)
                      & validateDoubleField(iextTextField, iextError, true)
                      & validateDoubleField(aTextField, aError, false) 
                      & validateDoubleField(bTextField, bError, false) 
                      & validateDoubleField(epsilonTextField, epsilonError, false);

            if(systemValid){
                double v0 = Double.parseDouble(v0TextField.getText()); 
                double w0 = Double.parseDouble(w0TextField.getText());
                double iext = Double.parseDouble(iextTextField.getText());
                double a = Double.parseDouble(aTextField.getText());
                double b = Double.parseDouble(bTextField.getText());
                double epsilon = Double.parseDouble(epsilonTextField.getText());

                if(epsilon >= 1.0){
                    epsilonTextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                    epsilonError.setText("Must be <= 1");
                    epsilonError.setVisible(true);
                    systemValid = false; 
                }
            }
                      
        } else if (selectedSystem.equals("SIR")) {
            systemValid = validateDoubleField(s0TextField, s0Error, false)
                      & validateDoubleField(i0TextField, i0Error, false)
                      & validateDoubleField(r0TextField, r0Error, false)
                      & validateDoubleField(kTextField, kError, false) 
                      & validateDoubleField(gamma2TextField, gamma2Error, false) 
                      & validateDoubleField(muTextField, muError, false);

            if (systemValid) {
                double s0 = Double.parseDouble(s0TextField.getText());
                double i0 = Double.parseDouble(i0TextField.getText());
                double r0 = Double.parseDouble(r0TextField.getText());
                double k = Double.parseDouble(kTextField.getText());
                double gamma2 = Double.parseDouble(gamma2TextField.getText());
                double mu = Double.parseDouble(muTextField.getText());


                if ((s0 + i0 + r0) > 1.0001) { 
                    s0TextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                    i0TextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                    r0TextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                    s0Error.setText("Sum (S+I+R)");
                    s0Error.setVisible(true);
                    i0Error.setText("must be");
                    i0Error.setVisible(true);
                    r0Error.setText(" <= 1.0");
                    r0Error.setVisible(true);
                    systemValid = false;
                }

                if( k > 5 || k < 1){
                    kTextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                    kError.setText("Must be 1 < k < 5");
                    kError.setVisible(true);
                    systemValid = false;
                }

                if( gamma2 > 5 || gamma2 < 1){
                    gamma2TextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                    gamma2Error.setText("Must be 1 < g < 5");
                    gamma2Error.setVisible(true);
                    systemValid = false;
                }

                if(mu >= 1.0){
                    muTextField.setStyle("-fx-border-color: red; -fx-border-width: 2px; -fx-background-color: #ffcccc;");
                    muError.setText("Must be <= 1");
                    muError.setVisible(true);
                    systemValid = false; 
                }
            }
        }
        
        if (formatValid && logicValid && systemValid) {
            
            if(selectedSystem.equals("Lotka-Volterra")){

                if(selectedSolver.equals("Euler")){
                    //Launch Euler solver 
                } else if(selectedSolver.equals("RK4")){
                    //Launch RK4 solver
                }

            }

           if(selectedSystem.equals("FitzHugh-Nagumo")){
                
                if(selectedSolver.equals("Euler")){
                    //Launch Euler solver 
                } else if(selectedSolver.equals("RK4")){
                    //Launch RK4 solver
                }

            }

           if(selectedSystem.equals("SIR")){
                
                if(selectedSolver.equals("Euler")){
                    //Launch Euler solver 
                } else if(selectedSolver.equals("RK4")){
                    //Launch RK4 solver
                } 

            }
            
        } 

    }

}
