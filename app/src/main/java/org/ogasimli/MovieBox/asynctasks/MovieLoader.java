package org.ogasimli.MovieBox.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import org.ogasimli.MovieBox.objects.MovieList;
import org.ogasimli.MovieBox.provigen.MovieContract;

import java.util.ArrayList;

/**
 * Async task loader to load favorite movies
 * Created by ogasimli on 26.07.2015.
 */
public class MovieLoader extends AsyncTaskLoader<ArrayList<MovieList.Movie>> {

    private ArrayList<String> mMovieIds;

    public MovieLoader(Context context, ArrayList<String> mMovieIds) {
        super(context);
        this.mMovieIds = mMovieIds;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mMovieIds == null || mMovieIds.size() == 0){
            deliverResult(new ArrayList<MovieList.Movie>());
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<MovieList.Movie> loadInBackground() {
        Uri uri = MovieContract.CONTENT_URI;
        String selectionMarks = "";
        String[] args = new String[mMovieIds.size()];
        for (int i = 0; i < args.length; i++) {
            selectionMarks += "?,";
            args[i] = mMovieIds.get(i);
        }
        selectionMarks = selectionMarks.substring(0, selectionMarks.length() - 1);
        String selection = MovieContract._ID + " in (" + selectionMarks + ")";
        Cursor cursor = getContext().getContentResolver().
                query(uri, null, selection, args, "");
        if (null == cursor) {
            return null;
        } else if (cursor.getCount() < 1) {
            cursor.close();
            return new ArrayList<>();
        } else {
            ArrayList<MovieList.Movie> movieList  = new ArrayList<>();
            int id = cursor.getColumnIndex(MovieContract._ID);
            int title = cursor.getColumnIndex(MovieContract.TITLE);
            int genre = cursor.getColumnIndex(MovieContract.GENRE);
            int posterPath = cursor.getColumnIndex(MovieContract.POSTER_PATH);
            int backdropPath = cursor.getColumnIndex(MovieContract.BACKDROP_PATH);
            int overview = cursor.getColumnIndex(MovieContract.OVERVIEW);
            int rating = cursor.getColumnIndex(MovieContract.RATING);
            int releaseDate = cursor.getColumnIndex(MovieContract.RELEASE_DATE);
            while (cursor.moveToNext()) {
                MovieList.Movie movie = new MovieList.Movie();
                movie.movieId = cursor.getString(id);
                movie.movieTitle = cursor.getString(title);
                movie.movieGenre = cursor.getString(genre);
                movie.backdropPath = cursor.getString(backdropPath);
                movie.posterPath = cursor.getString(posterPath);
                movie.movieOverview = cursor.getString(overview);
                movie.movieRating = cursor.getDouble(rating);
                movie.movieReleaseDate = cursor.getString(releaseDate);
                movieList.add(movie);
            }
            cursor.close();
            return movieList;
        }
    }

    @Override
    public void deliverResult(ArrayList<MovieList.Movie> data) {
        super.deliverResult(data);
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
