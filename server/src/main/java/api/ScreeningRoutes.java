package api;

import model.Screening;
import service.DataStore;
import util.AuthFilter;
import util.JsonUtil;
import spark.Request;
import spark.Response;

import java.time.LocalDateTime;
import java.util.List;

import static spark.Spark.*;

public class ScreeningRoutes {

    public void register() {
        get("/screenings", this::getAll);
        get("/screenings/movie/:id", this::getByMovie);
        post("/screenings", this::create);
        put("/screenings/:id", this::update);
        delete("/screenings/:id", this::deleteScreening);
    }

    private Object getAll(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireAuth(req, res);
        return JsonUtil.toJson(ApiResponse.ok(DataStore.getInstance().getScreenings()));
    }

    private Object getByMovie(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireAuth(req, res);

        int movieId = Integer.parseInt(req.params("id"));

        List<Screening> result = DataStore.getInstance().getScreenings().stream()
                .filter(s -> s.getMovieId() == movieId)
                .toList();

        return JsonUtil.toJson(ApiResponse.ok(result));
    }

    private Object create(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");

        Screening s = JsonUtil.fromJson(req.body(), Screening.class);

        String err = validate(s, -1);
        if (err != null) return JsonUtil.toJson(ApiResponse.error(err));

        DataStore ds = DataStore.getInstance();

        s.setId(ds.nextScreeningId());
        ds.getScreenings().add(s);
        ds.saveScreenings();

        return JsonUtil.toJson(ApiResponse.ok(s));
    }

    private Object update(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");

        int id = Integer.parseInt(req.params("id"));
        Screening updated = JsonUtil.fromJson(req.body(), Screening.class);
        updated.setId(id);

        String err = validate(updated, id);
        if (err != null) return JsonUtil.toJson(ApiResponse.error(err));

        DataStore ds = DataStore.getInstance();
        ds.getScreenings().replaceAll(s -> s.getId() == id ? updated : s);
        ds.saveScreenings();

        return JsonUtil.toJson(ApiResponse.ok(updated));
    }

    private Object deleteScreening(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");

        int id = Integer.parseInt(req.params("id"));
        DataStore ds = DataStore.getInstance();

        ds.getScreenings().removeIf(s -> s.getId() == id);
        ds.saveScreenings();

        return JsonUtil.toJson(ApiResponse.ok());
    }

    private String validate(Screening s, int excludeId) {
        if (s.getStartTime() == null)
            return "Data seansu nie może być pusta.";
        if (s.getStartTime().isBefore(LocalDateTime.now()))
            return "Nie można dodać seansu w przeszłości.";
        if (s.getTicketPrice() <= 0)
            return "Cena musi być dodatnia.";

        DataStore ds = DataStore.getInstance();

        if (ds.getMovies().stream().noneMatch(m -> m.getId() == s.getMovieId()))
            return "Wybrany film nie istnieje.";

        if (ds.getHalls().stream().noneMatch(h -> h.getId() == s.getHallId()))
            return "Wybrana sala nie istnieje.";

        int duration = ds.getMovies().stream()
                .filter(m -> m.getId() == s.getMovieId())
                .findFirst()
                .map(model.Movie::getDuration)
                .orElse(120);

        LocalDateTime end = s.getStartTime().plusMinutes(duration + 15);

        boolean collision = ds.getScreenings().stream()
                .filter(x -> x.getId() != excludeId)
                .filter(x -> x.getHallId() == s.getHallId())
                .anyMatch(x -> {
                    int dur = ds.getMovies().stream()
                            .filter(m -> m.getId() == x.getMovieId())
                            .findFirst()
                            .map(model.Movie::getDuration)
                            .orElse(120);

                    LocalDateTime xEnd = x.getStartTime().plusMinutes(dur + 15);

                    return s.getStartTime().isBefore(xEnd)
                            && end.isAfter(x.getStartTime());
                });

        if (collision)
            return "Sala jest zajęta w tym czasie (uwzględniono 15 min przerwy).";

        return null;
    }
}