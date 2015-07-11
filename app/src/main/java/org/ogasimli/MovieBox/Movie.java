package org.ogasimli.MovieBox;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Movie object class
 * Created by ogasimli on 01.07.2015.
 */
public class Movie implements Parcelable{

    private String movieTitle;
    private String movieGenre;
    private String posterPath;
    private String backdropPath;
    private String movieId;
    private String movieOverview;
    private String movieReleaseDate;
    private double movieRating;

    public Movie(){}

    private Movie(Parcel in){
        movieTitle = in.readString();
        movieGenre = in.readString();
        posterPath = in.readString();
        backdropPath = in.readString();
        movieId = in.readString();
        movieOverview = in.readString();
        movieReleaseDate = in.readString();
        movieRating = in.readDouble();
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(movieTitle);
        dest.writeString(movieGenre);
        dest.writeString(posterPath);
        dest.writeString(backdropPath);
        dest.writeString(movieId);
        dest.writeString(movieOverview);
        dest.writeString(movieReleaseDate);
        dest.writeDouble(movieRating);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>(){
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

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

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getMovieId() {
        return movieId;
    }

    public void setMovieId(String movieId) {
        this.movieId = movieId;
    }

    public String getMovieOverview() {
        return movieOverview;
    }

    public void setMovieOverview(String movieOverview) {
        this.movieOverview = movieOverview;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    public void setMovieReleaseDate(String movieReleaseDate) {
        this.movieReleaseDate = movieReleaseDate;
    }

    public double getMovieRating() {
        return movieRating;
    }

    public void setMovieRating(double movieRating) {
        this.movieRating = movieRating;
    }
}
