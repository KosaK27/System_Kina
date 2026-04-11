package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Reservation {
    public enum Status { RESERVED, PAID, CANCELLED }

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private int id;
    private int userId;
    private int screeningId;
    private List<List<Integer>> seats = new ArrayList<>();
    private Status status;
    private LocalDateTime createdAt;
    private double pricePaid;

    private String movieTitle;
    private LocalDateTime screeningTime;
    private String userName;
    private String hallName;

    public Reservation() {}

    public String getSeatsLabel() {
        if (seats == null || seats.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (List<Integer> s : seats) {
            if (sb.length() > 0) sb.append(", ");
            sb.append((char) ('A' + s.get(0) - 1)).append(s.get(1));
        }
        return sb.toString();
    }

    public String getFormattedTime() {
        return screeningTime != null ? screeningTime.format(FMT) : "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public int getScreeningId() { return screeningId; }
    public void setScreeningId(int screeningId) { this.screeningId = screeningId; }
    public List<List<Integer>> getSeats() { return seats; }
    public void setSeats(List<List<Integer>> seats) { this.seats = seats; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public double getPricePaid() { return pricePaid; }
    public void setPricePaid(double pricePaid) { this.pricePaid = pricePaid; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public LocalDateTime getScreeningTime() { return screeningTime; }
    public void setScreeningTime(LocalDateTime screeningTime) { this.screeningTime = screeningTime; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }
}