package org.ogasimli.MovieBox.provigen;

import android.net.Uri;

import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

/**
 * Trailers table contract
 * Created by ogasimli on 24.07.2015.
 */
public interface TrailerContract extends ProviGenBaseContract {

    @Column(Column.Type.TEXT)
    String MOVIE_ID = MovieContentProvider.COL_MOVIE_ID;

    @Column(Column.Type.TEXT)
    String KEY = "key";

    @Column(Column.Type.TEXT)
    String NAME = "name";

    @ContentUri
    Uri CONTENT_URI = Uri.parse(MovieContentProvider.AUTHORITY + "trailer");
}
