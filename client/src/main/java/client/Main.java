package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import util.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
        SceneManager.getInstance().setPrimaryStage(primaryStage);
        SceneManager.getInstance().switchTo("/view/LoginView.fxml", "System Kina");
    }

    public static void main(String[] args) {
        launch(args);
    }
}