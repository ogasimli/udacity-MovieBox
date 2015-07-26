package org.ogasimli.MovieBox.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import org.ogasimli.MovieBox.objects.TrailerList;
import org.ogasimli.MovieBox.provigen.MovieContentProvider;
import org.ogasimli.MovieBox.provigen.TrailerContract;

import java.util.ArrayList;

/**
 * Async task loader to load trailers
 * Created by ogasimli on 25.07.2015.
 */
public class TrailerLoader extends AsyncTaskLoader<ArrayList<TrailerList.Trailer>> {

    private String mMovieId;

    private ArrayList<TrailerList.Trailer> mTrailerList;

    public TrailerLoader(Context context, String mMovieId) {
        super(context);
        this.mMovieId = mMovieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mTrailerList != null && mTrailerList.size() > 0) {
            deliverResult(mTrailerList);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<TrailerList.Trailer> loadInBackground() {
        Uri uri = TrailerContract.CONTENT_URI;
        String selection = MovieContentProvider.COL_MOVIE_ID + "=?";
        String[] args = new String[]{mMovieId};
        Cursor cursor = getContext().getContentResolver().
                query(uri, null, selection, args, "");
        if (null == cursor) {
            return null;
        } else if (cursor.getCount() < 1) {
            cursor.close();
            return null;
        } else {
            mTrailerList = new ArrayList<>();
            int key = cursor.getColumnIndex(TrailerContract.KEY);
            int name = cursor.getColumnIndex(TrailerContract.NAME);
            int size = cursor.getColumnIndex(TrailerContract.SIZE);
            while (cursor.moveToNext()) {
                TrailerList.Trailer trailer = new TrailerList.Trailer();
                trailer.key = cursor.getString(key);
                trailer.name = cursor.getString(name);
                trailer.size = cursor.getString(size);
                mTrailerList.add(trailer);
            }
            cursor.close();
            return mTrailerList;
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
