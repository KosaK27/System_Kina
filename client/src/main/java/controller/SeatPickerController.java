package controller;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Hall;
import model.Reservation;
import model.Screening;
import network.ApiClient;
import util.AlertHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SeatPickerController {

    public static void openFor(Screening screening, List<Hall> allHalls,
                               Integer overrideUserId, Runnable onSuccess) {
        open(screening, allHalls, overrideUserId, false, onSuccess);
    }

    public static void openForEmployee(Screening screening, List<Hall> allHalls,
                                       Integer clientId, Runnable onSuccess) {
        open(screening, allHalls, clientId, true, onSuccess);
    }

    private static void open(Screening screening, List<Hall> allHalls,
                             Integer clientId, boolean isEmployee, Runnable onSuccess) {
        Hall hall = allHalls.stream()
                .filter(h -> h.getId() == screening.getHallId())
                .findFirst().orElse(null);
        if (hall == null) { AlertHelper.error("Nie znaleziono sali."); return; }

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Wybór miejsc - " + screening);

        VBox root = new VBox(10);
        root.setPadding(new Insets(14));
        root.setAlignment(Pos.CENTER);

        Label info = new Label(screening.toString());
        Label selectedLabel = new Label("Wybrane: brak");

        Set<String> takenSeats = new HashSet<>();
        List<List<Integer>> selectedSeats = new ArrayList<>();

        GridPane seatGrid = new GridPane();
        seatGrid.setHgap(5);
        seatGrid.setVgap(5);
        seatGrid.setAlignment(Pos.CENTER);

        for (int r = 1; r <= hall.getRows(); r++) {
            seatGrid.add(new Label(String.valueOf((char) ('A' + r - 1))), 0, r);
        }
        for (int c = 1; c <= hall.getSeatsPerRow(); c++) {
            Label lbl = new Label(String.valueOf(c));
            lbl.setMinWidth(32);
            lbl.setAlignment(Pos.CENTER);
            seatGrid.add(lbl, c, 0);
        }

        ToggleButton[][] buttons = new ToggleButton[hall.getRows() + 1][hall.getSeatsPerRow() + 1];
        for (int r = 1; r <= hall.getRows(); r++) {
            for (int c = 1; c <= hall.getSeatsPerRow(); c++) {
                ToggleButton btn = new ToggleButton();
                btn.setMinSize(32, 28);
                btn.setMaxSize(32, 28);
                String key = r + ":" + c;
                final int fr = r, fc = c;
                btn.setOnAction(e -> {
                    if (takenSeats.contains(key)) { btn.setSelected(false); return; }
                    if (btn.isSelected()) {
                        btn.setStyle("-fx-base: #4a90d9;");
                        selectedSeats.add(List.of(fr, fc));
                    } else {
                        btn.setStyle("");
                        selectedSeats.removeIf(s -> s.get(0) == fr && s.get(1) == fc);
                    }
                    updateLabel(selectedLabel, selectedSeats, screening.getTicketPrice());
                });
                buttons[r][c] = btn;
                seatGrid.add(btn, c, r);
            }
        }

        Button buyBtn = new Button("Kup bilet");
        Button reserveBtn = new Button("Rezerwuj");
        Button cancelBtn = new Button("Anuluj");

        HBox actions = isEmployee
                ? new HBox(8, buyBtn, cancelBtn)
                : new HBox(8, buyBtn, reserveBtn, cancelBtn);
        actions.setAlignment(Pos.CENTER);

        buyBtn.setOnAction(e -> {
            if (selectedSeats.isEmpty()) { AlertHelper.error("Wybierz co najmniej jedno miejsce."); return; }
            doBuy(stage, screening, new ArrayList<>(selectedSeats), clientId, onSuccess);
        });

        reserveBtn.setOnAction(e -> {
            if (selectedSeats.isEmpty()) { AlertHelper.error("Wybierz co najmniej jedno miejsce."); return; }
            doReserve(stage, screening, new ArrayList<>(selectedSeats), onSuccess);
        });

        cancelBtn.setOnAction(e -> stage.close());

        root.getChildren().addAll(info, new ScrollPane(seatGrid), selectedLabel, actions);

        loadTaken(screening.getId(), hall, buttons, takenSeats);

        stage.setScene(new Scene(root, 600, 520));
        stage.showAndWait();
    }

    private static void doReserve(Stage stage, Screening screening,
                                  List<List<Integer>> seats, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().reserve(screening.getId(), seats)
                        .ifPresent(err -> { throw new RuntimeException(err); });
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            AlertHelper.info("Zarezerwowano: " + formatSeats(seats));
            if (onSuccess != null) onSuccess.run();
            stage.close();
        });
        task.setOnFailed(e -> AlertHelper.error(e.getSource().getException().getMessage()));
        new Thread(task).start();
    }

    private static void doBuy(Stage stage, Screening screening,
                              List<List<Integer>> seats, Integer userId, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override protected Void call() throws Exception {
                ApiClient.getInstance().buyTicket(screening.getId(), seats, userId)
                        .ifPresent(err -> { throw new RuntimeException(err); });
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            AlertHelper.info("Zakupiono bilety: " + formatSeats(seats));
            if (onSuccess != null) onSuccess.run();
            stage.close();
        });
        task.setOnFailed(e -> AlertHelper.error(e.getSource().getException().getMessage()));
        new Thread(task).start();
    }

    private static void loadTaken(int screeningId, Hall hall,
                                  ToggleButton[][] buttons, Set<String> takenSeats) {
        Task<List<Reservation>> task = new Task<>() {
            @Override protected List<Reservation> call() throws Exception {
                return ApiClient.getInstance().getReservationsForScreening(screeningId);
            }
        };
        task.setOnSucceeded(e -> task.getValue().stream()
                .filter(r -> r.getStatus() != Reservation.Status.CANCELLED)
                .forEach(r -> r.getSeats().forEach(s -> {
                    int row = s.get(0), col = s.get(1);
                    takenSeats.add(row + ":" + col);
                    if (row >= 1 && row <= hall.getRows()
                            && col >= 1 && col <= hall.getSeatsPerRow()) {
                        buttons[row][col].setDisable(true);
                        buttons[row][col].setStyle("-fx-base: #e74c3c;");
                    }
                })));
        task.setOnFailed(e -> AlertHelper.error("Błąd ładowania zajętych miejsc."));
        new Thread(task).start();
    }

    private static void updateLabel(Label label, List<List<Integer>> seats, double price) {
        if (seats.isEmpty()) {
            label.setText("Wybrane: brak");
        } else {
            label.setText("Wybrane: " + formatSeats(seats)
                    + "  |  Łącznie: " + String.format("%.2f", seats.size() * price) + " zł");
        }
    }

    private static String formatSeats(List<List<Integer>> seats) {
        StringBuilder sb = new StringBuilder();
        for (List<Integer> s : seats) {
            if (sb.length() > 0) sb.append(", ");
            sb.append((char) ('A' + s.get(0) - 1)).append(s.get(1));
        }
        return sb.toString();
    }
}