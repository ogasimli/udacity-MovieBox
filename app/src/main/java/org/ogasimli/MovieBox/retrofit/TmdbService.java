package org.ogasimli.MovieBox.retrofit;

import org.ogasimli.MovieBox.movie.MovieList;
import org.ogasimli.MovieBox.movie.ReviewList;
import org.ogasimli.MovieBox.movie.TrailerList;

import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * TheMovieDB api services.
 * Created by ogasimli on 12.07.2015.
 */
public interface TmdbService {

    @GET("/discover/movie?include_adult=true&")
    void getMovie(@Query("sort_by") String sortBy, retrofit.Callback<MovieList> callback);

    @GET("/movie/{id}/reviews")
    void getReview(@Path("id") String id, retrofit.Callback<ReviewList> callback);

    @GET("/movie/{id}/videos")
    void getTrailer(@Path("id") String id, retrofit.Callback<TrailerList> callback);
}
