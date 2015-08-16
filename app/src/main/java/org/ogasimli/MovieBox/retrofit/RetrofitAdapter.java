package org.ogasimli.MovieBox.retrofit;

import org.ogasimli.MovieBox.BuildConfig;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * App RestAdapter configuration class.
 * Created by ogasimli on 12.07.2015.
 */
public class RetrofitAdapter {

    private final static String BASE_URL = "http://api.themoviedb.org/3/";

    private final static String API_KEY = BuildConfig.THE_MOVIE_DB_API_KEY;
    private static final RequestInterceptor mRequestInterceptor = new RequestInterceptor() {
        @Override
        public void intercept(RequestFacade request) {
            request.addQueryParam("api_key", API_KEY);
        }
    };
    private static RestAdapter mRestAdapter;

    public static RestAdapter getRestAdapter() {
        if (mRestAdapter == null) {
            mRestAdapter = new RestAdapter.Builder()
                    .setEndpoint(BASE_URL)
                    .setRequestInterceptor(mRequestInterceptor)
                    .build();
            if (BuildConfig.DEBUG) {
                mRestAdapter.setLogLevel(RestAdapter.LogLevel.FULL);
            }

        }
        return mRestAdapter;
    }
}
