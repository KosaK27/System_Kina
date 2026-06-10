package service;

import model.*;
import util.JsonUtil;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore instance;
    private final String url = "jdbc:oracle:thin:@localhost:1521:xe";
    private final String dbUser = "SYSTEM";
    private final String dbPassword = "qwerty";

    private DataStore() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DataStore getInstance() {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    public static synchronized DataStore getInstance(String dir) {
        if (instance == null) instance = new DataStore();
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, dbUser, dbPassword);
    }

    public List<User> getUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT id, email, password_hash, role, first_name FROM users";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String role = rs.getString("role");
                User u = factory.UserFactory.createUser(role);

                u.setId(rs.getInt("id"));
                u.setEmail(rs.getString("email"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setName(rs.getString("first_name"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insertUser(User u) {
        String sql = "INSERT INTO users (email, password_hash, role, first_name) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setString(1, u.getEmail());
            stmt.setString(2, u.getPasswordHash());
            stmt.setString(3, u.getRole());
            stmt.setString(4, u.getName());
            stmt.executeUpdate();
            try (ResultSet gk = stmt.getGeneratedKeys()) {
                if (gk.next()) u.setId(gk.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateUser(User u) {
        String sql = "UPDATE users SET email = ?, role = ?, first_name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, u.getEmail());
            stmt.setString(2, u.getRole());
            stmt.setString(3, u.getName());
            stmt.setInt(4, u.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Movie> getMovies() {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT id, title, description, duration_minutes, genre FROM movies";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Movie m = new Movie(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("duration_minutes"),
                        rs.getString("genre")
                );
                list.add(m);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insertMovie(Movie m) {
        String sql = "INSERT INTO movies (title, description, duration_minutes, genre) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setString(1, m.getTitle());
            stmt.setString(2, m.getDescription());
            stmt.setInt(3, m.getDuration());
            stmt.setString(4, m.getGenre());
            stmt.executeUpdate();
            try (ResultSet gk = stmt.getGeneratedKeys()) {
                if (gk.next()) m.setId(gk.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateMovie(Movie m) {
        String sql = "UPDATE movies SET title = ?, description = ?, duration_minutes = ?, genre = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, m.getTitle());
            stmt.setString(2, m.getDescription());
            stmt.setInt(3, m.getDuration());
            stmt.setString(4, m.getGenre());
            stmt.setInt(5, m.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMovie(int id) {
        String sql = "DELETE FROM movies WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Hall> getHalls() {
        List<Hall> list = new ArrayList<>();
        String sql = "SELECT id, name, rows_count, seats_per_row FROM halls";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Hall h = new Hall(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("rows_count"),
                        rs.getInt("seats_per_row")
                );
                list.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insertHall(Hall h) {
        String sql = "INSERT INTO halls (name, rows_count, seats_per_row) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setString(1, h.getName());
            stmt.setInt(2, h.getRows());
            stmt.setInt(3, h.getSeatsPerRow());
            stmt.executeUpdate();
            try (ResultSet gk = stmt.getGeneratedKeys()) {
                if (gk.next()) h.setId(gk.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateHall(Hall h) {
        String sql = "UPDATE halls SET name = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, h.getName());
            stmt.setInt(2, h.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteHall(int id) {
        String sql = "DELETE FROM halls WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Screening> getScreenings() {
        List<Screening> list = new ArrayList<>();
        String sql = "SELECT id, movie_id, hall_id, screening_time, ticket_price FROM screenings";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {

                Screening s = Screening.builder()
                        .id(rs.getInt("id"))
                        .movieId(rs.getInt("movie_id"))
                        .hallId(rs.getInt("hall_id"))
                        .startTime(rs.getTimestamp("screening_time").toLocalDateTime())
                        .ticketPrice(rs.getDouble("ticket_price"))
                        .build();

                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insertScreening(Screening s) {
        String sql = "INSERT INTO screenings (movie_id, hall_id, screening_time, ticket_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"ID"})) {
            stmt.setInt(1, s.getMovieId());
            stmt.setInt(2, s.getHallId());
            stmt.setTimestamp(3, Timestamp.valueOf(s.getStartTime()));
            stmt.setDouble(4, s.getTicketPrice());
            stmt.executeUpdate();
            try (ResultSet gk = stmt.getGeneratedKeys()) {
                if (gk.next()) s.setId(gk.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateScreening(Screening s) {
        String sql = "UPDATE screenings SET movie_id = ?, hall_id = ?, screening_time = ?, ticket_price = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, s.getMovieId());
            stmt.setInt(2, s.getHallId());
            stmt.setTimestamp(3, Timestamp.valueOf(s.getStartTime()));
            stmt.setDouble(4, s.getTicketPrice());
            stmt.setInt(5, s.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteScreening(int id) {
        String sql = "DELETE FROM screenings WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Reservation> getReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.id, r.screening_id, r.user_id, r.status, r.reserved_seats, s.ticket_price " +
                "FROM reservations r " +
                "JOIN screenings s ON r.screening_id = s.id";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                List<List<Integer>> seats = JsonUtil.fromJson(rs.getString("reserved_seats"), List.class);

                int seatsCount = (seats != null) ? seats.size() : 0;
                double totalPrice = seatsCount * rs.getDouble("ticket_price");

                Reservation r = new Reservation(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("screening_id"),
                        seats,
                        Reservation.Status.valueOf(rs.getString("status")),
                        LocalDateTime.now(),
                        totalPrice
                );
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insertReservation(Reservation r) {
        String sqlReservation = "INSERT INTO reservations (screening_id, user_id, status, reserved_seats) VALUES (?, ?, ?, ?)";
        String sqlTicketSold = "INSERT INTO tickets_sold (screening_id, seats_count, total_price, sale_time) VALUES (?, ?, ?, ?)";
        String sqlPrice = "SELECT ticket_price FROM screenings WHERE id = ?";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                double singleTicketPrice = 0.0;

                try (PreparedStatement stmtPrice = conn.prepareStatement(sqlPrice)) {
                    stmtPrice.setInt(1, r.getScreeningId());
                    try (ResultSet rsPrice = stmtPrice.executeQuery()) {
                        if (rsPrice.next()) {
                            singleTicketPrice = rsPrice.getDouble("ticket_price");
                        }
                    }
                }

                String seatsJsonOrString = JsonUtil.toJson(r.getSeats());

                try (PreparedStatement stmtRes = conn.prepareStatement(sqlReservation, new String[]{"ID"})) {
                    stmtRes.setInt(1, r.getScreeningId());
                    stmtRes.setInt(2, r.getUserId());
                    stmtRes.setString(3, r.getStatus().name());
                    stmtRes.setString(4, seatsJsonOrString);
                    stmtRes.executeUpdate();

                    try (ResultSet gk = stmtRes.getGeneratedKeys()) {
                        if (gk.next()) r.setId(gk.getInt(1));
                    }
                }

                if (r.getStatus() == Reservation.Status.PAID) {
                    int seatsCount = 0;

                    if (seatsJsonOrString != null && !seatsJsonOrString.trim().isEmpty()) {
                        String cleanSeats = seatsJsonOrString.replace("\"", "");
                        seatsCount = cleanSeats.split(",").length;
                    }

                    strategy.PriceStrategy priceStrategy = new strategy.RegularPriceStrategy();
                    double totalPrice = priceStrategy.calculatePrice(singleTicketPrice, seatsCount);

                    try (PreparedStatement stmtTicket = conn.prepareStatement(sqlTicketSold)) {
                        stmtTicket.setInt(1, r.getScreeningId());
                        stmtTicket.setInt(2, seatsCount);
                        stmtTicket.setDouble(3, totalPrice);
                        stmtTicket.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                        stmtTicket.executeUpdate();
                    }
                }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateReservation(Reservation r) {
        String sql = "UPDATE reservations SET status = ?, reserved_seats = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, r.getStatus().name());
            stmt.setString(2, JsonUtil.toJson(r.getSeats()));
            stmt.setInt(3, r.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<MovieReport> getMovieReports() {
        List<MovieReport> list = new ArrayList<>();
        String sql = "SELECT movie_id, movie_title, movie_genre, total_tickets_sold, total_revenue FROM v_movie_reports";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                MovieReport report = new MovieReport();
                report.setMovieId(rs.getInt("movie_id"));
                report.setMovieTitle(rs.getString("movie_title"));
                report.setMovieGenre(rs.getString("movie_genre"));
                report.setTotalTicketsSold(rs.getInt("total_tickets_sold"));
                report.setTotalRevenue(rs.getDouble("total_revenue"));
                list.add(report);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}