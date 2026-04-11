package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Screening {
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private int id;
    private int movieId;
    private int hallId;
    private LocalDateTime startTime;
    private double ticketPrice;

    private String movieTitle;
    private String hallName;

    public Screening() {}

    public boolean isPast() {
        return startTime != null && startTime.isBefore(LocalDateTime.now());
    }

    public boolean isOngoing() {
        if (startTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return !startTime.isAfter(now) && startTime.plusHours(4).isAfter(now);
    }

    public String getFormattedTime() {
        return startTime != null ? startTime.format(FMT) : "";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getMovieId() { return movieId; }
    public void setMovieId(int m) { this.movieId = m; }
    public int getHallId() { return hallId; }
    public void setHallId(int h) { this.hallId = h; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime t) { this.startTime = t; }
    public double getTicketPrice() { return ticketPrice; }
    public void setTicketPrice(double p) { this.ticketPrice = p; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String t) { this.movieTitle = t; }
    public String getHallName() { return hallName; }
    public void setHallName(String h) { this.hallName = h; }

    @Override
    public String toString() {
        String h = hallName != null ? hallName : "Sala #" + hallId;
        return h + " | " + getFormattedTime() + " | " + ticketPrice + " zł";
    }
}