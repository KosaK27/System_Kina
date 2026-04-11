package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Reservation {
    public enum Status { RESERVED, PAID, CANCELLED }

    private int id;
    private int userId;
    private int screeningId;
    private List<List<Integer>> seats = new ArrayList<>();
    private Status status;
    private LocalDateTime createdAt;
    private double pricePaid;

    public Reservation() {}

    public Reservation(int id, int userId, int screeningId, List<List<Integer>> seats,
                       Status status, LocalDateTime createdAt, double pricePaid) {
        this.id = id;
        this.userId = userId;
        this.screeningId = screeningId;
        this.seats = seats;
        this.status = status;
        this.createdAt = createdAt;
        this.pricePaid = pricePaid;
    }

    public String getSeatsLabel() {
        if (seats == null || seats.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (List<Integer> s : seats) {
            if (sb.length() > 0) sb.append(", ");
            sb.append((char) ('A' + s.get(0) - 1)).append(s.get(1));
        }
        return sb.toString();
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
}