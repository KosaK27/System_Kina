package api;

import model.Movie;
import service.DataStore;
import util.AuthFilter;
import util.JsonUtil;
import spark.Request;
import spark.Response;
import static spark.Spark.*;

public class MovieRoutes {

    public void register() {
        get("/movies", this::getAll);
        post("/movies", this::create);
        put("/movies/:id", this::update);
        delete("/movies/:id", this::deleteMovie);
    }

    private Object getAll(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireAuth(req, res);
        return JsonUtil.toJson(ApiResponse.ok(DataStore.getInstance().getMovies()));
    }

    private Object create(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");
        Movie movie = JsonUtil.fromJson(req.body(), Movie.class);
        String err = validate(movie, -1);
        if (err != null) return JsonUtil.toJson(ApiResponse.error(err));

        DataStore.getInstance().insertMovie(movie);
        return JsonUtil.toJson(ApiResponse.ok(movie));
    }

    private Object update(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");
        int id = Integer.parseInt(req.params("id"));
        Movie updated = JsonUtil.fromJson(req.body(), Movie.class);
        updated.setId(id);

        String err = validate(updated, id);
        if (err != null) return JsonUtil.toJson(ApiResponse.error(err));

        DataStore.getInstance().updateMovie(updated);
        return JsonUtil.toJson(ApiResponse.ok(updated));
    }

    private Object deleteMovie(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");
        int id = Integer.parseInt(req.params("id"));
        DataStore.getInstance().deleteMovie(id);
        return JsonUtil.toJson(ApiResponse.ok());
    }

    private String validate(Movie m, int excludeId) {
        if (m.getTitle() == null || m.getTitle().isBlank())
            return "Tytuł nie może być pusty.";
        if (m.getDescription() == null || m.getDescription().isBlank())
            return "Opis nie może być pusty.";
        if (m.getDuration() < Movie.MIN_DURATION || m.getDuration() > Movie.MAX_DURATION)
            return "Czas trwania: " + Movie.MIN_DURATION + "–" + Movie.MAX_DURATION + " min.";
        if (m.getGenre() == null || !Movie.GENRES.contains(m.getGenre()))
            return "Nieznany gatunek.";
        boolean dup = DataStore.getInstance().getMovies().stream()
                .filter(x -> x.getId() != excludeId)
                .anyMatch(x -> x.getTitle().equalsIgnoreCase(m.getTitle().trim()));
        if (dup) return "Film o tym tytule już istnieje.";
        return null;
    }
}