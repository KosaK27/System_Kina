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
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public void register() {
        get("/users", this::getAll);
        delete("/users/:id", this::deleteUser);
        post("/users/guest", this::createGuest);
    }

    private Object getAll(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");
        return JsonUtil.toJson(ApiResponse.ok(DataStore.getInstance().getUsers()));
    }

    private Object deleteUser(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");

        int id = Integer.parseInt(req.params("id"));
        DataStore ds = DataStore.getInstance();

        ds.getUsers().removeIf(u -> u.getId() == id);
        ds.saveUsers();

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

            User existing = ds.getUsers().stream()
                    .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                    .findFirst()
                    .orElse(null);

            if (existing != null)
                return JsonUtil.toJson(ApiResponse.ok(existing));

            GuestClient guest = new GuestClient(ds.nextUserId(), email);
            ds.getUsers().add(guest);
            ds.saveUsers();

            return JsonUtil.toJson(ApiResponse.ok(guest));
        }

        GuestClient guest = new GuestClient(ds.nextUserId());
        ds.getUsers().add(guest);
        ds.saveUsers();

        return JsonUtil.toJson(ApiResponse.ok(guest));
    }
}