package org.ogasimli.MovieBox.provigen;

import android.net.Uri;

import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

/**
 * Reviews table contract
 * Created by ogasimli on 24.07.2015.
 */
public interface ReviewContract extends ProviGenBaseContract {

    @Column(Column.Type.TEXT)
    String MOVIE_ID = MovieContentProvider.COL_MOVIE_ID;

    @Column(Column.Type.TEXT)
    String AUTHOR = "author";

    @Column(Column.Type.TEXT)
    String CONTENT = "content";

    @ContentUri
    Uri CONTENT_URI = Uri.parse(MovieContentProvider.AUTHORITY + "review");
}
