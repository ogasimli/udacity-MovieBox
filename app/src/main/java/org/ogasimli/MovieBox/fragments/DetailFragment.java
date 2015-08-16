package org.ogasimli.MovieBox.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.ogasimli.MovieBox.R;
import org.ogasimli.MovieBox.asynctasks.MovieStore;
import org.ogasimli.MovieBox.asynctasks.ReviewLoader;
import org.ogasimli.MovieBox.asynctasks.TrailerAndReviewStore;
import org.ogasimli.MovieBox.asynctasks.TrailerLoader;
import org.ogasimli.MovieBox.objects.MovieList;
import org.ogasimli.MovieBox.objects.ReviewList;
import org.ogasimli.MovieBox.objects.TrailerList;
import org.ogasimli.MovieBox.provigen.ReviewContract;
import org.ogasimli.MovieBox.provigen.TrailerContract;
import org.ogasimli.MovieBox.retrofit.RetrofitAdapter;
import org.ogasimli.MovieBox.retrofit.TmdbService;

import java.util.ArrayList;
import java.util.Random;
import java.util.StringTokenizer;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment containing movie details
 * Created by ogasimli on 11.07.2015.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<ArrayList> {

    public final static String MOVIE = "movie";

    public final static String FAVORITE = "favorite";

    private static final String FAVORITE_STATE = "favorite_state";

    private static final String TRAILER_STATE_KEY = "trailer_state";

    private static final String REVIEW_STATE_KEY = "review_state";

    private static final String TRAILER_VIEW_STATE_KEY = "trailer_view_state";

    private static final int TRAILER_VIEW_STATE_SUCCESS = 0;

    private static final int TRAILER_VIEW_STATE_FAILURE = 1;

    private static final String REVIEW_VIEW_STATE_KEY = "review_view_state";

    private static final int REVIEW_VIEW_STATE_SUCCESS = 0;

    private static final int REVIEW_VIEW_STATE_FAILURE = 1;

    private final static int TRAILER_LOADER_ID = 0;

    private final static int REVIEW_LOADER_ID = 1;

    private MovieList.Movie mMovie;

    private ImageButton mTrailerImageButton;

    private FloatingActionButton fab;

    private LinearLayout mReviewListView;

    private TextView mNoReviewTextView;

    private MenuItem mShareButton;

    private ArrayList<TrailerList.Trailer> mTrailerList;

    private ArrayList<ReviewList.Review> mReviewList;

    private boolean isFavorite;

    private boolean isConnected;

    private DetailActionListener mDetailActionListener;

    public static DetailFragment getInstance(MovieList.Movie movie, boolean isFavorite) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(MOVIE, movie);
        args.putBoolean(FAVORITE, isFavorite);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DetailActionListener) {
            mDetailActionListener = (DetailActionListener) activity;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        int trailerState = TRAILER_VIEW_STATE_FAILURE;
        if (mTrailerImageButton.getVisibility() == View.VISIBLE) {
            trailerState = TRAILER_VIEW_STATE_SUCCESS;
        }

        int reviewState = REVIEW_VIEW_STATE_FAILURE;
        if (mReviewListView.getVisibility() == View.VISIBLE) {
            reviewState = REVIEW_VIEW_STATE_SUCCESS;
        }

        outState.putInt(TRAILER_VIEW_STATE_KEY, trailerState);
        outState.putInt(REVIEW_VIEW_STATE_KEY, reviewState);
        outState.putBoolean(FAVORITE_STATE, isFavorite);
        outState.putParcelableArrayList(TRAILER_STATE_KEY, mTrailerList);
        outState.putParcelableArrayList(REVIEW_STATE_KEY, mReviewList);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_detail_fragment, menu);
        mShareButton = menu.findItem(R.id.menu_share);
        mShareButton.setEnabled(mTrailerList != null && mTrailerList.size() > 0);
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
        Log.e("Menu", "Destroyed!!");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_share) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_trailer_text,
                    mMovie.movieTitle, mTrailerList.get(0).getYoutubeLink()));
            intent.setType("text/plain");
            startActivity(Intent.createChooser(intent, getString(R.string.share_trailer_title)));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mMovie = getArguments().getParcelable(MOVIE);
        isFavorite = getArguments().getBoolean(FAVORITE);
        if (mMovie == null) {
            throw new NullPointerException("Movie object should be put into fragment arguments.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        ImageView detailBackdropImage = (ImageView) rootView.findViewById(R.id.backdrop_image);
        ImageView detailPosterImage = (ImageView) rootView.findViewById(R.id.detail_movie_poster);
        TextView detailMovieTitle = (TextView) rootView.findViewById(R.id.detail_title_text);
        TextView detailMovieGenre = (TextView) rootView.findViewById(R.id.detail_genre_text);
        TextView detailMovieRelease = (TextView) rootView.findViewById(R.id.detail_release_text);
        TextView detailMovieRating = (TextView) rootView.findViewById(R.id.detail_rating_text);
        RatingBar detailRatingBar = (RatingBar) rootView.findViewById(R.id.detail_rating_bar);
        TextView detailMovieOverview = (TextView) rootView.findViewById(R.id.detail_overview_text);
        mTrailerImageButton = (ImageButton) rootView.findViewById(R.id.play_image);
        mReviewListView = (LinearLayout) rootView.findViewById(R.id.list_view_review);
        mNoReviewTextView = (TextView) rootView.findViewById(R.id.detail_review_text);
        final CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout)
                rootView.findViewById(R.id.coordinator_layout);
        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        if (savedInstanceState == null) {
            ifDeviceIsConnected();
            if (isFavorite && !isConnected) {
                getActivity().getSupportLoaderManager().restartLoader(TRAILER_LOADER_ID, null, this);
                getActivity().getSupportLoaderManager().restartLoader(REVIEW_LOADER_ID, null, this);
            } else {
                loadTrailerAndResponse();
            }
        } else {
            int trailerState = savedInstanceState.
                    getInt(TRAILER_VIEW_STATE_KEY, TRAILER_VIEW_STATE_FAILURE);
            switch (trailerState) {
                case TRAILER_VIEW_STATE_FAILURE:
                    mTrailerImageButton.setVisibility(View.GONE);
                    break;
                case TRAILER_VIEW_STATE_SUCCESS:
                    mTrailerList = savedInstanceState.getParcelableArrayList(TRAILER_STATE_KEY);
                    mTrailerImageButton.setVisibility(View.VISIBLE);
                    break;
            }
            int reviewState = savedInstanceState.getInt(REVIEW_VIEW_STATE_KEY,
                    REVIEW_VIEW_STATE_FAILURE);
            switch (reviewState) {
                case REVIEW_VIEW_STATE_FAILURE:
                    mReviewListView.setVisibility(View.GONE);
                    mNoReviewTextView.setVisibility(View.VISIBLE);
                    break;
                case TRAILER_VIEW_STATE_SUCCESS:
                    mReviewList = savedInstanceState.getParcelableArrayList(REVIEW_STATE_KEY);
                    addReviewsToList();
                    break;
            }
            isFavorite = savedInstanceState.getBoolean(FAVORITE_STATE, false);
        }

        setFavorite(isFavorite);

        tintRatingBar(rootView, detailRatingBar);

        detailMovieTitle.setText(mMovie.movieTitle);
        detailMovieGenre.setText(mMovie.movieGenre);
        detailMovieRelease.setText(mMovie.movieReleaseDate);
        detailRatingBar.setRating((float) mMovie.movieRating);

        if (mMovie.movieRating == 10.0) {
            detailMovieRating.setText(String.format(rootView.getResources().
                    getString(R.string.detail_rating), "10"));
        } else {
            detailMovieRating.setText(String.format(rootView.getResources().
                            getString(R.string.detail_rating),
                    String.valueOf(mMovie.movieRating)));
        }

        Glide.with(this).
                load(mMovie.getPosterUrl()).
                placeholder(R.drawable.movie_placeholder).
                diskCacheStrategy(DiskCacheStrategy.ALL).
                into(detailPosterImage);

        Glide.with(this).
                load(mMovie.getBackdropPosterUrl()).
                diskCacheStrategy(DiskCacheStrategy.ALL).
                into(detailBackdropImage);

        if (mMovie.movieOverview != null) {
            detailMovieOverview.setText(mMovie.movieOverview);
        } else {
            detailMovieOverview.setText(R.string.no_overview);
        }

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grow);
        fab.startAnimation(animation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                favoriteMovie();
                String message = (isFavorite)
                        ? getString(R.string.added_to_favorites_snackbar)
                        : getString(R.string.removed_from_favorites_snackbar);
                Snackbar.
                        make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo_message_snackbar, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                favoriteMovie();
                            }
                        })
                        .show();
            }
        });

        mTrailerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTrailerList != null && mTrailerList.size() > 0) {
                    showDialog();
                }
            }
        });

        return rootView;
    }

    /*Method to check if device has a network connection*/
    private void ifDeviceIsConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initToolbar();
        initCollapsingToolbar();
    }

    /*Method to change the color of ratingBar*/
    private void tintRatingBar(View view, RatingBar ratingBar) {
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(view.getResources().
                        getColor(R.color.accent_color),
                PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(view.getResources().
                        getColor(R.color.rating_bar_empty_color),
                PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(view.getResources().
                        getColor(R.color.light_primary_color),
                PorterDuff.Mode.SRC_ATOP);
    }

    /*Initialize CollapsingToolbarLayout*/
    private void initCollapsingToolbar() {
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) getActivity().
                findViewById(R.id.collapsing_toolbar_layout);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
            collapsingToolbarLayout.setTitle(mMovie.movieTitle);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /*Initialize Toolbar*/
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.detail_toolbar);
        if (toolbar != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }
    }

    private void loadTrailerAndResponse() {
        RestAdapter adapter = RetrofitAdapter.getRestAdapter();
        TmdbService service = adapter.create(TmdbService.class);
        String id = mMovie.movieId;
        service.getTrailer(id, new Callback<TrailerList>() {
            @Override
            public void success(TrailerList trailerList, Response response) {
                mTrailerList = trailerList.results;
                if (mTrailerList != null && mTrailerList.size() > 0) {
                    mTrailerImageButton.setVisibility(View.VISIBLE);
                    mShareButton.setEnabled(true);
                } else {
                    mTrailerImageButton.setVisibility(View.GONE);
                    mShareButton.setEnabled(false);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                mTrailerImageButton.setVisibility(View.GONE);
                mShareButton.setEnabled(false);
                Log.d("RetrofitError", error.toString());
            }
        });

        service.getReview(id, new Callback<ReviewList>() {
            @Override
            public void success(ReviewList reviewList, Response response) {
                mReviewList = reviewList.results;
                addReviewsToList();
            }

            @Override
            public void failure(RetrofitError error) {
                mReviewListView.setVisibility(View.GONE);
                mNoReviewTextView.setVisibility(View.VISIBLE);
                Log.d("RetrofitError", error.toString());
            }
        });
    }

    /*Add reviews to the list*/
    @SuppressWarnings("deprecation")
    private void addReviewsToList() {
        if (mReviewList != null && mReviewList.size() > 0) {
            mReviewListView.removeAllViews();
            //TODO: Handle NullPointerException
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            Random random = new Random();
            for (ReviewList.Review review : mReviewList) {
                View view = inflater.inflate(R.layout.review_list_item, mReviewListView, false);
                ReviewViewHolder viewHolder = new ReviewViewHolder(view);
                viewHolder.userName.setText(review.author);
                viewHolder.reviewContent.setText(review.content);
                int color = Color.argb(255, random.nextInt(256), random.nextInt(256),
                        random.nextInt(256));
                Drawable drawable = ContextCompat
                        .getDrawable(getActivity(), R.drawable.circle_user_background);
                drawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    viewHolder.userAvatar.setBackgroundDrawable(drawable);
                } else {
                    viewHolder.userAvatar.setBackground(drawable);
                }
                mReviewListView.addView(view);
            }
            mReviewListView.setVisibility(View.VISIBLE);
            mNoReviewTextView.setVisibility(View.GONE);
        } else {
            mReviewListView.setVisibility(View.GONE);
            mNoReviewTextView.setVisibility(View.VISIBLE);
        }
    }

    /*Add movie and its trailer and reviews to favorites list*/
    private void favoriteMovie() {
        setFavorite(!isFavorite);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String favoritesString = prefs.getString("favorites", "");
        StringTokenizer stringTokenizer = new StringTokenizer(favoritesString, ",");
        ArrayList<String> list = new ArrayList<>();
        while (stringTokenizer.hasMoreTokens()) {
            list.add(stringTokenizer.nextToken());
        }
        if (isFavorite) {
            list.add(mMovie.movieId);
            storeMovie();
            storeTrailers();
            storeReviews();
        } else {
            list.remove(mMovie.movieId);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String id : list) {
            stringBuilder.append(id).append(",");
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("favorites", stringBuilder.toString());
        editor.apply();

        if (mDetailActionListener != null) {
            mDetailActionListener.onFavoriteChanged(isFavorite);
        }
    }

    private void setFavorite(boolean favorite) {
        isFavorite = favorite;
        int resId;
        if (isFavorite) {
            resId = R.drawable.ic_favorite_added;
        } else {
            resId = R.drawable.ic_favorite;
        }
        if (getActivity() != null) {
            fab.setImageDrawable(ContextCompat.getDrawable(getActivity(), resId));
        }
    }

    /*Methods to store movie data*/
    private void storeMovie() {
        new MovieStore(getActivity()).execute(mMovie);
    }

    /*Method to store movie trailers*/
    private void storeTrailers() {
        if (mTrailerList == null || mTrailerList.size() == 0) {
            return;
        }
        ContentValues[] values = new ContentValues[mTrailerList.size()];
        for (int i = 0; i < mTrailerList.size(); i++) {
            TrailerList.Trailer trailer = mTrailerList.get(i);
            ContentValues value = new ContentValues();
            value.put(TrailerContract.MOVIE_ID, mMovie.movieId);
            value.put(TrailerContract.KEY, trailer.key);
            value.put(TrailerContract.NAME, trailer.name);
            value.put(TrailerContract.SIZE, trailer.size);
            values[i] = value;
        }
        new TrailerAndReviewStore(getActivity(),
                TrailerContract.CONTENT_URI, mMovie.movieId)
                .execute(values);
    }

    /*Method to store movie reviews*/
    private void storeReviews() {
        if (mReviewList == null || mReviewList.size() == 0) {
            return;
        }
        ContentValues[] values = new ContentValues[mReviewList.size()];
        for (int i = 0; i < mReviewList.size(); i++) {
            ReviewList.Review review = mReviewList.get(i);
            ContentValues value = new ContentValues();
            value.put(ReviewContract.MOVIE_ID, mMovie.movieId);
            value.put(ReviewContract.AUTHOR, review.author);
            value.put(ReviewContract.CONTENT, review.content);
            values[i] = value;
        }
        new TrailerAndReviewStore(getActivity(),
                ReviewContract.CONTENT_URI, mMovie.movieId)
                .execute(values);
    }

    private void showDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        android.support.v4.app.DialogFragment newFragment = TrailerDialogFragment.getInstance(mTrailerList);
        newFragment.show(fragmentManager, "dialog");
    }

    /*Callbacks to query data from trailer and review tables*/
    @SuppressWarnings("unchecked")
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        if (id == TRAILER_LOADER_ID) {
            return new TrailerLoader(getActivity(), mMovie.movieId);
        } else {
            return new ReviewLoader(getActivity(), mMovie.movieId);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onLoadFinished(Loader loader, ArrayList data) {
        if (loader.getId() == TRAILER_LOADER_ID) {
            mTrailerList = data;
        } else {
            mReviewList = data;
            addReviewsToList();
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
    }

    public interface DetailActionListener {
        void onFavoriteChanged(boolean isChanged);
    }

    class ReviewViewHolder {

        public final TextView userName;
        public final TextView reviewContent;
        public final ImageView userAvatar;

        public ReviewViewHolder(View itemView) {
            userName = (TextView) itemView.findViewById(R.id.user_name_text_view);
            reviewContent = (TextView) itemView.findViewById(R.id.review_content_text_view);
            userAvatar = (ImageView) itemView.findViewById(R.id.avatar_image);
        }
    }
}
