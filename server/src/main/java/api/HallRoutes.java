package api;

import model.Hall;
import service.DataStore;
import util.AuthFilter;
import util.JsonUtil;
import spark.Request;
import spark.Response;
import static spark.Spark.*;

public class HallRoutes {

    public void register() {
        get("/halls", this::getAll);
        post("/halls", this::create);
        put("/halls/:id", this::updateName);
        delete("/halls/:id", this::deleteHall);
    }

    private Object getAll(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireAuth(req, res);
        return JsonUtil.toJson(ApiResponse.ok(DataStore.getInstance().getHalls()));
    }

    private Object create(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN");
        Hall hall = JsonUtil.fromJson(req.body(), Hall.class);
        String err = validate(hall, false);
        if (err != null) return JsonUtil.toJson(ApiResponse.error(err));

        DataStore.getInstance().insertHall(hall);
        return JsonUtil.toJson(ApiResponse.ok(hall));
    }

    private Object updateName(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN");
        int id = Integer.parseInt(req.params("id"));
        Hall incoming = JsonUtil.fromJson(req.body(), Hall.class);
        if (incoming.getName() == null || incoming.getName().isBlank())
            return JsonUtil.toJson(ApiResponse.error("Nazwa nie może być pusta."));

        DataStore ds = DataStore.getInstance();
        Hall existing = ds.getHalls().stream().filter(h -> h.getId() == id).findFirst().orElse(null);
        if (existing == null)
            return JsonUtil.toJson(ApiResponse.error("Sala nie istnieje."));

        existing.setName(incoming.getName());
        ds.updateHall(existing);
        return JsonUtil.toJson(ApiResponse.ok(existing));
    }

    private Object deleteHall(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN");
        int id = Integer.parseInt(req.params("id"));
        DataStore.getInstance().deleteHall(id);
        return JsonUtil.toJson(ApiResponse.ok());
    }

    private String validate(Hall h, boolean nameOnly) {
        if (h.getName() == null || h.getName().isBlank()) return "Nazwa nie może być pusta.";
        if (!nameOnly) {
            if (h.getRows() < 1 || h.getRows() > 20) return "Rzędy: 1–20.";
            if (h.getSeatsPerRow() < 1 || h.getSeatsPerRow() > 30) return "Miejsca: 1–30.";
        }
        return null;
    }
}