package model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void shouldStoreAndRetrieveClientDataCorrectly() {
        User client = new Client();
        client.setId(1);
        client.setEmail("test@kino.pl");
        client.setName("Jan");

        assertEquals(1, client.getId());
        assertEquals("test@kino.pl", client.getEmail());
        assertEquals("Jan", client.getName());
    }

    @Test
    public void shouldInitializeAdminCorrectly() {
        User admin = new Admin();

        assertNotNull(admin);
        assertTrue(admin instanceof Admin, "Obiekt powinien być instancją klasy Admin");
    }

    @Test
    public void shouldInitializeEmployeeCorrectly() {
        User employee = new Employee();
        assertNotNull(employee);
        assertTrue(employee instanceof Employee, "Obiekt powinien być instancją klasy Employee");
    }
}