package org.ogasimli.MovieBox.asynctasks;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import org.ogasimli.MovieBox.provigen.MovieContentProvider;

/**
 * AsyncTask to store trailer and review objects into database
 * Created by ogasimli on 24.07.2015.
 */
public class TrailerAndReviewStore extends AsyncTask<ContentValues[], Void, Void>{

    private final Context mContext;

    private final Uri mUri;

    private final String mMovieId;

    public TrailerAndReviewStore(Context context, Uri uri, String movieId) {
        this.mContext = context;
        this.mUri = uri;
        this.mMovieId = movieId;
    }

    @Override
    protected final Void doInBackground(ContentValues[]... params) {
        if (mContext != null){
            ContentResolver contentResolver = mContext.getContentResolver();
            contentResolver.delete(mUri,
                    MovieContentProvider.COL_MOVIE_ID + "=?",
                    new String[]{mMovieId});

            ContentValues[] contentValues = params[0];
            contentResolver.bulkInsert(mUri, contentValues);
        }
        return null;
    }
}
