package controller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Session;
import network.ApiClient;
import util.SceneManager;

public class LoginController {

    @FXML private TextField credentialField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    @FXML
    public void login() {
        String credential = credentialField.getText().trim();
        String password = passwordField.getText();

        if (credential.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Wypełnij wszystkie pola.");
            return;
        }

        Task<ApiClient.LoginResult> task = new Task<>() {
            @Override
            protected ApiClient.LoginResult call() throws Exception {
                return ApiClient.getInstance().login(credential,password).orElse(null);
            }
        };

        task.setOnSucceeded(e -> {
            setDisabled(false);
            ApiClient.LoginResult result = task.getValue();
            if (result != null) {
                Session.login(result.user(), result.token());
                System.out.println("TOKEN: " + Session.getToken()); // debug
                SceneManager.getInstance().switchTo("/view/MainView.fxml", "System Kina");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Błędne dane logowania.");
            }
        });

        task.setOnFailed(e -> {
            setDisabled(false);
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Błąd połączenia z serwerem.");
        });

        setDisabled(true);
        new Thread(task).start();
    }

    @FXML
    public void goToRegister() {
        SceneManager.getInstance().switchTo("/view/RegisterView.fxml","System Kina - Rejestracja");
    }

    private void setDisabled(boolean d) {
        loginButton.setDisable(d);
        registerButton.setDisable(d);
        credentialField.setDisable(d);
        passwordField.setDisable(d);
    }
}