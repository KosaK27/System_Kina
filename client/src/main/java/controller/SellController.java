package controller;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Hall;
import model.Movie;
import model.Screening;
import model.User;
import network.ApiClient;
import util.AlertHelper;

import java.util.List;

public class SellController {
    @FXML private ComboBox<Movie> movieCombo;
    @FXML private ComboBox<Screening> screeningCombo;
    @FXML private ComboBox<User> clientCombo;
    @FXML private Label statusLabel;

    private List<Hall> allHalls;
    private List<Screening> allScreenings;
    private List<Movie> allMovies;

    @FXML
    public void initialize() {
        movieCombo.setOnAction(e -> filterScreeningsByMovie());
        loadData();
    }

    private void filterScreeningsByMovie() {
        Movie selected = movieCombo.getValue();
        screeningCombo.getItems().clear();
        if (selected == null || allScreenings == null) return;
        List<Screening> filtered = allScreenings.stream()
                .filter(s -> s.getMovieId() == selected.getId())
                .filter(s -> !s.isPast())
                .toList();
        screeningCombo.setItems(FXCollections.observableArrayList(filtered));
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        statusLabel.setText("Ładowanie...");
        Task<List[]> task = new Task<>() {
            @Override protected List[] call() throws Exception {
                return new List[]{
                        ApiClient.getInstance().getAllScreenings(),
                        ApiClient.getInstance().getMovies(),
                        ApiClient.getInstance().getUsers(),
                        ApiClient.getInstance().getHalls()
                };
            }
        };
        task.setOnSucceeded(e -> {
            allScreenings = (List<Screening>) task.getValue()[0];
            allMovies = (List<Movie>) task.getValue()[1];
            List<User> users = (List<User>) task.getValue()[2];
            allHalls = (List<Hall>) task.getValue()[3];

            allScreenings.forEach(s -> {
                allMovies.stream().filter(m -> m.getId() == s.getMovieId()).findFirst()
                        .ifPresent(m -> s.setMovieTitle(m.getTitle()));
                allHalls.stream().filter(h -> h.getId() == s.getHallId()).findFirst()
                        .ifPresent(h -> s.setHallName(h.getName()));
            });

            movieCombo.setItems(FXCollections.observableArrayList(allMovies));

            User noAccount = new User();
            noAccount.setId(-1);
            noAccount.setName("- Klient bez konta -");
            noAccount.setRole("GUEST");

            clientCombo.getItems().clear();
            clientCombo.getItems().add(noAccount);
            clientCombo.getItems().addAll(
                    users.stream().filter(u -> "CLIENT".equals(u.getRole())
                            || "GUEST".equals(u.getRole())).toList());
            statusLabel.setText("");
        });
        task.setOnFailed(e -> statusLabel.setText("Błąd połączenia."));
        new Thread(task).start();
    }

    @FXML public void openSeatPicker() {
        Screening screening = screeningCombo.getValue();
        User client = clientCombo.getValue();
        if (screening == null) { AlertHelper.error("Wybierz seans."); return; }
        if (client == null) { AlertHelper.error("Wybierz klienta."); return; }
        if (allHalls == null) return;

        Integer userId = client.getId() == -1 ? null : client.getId();
        SeatPickerController.openForEmployee(screening, allHalls, userId, this::loadData);
    }
}