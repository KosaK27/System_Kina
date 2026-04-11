package service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import model.*;
import util.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private static DataStore instance;

    private final ObjectMapper mapper;
    private final String dataDir;

    private List<User> users = new ArrayList<>();
    private List<Movie> movies = new ArrayList<>();
    private List<Hall> halls = new ArrayList<>();
    private List<Screening> screenings = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();

    private DataStore(String dataDir) {
        this.dataDir = dataDir;
        new File(dataDir).mkdirs();
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        load();
    }

    public static DataStore getInstance(String dataDir) {
        if (instance == null) instance = new DataStore(dataDir);
        return instance;
    }

    public static DataStore getInstance() {
        if (instance == null) throw new IllegalStateException("DataStore nie zainicjowany.");
        return instance;
    }

    private void load() {
        users = loadList("users.json", new TypeReference<>() {});
        movies = loadList("movies.json", new TypeReference<>() {});
        halls = loadList("halls.json", new TypeReference<>() {});
        screenings = loadList("screenings.json", new TypeReference<>() {});
        reservations = loadList("reservations.json", new TypeReference<>() {});

        if (users.isEmpty()) seedUsers();
        if (movies.isEmpty()) seedMovies();
        if (halls.isEmpty()) seedHalls();
        if (screenings.isEmpty()) seedScreenings();
    }

    private <T> List<T> loadList(String file, TypeReference<List<T>> ref) {
        File f = new File(dataDir + "/" + file);
        if (!f.exists()) return new ArrayList<>();
        try {
            return mapper.readValue(f, ref);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    private void save(String file, Object data) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(dataDir + "/" + file), data);
        } catch (IOException e) {
            throw new RuntimeException("Błąd zapisu: " + file, e);
        }
    }

    public void saveUsers() { save("users.json", users); }
    public void saveMovies() { save("movies.json", movies); }
    public void saveHalls() { save("halls.json", halls); }
    public void saveScreenings() { save("screenings.json", screenings); }
    public void saveReservations() { save("reservations.json", reservations); }

    public List<User> getUsers() { return users; }
    public List<Movie> getMovies() { return movies; }
    public List<Hall> getHalls() { return halls; }
    public List<Screening> getScreenings() { return screenings; }
    public List<Reservation> getReservations() { return reservations; }

    public int nextUserId() { return users.stream().mapToInt(User::getId).max().orElse(0) + 1; }
    public int nextMovieId() { return movies.stream().mapToInt(Movie::getId).max().orElse(0) + 1; }
    public int nextHallId() { return halls.stream().mapToInt(Hall::getId).max().orElse(0) + 1; }
    public int nextScreeningId() { return screenings.stream().mapToInt(Screening::getId).max().orElse(0) + 1; }
    public int nextReservationId() { return reservations.stream().mapToInt(Reservation::getId).max().orElse(0) + 1; }

    private void seedUsers() {
        users.add(new Admin(1, "Admin", "admin@poczta.pl", PasswordUtil.hash("admin123")));
        users.add(new Employee(2, "Pracownik", "pracownik@poczta.pl", PasswordUtil.hash("pracownik123")));
        users.add(new Client(3, "Klient", "klient@poczta.pl", PasswordUtil.hash("klient123")));
        saveUsers();
    }

    private void seedMovies() {
        movies.add(new Movie(1, "Shrek 1", "Opis 1", 148, "Sci-Fi"));
        movies.add(new Movie(2, "Shrek 2", "Opis 2", 90, "Animacja"));
        movies.add(new Movie(3, "Shrek 3", "Opis 3", 136, "Akcja"));
        saveMovies();
    }

    private void seedHalls() {
        halls.add(new Hall(1, "Sala A", 8, 10));
        halls.add(new Hall(2, "Sala B", 6, 8));
        halls.add(new Hall(3, "Sala C", 4, 6));
        saveHalls();
    }

    private void seedScreenings() {
        LocalDateTime base = LocalDateTime.now();
        screenings.add(new Screening(1, 1, 1, base.plusDays(1).withHour(18).withMinute(0), 25.0));
        screenings.add(new Screening(2, 1, 2, base.plusDays(2).withHour(20).withMinute(30), 25.0));
        screenings.add(new Screening(3, 2, 1, base.plusDays(1).withHour(15).withMinute(0), 20.0));
        screenings.add(new Screening(4, 3, 3, base.plusDays(3).withHour(19).withMinute(0), 35.0));
        saveScreenings();
    }
}