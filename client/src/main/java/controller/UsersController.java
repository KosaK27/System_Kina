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

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;

    private final ObservableList<User> usersList = FXCollections.observableArrayList();
    private FilteredList<User> filteredUsers;

    @FXML
    public void initialize() {
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));

        filteredUsers = new FilteredList<>(usersList, p -> true);
        SortedList<User> sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(usersTable.comparatorProperty());
        usersTable.setItems(sortedUsers);

        loadData();
    }

    public void loadData() {
        if (statusLabel != null) statusLabel.setText("Odświeżanie bazy użytkowników...");

        Task<List<User>> task = new Task<> () {
            @Override
            protected List<User> call() throws Exception {
                return ApiClient.getInstance().getUsers();
            }
        };

        task.setOnSucceeded(e -> {
            if (task.getValue() != null) {
                usersList.setAll(task.getValue());
            }
            if (statusLabel != null) statusLabel.setText("Wszystkich kont: " + usersList.size());
        });

        task.setOnFailed(e -> {
            if (statusLabel != null) statusLabel.setText("Błąd ładowania danych z serwera.");
        });

        new Thread(task).start();
    }

    public void loadUsers() {
        loadData();
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        filteredUsers.setPredicate(user -> {
            if (keyword.isEmpty()) return true;
            return (user.getEmail() != null && user.getEmail().toLowerCase().contains(keyword))
                    || (user.getName() != null && user.getName().toLowerCase().contains(keyword));
        });
    }

    @FXML
    private void handleDeleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.error("Wybierz konto z tabeli.");
            return;
        }
        if (selected.getId() == 1 || "ADMIN".equals(selected.getRole())) {
            AlertHelper.error("Nie można usunąć konta administratora.");
            return;
        }
        if (AlertHelper.confirm("Usunąć użytkownika " + selected.getEmail() + "?")) {
            new Thread(() -> {
                try {
                    ApiClient.getInstance().deleteUser(selected.getId());
                    javafx.application.Platform.runLater(this::loadData);
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> AlertHelper.error("Błąd zapisu."));
                }
            }).start();
        }
    }
}