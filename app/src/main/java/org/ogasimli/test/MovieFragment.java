package org.ogasimli.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;

    public MovieFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (isOnline()){
            loadMovieData();
        }else {
            Toast.makeText(getActivity(),"No internet connection",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_moviefragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        SharedPreferences.Editor prefs = getActivity().getPreferences(Context.MODE_PRIVATE).edit();

        switch (id){
            case R.id.action_popularity:
                prefs.putString("sort_order", "popularity.desc");
                prefs.apply();
                break;
            case R.id.action_rating:
                prefs.putString("sort_order", "vote_average.desc");
                prefs.apply();
                break;
            case R.id.action_revenue:
                prefs.putString("sort_order", "revenue.desc");
                prefs.apply();
                break;
        }

        loadMovieData();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new GridLayoutManager(getActivity(),2);
        mRecyclerView.setLayoutManager(mLayoutManager);

        return rootView;
    }

    public void loadMovieData(){
        FetchMovieTask fetchMovieTask = new FetchMovieTask();
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        String defaultValue = getResources().getString(R.string.sort_order);
        String sortOrder = prefs.getString("sort_order", defaultValue);
        fetchMovieTask.execute(sortOrder);
    }


    private boolean isOnline(){
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
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the movie data, there's no point in attemping
                // to parse it.
                return null;
            } finally{
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

            for(int i = 0; i < movieArray.length(); i++) {
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

        private String getGenre (String genreId){
            String[] id = getResources().getStringArray(R.array.poster_id);
            String[] value = getResources().getStringArray(R.array.poster_id_values);
            String valueGenre=null;

            for (int i = 0; i < id.length; i++) {
                if (genreId.equals(id[i])){
                    valueGenre = value[i];
                    break;
                }
            }
            return valueGenre;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            if (movies!=null){
                mAdapter = new MovieAdapter(movies, getActivity());
                mRecyclerView.setAdapter(mAdapter);
//              mAdapter.setMovieList(null);
            }
        }
    }
}
