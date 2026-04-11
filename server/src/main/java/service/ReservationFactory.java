package service;

import model.Reservation;
import model.Reservation.Status;

import java.time.LocalDateTime;
import java.util.List;

public class ReservationFactory {

    public static Reservation reserved(int id, int userId, int screeningId, List<List<Integer>> seats) {
        return new Reservation(id, userId, screeningId, seats,
                Status.RESERVED, LocalDateTime.now(), 0.0);
    }

    public static Reservation paid(int id, int userId, int screeningId, List<List<Integer>> seats, double price) {
        return new Reservation(id, userId, screeningId, seats,
                Status.PAID, LocalDateTime.now(), price);
    }
}