package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class HallTest {

    @Test
    public void shouldSetAndGetHallPropertiesCorrectly() {
        Hall hall = new Hall();
        hall.setId(5);
        hall.setName("Sala VIP");

        assertEquals(5, hall.getId());
        assertEquals("Sala VIP", hall.getName());
    }
}