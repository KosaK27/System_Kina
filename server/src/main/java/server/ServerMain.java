package server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import api.*;
import service.DataStore;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import static spark.Spark.*;

public class ServerMain extends Application {

    private static final int PORT = 9999;
    private static final String DATA = "data";

    private static volatile boolean running = false;
    private static TextArea logArea;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Kino - Serwer");
        stage.setOnCloseRequest(e -> {
            if (running) stopServer();
            Platform.exit();
            System.exit(0);
        });

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: monospace; -fx-font-size: 12;");
        VBox.setVgrow(logArea, Priority.ALWAYS);

        Label statusLabel = new Label("● Zatrzymany");
        statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button startBtn = new Button("▶ Start");
        Button stopBtn = new Button("■ Stop");
        Button clearBtn = new Button("Wyczyść log");
        stopBtn.setDisable(true);

        startBtn.setOnAction(e -> {
            startServer();
            statusLabel.setText("● Działa na porcie " + PORT);
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
            startBtn.setDisable(true);
            stopBtn.setDisable(false);
        });

        stopBtn.setOnAction(e -> {
            stopServer();
            statusLabel.setText("● Zatrzymany");
            statusLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
            startBtn.setDisable(false);
            stopBtn.setDisable(true);
        });

        clearBtn.setOnAction(e -> logArea.clear());

        HBox toolbar = new HBox(8, startBtn, stopBtn, clearBtn,
                new Separator(javafx.geometry.Orientation.VERTICAL), statusLabel);
        toolbar.setPadding(new Insets(8));
        toolbar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label footer = new Label("Port: " + PORT + "  |  Dane: " + DATA + "/");
        footer.setPadding(new Insets(4, 8, 4, 8));

        VBox root = new VBox(toolbar, logArea, footer);
        stage.setScene(new Scene(root, 680, 420));
        stage.show();

        log("Serwer gotowy. Kliknij Start aby uruchomić.");
    }

    private void startServer() {
        if (running) return;
        running = true;

        DataStore.getInstance(DATA);

        port(PORT);
        threadPool(8);

        after((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
        });

        options("/*", (req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type,Authorization");
            return "OK";
        });

        new AuthRoutes().register();
        new UserRoutes().register();
        new MovieRoutes().register();
        new HallRoutes().register();
        new ScreeningRoutes().register();
        new ReservationRoutes().register();

        exception(Exception.class, (e, req, res) -> {
            if (!(e instanceof spark.HaltException)) {
                res.type("application/json");
                res.status(500);
                res.body("{\"success\":false,\"message\":\"Błąd serwera.\"}");
                log("ERROR: " + e.getMessage());
            }
        });

        after((req, res) ->
                log(req.requestMethod() + " " + req.pathInfo() + " → " + res.status()));

        awaitInitialization();
        log("Serwer uruchomiony na porcie " + PORT);
    }

    private void stopServer() {
        spark.Spark.stop();
        running = false;
        log("Serwer zatrzymany.");
    }

    public static void log(String msg) {
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String line = "[" + time + "] " + msg + "\n";

        if (logArea != null) {
            Platform.runLater(() -> {
                logArea.appendText(line);
                logArea.setScrollTop(Double.MAX_VALUE);
            });
        } else {
            System.out.print(line);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}