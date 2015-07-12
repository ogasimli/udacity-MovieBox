package org.ogasimli.MovieBox.movie;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Movie object class
 * Created by ogasimli on 01.07.2015.
 * TODO: Replace String arrays with getResources method
 */
public class MovieList {

    public ArrayList<Movie> results;

    public static class Movie implements Parcelable {

        @SerializedName("original_title")
        public String movieTitle;

        @SerializedName("genre_ids")
        public List<String> genreIds = new ArrayList<>();

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

        private String movieGenre;

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

        public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
            public Movie createFromParcel(Parcel in) {
                return new Movie(in);
            }

            public Movie[] newArray(int size) {
                return new Movie[size];
            }
        };

        public String getMovieGenre() {
            return movieGenre;
        }

        public void setMovieGenre(String movieGenre) {
            this.movieGenre = movieGenre;
        }

        public String getMovieGenre(List<String> genreIds) {
            String valueGenre = null;
            if (genreIds.size() != 0) {
                String firstGenreId = genreIds.get(0);
                String[] id = {"28", "12", "16", "35", "80", "99", "18", "10751", "14", "10769",
                        "36", "27", "10402", "9648", "10749", "878", "10770", "53", "10752", "37"};
                String[] value = {"Action", "Adventure", "Animation", "Comedy", "Crime",
                        "Documentary", "Drama", "Family", "Fantasy", "Foreign", "History",
                        "Horror", "Music", "Mystery", "Romance", "Science Fiction",
                        "TV Movie", "Thriller", "War", "Western"};
                //String[] id = context.getResources().getStringArray(R.array.genre_id);
                //String[] value = context.getResources().getStringArray(R.array.genre_id_values);

                for (int i = 0; i < id.length; i++) {
                    if (firstGenreId.equals(id[i])) {
                        valueGenre = value[i];
                        break;
                    }
                }
            } else {
                valueGenre = "";
            }

            setMovieGenre(valueGenre);

            return valueGenre;
        }

        //Additional methods to get poster URLs
        private final static String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";

        public String getPosterUrl() {
            return POSTER_BASE_URL + "w185" + posterPath;
        }

        public String getBackdropPosterUrl() {
            return POSTER_BASE_URL + "w500" + backdropPath;
        }
    }
}