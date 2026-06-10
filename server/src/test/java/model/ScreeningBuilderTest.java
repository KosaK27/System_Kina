package model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

public class ScreeningBuilderTest {

    @Test
    public void shouldBuildScreeningObjectCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        Screening screening = Screening.builder()
                .id(10)
                .movieId(5)
                .hallId(2)
                .startTime(now)
                .ticketPrice(25.50)
                .build();

        assertNotNull(screening);
        assertEquals(10, screening.getId());
        assertEquals(5, screening.getMovieId());
        assertEquals(2, screening.getHallId());
        assertEquals(now, screening.getStartTime());
        assertEquals(25.50, screening.getTicketPrice());
    }
}