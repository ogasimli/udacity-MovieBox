package org.ogasimli.MovieBox;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import org.ogasimli.MovieBox.fragments.DetailFragment;
import org.ogasimli.MovieBox.objects.MovieList;


public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        int sw = getResources().getConfiguration().smallestScreenWidthDp;
        if (sw == 600 && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
        }

        setContentView(R.layout.activity_detail);

        MovieList.Movie movie;
        boolean isFavorite;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            movie = extras.getParcelable(DetailFragment.MOVIE);
            isFavorite = extras.getBoolean(DetailFragment.FAVORITE);
        } else {
            throw new NullPointerException("No movie found in extras");
        }

        if (savedInstanceState == null) {
            DetailFragment fragment = DetailFragment.getInstance(movie, isFavorite);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Reverse the scene transition by home button press
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
