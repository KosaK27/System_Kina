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
import javafx.scene.layout.HBox;
import model.Movie;
import model.Session;
import network.ApiClient;
import util.AlertHelper;
import view.MovieFormDialog;

import java.util.List;
import java.util.stream.Collectors;

public class MovieController {

    @FXML private TableView<Movie> movieTable;
    @FXML private TableColumn<Movie,Integer> idColumn;
    @FXML private TableColumn<Movie,String> titleColumn;
    @FXML private TableColumn<Movie,String> descriptionColumn;
    @FXML private TableColumn<Movie,Integer> durationColumn;
    @FXML private TableColumn<Movie,String> genreColumn;
    @FXML private HBox adminControls;
    @FXML private TextField searchField;
    @FXML private FlowPane genreCheckboxes;
    @FXML private Label statusLabel;

    private final ObservableList<Movie> movieList = FXCollections.observableArrayList();
    private FilteredList<Movie> filteredList;
    private List<CheckBox> genreBoxes;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        durationColumn.setCellValueFactory(new PropertyValueFactory<>("duration"));
        genreColumn.setCellValueFactory(new PropertyValueFactory<>("genre"));

        filteredList = new FilteredList<>(movieList,m -> true);
        SortedList<Movie> sorted = new SortedList<>(filteredList);
        sorted.comparatorProperty().bind(movieTable.comparatorProperty());
        movieTable.setItems(sorted);
        movieTable.setPlaceholder(new Label("Brak filmów."));

        genreBoxes = Movie.GENRES.stream()
                .map(CheckBox::new)
                .collect(Collectors.toList());
        genreCheckboxes.getChildren().addAll(genreBoxes);

        boolean isAdmin = Session.isAdmin();
        adminControls.setVisible(isAdmin);
        adminControls.setManaged(isAdmin);

        movieTable.setRowFactory(tv -> {
            TableRow<Movie> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    openScreenings(row.getItem());
                }
            });
            return row;
        });

        loadMovies();
    }

    private void openScreenings(Movie movie) {
        MainController mc = MainController.getInstance();
        if (mc != null) mc.switchToScreenings(movie.getTitle());
    }

    public void loadMovies() {
        statusLabel.setText("Ładowanie...");

        Task<List<Movie>> task = new Task<>() {
            @Override
            protected List<Movie> call() throws Exception {
                return ApiClient.getInstance().getMovies();
            }
        };

        task.setOnSucceeded(e -> {
            movieList.setAll(task.getValue());
            statusLabel.setText("Filmy: " + task.getValue().size() + "  (podwójne kliknięcie = seanse)");
        });

        task.setOnFailed(e -> statusLabel.setText("Błąd połączenia."));
        new Thread(task).start();
    }

    @FXML
    public void search() {
        String keyword = searchField.getText().toLowerCase();

        List<String> selected = genreBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());

        filteredList.setPredicate(m -> {
            boolean matchSearch =
                    keyword.isEmpty()
                            || m.getTitle().toLowerCase().contains(keyword)
                            || m.getDescription().toLowerCase().contains(keyword);

            boolean matchGenre =
                    selected.isEmpty() || selected.contains(m.getGenre());

            return matchSearch && matchGenre;
        });
    }

    @FXML
    public void clearFilter() {
        searchField.clear();
        genreBoxes.forEach(cb -> cb.setSelected(false));
        filteredList.setPredicate(m -> true);
    }

    @FXML
    public void addMovie() {
        Movie movie = MovieFormDialog.show(null,movieList);
        if (movie == null) return;

        runTask(() ->
                ApiClient.getInstance().saveMovie(movie)
                        .ifPresent(err -> { throw new RuntimeException(err); })
        );
    }

    @FXML
    public void editMovie() {
        Movie sel = movieTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertHelper.error("Wybierz film.");
            return;
        }

        Movie updated = MovieFormDialog.show(sel,movieList);
        if (updated == null) return;

        runTask(() ->
                ApiClient.getInstance().saveMovie(updated)
                        .ifPresent(err -> { throw new RuntimeException(err); })
        );
    }

    @FXML
    public void deleteMovie() {
        Movie sel = movieTable.getSelectionModel().getSelectedItem();
        if (sel == null) {
            AlertHelper.error("Wybierz film.");
            return;
        }

        if (!AlertHelper.confirm("Usunąć \"" + sel.getTitle() + "\"?\nWszystkie seanse i rezerwacje zostaną anulowane.")) return;

        runTask(() ->
                ApiClient.getInstance().deleteMovie(sel.getId())
        );
    }

    private void runTask(RunnableEx action) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                action.run();
                return null;
            }
        };

        task.setOnSucceeded(e -> loadMovies());
        task.setOnFailed(e -> AlertHelper.error(e.getSource().getException().getMessage()));
        new Thread(task).start();
    }

    @FunctionalInterface
    interface RunnableEx {
        void run() throws Exception;
    }
}