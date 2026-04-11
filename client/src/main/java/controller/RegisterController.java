package controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import network.ApiClient;
import util.SceneManager;

import java.util.Optional;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private Label messageLabel;
    @FXML private Button registerButton;

    @FXML
    public void register() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Wypełnij wszystkie pola.");
            return;
        }

        if (!password.equals(confirm)) {
            messageLabel.setText("Hasła nie są identyczne.");
            return;
        }

        Task<Optional<String>> task = new Task<>() {
            @Override
            protected Optional<String> call() throws Exception {
                return ApiClient.getInstance().register(name,email,password);
            }
        };

        task.setOnSucceeded(e -> {
            registerButton.setDisable(false);

            if (task.getValue().isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Konto utworzone. Możesz się zalogować.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText(task.getValue().get());
            }
        });

        task.setOnFailed(e -> {
            registerButton.setDisable(false);
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Błąd połączenia z serwerem.");
        });

        registerButton.setDisable(true);
        new Thread(task).start();
    }

    @FXML
    public void goToLogin() {
        SceneManager.getInstance().switchTo("/view/LoginView.fxml","System Kina");
    }
}