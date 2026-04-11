package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import model.Hall;
import model.Movie;
import model.Screening;
import model.Session;
import network.ApiClient;
import util.AlertHelper;
import view.ScreeningFormDialog;

import java.util.List;

public class ScreeningController {
    @FXML private TableView<Screening> screeningTable;
    @FXML private TableColumn<Screening, String> movieColumn;
    @FXML private TableColumn<Screening, String> hallColumn;
    @FXML private TableColumn<Screening, String> timeColumn;
    @FXML private TableColumn<Screening, Double> priceColumn;
    @FXML private TableColumn<Screening, String> statusColumn;
    @FXML private HBox adminControls;
    @FXML private HBox clientControls;
    @FXML private TextField searchField;
    @FXML private Label statusLabel;

    private final ObservableList<Screening> screeningList = FXCollections.observableArrayList();
    private FilteredList<Screening> filteredList;
    private List<Movie> allMovies;
    private List<Hall> allHalls;

    @FXML
    public void initialize() {
        movieColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getMovieTitle() != null
                                ? cell.getValue().getMovieTitle()
                                : "Film #" + cell.getValue().getMovieId()));
        hallColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getHallName() != null
                                ? cell.getValue().getHallName()
                                : "Sala #" + cell.getValue().getHallId()));
        timeColumn.setCellValueFactory(cell ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        cell.getValue().getFormattedTime()));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("ticketPrice"));
        statusColumn.setCellValueFactory(cell -> {
            Screening s = cell.getValue();
            String st = s.isPast() ? "Zakończony" : s.isOngoing() ? "Trwa" : "Planowany";
            return new javafx.beans.property.ReadOnlyStringWrapper(st);
        });

        filteredList = new FilteredList<>(screeningList, s -> true);
        SortedList<Screening> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(screeningTable.comparatorProperty());
        screeningTable.setItems(sorted);
        screeningTable.setPlaceholder(new Label("Brak seansów."));

        screeningTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Screening item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("");
                if (!empty && item != null) {
                    if (item.isPast()) setStyle("-fx-opacity: 0.45;");
                    else if (item.isOngoing()) setStyle("-fx-font-weight: bold;");
                }
            }
        });

        boolean canManage = Session.canManage();
        boolean isClient = !canManage;

        adminControls.setVisible(canManage);
        adminControls.setManaged(canManage);
        clientControls.setVisible(isClient);
        clientControls.setManaged(isClient);

        screeningTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && isClient) bookSeats();
        });

        loadAll();
    }

    public void filterByMovie(String movieTitle) {
        searchField.setText(movieTitle);
        applyFilter();
    }

    @FXML
    public void search() {
        applyFilter();
    }

    @FXML
    public void clearFilter() {
        searchField.clear();
        applyFilter();
    }

    private void applyFilter() {
        String keyword = searchField.getText().toLowerCase();
        filteredList.setPredicate(s -> {
            boolean matchSearch = keyword.isEmpty()
                    || (s.getMovieTitle() != null && s.getMovieTitle().toLowerCase().contains(keyword))
                    || (s.getHallName() != null && s.getHallName().toLowerCase().contains(keyword));
            boolean showPast = Session.canManage() || !s.isPast();
            return matchSearch && showPast;
        });
    }

    @SuppressWarnings("unchecked")
    public void loadAll() {
        statusLabel.setText("Ładowanie...");
        Task<List[]> task = new Task<>() {
            @Override protected List[] call() throws Exception {
                return new List[]{
                        ApiClient.getInstance().getAllScreenings(),
                        ApiClient.getInstance().getMovies(),
                        ApiClient.getInstance().getHalls()
                };
            }
        };
        task.setOnSucceeded(e -> {
            List<Screening> screenings = (List<Screening>) task.getValue()[0];
            allMovies = (List<Movie>) task.getValue()[1];
            allHalls = (List<Hall>) task.getValue()[2];
            screenings.forEach(s -> {
                allMovies.stream().filter(m -> m.getId() == s.getMovieId()).findFirst()
                        .ifPresent(m -> s.setMovieTitle(m.getTitle()));
                allHalls.stream().filter(h -> h.getId() == s.getHallId()).findFirst()
                        .ifPresent(h -> s.setHallName(h.getName()));
            });
            screeningList.setAll(screenings);
            applyFilter();
            statusLabel.setText("Seanse: " + filteredList.size());
        });
        task.setOnFailed(e -> statusLabel.setText("Błąd połączenia."));
        new Thread(task).start();
    }

    @FXML public void bookSeats() {
        Screening sel = screeningTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertHelper.error("Wybierz seans."); return; }
        if (sel.isPast()) { AlertHelper.error("Ten seans już się zakończył."); return; }
        if (allHalls == null) return;
        SeatPickerController.openFor(sel, allHalls, null, () -> {
            MainController mc = MainController.getInstance();
            if (mc != null) mc.switchToMyReservations();
        });
    }

    @FXML public void addScreening() {
        if (allMovies == null) return;
        ScreeningFormDialog.show(allMovies, allHalls, null, this::loadAll);
    }

    @FXML public void editScreening() {
        Screening sel = screeningTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertHelper.error("Wybierz seans."); return; }
        ScreeningFormDialog.show(allMovies, allHalls, sel, this::loadAll);
    }

    @FXML public void deleteScreening() {
        Screening sel = screeningTable.getSelectionModel().getSelectedItem();
        if (sel == null) { AlertHelper.error("Wybierz seans."); return; }
        if (!AlertHelper.confirm("Usunąć seans?\nWszystkie rezerwacje zostaną anulowane.")) return;
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().deleteScreening(sel.getId());
                return null;
            }
        };
        task.setOnSucceeded(e -> loadAll());
        task.setOnFailed(e -> AlertHelper.error("Błąd połączenia."));
        new Thread(task).start();
    }
}