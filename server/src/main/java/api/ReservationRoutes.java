package api;

import io.jsonwebtoken.Claims;
import model.Reservation;
import model.Reservation.Status;
import model.Screening;
import service.DataStore;
import service.ReservationFactory;
import util.AuthFilter;
import util.JsonUtil;
import spark.Request;
import spark.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static spark.Spark.*;

public class ReservationRoutes {

    public void register() {
        get("/reservations", this::getAll);
        get("/reservations/user/:userId", this::getByUser);
        get("/reservations/screening/:id", this::getByScreening);
        post("/reservations/reserve", this::reserve);
        post("/reservations/buy", this::buy);
        put("/reservations/:id/pay", this::pay);
        put("/reservations/:id/cancel", this::cancel);
    }

    private Object getAll(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireRole(req, res, "ADMIN", "EMPLOYEE");
        return JsonUtil.toJson(ApiResponse.ok(DataStore.getInstance().getReservations()));
    }

    private Object getByUser(Request req, Response res) {
        res.type("application/json");
        Claims claims = AuthFilter.requireAuth(req, res);
        int requestedId = Integer.parseInt(req.params("userId"));
        String role = claims.get("role", String.class);
        int callerId = Integer.parseInt(claims.getSubject());

        if (!role.equals("ADMIN") && !role.equals("EMPLOYEE") && callerId != requestedId)
            return JsonUtil.toJson(ApiResponse.error("Brak uprawnień."));

        var list = DataStore.getInstance().getReservations().stream().filter(r -> r.getUserId() == requestedId).toList();
        return JsonUtil.toJson(ApiResponse.ok(list));
    }

    private Object getByScreening(Request req, Response res) {
        res.type("application/json");
        AuthFilter.requireAuth(req, res);
        int screeningId = Integer.parseInt(req.params("id"));
        var list = DataStore.getInstance().getReservations().stream().filter(r -> r.getScreeningId() == screeningId).toList();
        return JsonUtil.toJson(ApiResponse.ok(list));
    }

    private Object reserve(Request req, Response res) {
        res.type("application/json");
        Claims claims = AuthFilter.requireAuth(req, res);
        String role = claims.get("role", String.class);

        if ("ADMIN".equals(role) || "EMPLOYEE".equals(role))
            return JsonUtil.toJson(ApiResponse.error("Pracownicy i admini nie mogą rezerwować."));

        Map<?, ?> body = JsonUtil.fromJson(req.body(), Map.class);
        int userId = Integer.parseInt(claims.getSubject());
        int screeningId = ((Number) body.get("screeningId")).intValue();
        List<List<Integer>> seats = parseSeats((List<?>) body.get("seats"));

        String err = checkSeats(screeningId, seats);
        if (err != null) return JsonUtil.toJson(ApiResponse.error(err));

        DataStore ds = DataStore.getInstance();
        Reservation r = ReservationFactory.reserved(0, userId, screeningId, seats);
        ds.insertReservation(r);
        return JsonUtil.toJson(ApiResponse.ok(r));
    }

    private Object buy(Request req, Response res) {
        res.type("application/json");
        Claims claims = AuthFilter.requireAuth(req, res);
        Map<?, ?> body = JsonUtil.fromJson(req.body(), Map.class);
        int screeningId = ((Number) body.get("screeningId")).intValue();
        List<List<Integer>> seats = parseSeats((List<?>) body.get("seats"));
        String role = claims.get("role", String.class);
        int userId;

        DataStore ds = DataStore.getInstance();
        if ("ADMIN".equals(role) || "EMPLOYEE".equals(role)) {
            Object userIdRaw = body.get("userId");
            if (userIdRaw != null) {
                userId = ((Number) userIdRaw).intValue();
            } else {
                model.GuestClient guest = new model.GuestClient(0);
                ds.insertUser(guest);
                userId = guest.getId();
            }
        } else {
            userId = Integer.parseInt(claims.getSubject());
        }

        String err = checkSeats(screeningId, seats);
        if (err != null) return JsonUtil.toJson(ApiResponse.error(err));

        double price = ds.getScreenings().stream().filter(s -> s.getId() == screeningId).mapToDouble(Screening::getTicketPrice).findFirst().orElse(0.0) * seats.size();
        Reservation r = ReservationFactory.paid(0, userId, screeningId, seats, price);
        ds.insertReservation(r);
        return JsonUtil.toJson(ApiResponse.ok(r));
    }

    private Object pay(Request req, Response res) {
        res.type("application/json");
        Claims claims = AuthFilter.requireAuth(req, res);
        int id = Integer.parseInt(req.params("id"));
        DataStore ds = DataStore.getInstance();
        Reservation r = ds.getReservations().stream().filter(x -> x.getId() == id).findFirst().orElse(null);

        if (r == null) return JsonUtil.toJson(ApiResponse.error("Rezerwacja nie istnieje."));

        String role = claims.get("role", String.class);
        int callerId = Integer.parseInt(claims.getSubject());

        if (!role.equals("ADMIN") && !role.equals("EMPLOYEE") && r.getUserId() != callerId)
            return JsonUtil.toJson(ApiResponse.error("Brak uprawnień."));

        if (r.getStatus() != Status.RESERVED)
            return JsonUtil.toJson(ApiResponse.error("Można opłacić tylko RESERVED."));

        r.setStatus(Status.PAID);
        ds.updateReservation(r);
        return JsonUtil.toJson(ApiResponse.ok(r));
    }

    private Object cancel(Request req, Response res) {
        res.type("application/json");
        Claims claims = AuthFilter.requireAuth(req, res);
        int id = Integer.parseInt(req.params("id"));
        DataStore ds = DataStore.getInstance();
        Reservation r = ds.getReservations().stream().filter(x -> x.getId() == id).findFirst().orElse(null);

        if (r == null) return JsonUtil.toJson(ApiResponse.error("Rezerwacja nie istnieje."));

        String role = claims.get("role", String.class);
        int callerId = Integer.parseInt(claims.getSubject());

        if (!role.equals("ADMIN") && !role.equals("EMPLOYEE") && r.getUserId() != callerId)
            return JsonUtil.toJson(ApiResponse.error("Brak uprawnień."));

        if (r.getStatus() == Status.CANCELLED)
            return JsonUtil.toJson(ApiResponse.error("Już anulowana."));

        r.setStatus(Status.CANCELLED);
        ds.updateReservation(r);
        return JsonUtil.toJson(ApiResponse.ok(r));
    }

    private String checkSeats(int screeningId, List<List<Integer>> seats) {
        DataStore ds = DataStore.getInstance();
        if (ds.getScreenings().stream().noneMatch(s -> s.getId() == screeningId))
            return "Seans nie istnieje.";
        if (seats == null || seats.isEmpty())
            return "Wybierz co najmniej jedno miejsce.";

        for (List<Integer> seat : seats) {
            boolean taken = ds.getReservations().stream()
                    .filter(r -> r.getScreeningId() == screeningId)
                    .filter(r -> r.getStatus() != Status.CANCELLED)
                    .flatMap(r -> r.getSeats().stream())
                    .anyMatch(s -> s.get(0).equals(seat.get(0)) && s.get(1).equals(seat.get(1)));
            if (taken)
                return "Miejsce " + (char) ('A' + seat.get(0) - 1) + seat.get(1) + " jest już zajęte.";
        }
        return null;
    }

    private List<List<Integer>> parseSeats(List<?> raw) {
        List<List<Integer>> seats = new ArrayList<>();
        if (raw == null) return seats;
        for (Object item : raw) {
            if (item instanceof List<?> pair) {
                List<Integer> seat = new ArrayList<>();
                seat.add(((Number) pair.get(0)).intValue());
                seat.add(((Number) pair.get(1)).intValue());
                seats.add(seat);
            }
        }
        return seats;
    }
}