package view;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import model.Hall;
import model.Movie;
import model.Screening;
import network.ApiClient;
import util.AlertHelper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ScreeningFormDialog {

    public static void show(List<Movie> allMovies, List<Hall> allHalls,
                            Screening screening, Runnable onSuccess) {
        Dialog<Screening> dialog = new Dialog<>();
        dialog.setTitle(screening == null ? "Dodaj seans" : "Edytuj seans");
        ButtonType saveBtn = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        ComboBox<Movie> movieBox = new ComboBox<>(FXCollections.observableArrayList(allMovies));
        if (screening != null)
            allMovies.stream().filter(m -> m.getId() == screening.getMovieId())
                    .findFirst().ifPresent(movieBox::setValue);
        movieBox.setPromptText("Wybierz film");

        ComboBox<Hall> hallBox = new ComboBox<>(FXCollections.observableArrayList(allHalls));
        if (screening != null)
            allHalls.stream().filter(h -> h.getId() == screening.getHallId())
                    .findFirst().ifPresent(hallBox::setValue);
        hallBox.setPromptText("Wybierz salę");

        DatePicker datePicker = new DatePicker(
                screening != null ? screening.getStartTime().toLocalDate()
                        : LocalDate.now().plusDays(1));

        Spinner<Integer> hourSpinner = new Spinner<>(0, 23,
                screening != null ? screening.getStartTime().getHour() : 18);
        Spinner<Integer> minSpinner  = new Spinner<>(0, 59,
                screening != null ? screening.getStartTime().getMinute() : 0, 5);
        Spinner<Double>  priceSpinner = new Spinner<>(1.0, 200.0,
                screening != null ? screening.getTicketPrice() : 25.0, 0.5);
        priceSpinner.setEditable(true);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);

        grid.add(new Label("Film:"),      0, 0); grid.add(movieBox,   1, 0);
        grid.add(new Label("Sala:"),      0, 1); grid.add(hallBox,    1, 1);
        grid.add(new Label("Data:"),      0, 2); grid.add(datePicker, 1, 2);
        grid.add(new Label("Godzina:"),   0, 3);
        grid.add(new HBox(4, hourSpinner, new Label(":"), minSpinner), 1, 3);
        grid.add(new Label("Cena (zł):"), 0, 4); grid.add(priceSpinner, 1, 4);
        grid.add(errorLabel,              0, 5, 2, 1);
        dialog.getDialogPane().setContent(grid);

        Button save = (Button) dialog.getDialogPane().lookupButton(saveBtn);
        save.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            if (movieBox.getValue() == null)    { errorLabel.setText("Wybierz film."); e.consume(); return; }
            if (hallBox.getValue() == null)     { errorLabel.setText("Wybierz salę."); e.consume(); return; }
            if (datePicker.getValue() == null)  { errorLabel.setText("Wybierz datę."); e.consume(); return; }
            if (!datePicker.getValue().isAfter(LocalDate.now().minusDays(1))) {
                errorLabel.setText("Data musi być w przyszłości."); e.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            Screening s = new Screening();
            s.setId(screening != null ? screening.getId() : 0);
            s.setMovieId(movieBox.getValue().getId());
            s.setHallId(hallBox.getValue().getId());
            s.setStartTime(LocalDateTime.of(datePicker.getValue(),
                    LocalTime.of(hourSpinner.getValue(), minSpinner.getValue())));
            s.setTicketPrice(priceSpinner.getValue());
            return s;
        });

        dialog.showAndWait().ifPresent(result -> {
            Task<Void> task = new Task<>() {
                @Override protected Void call() throws Exception {
                    ApiClient.getInstance().saveScreening(result)
                            .ifPresent(err -> { throw new RuntimeException(err); });
                    return null;
                }
            };
            task.setOnSucceeded(e -> onSuccess.run());
            task.setOnFailed(e -> AlertHelper.error(e.getSource().getException().getMessage()));
            new Thread(task).start();
        });
    }
}