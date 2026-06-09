package api;

import model.GuestClient;
import model.User;
import service.DataStore;
import util.AuthFilter;
import util.JsonUtil;
import spark.Request;
import spark.Response;
import java.util.Map;
import java.util.regex.Pattern;
import static spark.Spark.*;

public class UserRoutes {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public void register() {
        get("/users", this::getAll);
        put("/users", this::updateUser);
        delete("/users/:id", this::deleteUser);
        post("/users/guest", this::createGuest);
        get("/reports/movies", this::getMovieReports);
    }

    private Object getAll(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");
        return JsonUtil.toJson(ApiResponse.ok(DataStore.getInstance().getUsers()));
    }

    private Object getMovieReports(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN");
        try {
            return JsonUtil.toJson(ApiResponse.ok(DataStore.getInstance().getMovieReports()));
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.toJson(ApiResponse.error("Błąd serwera podczas pobierania raportu: " + e.getMessage()));
        }
    }

    private Object updateUser(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN");

        try {
            Map<?, ?> body = JsonUtil.fromJson(req.body(), Map.class);
            if (body == null || body.get("id") == null) {
                return JsonUtil.toJson(ApiResponse.error("Nieprawidłowe dane użytkownika."));
            }

            int id = ((Number) body.get("id")).intValue();
            String newRole = (String) body.get("role");
            String name = (String) body.get("name");
            String email = (String) body.get("email");

            if (id == 1 && !"ADMIN".equals(newRole)) {
                return JsonUtil.toJson(ApiResponse.error("Nie można odebrać roli głównemu Administratorowi!"));
            }

            DataStore ds = DataStore.getInstance();
            User existingUser = ds.getUsers().stream()
                    .filter(u -> u.getId() == id)
                    .findFirst()
                    .orElse(null);

            if (existingUser == null) {
                return JsonUtil.toJson(ApiResponse.error("Użytkownik nie istnieje w bazie danych."));
            }

            ds.deleteUser(id);

            User updatedUser;
            String pwdHash = existingUser.getPasswordHash();
            String finalName = (name != null) ? name : existingUser.getName();
            String finalEmail = (email != null) ? email : existingUser.getEmail();

            if ("ADMIN".equalsIgnoreCase(newRole)) {
                updatedUser = new model.Admin(id, finalName, finalEmail, pwdHash);
            } else if ("EMPLOYEE".equalsIgnoreCase(newRole)) {
                updatedUser = new model.Employee(id, finalName, finalEmail, pwdHash);
            } else {
                updatedUser = new model.Client(id, finalName, finalEmail, pwdHash);
            }

            ds.insertUser(updatedUser);

            return JsonUtil.toJson(ApiResponse.ok());
        } catch (Exception e) {
            e.printStackTrace();
            return JsonUtil.toJson(ApiResponse.error("Błąd serwera: " + e.getMessage()));
        }
    }

    private Object deleteUser(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");
        int id = Integer.parseInt(req.params("id"));

        if (id == 1) {
            return JsonUtil.toJson(ApiResponse.error("Nie można usunąć głównego Administratora."));
        }

        DataStore.getInstance().deleteUser(id);
        return JsonUtil.toJson(ApiResponse.ok());
    }

    private Object createGuest(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");
        Map<?, ?> body = JsonUtil.fromJson(req.body(), Map.class);
        String email = body != null ? (String) body.get("email") : null;
        DataStore ds = DataStore.getInstance();

        if (email != null && !email.isBlank()) {
            if (!EMAIL_PATTERN.matcher(email).matches())
                return JsonUtil.toJson(ApiResponse.error("Nieprawidłowy format email."));
            User existing = ds.getUsers().stream().filter(u -> email.equalsIgnoreCase(u.getEmail())).findFirst().orElse(null);
            if (existing != null)
                return JsonUtil.toJson(ApiResponse.ok(existing));

            GuestClient guest = new GuestClient(0, email);
            ds.insertUser(guest);
            return JsonUtil.toJson(ApiResponse.ok(guest));
        }

        GuestClient guest = new GuestClient(0);
        ds.insertUser(guest);
        return JsonUtil.toJson(ApiResponse.ok(guest));
    }
}