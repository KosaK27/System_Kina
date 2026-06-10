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


    public static class Builder {
        private int id;
        private int movieId;
        private int hallId;
        private LocalDateTime startTime;
        private double ticketPrice;
        private String movieTitle;
        private String hallName;

        public Builder id(int id) { this.id = id; return this; }
        public Builder movieId(int movieId) { this.movieId = movieId; return this; }
        public Builder hallId(int hallId) { this.hallId = hallId; return this; }
        public Builder startTime(LocalDateTime startTime) { this.startTime = startTime; return this; }
        public Builder ticketPrice(double ticketPrice) { this.ticketPrice = ticketPrice; return this; }
        public Builder movieTitle(String movieTitle) { this.movieTitle = movieTitle; return this; }
        public Builder hallName(String hallName) { this.hallName = hallName; return this; }

        public Screening build() {
            Screening screening = new Screening(id, movieId, hallId, startTime, ticketPrice);
            screening.setMovieTitle(this.movieTitle);
            screening.setHallName(this.hallName);
            return screening;
        }
    }

    public static Builder builder() {
        return new Builder();
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