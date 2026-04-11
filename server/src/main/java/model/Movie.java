package model;

import java.util.List;

public class Movie {
    public static final List<String> GENRES =
            List.of("Akcja", "Komedia", "Dramat", "Horror", "Sci-Fi", "Animacja");
    public static final int MIN_DURATION = 1;
    public static final int MAX_DURATION = 240;

    private int id;
    private String title;
    private String description;
    private int duration;
    private String genre;

    public Movie() {}
    public Movie(int id, String title, String description, int duration, String genre) {
        this.id = id; this.title = title; this.description = description;
        this.duration = duration; this.genre = genre;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    @Override public String toString() { return title; }
}