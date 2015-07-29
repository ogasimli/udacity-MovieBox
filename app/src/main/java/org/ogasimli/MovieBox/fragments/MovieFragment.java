package org.ogasimli.MovieBox.fragments;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.ogasimli.MovieBox.DetailActivity;
import org.ogasimli.MovieBox.MainActivity;
import org.ogasimli.MovieBox.R;
import org.ogasimli.MovieBox.asynctasks.MovieLoader;
import org.ogasimli.MovieBox.objects.MovieAdapter;
import org.ogasimli.MovieBox.objects.MovieList;
import org.ogasimli.MovieBox.retrofit.RetrofitAdapter;
import org.ogasimli.MovieBox.retrofit.TmdbService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing list of movies.
 * Created by ogasimli on 01.07.2015.
 */
public class MovieFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList<MovieList.Movie>> {

    private static final String LIST_STATE_KEY = "list_state";
    private static final String MENU_CHECKED_STATE = "checked";
    private static final String MENU_SORT_ORDER = "sort_order";
    private static final String VIEW_STATE_KEY = "view_state";
    private final static int VIEW_STATE_ERROR = 0;
    private final static int VIEW_STATE_RESULTS = 1;
    private final static int VIEW_STATE_NO_FAVORITES = 2;
    private static final int MOVIE_LOADER_ID = 0;
    private LinearLayout mErrorLinearLayout;
    private LinearLayout mNoFavoritesLinearLayout;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MovieAdapter mMovieAdapter;
    private ArrayList<MovieList.Movie> mMovieList;
    private final MovieAdapter.OnItemClickListener itemClickListener = new MovieAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            mMovieList = mMovieAdapter.getMovieList();
            if (mMovieList != null) {
                MovieList.Movie passedMovie = mMovieList.get(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(MainActivity.PACKAGE_NAME, passedMovie);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(getActivity(),
                                    v.findViewById(R.id.movie_poster), "poster");
                    getActivity().startActivity(intent, options.toBundle());
                } else {
                    getActivity().startActivity(intent);
                }
            } else {
                Toast.makeText(getActivity(), getActivity().getString(R.string.unable_to_fetch_data_message), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private String mSortOrder;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    //TODO: replace this with actionlistener, this results in destroying the view while orientation change
    @Override
    public void onResume() {
        super.onResume();
        //Reload favorites list
        if (mSortOrder.equals(getString(R.string.sort_order_favorites))) {
            loadFavoriteMovies();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int state = VIEW_STATE_RESULTS;
        if (mErrorLinearLayout.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_ERROR;
        } else if (mNoFavoritesLinearLayout.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_NO_FAVORITES;
        }

        outState.putInt(VIEW_STATE_KEY, state);
        outState.putParcelableArrayList(LIST_STATE_KEY, mMovieList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main_fragment, menu);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int checked = prefs.getInt(MENU_CHECKED_STATE, R.id.action_popularity);
        MenuItem menuItem = menu.findItem(checked);
        if (menuItem != null) {
            menuItem.setChecked(true);
        } else {
            menu.findItem(R.id.action_popularity).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences.Editor prefs = PreferenceManager.
                getDefaultSharedPreferences(getActivity()).edit();

        switch (item.getItemId()) {
            case R.id.action_popularity:
                prefs.putString(MENU_SORT_ORDER, getResources().getString(R.string.sort_order_popularity));
                prefs.putInt(MENU_CHECKED_STATE, item.getItemId());
                prefs.apply();
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_rating:
                prefs.putString(MENU_SORT_ORDER, getResources().getString(R.string.sort_order_rating));
                prefs.putInt(MENU_CHECKED_STATE, item.getItemId());
                prefs.apply();
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_revenue:
                prefs.putString(MENU_SORT_ORDER, getResources().getString(R.string.sort_order_revenue));
                prefs.putInt(MENU_CHECKED_STATE, item.getItemId());
                prefs.apply();
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_favorites:
                prefs.putString(MENU_SORT_ORDER, getString(R.string.sort_order_favorites));
                prefs.putInt(MENU_CHECKED_STATE, item.getItemId());
                prefs.apply();
                item.setChecked(!item.isChecked());
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        getData();
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mErrorLinearLayout = (LinearLayout) rootView.findViewById(R.id.error_view);
        mNoFavoritesLinearLayout = (LinearLayout) rootView.findViewById(R.id.no_favorites_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mMovieAdapter = new MovieAdapter();
        mMovieAdapter.setOnItemClickListener(itemClickListener);

        /*loadMovieData if savedInstanceState is null, load from already fetched data
         if savedInstanceSate is not null*/
        if (savedInstanceState == null || !savedInstanceState.containsKey(LIST_STATE_KEY)) {
            getData();
        } else {
            int state = savedInstanceState.getInt(VIEW_STATE_KEY, VIEW_STATE_ERROR);
            switch (state) {
                case VIEW_STATE_ERROR:
                    showErrorView();
                    break;
                case VIEW_STATE_RESULTS:
                    mMovieList = savedInstanceState.getParcelableArrayList(LIST_STATE_KEY);
                    mMovieAdapter.setMovieList(mMovieList);
                    mRecyclerView.setAdapter(mMovieAdapter);
                    showResultView();
                    break;
                case VIEW_STATE_NO_FAVORITES:
                    showNoFavoritesView();
                    break;
            }
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize Toolbar
        initToolbar();

        //set GridLayoutManagers grid number based on the orientation of device
        int orientation = getResources().getConfiguration().orientation;
        int spanCount = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 3 : 2;

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.accent_color,
                R.color.primary_color);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
    }

    /*Method to get data from database or by using loadMovieData*/
    private void getData() {
//        android.os.Debug.waitForDebugger();
        showLoadingView();
        mSortOrder = getSortOrder();
        if (mSortOrder.equals(getString(R.string.sort_order_favorites))) {
            loadFavoriteMovies();
        } else {
            getActivity().getSupportLoaderManager().destroyLoader(MOVIE_LOADER_ID);
            loadMovieData();
        }
    }

    /*Method to get favorite movies data from database*/
    private void loadFavoriteMovies() {
        getActivity().getSupportLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    /*Method to load data from TMDB*/
    private void loadMovieData() {
        RestAdapter adapter = RetrofitAdapter.getRestAdapter();
        TmdbService service = adapter.create(TmdbService.class);
        service.getMovie(mSortOrder, new Callback<MovieList>() {
            @Override
            public void success(MovieList movies, Response response) {
                mMovieList = movies.results;
                if (mMovieList != null && mMovieList.size() > 0) {
                    mMovieAdapter.setMovieList(mMovieList);
                    mRecyclerView.setAdapter(mMovieAdapter);
                    mMovieAdapter.notifyDataSetChanged();
                    showResultView();
                } else {
                    showErrorView();
                }
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorView();
                Log.d("RetrofitError", error.toString());
            }
        });
    }

    /*Method to get list of favorite movie ids in a reversed order*/
    private ArrayList<String> getFavoriteList() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String favoritesString = prefs.getString("favorites", "");
        ArrayList<String> list = new ArrayList<>();
        if (favoritesString.length() > 0) {
            StringTokenizer st = new StringTokenizer(favoritesString, ",");
            while (st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
        }
        Collections.reverse(list);
        return list;
    }

    /*Method to get sort order from SharedPreferences*/
    @NonNull
    private String getSortOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultValue = getResources().getString(R.string.sort_order_popularity);
        return prefs.getString(MENU_SORT_ORDER, defaultValue);
    }

    /*Method to show error view*/
    private void showErrorView() {
        mErrorLinearLayout.setVisibility(View.VISIBLE);
        mNoFavoritesLinearLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        hideLoadingView();
    }

    /*Method to show result view*/
    private void showResultView() {
        mErrorLinearLayout.setVisibility(View.GONE);
        mNoFavoritesLinearLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        hideLoadingView();
    }

    /*Method to show that there is no movie in favorites list*/
    private void showNoFavoritesView() {
        mErrorLinearLayout.setVisibility(View.GONE);
        mNoFavoritesLinearLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        hideLoadingView();
    }

    /*Method to start swiperefreshlayout*/
    private void showLoadingView() {
        mRecyclerView.setVisibility(View.GONE);
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
    }

    /*Method to cancel swiperefreshlayout*/
    private void hideLoadingView() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }

    /*Initialize Toolbar*/
    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
    }

    /*Callbacks to query data from movie table*/
    @Override
    public Loader<ArrayList<MovieList.Movie>> onCreateLoader(int id, Bundle args) {
        return new MovieLoader(getActivity(), getFavoriteList());
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<MovieList.Movie>> loader, ArrayList<MovieList.Movie> data) {
        if (data == null) {
            showErrorView();
        } else if (data.size() > 0) {
            mMovieAdapter.setMovieList(data);
            mRecyclerView.setAdapter(mMovieAdapter);
            mMovieAdapter.notifyDataSetChanged();
            showResultView();
        } else {
            showNoFavoritesView();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MovieList.Movie>> loader) {
    }
}
