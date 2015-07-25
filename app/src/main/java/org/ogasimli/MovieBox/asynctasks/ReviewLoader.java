package org.ogasimli.MovieBox.asynctasks;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

import org.ogasimli.MovieBox.objects.ReviewList;
import org.ogasimli.MovieBox.provigen.MovieContentProvider;
import org.ogasimli.MovieBox.provigen.ReviewContract;

import java.util.ArrayList;

/**
 * Async task loader to load rreviews
 * Created by ogasimli on 26.07.2015.
 */
public class ReviewLoader extends AsyncTaskLoader<ArrayList<ReviewList.Review>> {

    private String mMovieId;

    private ArrayList<ReviewList.Review> mReviewList;

    public ReviewLoader(Context context, String mMovieId) {
        super(context);
        this.mMovieId = mMovieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (mReviewList != null) {
            deliverResult(mReviewList);
        } else {
            forceLoad();
        }
    }

    @Override
    public ArrayList<ReviewList.Review> loadInBackground() {
        Uri uri = ReviewContract.CONTENT_URI;
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
            mReviewList = new ArrayList<>();
            int author = cursor.getColumnIndex(ReviewContract.AUTHOR);
            int content = cursor.getColumnIndex(ReviewContract.CONTENT);
            while (cursor.moveToNext()) {
                ReviewList.Review review = new ReviewList.Review();
                review.author = cursor.getString(author);
                review.content = cursor.getString(content);
                mReviewList.add(review);
            }
            cursor.close();
            return mReviewList;
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
