package model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Screening {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private int id;
    private int movieId;
    private int hallId;
    private LocalDateTime startTime;
    private double ticketPrice;

    @JsonIgnore private String movieTitle;
    @JsonIgnore private String hallName;

    public Screening() {}

    public Screening(int id, int movieId, int hallId, LocalDateTime startTime, double ticketPrice) {
        this.id = id;
        this.movieId = movieId;
        this.hallId = hallId;
        this.startTime = startTime;
        this.ticketPrice = ticketPrice;
    }

    @JsonIgnore
    public boolean isPast() {
        return startTime != null && startTime.isBefore(LocalDateTime.now());
    }

    @JsonIgnore
    public boolean isOngoing() {
        if (startTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !startTime.isAfter(now) && startTime.plusHours(4).isAfter(now);
    }

    @JsonIgnore
    public String getFormattedTime() {
        return startTime != null ? startTime.format(FMT) : "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }
    public int getHallId() { return hallId; }
    public void setHallId(int hallId) { this.hallId = hallId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String t) { this.movieTitle = t; }
    public String getHallName() { return hallName; }
    public void setHallName(String h) { this.hallName = h; }
}