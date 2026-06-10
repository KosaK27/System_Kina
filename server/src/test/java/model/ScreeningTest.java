package model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class ScreeningTest {

    @Test
    public void shouldReturnTrueWhenScreeningIsInThePast() {
        Screening screening = new Screening();
        screening.setStartTime(LocalDateTime.now().minusDays(1));

        assertTrue(screening.isPast(), "Seans z wczoraj powinien być oznaczony jako przeszły");
    }

    @Test
    public void shouldReturnFalseWhenScreeningIsInTheFuture() {
        Screening screening = new Screening();
        screening.setStartTime(LocalDateTime.now().plusDays(1));

        assertFalse(screening.isPast(), "Seans z jutra nie może być w przeszłości");
    }

    @Test
    public void shouldReturnTrueWhenScreeningIsOngoing() {
        Screening screening = new Screening();
        screening.setStartTime(LocalDateTime.now().minusHours(1));

        assertTrue(screening.isOngoing(), "Seans, który zaczął się godzinę temu, powinien trwać");
    }

    @Test
    public void shouldReturnFalseWhenScreeningNotStartedYet() {
        Screening screening = new Screening();
        screening.setStartTime(LocalDateTime.now().plusHours(2));

        assertFalse(screening.isOngoing(), "Seans z przyszłości nie może mieć statusu trwającego");
    }
}