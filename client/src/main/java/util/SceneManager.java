package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SceneManager {
    private static SceneManager instance;
    private Stage primaryStage;
    private final List<Consumer<String>> listeners = new ArrayList<>();

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) instance = new SceneManager();
        return instance;
    }

    public void setPrimaryStage(Stage stage) { this.primaryStage = stage; }

    public void addSceneChangeListener(Consumer<String> l) { listeners.add(l); }

    public void switchTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle(title);
            primaryStage.show();
            listeners.forEach(l -> l.accept(fxmlPath));
        } catch (Exception e) {
            throw new RuntimeException("Błąd ładowania widoku: " + fxmlPath, e);
        }
    }

    public Stage getStage() { return primaryStage; }
}