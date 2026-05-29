package UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/golf.fxml"));
        Parent root = loader.load();
        
        // Grab the controller and pass the primary stage
        Controller controller = loader.getController();
        controller.setMainWindow(primaryStage);
        
        // Setup the Phase 2 Window
        primaryStage.setTitle("Project 1-2: Phase 2 - Crazy Putting!"); 
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}