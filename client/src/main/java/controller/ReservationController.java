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
import model.Movie;
import model.Hall;
import model.Reservation;
import model.Screening;
import model.Session;
import network.ApiClient;
import util.AlertHelper;

import java.util.List;
import java.util.stream.Collectors;

public class ReservationController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation,Integer> idColumn;
    @FXML private TableColumn<Reservation,String> movieColumn;
    @FXML private TableColumn<Reservation,String> timeColumn;
    @FXML private TableColumn<Reservation,String> seatsColumn;
    @FXML private TableColumn<Reservation,String> statusColumn;
    @FXML private TableColumn<Reservation,String> hallColumn;
    @FXML private TableColumn<Reservation,Double> priceColumn;
    @FXML private FlowPane statusCheckboxes;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final ObservableList<Reservation> reservationList = FXCollections.observableArrayList();
    private FilteredList<Reservation> filteredList;
    private List<CheckBox> statusBoxes;

    @FXML
    public void initialize() {

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        movieColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getMovieTitle() != null
                                ? cell.getValue().getMovieTitle()
                                : "Seans #" + cell.getValue().getScreeningId()));

        timeColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getFormattedTime()));

        seatsColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getSeatsLabel()));

        statusColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        translateStatus(cell.getValue().getStatus())));

        hallColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getHallName() != null
                                ? cell.getValue().getHallName() : ""));

        priceColumn.setCellValueFactory(new PropertyValueFactory<>("pricePaid"));

        filteredList = new FilteredList<>(reservationList,r -> true);
        SortedList<Reservation> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(reservationTable.comparatorProperty());
        reservationTable.setItems(sorted);
        reservationTable.setPlaceholder(new Label("Brak rezerwacji."));

        statusBoxes = List.of(
                new CheckBox("Zarezerwowane"),
                new CheckBox("Opłacone"),
                new CheckBox("Anulowane"));

        statusCheckboxes.getChildren().addAll(statusBoxes);

        loadReservations();
    }

    @SuppressWarnings("unchecked")
    public void loadReservations() {

        statusLabel.setText("Ładowanie...");
        int userId = Session.getLoggedUser().getId();

        Task<List<Object>> task = new Task<>() {
            @Override
            protected List<Object> call() throws Exception {
                return List.of(
                        ApiClient.getInstance().getMyReservations(userId),
                        ApiClient.getInstance().getAllScreenings(),
                        ApiClient.getInstance().getMovies(),
                        ApiClient.getInstance().getHalls()
                );
            }
        };

        task.setOnSucceeded(e -> {

            List<Reservation> reservations = (List<Reservation>) task.getValue().get(0);
            List<Screening> screenings = (List<Screening>) task.getValue().get(1);
            List<Movie> movies = (List<Movie>) task.getValue().get(2);
            List<Hall> halls = (List<Hall>) task.getValue().get(3);

            screenings.forEach(s ->
                    halls.stream()
                            .filter(h -> h.getId() == s.getHallId())
                            .findFirst()
                            .ifPresent(h -> s.setHallName(h.getName()))
            );

            reservations.forEach(r ->
                    screenings.stream()
                            .filter(s -> s.getId() == r.getScreeningId())
                            .findFirst()
                            .ifPresent(s -> {

                                r.setScreeningTime(s.getStartTime());
                                r.setHallName(s.getHallName());

                                movies.stream()
                                        .filter(m -> m.getId() == s.getMovieId())
                                        .findFirst()
                                        .ifPresent(m -> r.setMovieTitle(m.getTitle()));
                            })
            );

            reservationList.setAll(reservations);
            statusLabel.setText("Rezerwacje: " + reservations.size());
        });

        task.setOnFailed(e -> statusLabel.setText("Błąd połączenia."));
        new Thread(task).start();
    }

    @FXML
    public void search() {

        String keyword = searchField.getText().toLowerCase();

        List<String> selected = statusBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());

        filteredList.setPredicate(r -> {

            boolean matchSearch =
                    keyword.isEmpty()
                            || (r.getMovieTitle() != null
                            && r.getMovieTitle().toLowerCase().contains(keyword));

            boolean matchStatus =
                    selected.isEmpty()
                            || selected.contains(translateStatus(r.getStatus()));

            return matchSearch && matchStatus;
        });
    }

    @FXML
    public void clearFilter() {

        searchField.clear();
        statusBoxes.forEach(cb -> cb.setSelected(false));
        filteredList.setPredicate(r -> true);
    }

    @FXML
    public void payForSelected() {

        Reservation sel = reservationTable.getSelectionModel().getSelectedItem();

        if (sel == null) {
            AlertHelper.error("Wybierz rezerwację.");
            return;
        }

        if (sel.getStatus() != Reservation.Status.RESERVED) {
            AlertHelper.error("Można opłacić tylko RESERVED.");
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {

                ApiClient.getInstance().payForReservation(sel.getId())
                        .ifPresent(err -> { throw new RuntimeException(err); });

                return null;
            }
        };

        task.setOnSucceeded(e -> loadReservations());
        task.setOnFailed(e ->
                AlertHelper.error(e.getSource().getException().getMessage()));

        new Thread(task).start();
    }

    @FXML
    public void cancelSelected() {

        Reservation sel = reservationTable.getSelectionModel().getSelectedItem();

        if (sel == null) {
            AlertHelper.error("Wybierz rezerwację.");
            return;
        }

        if (sel.getStatus() == Reservation.Status.CANCELLED) {
            AlertHelper.error("Już anulowana.");
            return;
        }

        if (!AlertHelper.confirm("Anulować tę rezerwację?")) return;

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {

                ApiClient.getInstance().cancelReservation(sel.getId())
                        .ifPresent(err -> { throw new RuntimeException(err); });

                return null;
            }
        };

        task.setOnSucceeded(e -> loadReservations());
        task.setOnFailed(e ->
                AlertHelper.error(e.getSource().getException().getMessage()));

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