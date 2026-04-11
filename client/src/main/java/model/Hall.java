package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Hall {
    private int id;
    private String name;
    private int rows;
    private int seatsPerRow;

    public Hall() {}
    public Hall(int id, String name, int rows, int seatsPerRow) {
        this.id = id; this.name = name; this.rows = rows; this.seatsPerRow = seatsPerRow;
    }

    public int getTotalSeats() { return rows * seatsPerRow; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String n) { this.name = n; }
    public int getRows() { return rows; }
    public void setRows(int r) { this.rows = r; }
    public int getSeatsPerRow() { return seatsPerRow; }
    public void setSeatsPerRow(int s) { this.seatsPerRow = s; }

    @Override public String toString() { return name + " (" + getTotalSeats() + " miejsc)"; }
}