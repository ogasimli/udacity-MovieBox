package org.ogasimli.MovieBox.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import org.ogasimli.MovieBox.movie.MovieAdapter;
import org.ogasimli.MovieBox.movie.MovieList;
import org.ogasimli.MovieBox.retrofit.RetrofitAdapter;
import org.ogasimli.MovieBox.retrofit.TmdbService;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * A placeholder fragment containing list of movies.
 * Created by ogasimli on 01.07.2015.
 */
public class MovieFragment extends Fragment {

    private LinearLayout mLinearLayout;
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private MovieAdapter mAdapter;
    private ArrayList<MovieList.Movie> movieList;
    private static final String LIST_STATE_KEY = "list_state";
    private static final String MENU_CHECKED_STATE = "checked";
    private static final String MENU_SORT_ORDER = "sort_order";
    private static final String VIEW_STATE_KEY = "view_state";
    private final static int VIEW_STATE_ERROR = 0;
    private final static int VIEW_STATE_RESULTS = 1;

/*    public MovieFragment() {
    }*/

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int state = VIEW_STATE_RESULTS;
        if (mLinearLayout.getVisibility() == View.VISIBLE) {
            state = VIEW_STATE_ERROR;
        }

        outState.putInt(VIEW_STATE_KEY, state);
        outState.putParcelableArrayList(LIST_STATE_KEY, movieList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_moviefragment, menu);

        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        int checked = prefs.getInt(MENU_CHECKED_STATE, R.id.action_popularity);
        MenuItem menuItem = menu.findItem(checked);
        if (menuItem!=null){
        menuItem.setChecked(true);
        }else {
            menu.findItem(R.id.action_popularity).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences.Editor prefs = getActivity().getPreferences(Context.MODE_PRIVATE).edit();

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
            default:
                return super.onOptionsItemSelected(item);
        }
        loadMovieData();
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mLinearLayout = (LinearLayout) rootView.findViewById(R.id.error_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mAdapter = new MovieAdapter();
        mAdapter.setOnItemClickListener(itemClickListener);

        //loadMovieData if savedInstanceState is null, load from already fetched data
        // if savedInstanceSate is not null
        if (savedInstanceState == null || !savedInstanceState.containsKey(LIST_STATE_KEY)) {
            loadMovieData();
        } else {
            int state = savedInstanceState.getInt(VIEW_STATE_KEY, VIEW_STATE_ERROR);
            switch (state) {
                case VIEW_STATE_ERROR:
                    showErrorView();
                    break;
                case VIEW_STATE_RESULTS:
                    movieList = savedInstanceState.getParcelableArrayList(LIST_STATE_KEY);
                    mAdapter.setMovieList(movieList);
                    mRecyclerView.setAdapter(mAdapter);
                    showResultView();
                    break;
            }
        }

        return rootView;
    }

    private final MovieAdapter.OnItemClickListener itemClickListener = new MovieAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            movieList = mAdapter.getMovieList();
            if (movieList != null) {
                MovieList.Movie passedMovie = movieList.get(position);
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


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize Toolbar
        initToolbar();

        mRecyclerView.setHasFixedSize(true);

        //set GridLayoutManagers grid number based on the orientation of device
        int orientation = getResources().getConfiguration().orientation;
        int spanCount = (orientation == Configuration.ORIENTATION_LANDSCAPE) ? 3 : 2;

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(), spanCount);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.accent_color,
                R.color.primary_color);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() { loadMovieData(); }
        });
    }

    private void loadMovieData() {
        showLoadingView();
        RestAdapter adapter = RetrofitAdapter.getRestAdapter();
        TmdbService service = adapter.create(TmdbService.class);
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.sort_order_popularity);
        String sortOrder = prefs.getString(MENU_SORT_ORDER, defaultValue);
        service.getMovie(sortOrder, new Callback<MovieList>() {
            @Override
            public void success(MovieList movies, Response response) {
                movieList = movies.results;
                mAdapter.setMovieList(movieList);
                mRecyclerView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
                showResultView();
            }

            @Override
            public void failure(RetrofitError error) {
                showErrorView();
                Log.d("RetrofitError", error.toString());
            }
        });
    }

    private void showErrorView() {
        mRecyclerView.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
        mLinearLayout.setVisibility(View.VISIBLE);
    }

    private void showLoadingView() {
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (!mSwipeRefreshLayout.isRefreshing()) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
            }
        });
    }

    private void showResultView() {
        mLinearLayout.setVisibility(View.GONE);
        mSwipeRefreshLayout.setRefreshing(false);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
    }
}
