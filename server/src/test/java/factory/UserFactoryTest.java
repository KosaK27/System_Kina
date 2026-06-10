package factory;

import model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserFactoryTest {

    @Test
    public void shouldCreateAdminObjectWhenRoleIsAdmin() {
        String role = "ADMIN";

        User user = UserFactory.createUser(role);

        assertNotNull(user, "Obiekt użytkownika nie powinien być nullem");
        assertTrue(user instanceof Admin, "Dla roli ADMIN fabryka powinna utworzyć obiekt klasy Admin");
    }

    @Test
    public void shouldCreateClientObjectWhenRoleIsClientWithSpacesOrLowerCases() {
        String role = "  client  ";

        User user = UserFactory.createUser(role);

        assertNotNull(user);
        assertTrue(user instanceof Client, "Fabryka powinna ignorować wielkość liter i spacje");
    }

    @Test
    public void shouldReturnGuestClientWhenRoleIsUnknownOrNull() {
        String unknownRole = "SUPER_USER";
        String nullRole = null;

        User user1 = UserFactory.createUser(unknownRole);
        User user2 = UserFactory.createUser(nullRole);

        assertTrue(user1 instanceof GuestClient, "Dla nieznanej roli powinien powstać GuestClient");
        assertTrue(user2 instanceof GuestClient, "Dla roli null powinien powstać GuestClient");
    }
}