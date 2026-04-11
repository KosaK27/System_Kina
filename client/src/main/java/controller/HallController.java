package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import model.Hall;
import network.ApiClient;
import util.AlertHelper;

import java.util.List;

public class HallController {

    @FXML private TableView<Hall> hallTable;
    @FXML private TableColumn<Hall,Integer> idColumn;
    @FXML private TableColumn<Hall,String> nameColumn;
    @FXML private TableColumn<Hall,Integer> rowsColumn;
    @FXML private TableColumn<Hall,Integer> seatsPerRowColumn;
    @FXML private Label statusLabel;

    private final ObservableList<Hall> hallList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        rowsColumn.setCellValueFactory(new PropertyValueFactory<>("rows"));
        seatsPerRowColumn.setCellValueFactory(new PropertyValueFactory<>("seatsPerRow"));

        SortedList<Hall> sorted = new SortedList<>(hallList);
        sorted.comparatorProperty().bind(hallTable.comparatorProperty());
        hallTable.setItems(sorted);
        hallTable.setPlaceholder(new Label("Brak sal."));
        loadHalls();
    }

    public void loadHalls() {
        statusLabel.setText("Ładowanie...");
        Task<List<Hall>> task = new Task<>() {
            @Override protected List<Hall> call() throws Exception {
                return ApiClient.getInstance().getHalls();
            }
        };
        task.setOnSucceeded(e -> {
            hallList.setAll(task.getValue());
            statusLabel.setText("Sale: " + task.getValue().size());
        });
        task.setOnFailed(e -> statusLabel.setText("Błąd połączenia."));
        new Thread(task).start();
    }

    @FXML public void addHall() { showForm(null); }

    @FXML
    public void editHall() {
        Hall sel = hallTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertHelper.error("Wybierz salę.");
            return;
        }
        showForm(sel);
    }

    @FXML
    public void deleteHall() {
        Hall sel = hallTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertHelper.error("Wybierz salę.");
            return;
        }
        if (!AlertHelper.confirm("Usunąć salę \"" + sel.getName() + "\"?")) return;

        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().deleteHall(sel.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadHalls());
        task.setOnFailed(e -> AlertHelper.error("Błąd połączenia."));
        new Thread(task).start();
    }

    private void showForm(Hall hall) {
        Dialog<Hall> dialog = new Dialog<>();
        dialog.setTitle(hall == null ? "Dodaj salę" : "Edytuj salę");

        ButtonType saveBtn = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);

        TextField nameField = new TextField(hall != null ? hall.getName() : "");
        Spinner<Integer> rowsSpinner = new Spinner<>(1,20,hall != null ? hall.getRows() : 6);
        Spinner<Integer> seatsSpinner = new Spinner<>(1,30,hall != null ? hall.getSeatsPerRow() : 10);

        rowsSpinner.setEditable(true);
        seatsSpinner.setEditable(true);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);

        grid.add(new Label("Nazwa:"),0,0);
        grid.add(nameField,1,0);
        grid.add(new Label("Rzędy:"),0,1);
        grid.add(rowsSpinner,1,1);
        grid.add(new Label("Miejsc w rzędzie:"),0,2);
        grid.add(seatsSpinner,1,2);
        grid.add(errorLabel,0,3,2,1);

        dialog.getDialogPane().setContent(grid);

        Button save = (Button) dialog.getDialogPane().lookupButton(saveBtn);
        save.addEventFilter(javafx.event.ActionEvent.ACTION,e -> {
            if (nameField.getText().isBlank()) {
                errorLabel.setText("Nazwa nie może być pusta.");
                e.consume();
            }
        });

        dialog.setResultConverter(btn ->
                btn != saveBtn
                        ? null
                        : new Hall(
                        hall != null ? hall.getId() : 0,
                        nameField.getText().trim(),
                        rowsSpinner.getValue(),
                        seatsSpinner.getValue()
                )
        );

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    ApiClient.getInstance().saveHall(result)
                            .ifPresent(err -> { throw new RuntimeException(err); });
                    return null;
                }
            };
            task.setOnSucceeded(e -> loadHalls());
            task.setOnFailed(e -> AlertHelper.error(e.getSource().getException().getMessage()));
            new Thread(task).start();
        });
    }
}