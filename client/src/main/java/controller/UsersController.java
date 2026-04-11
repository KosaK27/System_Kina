package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.User;
import network.ApiClient;
import util.AlertHelper;

import java.util.List;

public class UsersController {

    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User,Integer> userIdCol;
    @FXML private TableColumn<User,String> userNameCol;
    @FXML private TableColumn<User,String> userEmailCol;
    @FXML private TableColumn<User,String> userRoleCol;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private FilteredList<User> filteredList;

    @FXML
    public void initialize() {

        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        userEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        userTable.setRowFactory(tv -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    MainController mc = MainController.getInstance();
                    if (mc != null) mc.switchToAllReservationsFilteredByUser(row.getItem().getName());
                }
            });
            return row;
        });

        filteredList = new FilteredList<>(userList,u -> true);
        SortedList<User> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(userTable.comparatorProperty());
        userTable.setItems(sorted);
        userTable.setPlaceholder(new Label("Brak użytkowników."));

        loadUsers();
    }

    public void loadUsers() {

        statusLabel.setText("Ładowanie...");

        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return ApiClient.getInstance().getUsers();
            }
        };

        task.setOnSucceeded(e -> {
            userList.setAll(task.getValue());
            statusLabel.setText("Użytkownicy: " + task.getValue().size());
        });

        task.setOnFailed(e -> statusLabel.setText("Błąd połączenia."));
        new Thread(task).start();
    }

    @FXML
    public void search() {

        String keyword = searchField.getText().toLowerCase();

        filteredList.setPredicate(u ->
                keyword.isEmpty()
                        || u.getName().toLowerCase().contains(keyword)
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword))
                        || u.getRole().toLowerCase().contains(keyword)
        );
    }

    @FXML
    public void viewReservations() {

        User sel = userTable.getSelectionModel().getSelectedItem();

        if (sel == null) {
            AlertHelper.error("Wybierz użytkownika.");
            return;
        }

        MainController mc = MainController.getInstance();
        if (mc != null) mc.switchToAllReservationsFilteredByUser(sel.getName());
    }

    @FXML
    public void deleteUser() {

        User sel = userTable.getSelectionModel().getSelectedItem();

        if (sel == null) {
            AlertHelper.error("Wybierz użytkownika.");
            return;
        }

        if (sel.getId() == model.Session.getLoggedUser().getId()) {
            AlertHelper.error("Nie możesz usunąć własnego konta.");
            return;
        }

        if (!AlertHelper.confirm("Usunąć użytkownika \"" + sel.getName() + "\"?")) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiClient.getInstance().deleteUser(sel.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> loadUsers());
        task.setOnFailed(e -> AlertHelper.error("Błąd połączenia."));

        new Thread(task).start();
    }
}