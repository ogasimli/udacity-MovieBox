package org.ogasimli.MovieBox.objects;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Movie object class
 * Created by ogasimli on 01.07.2015.
 */
public class MovieList {

    public ArrayList<Movie> results;

    public static class Movie implements Parcelable {

        public static final Creator<Movie> CREATOR = new Creator<Movie>() {
            public Movie createFromParcel(Parcel in) {
                return new Movie(in);
            }

            public Movie[] newArray(int size) {
                return new Movie[size];
            }
        };
        //Additional methods to get poster URLs
        private final static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
        @SerializedName("genre_ids")
        public final ArrayList<String> genreIds = new ArrayList<>();
        @SerializedName("original_title")
        public String movieTitle;
        @SerializedName("poster_path")
        public String posterPath;
        @SerializedName("backdrop_path")
        public String backdropPath;
        @SerializedName("id")
        public String movieId;
        @SerializedName("overview")
        public String movieOverview;
        @SerializedName("release_date")
        public String movieReleaseDate;
        @SerializedName("vote_average")
        public double movieRating;
        public String movieGenre;

        public Movie() {
        }

        private Movie(Parcel in) {
            movieTitle = in.readString();
            movieGenre = in.readString();
            posterPath = in.readString();
            backdropPath = in.readString();
            movieId = in.readString();
            movieOverview = in.readString();
            movieReleaseDate = in.readString();
            movieRating = in.readDouble();
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

        public int describeContents() {
            return 0;
        }

        public String getPosterUrl() {
            return POSTER_BASE_URL + "w185" + posterPath;
        }

        public String getBackdropPosterUrl() {
            return POSTER_BASE_URL + "w780" + backdropPath;
        }
    }
}