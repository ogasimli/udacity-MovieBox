package org.ogasimli.MovieBox.asynctasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.ogasimli.MovieBox.objects.MovieList;
import org.ogasimli.MovieBox.provigen.MovieContract;

/**
 * AsyncTask to store movie objects into database
 * Created by ogasimli on 24.07.2015.
 */
public class MovieStore extends AsyncTask<MovieList.Movie, Void, Void> {

    private final Context mContext;

    public MovieStore(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected final Void doInBackground(MovieList.Movie... params) {
        if (mContext != null) {
            MovieList.Movie movie = params[0];
            Uri contentUri = MovieContract.CONTENT_URI;
            ContentResolver contentResolver = mContext.getContentResolver();
            ContentValues newValues = new ContentValues();
            newValues.put(MovieContract._ID, movie.movieId);
            newValues.put(MovieContract.TITLE, movie.movieTitle);
            newValues.put(MovieContract.GENRE, movie.movieGenre);
            newValues.put(MovieContract.POSTER_PATH, movie.posterPath);
            newValues.put(MovieContract.BACKDROP_PATH, movie.backdropPath);
            newValues.put(MovieContract.OVERVIEW, movie.movieOverview);
            newValues.put(MovieContract.RATING, movie.movieRating);
            newValues.put(MovieContract.RELEASE_DATE, movie.movieReleaseDate);
            contentResolver.insert(contentUri, newValues);
        }
        return null;
    }
}

