package api;

import model.*;
import service.DataStore;
import util.JwtUtil;
import util.PasswordUtil;
import util.JsonUtil;
import spark.Request;
import spark.Response;
import java.util.Map;
import java.util.regex.Pattern;
import static spark.Spark.*;

public class AuthRoutes {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public void register() {
        post("/auth/login", this::login);
        post("/auth/register", this::registerUser);
    }

    private Object login(Request req, Response res) {
        res.type("application/json");
        Map<?, ?> body = JsonUtil.fromJson(req.body(), Map.class);
        String credential = (String) body.get("credential");
        String password = (String) body.get("password");

        if (credential == null || password == null)
            return JsonUtil.toJson(ApiResponse.error("Wypełnij wszystkie pola."));

        DataStore ds = DataStore.getInstance();
        User user = ds.getUsers().stream()
                .filter(u -> u.canLogin())
                .filter(u -> u.getName().equalsIgnoreCase(credential) || credential.equalsIgnoreCase(u.getEmail()))
                .findFirst().orElse(null);

        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash()))
            return JsonUtil.toJson(ApiResponse.error("Błędne dane logowania."));

        String token = JwtUtil.generate(user.getId(), user.getRole());
        return JsonUtil.toJson(ApiResponse.ok(Map.of(
                "token", token,
                "id", user.getId(),
                "name", user.getName(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "role", user.getRole()
        )));
    }

    private Object registerUser(Request req, Response res) {
        res.type("application/json");
        Map<?, ?> body = JsonUtil.fromJson(req.body(), Map.class);
        String name = (String) body.get("name");
        String email = (String) body.get("email");
        String password = (String) body.get("password");

        if (name == null || name.isBlank())
            return JsonUtil.toJson(ApiResponse.error("Nazwa nie może być pusta."));

        if (email == null || !EMAIL_PATTERN.matcher(email).matches())
            return JsonUtil.toJson(ApiResponse.error("Nieprawidłowy format email."));

        if (password == null || password.length() < 4)
            return JsonUtil.toJson(ApiResponse.error("Hasło musi mieć co najmniej 4 znaki."));

        DataStore ds = DataStore.getInstance();
        boolean nameTaken = ds.getUsers().stream().anyMatch(u -> u.getName().equalsIgnoreCase(name));
        if (nameTaken)
            return JsonUtil.toJson(ApiResponse.error("Nazwa użytkownika już istnieje."));

        User existingByEmail = ds.getUsers().stream().filter(u -> email.equalsIgnoreCase(u.getEmail())).findFirst().orElse(null);

        if (existingByEmail instanceof GuestClient) {
            existingByEmail.setName(name);
            existingByEmail.setPasswordHash(PasswordUtil.hash(password));
            Client upgraded = new Client(existingByEmail.getId(), name, email, PasswordUtil.hash(password));
            ds.updateUser(upgraded);
            return JsonUtil.toJson(ApiResponse.ok());
        }

        if (existingByEmail != null)
            return JsonUtil.toJson(ApiResponse.error("Email jest już zajęty."));

        ds.insertUser(new Client(0, name, email, PasswordUtil.hash(password)));
        return JsonUtil.toJson(ApiResponse.ok());
    }
}