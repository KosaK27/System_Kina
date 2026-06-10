package factory;

import model.*;

public class UserFactory {

    public static User createUser(String role) {
        if (role == null) {
            return new GuestClient();
        }

        return switch (role.trim().toUpperCase()) {
            case "ADMIN" -> new Admin();
            case "EMPLOYEE" -> new Employee();
            case "CLIENT" -> new Client();
            default -> new GuestClient();
        };
    }
}