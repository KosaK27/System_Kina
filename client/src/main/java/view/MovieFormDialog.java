package view;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import model.Movie;

public class MovieFormDialog {

    public static Movie show(Movie movie, ObservableList<Movie> existing) {
        Dialog<Movie> dialog = new Dialog<>();
        dialog.setTitle(movie == null ? "Dodaj film" : "Edytuj film");
        ButtonType saveBtn = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(8);

        TextField titleField    = new TextField(movie != null ? movie.getTitle() : "");
        TextField descField     = new TextField(movie != null ? movie.getDescription() : "");
        TextField durationField = new TextField(movie != null ? String.valueOf(movie.getDuration()) : "");
        durationField.setPromptText(Movie.MIN_DURATION + "–" + Movie.MAX_DURATION + " min");
        durationField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) durationField.setText(n.replaceAll("[^\\d]", ""));
        });

        ComboBox<String> genreBox = new ComboBox<>();
        genreBox.getItems().addAll(Movie.GENRES);
        if (movie != null) genreBox.setValue(movie.getGenre());
        genreBox.setPromptText("Wybierz gatunek");

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(260);

        grid.add(new Label("Tytuł:"),      0, 0); grid.add(titleField,    1, 0);
        grid.add(new Label("Opis:"),       0, 1); grid.add(descField,     1, 1);
        grid.add(new Label("Czas (min):"), 0, 2); grid.add(durationField, 1, 2);
        grid.add(new Label("Gatunek:"),    0, 3); grid.add(genreBox,      1, 3);
        grid.add(errorLabel,               0, 4, 2, 1);
        dialog.getDialogPane().setContent(grid);

        Button save = (Button) dialog.getDialogPane().lookupButton(saveBtn);
        save.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            String err = validate(titleField.getText(), descField.getText(),
                    durationField.getText(), genreBox.getValue(),
                    existing, movie != null ? movie.getId() : -1);
            if (err != null) { errorLabel.setText(err); e.consume(); }
        });

        Platform.runLater(titleField::requestFocus);

        dialog.setResultConverter(btn -> {
            if (btn != saveBtn) return null;
            return new Movie(
                    movie != null ? movie.getId() : 0,
                    titleField.getText().trim(),
                    descField.getText().trim(),
                    Integer.parseInt(durationField.getText().trim()),
                    genreBox.getValue());
        });

        return dialog.showAndWait().orElse(null);
    }

    private static String validate(String title, String desc, String dur,
                                   String genre, ObservableList<Movie> existing, int currentId) {
        if (title == null || title.isBlank()) return "Tytuł nie może być pusty.";
        if (desc  == null || desc.isBlank())  return "Opis nie może być pusty.";
        if (dur   == null || dur.isBlank())   return "Czas trwania nie może być pusty.";
        int d;
        try { d = Integer.parseInt(dur.trim()); }
        catch (NumberFormatException e) { return "Czas trwania musi być liczbą."; }
        if (d < Movie.MIN_DURATION) return "Min. " + Movie.MIN_DURATION + " min.";
        if (d > Movie.MAX_DURATION) return "Max. " + Movie.MAX_DURATION + " min.";
        if (genre == null)          return "Wybierz gatunek.";
        boolean dup = existing.stream()
                .filter(m -> m.getId() != currentId)
                .anyMatch(m -> m.getTitle().equalsIgnoreCase(title.trim()));
        if (dup) return "Film o tym tytule już istnieje.";
        return null;
    }
}