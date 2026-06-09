package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import model.Movie;
import model.MovieReport;
import model.User;
import network.ApiClient;
import util.AlertHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdminPanelController {

    @FXML private TextField searchField;
    @FXML private Label statusLabel;
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idCol;
    @FXML private TableColumn<User, String> nameCol;
    @FXML private TableColumn<User, String> emailCol;
    @FXML private TableColumn<User, String> roleCol;
    @FXML private TableColumn<User, Void> actionCol;

    @FXML private ComboBox<Movie> movieComboBox;
    @FXML private TextArea reportTextArea;

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

        setupActionColumn();
        loadData();
    }

    public void loadData() {
        if (statusLabel != null) statusLabel.setText("Pobieranie pełnych danych administracyjnych...");

        Task<List<Object>> task = new Task<>() {
            @Override
            protected List<Object> call() throws Exception {
                List<User> users = ApiClient.getInstance().getUsers();
                List<Movie> movies = ApiClient.getInstance().getMovies();
                return List.of(users, movies);
            }
        };

        task.setOnSucceeded(e -> {
            List<Object> result = task.getValue();
            if (result != null && result.size() == 2) {
                if (result.get(0) instanceof List<?>) {
                    List<?> rawUsers = (List<?>) result.get(0);
                    List<User> users = new ArrayList<>();
                    for (Object obj : rawUsers) {
                        if (obj instanceof User) users.add((User) obj);
                    }
                    usersList.setAll(users);
                }

                if (result.get(1) instanceof List<?>) {
                    List<?> rawMovies = (List<?>) result.get(1);
                    List<Movie> movies = new ArrayList<>();
                    for (Object obj : rawMovies) {
                        if (obj instanceof Movie) movies.add((Movie) obj);
                    }
                    if (movieComboBox != null) {
                        movieComboBox.setItems(FXCollections.observableArrayList(movies));
                    }
                }
            }
            usersTable.refresh();
            if (statusLabel != null) statusLabel.setText("Zsynchronizowano kont: " + usersList.size());
        });

        task.setOnFailed(e -> {
            if (statusLabel != null) statusLabel.setText("Błąd połączenia API.");
        });

        new Thread(task).start();
    }

    private void setupActionColumn() {
        actionCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<User, Void> call(final TableColumn<User, Void> param) {
                return new TableCell<>() {
                    private final ComboBox<String> roleCombo = new ComboBox<>(
                            FXCollections.observableArrayList("CLIENT", "EMPLOYEE", "ADMIN")
                    );
                    private boolean isUpdating = false;

                    {
                        roleCombo.setPromptText("Zmień");
                        roleCombo.setPrefWidth(120);

                        roleCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldRole, newRole) -> {
                            if (isUpdating) return;

                            User user = getTableRow() != null ? getTableRow().getItem() : null;
                            if (user != null && newRole != null && !newRole.equals(user.getRole())) {
                                handleRoleChange(user, newRole);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            User u = getTableRow() != null ? getTableRow().getItem() : null;
                            if (u != null) {
                                isUpdating = true;
                                if (u.getId() == 1 || "admin@poczta.pl".equalsIgnoreCase(u.getEmail())) {
                                    roleCombo.setDisable(true);
                                } else {
                                    roleCombo.setDisable(false);
                                }
                                roleCombo.setValue(u.getRole());
                                isUpdating = false;
                            }
                            setGraphic(roleCombo);
                        }
                    }
                };
            }
        });
    }

    private void handleRoleChange(User user, String newRole) {
        if (!AlertHelper.confirm("Zapisać uprawnienia " + newRole + " dla użytkownika " + user.getEmail() + "?")) {
            loadData();
            return;
        }

        user.setRole(newRole);

        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiClient.getInstance().updateUser(user);
                return null;
            }
        };

        updateTask.setOnSucceeded(e -> {
            AlertHelper.info("Modyfikacja roli zapisana pomyślnie.");
            loadData();
        });

        updateTask.setOnFailed(e -> {
            AlertHelper.error("Błąd podczas próby aktualizacji roli.");
            loadData();
        });

        new Thread(updateTask).start();
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
    public void handleDeleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.error("Zaznacz konto z tabeli.");
            return;
        }
        if (selected.getId() == 1 || "ADMIN".equals(selected.getRole())) {
            AlertHelper.error("Konto administratora jest chronione.");
            return;
        }
        if (AlertHelper.confirm("Usunąć użytkownika " + selected.getEmail() + "?")) {
            new Thread(() -> {
                try {
                    ApiClient.getInstance().deleteUser(selected.getId());
                    javafx.application.Platform.runLater(this::loadData);
                } catch (Exception ex) {
                    javafx.application.Platform.runLater(() -> AlertHelper.error("Błąd podczas usuwania."));
                }
            }).start();
        }
    }

    @FXML
    private void handleGenerateReport() {
        if (movieComboBox == null || reportTextArea == null) return;

        Movie selectedMovie = movieComboBox.getSelectionModel().getSelectedItem();
        if (selectedMovie == null) {
            AlertHelper.error("Wybierz film z listy.");
            return;
        }

        reportTextArea.setText("Agregowanie danych finansowych z widoku Oracle v_movie_reports...");

        Task<List<MovieReport>> task = new Task<>() {
            @Override
            protected List<MovieReport> call() throws Exception {
                return ApiClient.getInstance().getMovieReports();
            }
        };

        task.setOnSucceeded(e -> {
            List<?> rawReports = task.getValue();
            int tickets = 0;
            double revenue = 0.0;

            if (rawReports != null) {
                for (Object obj : rawReports) {
                    int currentId = 0;
                    int currentTickets = 0;
                    double currentRevenue = 0.0;

                    if (obj instanceof Map<?, ?> map) {
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            String key = String.valueOf(entry.getKey()).toLowerCase();
                            Object val = entry.getValue();
                            if (val instanceof Number num) {
                                if (key.equals("movie_id") || key.equals("movieid") || key.equals("id")) {
                                    currentId = num.intValue();
                                } else if (key.equals("total_tickets_sold") || key.equals("totalticketssold") || key.equals("total_tickets")) {
                                    currentTickets = num.intValue();
                                } else if (key.equals("total_revenue") || key.equals("totalrevenue") || key.equals("revenue")) {
                                    currentRevenue = num.doubleValue();
                                }
                            }
                        }
                    } else if (obj instanceof MovieReport rep) {
                        currentId = rep.getMovieId();
                        currentTickets = rep.getTotalTicketsSold();
                        currentRevenue = rep.getTotalRevenue();
                    }

                    if (currentId == selectedMovie.getId()) {
                        tickets = currentTickets;
                        revenue = currentRevenue;
                        break;
                    }
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append("==================================================\n");
            sb.append("      URZĘDOWY RAPORT SPRZEDAŻY DLA FILMU         \n");
            sb.append("==================================================\n");
            sb.append("Tytuł filmu:         ").append(selectedMovie.getTitle()).append("\n");
            sb.append("Gatunek:             ").append(selectedMovie.getGenre()).append("\n");
            sb.append("--------------------------------------------------\n");
            sb.append("Sprzedane bilety (Baza Oracle):  ").append(tickets).append(" szt.\n");
            sb.append("Zarejestrowany przychód z filmu: ").append(String.format("%.2f", revenue)).append(" PLN\n");
            sb.append("==================================================\n");
            sb.append("Generowanie zakończone sukcesem.");
            reportTextArea.setText(sb.toString());
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            reportTextArea.setText("Błąd: Brak synchronizacji z widokiem bazodanowym.\n" +
                    (exception != null ? exception.getMessage() : "Brak szczegółów błędu."));
        });

        new Thread(task).start();
    }
}