package org.ogasimli.MovieBox.retrofit;

import org.ogasimli.MovieBox.movie.MovieList;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * TheMovieDB api services.
 * Created by ogasimli on 12.07.2015.
 */
public interface TmdbService {

    @GET("/discover/movie?include_adult=true&")
    void getMovie(@Query("sort_by") String sortBy, retrofit.Callback<MovieList> callback);
}
