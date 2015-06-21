package org.ogasimli.test;

/**
 * Created by ogasimli on 20.06.2015.
 */
public class Movie {
    private String movieTitle;
    private String movieGenre;
    private int movieThumbnail;
    private double movieRating;

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieGenre() {
        return movieGenre;
    }

    public void setMovieGenre(String movieGenre) {
        this.movieGenre = movieGenre;
    }

    public int getMovieThumbnail() {
        return movieThumbnail;
    }

    public void setMovieThumbnail(int movieThumbnail) {
        this.movieThumbnail = movieThumbnail;
    }

    public double getMovieRating() {
        return movieRating;
    }

    public void setMovieRating(double movieRating) {
        this.movieRating = movieRating;
    }
}
