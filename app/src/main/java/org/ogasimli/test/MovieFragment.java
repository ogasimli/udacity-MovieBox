package org.ogasimli.test;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


/**
 * A placeholder fragment containing a simple view.
 */
public class MovieFragment extends Fragment {

    private FragmentActivity mActivity;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView.LayoutManager mLayoutManager;
    private MovieAdapter mAdapter;
    private List<Movie> movieList;
    private static final String LIST_STATE_KEY = "listState";
    private Parcelable mListState = null;

    public MovieFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mActivity = (FragmentActivity) activity;
        setRetainInstance(true);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        //loadMovieData();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mListState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(LIST_STATE_KEY, mListState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE_KEY);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mListState != null) {
            mLayoutManager.onRestoreInstanceState(mListState);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_moviefragment, menu);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
            menu.findItem(R.id.sort_by).setVisible(false);
        }
        
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        int checked = prefs.getInt("checked", R.id.action_popularity);
        MenuItem menuItem = menu.findItem(checked);
        menuItem.setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        SharedPreferences.Editor prefs = getActivity().getPreferences(Context.MODE_PRIVATE).edit();

        switch (item.getItemId()) {
            case R.id.action_popularity:
                prefs.putString("sort_order", "popularity.desc");
                prefs.putInt("checked", item.getItemId());
                prefs.apply();
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_rating:
                prefs.putString("sort_order", "vote_average.desc");
                prefs.putInt("checked", item.getItemId());
                prefs.apply();
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_revenue:
                prefs.putString("sort_order", "revenue.desc");
                prefs.putInt("checked", item.getItemId());
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
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh_layout);
        loadMovieData();
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mRecyclerView.setLayoutManager(mLayoutManager);
        //colors are  not working
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.accent_color,
                R.color.accent_material_light,
                R.color.red,
                R.color.primary_color);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mAdapter != null) {
                    mSwipeRefreshLayout.setRefreshing(false);
                } else {
                    if (!isOnline()) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_LONG).show();
                    } else {
                        loadMovieData();
                    }
                }
            }
        });
    }

    public void loadMovieData() {
        if (isOnline()) {
            FetchMovieTask fetchMovieTask = new FetchMovieTask();
            SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
            String defaultValue = getResources().getString(R.string.sort_order);
            String sortOrder = prefs.getString("sort_order", defaultValue);
            fetchMovieTask.execute(sortOrder);
        } else {
            Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isOnline() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

        private final String LOG_TAG = FetchMovieTask.class.getSimpleName();

        @Override
        protected void onPreExecute() {
            mSwipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    if (!mSwipeRefreshLayout.isRefreshing()) {
                        mSwipeRefreshLayout.setRefreshing(true);
                    }
                }
            });
        }

        protected List<Movie> doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String movieJsonStr = null;


            final String apiKey = "65f1348325dc3a10a0a408c9c9100c31";

            try {
                // Construct the URL for the TheMovieDB query
                final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?include_adult=true";
                final String QUERY_PARAM = "sort_by";
                final String API_KEY = "api_key";

                Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(API_KEY, apiKey)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to TheMovieDB, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                movieJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error", e);
/*                Handler handler = new Handler();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Unknown error occurred..", Toast.LENGTH_LONG).show();
                    }
                });*/
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getMovieDataFromJson(movieJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        private List<Movie> getMovieDataFromJson(String MovieJsonStr)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OMW_RESULTS = "results";
            final String OWM_TITLE = "original_title";
            final String OWM_ID = "id";
            final String OWM_GENRE_ID = "genre_ids";
            final String OWM_POSTER_PATH = "poster_path";
            final String OWM_BACKDROP_PATH = "backdrop_path";
            final String OWM_RATING = "vote_average";
            final String OWM_RELEASE_DATE = "release_date";
            final String OWM_OVERVIEW = "overview";

            List<Movie> movieList = new ArrayList<>();
            JSONObject movieJson = new JSONObject(MovieJsonStr);
            JSONArray movieArray = movieJson.getJSONArray(OMW_RESULTS);

            for (int i = 0; i < movieArray.length(); i++) {
                Movie movie = new Movie();
                String title;
                String id;
                String genreId;
                String posterPath;
                String backdropPath;
                String releaseDate;
                String overview;
                double rating;

                // Get the JSON object representing the single movie
                JSONObject singleMovie = movieArray.getJSONObject(i);

                //Get movie title
                title = singleMovie.getString(OWM_TITLE);
                //Get movie ID
                id = singleMovie.getString(OWM_ID);

                //Get movie genre
                try {
                    genreId = getGenre(singleMovie.getJSONArray(OWM_GENRE_ID).getString(0));
                } catch (JSONException e) {
                    genreId = "";
                }

                //Get poster path
                try {
                    posterPath = singleMovie.getString(OWM_POSTER_PATH);
                } catch (JSONException e) {
                    posterPath = "";
                }

                //Get backdrop poster
                try {
                    backdropPath = singleMovie.getString(OWM_BACKDROP_PATH);
                } catch (JSONException e) {
                    backdropPath = "";
                }

                //Get release date of movie
                try {
                    releaseDate = singleMovie.getString(OWM_RELEASE_DATE);
                } catch (JSONException e) {
                    releaseDate = "";
                }

                //Get movie overview
                try {
                    overview = singleMovie.getString(OWM_OVERVIEW);
                } catch (JSONException e) {
                    overview = "";
                }

                //Get movie rating
                try {
                    rating = singleMovie.getDouble(OWM_RATING);
                } catch (JSONException e) {
                    rating = 0;
                }

                //Set fetched results to movie object
                movie.setMovieTitle(title);
                movie.setMovieId(id);
                movie.setMovieGenre(genreId);
                movie.setPosterPath(posterPath);
                movie.setBackdropPath(backdropPath);
                movie.setMovieReleaseDate(releaseDate);
                movie.setMovieOverview(overview);
                movie.setMovieRating(rating);
                movieList.add(movie);
            }

            return movieList;
        }

        private String getGenre(String genreId) {
            String[] id = getResources().getStringArray(R.array.poster_id);
            String[] value = getResources().getStringArray(R.array.poster_id_values);
            String valueGenre = null;

            for (int i = 0; i < id.length; i++) {
                if (genreId.equals(id[i])) {
                    valueGenre = value[i];
                    break;
                }
            }
            return valueGenre;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies != null) {
                mAdapter = new MovieAdapter(movies);
                mRecyclerView.setAdapter(mAdapter);
                mSwipeRefreshLayout.setRefreshing(false);

                mAdapter.SetOnItemClickListener(
                        new MovieAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View v, int position) {
                                movieList = mAdapter.getMovieList();
                                if (movieList != null) {
                                    //Log.e("Click Action", "Click position is " + position);
                                    Movie passedMovie = movieList.get(position);

                                    String movieTitle = passedMovie.getMovieTitle();
                                    String movieGenre = passedMovie.getMovieGenre();
                                    String posterPath = passedMovie.getPosterPath();
                                    String backdropPath = passedMovie.getBackdropPath();
                                    String movieId = passedMovie.getMovieId();
                                    String movieOverview = passedMovie.getMovieOverview();
                                    String movieReleaseDate = passedMovie.getMovieReleaseDate();
                                    double movieRating = passedMovie.getMovieRating();

                                    Intent intent = new Intent(getActivity(), DetailActivity.class);

                                    String packageName = MainActivity.PACKAGE_NAME;

                                    intent.putExtra(packageName + ".movieTitle", movieTitle);
                                    intent.putExtra(packageName + ".movieGenre", movieGenre);
                                    intent.putExtra(packageName + ".posterPath", posterPath);
                                    intent.putExtra(packageName + ".backdropPath", backdropPath);
                                    intent.putExtra(packageName + ".movieId", movieId);
                                    intent.putExtra(packageName + ".movieOverview", movieOverview);
                                    intent.putExtra(packageName + ".movieReleaseDate", movieReleaseDate);
                                    intent.putExtra(packageName + ".movieRating", movieRating);

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        ActivityOptionsCompat options = ActivityOptionsCompat.
                                                makeSceneTransitionAnimation(mActivity, v.findViewById(R.id.movie_poster), "poster");
                                        mActivity.startActivity(intent, options.toBundle());
                                    } else {
                                        getActivity().startActivity(intent);
                                    }
                                }
                            }
                        }
                );
            }
        }
    }
}
