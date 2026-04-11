package util;

import io.jsonwebtoken.Claims;
import spark.Request;
import spark.Response;

public class AuthFilter {

    public static Claims requireAuth(Request req, Response res) {
        String header = req.headers("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            halt(res, 401, "Brak tokenu autoryzacji.");
        }
        try {
            return JwtUtil.verify(header.substring(7));
        } catch (Exception e) {
            halt(res, 401, "Nieprawidłowy lub wygasły token.");
            return null;
        }
    }

    public static Claims requireRole(Request req, Response res, String... roles) {
        Claims claims = requireAuth(req, res);
        String role = claims.get("role", String.class);
        for (String r : roles) {
            if (r.equals(role)) return claims;
        }
        halt(res, 403, "Brak uprawnień.");
        return null;
    }

    private static void halt(Response res, int code, String message) {
        res.type("application/json");
        res.status(code);
        res.body("{\"success\":false,\"message\":\"" + message + "\"}");
        spark.Spark.halt(code, message);
    }
}