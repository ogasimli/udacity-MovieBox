/*
package org.ogasimli.test;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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

*/
/**
 * Created by ogasimli on 22.06.2015.
 *//*

public class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    MovieFragment movieFragment;
    RecyclerView.Adapter adapter;

    protected List<Movie> doInBackground(String... params) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String movieJsonStr = null;


        final String apiKey = "65f1348325dc3a10a0a408c9c9100c31";

        try {
            // Construct the URL for the TheMovieDB query
            final String MOVIE_BASE_URL = "http://api.themoviedb.org/3/discover/movie?sort_by=popularity.desc&";
            final String API_KEY = "api_key";

            Uri builtUri = Uri.parse(MOVIE_BASE_URL).buildUpon()
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
        final String OWM_RATING = "vote_average";

        List<Movie> movieList = new ArrayList<>();
        JSONObject movieJson = new JSONObject(MovieJsonStr);
        JSONArray movieArray = movieJson.getJSONArray(OMW_RESULTS);

        for(int i = 0; i < movieArray.length(); i++) {
            Movie movie = new Movie();
            String title;
            String id;
            String genreId;
            String posterPath;
            double rating;

            // Get the JSON object representing the single movie
            JSONObject singleMovie = movieArray.getJSONObject(i);
            //Get movie title
            title = singleMovie.getString(OWM_TITLE);
            movie.setMovieTitle(title);
            //Get movie ID
            id = singleMovie.getString(OWM_ID);
            movie.setMovieId(id);
            //Get movie genre
            genreId = singleMovie.getJSONArray(OWM_GENRE_ID).getString(0);
            movie.setMovieGenre(genreId);
            //Get poster path
            posterPath = singleMovie.getString(OWM_POSTER_PATH);
            movie.setPosterPath(posterPath);
            //Get movie rating
            rating = singleMovie.getDouble(OWM_RATING);
            movie.setMovieRating(rating);

            movieList.add(movie);
        }
        return movieList;
    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        if (movies!=null){
            adapter = movieFragment.mAdapter;
            adapter = new MovieAdapter(movies);
            movieFragment.mRecyclerView.setAdapter(adapter);
//              mAdapter.setMovieList(null);
        }
    }
}
*/
