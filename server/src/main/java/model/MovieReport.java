package model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieReport {
    @JsonProperty("movie_id")
    private int movieId;

    @JsonProperty("movie_title")
    private String movieTitle;

    @JsonProperty("movie_genre")
    private String movieGenre;

    @JsonProperty("total_tickets_sold")
    private int totalTicketsSold;

    @JsonProperty("total_revenue")
    private double totalRevenue;

    public MovieReport() {
    }

    public int getMovieId() { return movieId; }
    public void setMovieId(int movieId) { this.movieId = movieId; }

    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }

    public String getMovieGenre() { return movieGenre; }
    public void setMovieGenre(String movieGenre) { this.movieGenre = movieGenre; }

    public int getTotalTicketsSold() { return totalTicketsSold; }
    public void setTotalTicketsSold(int totalTicketsSold) { this.totalTicketsSold = totalTicketsSold; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}