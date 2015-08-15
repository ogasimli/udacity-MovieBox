package org.ogasimli.MovieBox;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import org.ogasimli.MovieBox.fragments.DetailFragment;
import org.ogasimli.MovieBox.fragments.MovieFragment;
import org.ogasimli.MovieBox.objects.MovieList;


public class MainActivity extends AppCompatActivity implements MovieFragment.MovieActionListener,
        DetailFragment.DetailActionListener {

    private static final String DETAIL_FRAGMENT_TAG = "DFT";

    private static final String MOVIE_FRAGMENT_TAG = "MFT";

    public static String PACKAGE_NAME;

    private boolean isDualPane;

    private MovieFragment mMoviesFragment;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Set Window.FEATURE_ACTIVITY_TRANSITIONSin order to enable transition effect
        getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initToolbar();

        //Get package name to use within intents
        PACKAGE_NAME = getApplicationContext().getPackageName();

        FrameLayout mDetailContainer = (FrameLayout) findViewById(R.id.detail_container);

        isDualPane = (mDetailContainer != null);

        if (savedInstanceState == null) {
            mMoviesFragment = MovieFragment.getInstance(isDualPane);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.movie_container, mMoviesFragment, MOVIE_FRAGMENT_TAG)
                    .commit();
        } else {
            mMoviesFragment = (MovieFragment) getSupportFragmentManager().
                    findFragmentByTag(MOVIE_FRAGMENT_TAG);
            mMoviesFragment.setDualPane(isDualPane);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_fragment, menu);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int checked = prefs.getInt("checked", R.id.action_popularity);
        MenuItem menuItem = menu.findItem(checked);
        if (menuItem != null) {
            menuItem.setChecked(true);
        } else {
            menu.findItem(R.id.action_popularity).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String sortOrder = null;
        int checkedMenuItem = 0;
        switch (item.getItemId()) {
            case R.id.action_popularity:
                sortOrder = getResources().getString(R.string.sort_order_popularity);
                checkedMenuItem = item.getItemId();
                break;
            case R.id.action_rating:
                sortOrder = getResources().getString(R.string.sort_order_rating);
                checkedMenuItem = item.getItemId();
                break;
            case R.id.action_revenue:
                sortOrder = getResources().getString(R.string.sort_order_revenue);
                checkedMenuItem = item.getItemId();
                break;
            case R.id.action_favorites:
                sortOrder = getString(R.string.sort_order_favorites);
                checkedMenuItem = item.getItemId();
                break;
        }
        if (sortOrder != null) {
            item.setChecked(!item.isChecked());
            mMoviesFragment.setSortOrder(sortOrder, checkedMenuItem);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMovieSelected(MovieList.Movie movie, boolean isFavorite, View view) {
        if (!isDualPane) {
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
        } else {
            DetailFragment detailFragment = DetailFragment.getInstance(movie, isFavorite);
            getSupportFragmentManager().
                    beginTransaction().
                    replace(R.id.detail_container, detailFragment, DETAIL_FRAGMENT_TAG).
                    commitAllowingStateLoss();
        }
    }

    @Override
    public void onEmptyMovieList() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(DETAIL_FRAGMENT_TAG);
        if (isDualPane && fragment != null) {
            fragmentManager.beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }
    }

    /*Initialize Toolbar*/
    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onFavoriteChanged(boolean isChanged) {
        if (!isChanged) {
            mMoviesFragment.favoriteChanged();
        }
    }
}
