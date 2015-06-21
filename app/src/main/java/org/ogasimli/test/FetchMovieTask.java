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

/**
 * Created by ogasimli on 21.06.2015.
 */
public class FetchMovieTask extends AsyncTask<String, Void, List<Movie>> {

    private final String LOG_TAG = FetchMovieTask.class.getSimpleName();
    MovieAdapter mAdapter;
    RecyclerView mRecyclerView;

    @Override
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
            // If the code didn't successfully get the weather data, there's no point in attemping
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

/*        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];

        // Data is fetched in Celsius by default.
        // If user prefers to see in Fahrenheit, convert the values here.
        // We do this rather than fetching in Fahrenheit so that the user can
        // change this option without us having to re-fetch the data once
        // we start storing the values in a database.
        SharedPreferences sharedPrefs =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String unitType = sharedPrefs.getString(
                getString(R.string.pref_units_key),
                getString(R.string.pref_units_metric));*/

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
            //Get movie genre
            genreId = singleMovie.getJSONArray(OWM_GENRE_ID).getString(0);
            movie.setMovieGenre(genreId);
            //Get poster path
            posterPath = singleMovie.getString(OWM_POSTER_PATH);
            //Get movie rating
            rating = singleMovie.getDouble(OWM_RATING);
            movie.setMovieRating(rating);

            movieList.add(movie);


/*            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            title = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = singleMovie.getJSONArray(OWM_WEATHER).getJSONObject(0);
            genreId = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = singleMovie.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            pposterpath = formatHighLows(high, low, unitType);
            resultStrs[i] = title + " - " + genreId + " - " + pposterpath;*/
        }

        return movieList;

    }

    @Override
    protected void onPostExecute(List<Movie> movies) {
        if (movies!=null){
//              mAdapter.setMovieList(null);
                mAdapter = new MovieAdapter(movies);
                mRecyclerView.setAdapter(mAdapter);
        }
    }
}

