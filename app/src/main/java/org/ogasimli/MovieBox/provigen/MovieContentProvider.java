package org.ogasimli.MovieBox.provigen;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;

import com.tjeannin.provigen.ProviGenOpenHelper;
import com.tjeannin.provigen.ProviGenProvider;

/**
 * Movie content provider class.
 * Created by ogasimli on 24.07.2015.
 */
public class MovieContentProvider extends ProviGenProvider {

    public static final String COL_MOVIE_ID = "movie_id";

    public static final String AUTHORITY = "content://org.ogasimli.MovieBox/";

    private static final Class[] contracts = new Class[]{
            MovieContract.class, TrailerContract.class, ReviewContract.class};

    @Override
    public SQLiteOpenHelper openHelper(Context context) {
        return new ProviGenOpenHelper(getContext(), "movie_db", null, 1, contracts);
    }

    @Override
    public Class[] contractClasses() {
        return contracts;
    }
}
