package org.ogasimli.MovieBox.fragments;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

    private static final String SELECTED_MOVIE_KEY = "selected_movie";

    private static final String IS_DUAL_PANE = "is_dual_pane";

    private final static int VIEW_STATE_ERROR = 0;

    private final static int VIEW_STATE_RESULTS = 1;

    private final static int VIEW_STATE_NO_FAVORITES = 2;

    private static final int MOVIE_LOADER_ID = 0;

    private LinearLayout mErrorLinearLayout;

    private LinearLayout mNoFavoritesLinearLayout;

    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private String mSortOrder;

    private MovieAdapter mMovieAdapter;
    private final MovieAdapter.OnItemClickListener itemClickListener
            = new MovieAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(View v, int position) {
            mMovieAdapter.selectMovie(position, v.findViewById(R.id.movie_poster));
        }
    };
    private ArrayList<MovieList.Movie> mMovieList;
    private MovieActionListener mMovieActionListener;
    private boolean isDualPane;

    public static MovieFragment getInstance(boolean isDualPane) {
        MovieFragment fragment = new MovieFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_DUAL_PANE, isDualPane);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
        try {
            mMovieActionListener = (MovieActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
        isDualPane = getArguments().getBoolean(IS_DUAL_PANE, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Reload favorites list
        if (!isDualPane && mSortOrder.equals(getString(R.string.sort_order_favorites))) {
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
        outState.putInt(SELECTED_MOVIE_KEY, mMovieAdapter.mSelectedPosition);

    }

/*    @Override
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
    }*/

/*    @Override
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
            setSortOrder(sortOrder, checkedMenuItem);
            getData();
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mErrorLinearLayout = (LinearLayout) rootView.findViewById(R.id.error_view);
        mNoFavoritesLinearLayout = (LinearLayout) rootView.findViewById(R.id.no_favorites_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mMovieAdapter = new MovieAdapter(getActivity(), mMovieActionListener, isDualPane);
        mMovieAdapter.setOnItemClickListener(itemClickListener);
        RecyclerView.LayoutManager mLayoutManager =
                new GridLayoutManager(getActivity(), calculateSpanCount());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mMovieAdapter);

        /*loadMovieData if savedInstanceState is null, load from already fetched data
         if savedInstanceSate is not null*/
        if (savedInstanceState == null || !savedInstanceState.containsKey(LIST_STATE_KEY)
                || !savedInstanceState.containsKey(VIEW_STATE_KEY)
                || !savedInstanceState.containsKey(SELECTED_MOVIE_KEY)) {
            getData();
        } else {
            int state = savedInstanceState.getInt(VIEW_STATE_KEY, VIEW_STATE_ERROR);
            switch (state) {
                case VIEW_STATE_ERROR:
                    showErrorView();
                    break;
                case VIEW_STATE_RESULTS:
                    mMovieList = savedInstanceState.getParcelableArrayList(LIST_STATE_KEY);
                    mMovieAdapter.mSelectedPosition = savedInstanceState.
                            getInt(SELECTED_MOVIE_KEY, 0);
                    mMovieAdapter.setMovieList(mMovieList);
//                    mRecyclerView.setAdapter(mMovieAdapter);
                    showResultView();
                    if (isDualPane) {
                        mRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.scrollToPosition(mMovieAdapter.mSelectedPosition);
                            }
                        });
                    }
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

    /*Method to calculate grid span count*/
    private int calculateSpanCount() {
        int orientation = getResources().getConfiguration().orientation;
        int sw = getResources().getConfiguration().smallestScreenWidthDp;
        boolean landscape = (orientation == Configuration.ORIENTATION_LANDSCAPE);
        if (sw < 600) {
            return (landscape) ? 3 : 2;
        } else if (sw < 720) {
            return (landscape) ? 2 : 3;
        } else {
            return (landscape) ? 3 : 2;
        }
    }

    /*Method to get data from database or by using loadMovieData*/
    private void getData() {
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
//                    mRecyclerView.setAdapter(mMovieAdapter);
//                    mMovieAdapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(0);
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

    /*Method to store sort order and checked menu item*/
    public void setSortOrder(String sortOrder, int checkedMenuItem) {
        mSortOrder = sortOrder;
        mMovieAdapter.mSelectedPosition = 0;
        SharedPreferences.Editor prefs = PreferenceManager.
                getDefaultSharedPreferences(getActivity()).edit();
        prefs.putString(MENU_SORT_ORDER, sortOrder);
        prefs.putInt(MENU_CHECKED_STATE, checkedMenuItem);
        prefs.apply();
        getData();
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
        mMovieActionListener.onEmptyMovieList();
    }

    /*Method to show result view*/
    private void showResultView() {
        mErrorLinearLayout.setVisibility(View.GONE);
        mNoFavoritesLinearLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        hideLoadingView();
        mMovieActionListener.onEmptyMovieList();
    }

    /*Method to show that there is no movie in favorites list*/
    private void showNoFavoritesView() {
        mErrorLinearLayout.setVisibility(View.GONE);
        mNoFavoritesLinearLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        hideLoadingView();
        mMovieActionListener.onEmptyMovieList();
    }

    /*Method to start SwipeRefreshLayout*/
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

    /*Method to cancel SwipeRefreshLayout*/
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

    public void setDualPane(boolean isDualPane) {
        this.isDualPane = isDualPane;
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
            mMovieList = data;
            mMovieAdapter.setMovieList(mMovieList);
//            mRecyclerView.setAdapter(mMovieAdapter);
//            mMovieAdapter.notifyDataSetChanged();
            mRecyclerView.scrollToPosition(0);
            showResultView();
        } else {
            showNoFavoritesView();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MovieList.Movie>> loader) {
    }

    public void favoriteChanged() {
        if (isDualPane && mSortOrder.equals(getString(R.string.sort_order_favorites))) {
            loadFavoriteMovies();
        }
    }

    public interface MovieActionListener {
        void onMovieSelected(MovieList.Movie movie, boolean isFavorite, View view);
        void onEmptyMovieList();
    }
}
