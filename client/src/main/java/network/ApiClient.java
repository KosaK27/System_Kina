package network;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ApiClient {
    private static ApiClient instance;
    private static final String BASE = "http://localhost:9999";

    private final HttpClient   http;
    private final ObjectMapper mapper;

    private ApiClient() {
        http = HttpClient.newHttpClient();
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ApiClient getInstance() {
        if (instance == null) instance = new ApiClient();
        return instance;
    }

    private String token() { return model.Session.getToken(); }

    private HttpRequest.Builder base(String path) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE + path))
                .header("Content-Type", "application/json");
        if (token() != null) builder.header("Authorization", "Bearer " + token());
        return builder;
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(base(path).GET().build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, Object body) throws Exception {
        String json = mapper.writeValueAsString(body);
        return http.send(base(path).POST(HttpRequest.BodyPublishers.ofString(json)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> put(String path, Object body) throws Exception {
        String json = body != null ? mapper.writeValueAsString(body) : "{}";
        return http.send(base(path).PUT(HttpRequest.BodyPublishers.ofString(json)).build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path) throws Exception {
        return http.send(base(path).DELETE().build(), HttpResponse.BodyHandlers.ofString());
    }

    private boolean isSuccess(String body) throws Exception {
        return mapper.readTree(body).get("success").asBoolean();
    }

    private String errorMsg(String body) throws Exception {
        var node = mapper.readTree(body).get("message");
        return node != null && !node.isNull() ? node.asText() : "Nieznany błąd.";
    }

    private <T> T extractData(String body, Class<T> clazz) throws Exception {
        return mapper.treeToValue(mapper.readTree(body).get("data"), clazz);
    }

    private <T> List<T> extractList(String body, TypeReference<List<T>> ref) throws Exception {
        return mapper.readValue(
                mapper.treeAsTokens(mapper.readTree(body).get("data")), ref);
    }

    public record LoginResult(User user, String token) {}

    public Optional<LoginResult> login(String credential, String password) throws Exception {
        var res = post("/auth/login", Map.of("credential", credential, "password", password));
        if (!isSuccess(res.body())) return Optional.empty();
        var data = mapper.readTree(res.body()).get("data");
        User user = new User();
        user.setId(data.get("id").asInt());
        user.setName(data.get("name").asText());
        user.setEmail(data.get("email").asText());
        user.setRole(data.get("role").asText());
        String jwt = data.get("token").asText();
        return Optional.of(new LoginResult(user, jwt));
    }

    public Optional<String> register(String name, String email, String password) throws Exception {
        var res = post("/auth/register", Map.of("name", name, "email", email, "password", password));
        if (isSuccess(res.body())) return Optional.empty();
        return Optional.of(errorMsg(res.body()));
    }

    public List<User> getUsers() throws Exception {
        return extractList(get("/users").body(), new TypeReference<>() {});
    }

    public void deleteUser(int id) throws Exception { delete("/users/" + id); }

    public User createGuestUser(String email) throws Exception {
        var res = post("/users/guest", Map.of("email", email));
        if (!isSuccess(res.body())) throw new RuntimeException(errorMsg(res.body()));
        return extractData(res.body(), User.class);
    }

    public List<Movie> getMovies() throws Exception {
        return extractList(get("/movies").body(), new TypeReference<>() {});
    }

    public Optional<String> saveMovie(Movie movie) throws Exception {
        var res = movie.getId() == 0
                ? post("/movies", movie) : put("/movies/" + movie.getId(), movie);
        if (isSuccess(res.body())) return Optional.empty();
        return Optional.of(errorMsg(res.body()));
    }

    public void deleteMovie(int id) throws Exception { delete("/movies/" + id); }

    public List<Hall> getHalls() throws Exception {
        return extractList(get("/halls").body(), new TypeReference<>() {});
    }

    public Optional<String> saveHall(Hall hall) throws Exception {
        var res = hall.getId() == 0
                ? post("/halls", hall) : put("/halls/" + hall.getId(), hall);
        if (isSuccess(res.body())) return Optional.empty();
        return Optional.of(errorMsg(res.body()));
    }

    public void deleteHall(int id) throws Exception { delete("/halls/" + id); }

    public List<Screening> getScreeningsByMovie(int movieId) throws Exception {
        return extractList(get("/screenings/movie/" + movieId).body(), new TypeReference<>() {});
    }

    public List<Screening> getAllScreenings() throws Exception {
        return extractList(get("/screenings").body(), new TypeReference<>() {});
    }

    public Optional<String> saveScreening(Screening s) throws Exception {
        var res = s.getId() == 0
                ? post("/screenings", s) : put("/screenings/" + s.getId(), s);
        if (isSuccess(res.body())) return Optional.empty();
        return Optional.of(errorMsg(res.body()));
    }

    public void deleteScreening(int id) throws Exception { delete("/screenings/" + id); }

    public List<Reservation> getMyReservations(int userId) throws Exception {
        return extractList(get("/reservations/user/" + userId).body(), new TypeReference<>() {});
    }

    public List<Reservation> getAllReservations() throws Exception {
        return extractList(get("/reservations").body(), new TypeReference<>() {});
    }

    public List<Reservation> getReservationsForScreening(int screeningId) throws Exception {
        return extractList(get("/reservations/screening/" + screeningId).body(), new TypeReference<>() {});
    }

    public Optional<String> reserve(int screeningId, List<List<Integer>> seats) throws Exception {
        var res = post("/reservations/reserve",
                Map.of("screeningId", screeningId, "seats", seats));
        if (isSuccess(res.body())) return Optional.empty();
        return Optional.of(errorMsg(res.body()));
    }

    public Optional<String> buyTicket(int screeningId, List<List<Integer>> seats,
                                      Integer userId) throws Exception {
        Object body = userId != null
                ? Map.of("screeningId", screeningId, "seats", seats, "userId", userId)
                : Map.of("screeningId", screeningId, "seats", seats);
        var res = post("/reservations/buy", body);
        if (isSuccess(res.body())) return Optional.empty();
        return Optional.of(errorMsg(res.body()));
    }

    public Optional<String> payForReservation(int id) throws Exception {
        var res = put("/reservations/" + id + "/pay", null);
        if (isSuccess(res.body())) return Optional.empty();
        return Optional.of(errorMsg(res.body()));
    }

    public Optional<String> cancelReservation(int id) throws Exception {
        var res = put("/reservations/" + id + "/cancel", null);
        if (isSuccess(res.body())) return Optional.empty();
        return Optional.of(errorMsg(res.body()));
    }
}