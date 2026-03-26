import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Controller {

    private Stage mainWindow;

    public void setMainWindow(Stage mainWindow){
        this.mainWindow = mainWindow;
    }

    @FXML
    private ChoiceBox<String> solverChoiceBox;

    @FXML
    private Spinner<Double> stepSizeSpinner;

    @FXML
    private Spinner<Double> integTimeSpinner;

    @FXML
    private Spinner<Double> startPosSpinner;

    @FXML
    private ChoiceBox<String> systemChoiceBox;

     @FXML
    private VBox lotkaVBox;

    @FXML
    private VBox fitzVBox;

    @FXML
    private VBox sirVBox;
    
    @FXML
    public void initialize() {

        solverChoiceBox.getItems().addAll("Euler", "RK4");

        SpinnerValueFactory<Double> stepFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01, 1.0, 0.1, 0.01);
        stepSizeSpinner.setValueFactory(stepFactory);

        SpinnerValueFactory<Double> timeFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(0.01, 100, 1, 0.01);
        integTimeSpinner.setValueFactory(timeFactory);

        SpinnerValueFactory<Double> posFactory = new SpinnerValueFactory.DoubleSpinnerValueFactory(-100.0, 100.0, 0.0, 1.0);
        startPosSpinner.setValueFactory(posFactory);

        systemChoiceBox.getItems().addAll("Lotka-Volterra", "FitzHugh-Nagumo", "SIR");

        lotkaVBox.setVisible(false);
        fitzVBox.setVisible(false);
        sirVBox.setVisible(false);


        systemChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
        
            lotkaVBox.setVisible(false);
            fitzVBox.setVisible(false);
            sirVBox.setVisible(false);

            if (newVal.equals("Lotka-Volterra")) {
                lotkaVBox.setVisible(true);
            } else if (newVal.equals("FitzHugh-Nagumo")) {
                fitzVBox.setVisible(true);
            } else if (newVal.equals("SIR")) {
                sirVBox.setVisible(true);
            }

        });

        
    }

}
