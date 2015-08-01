package org.ogasimli.MovieBox;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;

import org.ogasimli.MovieBox.fragments.DetailFragment;
import org.ogasimli.MovieBox.fragments.MovieFragment;
import org.ogasimli.MovieBox.objects.MovieList;


public class MainActivity extends AppCompatActivity
        implements MovieFragment.MovieActionListener {

    public static String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set Window.FEATURE_ACTIVITY_TRANSITIONSin order to enable transition effect
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().
                    beginTransaction().
                    add(R.id.main_container, new MovieFragment()).
                    commit();
        }
        //Get package name to use within intents
        PACKAGE_NAME = getApplicationContext().getPackageName();
    }

    @Override
    public void onMovieSelected(MovieList.Movie movie, boolean isFavorite, View view) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailFragment.MOVIE, movie);
        intent.putExtra(DetailFragment.FAVORITE, isFavorite);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this,
                            view, "poster");
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }
}
