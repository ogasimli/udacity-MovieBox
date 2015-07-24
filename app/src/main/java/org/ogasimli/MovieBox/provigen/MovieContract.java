package org.ogasimli.MovieBox.provigen;

import android.net.Uri;

import com.tjeannin.provigen.ProviGenBaseContract;
import com.tjeannin.provigen.annotation.Column;
import com.tjeannin.provigen.annotation.ContentUri;

/**
 * Movies table contract
 * Created by ogasimli on 24.07.2015.
 */
public interface MovieContract extends ProviGenBaseContract{

    @Column(Column.Type.TEXT)
    String TITLE = "title";

    @Column(Column.Type.TEXT)
    String GENRE = "genre";

    @Column(Column.Type.TEXT)
    String POSTER_PATH = "poster_path";

    @Column(Column.Type.TEXT)
    String BACKDROP_PATH = "backdrop_path";

    @Column(Column.Type.TEXT)
    String OVERVIEW = "overview";

    @Column(Column.Type.REAL)
    String RATING = "rating";

    @Column(Column.Type.TEXT)
    String RELEASE_DATE = "release_date";

    @ContentUri
    Uri CONTENT_URI = Uri.parse(MovieContentProvider.AUTHORITY + "movie");
}
