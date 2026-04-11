package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import model.*;
import network.ApiClient;
import util.AlertHelper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeReservationController {
    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Integer> resIdCol;
    @FXML private TableColumn<Reservation, String> resUserCol;
    @FXML private TableColumn<Reservation, String> resMovieCol;
    @FXML private TableColumn<Reservation, String> resTimeCol;
    @FXML private TableColumn<Reservation, String> resSeatCol;
    @FXML private TableColumn<Reservation, String> resStatusCol;
    @FXML private TableColumn<Reservation, Double> resPriceCol;
    @FXML private FlowPane statusBoxesPane;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private FilteredList<Reservation> filteredList;
    private List<CheckBox> statusBoxes;

    @FXML
    public void initialize() {
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        resUserCol.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getUserName() != null
                                ? cell.getValue().getUserName()
                                : "Użytkownik #" + cell.getValue().getUserId()));
        resMovieCol.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getMovieTitle() != null
                                ? cell.getValue().getMovieTitle()
                                : "Seans #" + cell.getValue().getScreeningId()));
        resTimeCol.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getFormattedTime()));
        resSeatCol.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getSeatsLabel()));
        resStatusCol.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        translateStatus(cell.getValue().getStatus())));
        resPriceCol.setCellValueFactory(new PropertyValueFactory<>("pricePaid"));

        filteredList = new FilteredList<>(reservationList, r -> true);
        SortedList<Reservation> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(reservationTable.comparatorProperty());
        reservationTable.setItems(sorted);
        reservationTable.setPlaceholder(new Label("Brak rezerwacji."));

        statusBoxes = List.of(
                new CheckBox("Zarezerwowane"),
                new CheckBox("Opłacone"),
                new CheckBox("Anulowane"));
        statusBoxesPane.getChildren().addAll(statusBoxes);

        loadAll();
    }

    public void filterByUser(String userName) {
        searchField.setText(userName);
        search();
    }

    @SuppressWarnings("unchecked")
    public void loadAll() {
        statusLabel.setText("Ładowanie...");
        Task<List<Object>> task = new Task<>() {
            @Override protected List<Object> call() throws Exception {
                return List.of(
                        ApiClient.getInstance().getAllReservations(),
                        ApiClient.getInstance().getUsers(),
                        ApiClient.getInstance().getAllScreenings(),
                        ApiClient.getInstance().getMovies(),
                        ApiClient.getInstance().getHalls()
                );
            }
        };
        task.setOnSucceeded(e -> {
            List<Reservation> reservations = (List<Reservation>)task.getValue().get(0);
            List<User> users = (List<User>)task.getValue().get(1);
            List<Screening> screenings = (List<Screening>)task.getValue().get(2);
            List<Movie> movies = (List<Movie>)task.getValue().get(3);
            List<Hall> halls = (List<Hall>)task.getValue().get(4);

            Map<Integer, String> userNames = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getName));

            screenings.forEach(s -> {
                movies.stream().filter(m -> m.getId() == s.getMovieId()).findFirst()
                        .ifPresent(m -> s.setMovieTitle(m.getTitle()));
                halls.stream().filter(h -> h.getId() == s.getHallId()).findFirst()
                        .ifPresent(h -> s.setHallName(h.getName()));
            });

            reservations.forEach(r -> {
                r.setUserName(userNames.getOrDefault(r.getUserId(),
                        "Użytkownik #" + r.getUserId()));
                screenings.stream().filter(s -> s.getId() == r.getScreeningId())
                        .findFirst().ifPresent(s -> {
                            r.setScreeningTime(s.getStartTime());
                            r.setHallName(s.getHallName());
                            r.setMovieTitle(s.getMovieTitle());
                        });
            });

            reservationList.setAll(reservations);
            statusLabel.setText("Rezerwacje: " + reservations.size());
            search();
        });
        task.setOnFailed(e -> statusLabel.setText("Błąd połączenia."));
        new Thread(task).start();
    }

    @FXML public void search() {
        String keyword = searchField.getText().toLowerCase();
        List<String> selected = statusBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());
        filteredList.setPredicate(r -> {
            boolean matchSearch = keyword.isEmpty()
                    || (r.getUserName() != null && r.getUserName().toLowerCase().contains(keyword))
                    || (r.getMovieTitle() != null && r.getMovieTitle().toLowerCase().contains(keyword));
            boolean matchStatus = selected.isEmpty()
                    || selected.contains(translateStatus(r.getStatus()));
            return matchSearch && matchStatus;
        });
    }

    @FXML public void clearFilter() {
        searchField.clear();
        statusBoxes.forEach(cb -> cb.setSelected(false));
        filteredList.setPredicate(r -> true);
    }

    @FXML public void cancelSelected() {
        Reservation sel = reservationTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertHelper.error("Wybierz rezerwację."); return; }
        if (sel.getStatus() == Reservation.Status.CANCELLED) {
            AlertHelper.error("Już anulowana."); return;
        }
        if (!AlertHelper.confirm("Anulować tę rezerwację?")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().cancelReservation(sel.getId())
                        .ifPresent(err -> { throw new RuntimeException(err); });
                return null;
            }
        };
        task.setOnSucceeded(e -> loadAll());
        task.setOnFailed(e -> AlertHelper.error(e.getSource().getException().getMessage()));
        new Thread(task).start();
    }

    @FXML public void paySelected() {
        Reservation sel = reservationTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertHelper.error("Wybierz rezerwację."); return; }
        if (sel.getStatus() != Reservation.Status.RESERVED) {
            AlertHelper.error("Tylko RESERVED można opłacić."); return;
        }
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().payForReservation(sel.getId())
                        .ifPresent(err -> { throw new RuntimeException(err); });
                return null;
            }
        };
        task.setOnSucceeded(e -> loadAll());
        task.setOnFailed(e -> AlertHelper.error(e.getSource().getException().getMessage()));
        new Thread(task).start();
    }

    private String translateStatus(Reservation.Status s) {
        if (s == null) return "";
        return switch (s) {
            case RESERVED -> "Zarezerwowane";
            case PAID -> "Opłacone";
            case CANCELLED -> "Anulowane";
        };
    }
}