package model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;

public class ReservationTest {

    @Test
    public void shouldInitializeReservationWithCorrectValues() {
        int id = 1;
        int userId = 10;
        int screeningId = 100;
        Reservation.Status status = Reservation.Status.PAID;
        double price = 50.0;

        Reservation reservation = new Reservation(
                id,
                userId,
                screeningId,
                new ArrayList<>(),
                status,
                java.time.LocalDateTime.now(),
                price
        );

        assertEquals(id, reservation.getId());
        assertEquals(userId, reservation.getUserId());
        assertEquals(screeningId, reservation.getScreeningId());
        assertEquals(Reservation.Status.PAID, reservation.getStatus());
    }

    @Test
    public void shouldAllowSettingAndGettingIdAndStatus() {

        Reservation reservation = new Reservation();

        reservation.setId(500);
        reservation.setStatus(Reservation.Status.PAID);

        assertEquals(500, reservation.getId());
        assertEquals(Reservation.Status.PAID, reservation.getStatus());
    }
}