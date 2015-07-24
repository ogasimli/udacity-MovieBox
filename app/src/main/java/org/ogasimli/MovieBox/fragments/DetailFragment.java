package org.ogasimli.MovieBox.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
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

import org.ogasimli.MovieBox.MainActivity;
import org.ogasimli.MovieBox.R;
import org.ogasimli.MovieBox.movie.MovieList;
import org.ogasimli.MovieBox.movie.ReviewList;
import org.ogasimli.MovieBox.movie.TrailerList;
import org.ogasimli.MovieBox.retrofit.RetrofitAdapter;
import org.ogasimli.MovieBox.retrofit.TmdbService;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Fragment containing movie details
 * Created by ogasimli on 11.07.2015.
 */
public class DetailFragment extends Fragment {

    private Context mContext;
    private MovieList.Movie mMovie;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ImageButton mTrailerImageButton;
    private LinearLayout mReviewListView;
    private TextView mNoReviewTextView;
    private ArrayList<TrailerList.Trailer> mTrailerList;
    private ArrayList<ReviewList.Review> mReviewList;
    private static final String TRAILER_STATE_KEY = "trailer_state";
    private static final String REVIEW_STATE_KEY = "review_state";
    private static final String TRAILER_VIEW_STATE_KEY = "trailer_view_state";
    private static final int TRAILER_VIEW_STATE_SUCCESS = 0;
    private static final int TRAILER_VIEW_STATE_FAILURE = 1;
    private static final String REVIEW_VIEW_STATE_KEY = "review_view_state";
    private static final int REVIEW_VIEW_STATE_SUCCESS = 0;
    private static final int REVIEW_VIEW_STATE_FAILURE = 1;

    public static DetailFragment getInstance(MovieList.Movie movie) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.PACKAGE_NAME, movie);
        fragment.setArguments(args);
        return fragment;
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
        outState.putParcelableArrayList(TRAILER_STATE_KEY, mTrailerList);
        outState.putParcelableArrayList(REVIEW_STATE_KEY, mReviewList);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovie = getArguments().getParcelable(MainActivity.PACKAGE_NAME);
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
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        //Change the color of ratingBar
        LayerDrawable stars = (LayerDrawable) detailRatingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(rootView.getResources().getColor(R.color.accent_color),
                PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(rootView.getResources().getColor(R.color.light_primary_color),
                PorterDuff.Mode.SRC_ATOP);

        if (savedInstanceState == null || !savedInstanceState.containsKey(TRAILER_STATE_KEY) ||
                !savedInstanceState.containsKey(REVIEW_STATE_KEY)) {
            loadTrailerAndResponse();
        } else {
        int trailerState = savedInstanceState.getInt(TRAILER_VIEW_STATE_KEY,
                TRAILER_VIEW_STATE_FAILURE);
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
        }

        detailMovieTitle.setText(mMovie.movieTitle);
        if (mMovie.movieGenre != null){
            detailMovieGenre.setText(mMovie.movieGenre);
        }else {
            detailMovieGenre.setText("Unknown");
        }
        detailMovieRelease.setText(mMovie.movieReleaseDate);

        String rating;
        if (mMovie.movieRating == 10.0){
            rating = String.format(rootView.getResources().getString(R.string.detail_rating), "10");
        }else {
            rating = String.format(rootView.getResources().getString(R.string.detail_rating), String.valueOf(mMovie.movieRating));
        }
        detailMovieRating.setText(rating);
        detailRatingBar.setRating((float) mMovie.movieRating);
        mContext = detailPosterImage.getContext();
        Glide.with(mContext).
                load(mMovie.getPosterUrl()).
                placeholder(R.drawable.movie_placeholder).
                diskCacheStrategy(DiskCacheStrategy.ALL).
                into(detailPosterImage);
        mContext = detailBackdropImage.getContext();
        Glide.with(mContext).
                load(mMovie.getBackdropPosterUrl()).
                placeholder(R.drawable.movie_placeholder).
                diskCacheStrategy(DiskCacheStrategy.ALL).
                into(detailBackdropImage);
        if (mMovie.movieOverview != null){
        detailMovieOverview.setText(mMovie.movieOverview);
        }else {
            detailMovieOverview.setText(R.string.no_overview);
        }

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grow);
        fab.startAnimation(animation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(mCoordinatorLayout, mContext.getString(R.string.fab_message),
                        Snackbar.LENGTH_SHORT)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        })
                        .show();
            }
        });

        mTrailerImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Initialize Toolbar
        initToolbar();

        //Initialize CollapsingToolbarLayout
        initCollapsingToolbar();
    }

    private void initCollapsingToolbar() {
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) getActivity().
                findViewById(R.id.collapsing_toolbar_layout);
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        mCollapsingToolbarLayout.setTitle(mMovie.movieTitle);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) getActivity().findViewById(R.id.detail_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
    }

    private void loadTrailerAndResponse() {
        RestAdapter adapter = RetrofitAdapter.getRestAdapter();
        TmdbService service = adapter.create(TmdbService.class);
        String id = mMovie.movieId;
        service.getTrailer(id, new Callback<TrailerList>() {
            @Override
            public void success(TrailerList trailerList, Response response) {
                mTrailerList = trailerList.results;
                if (mTrailerList.size() != 0){
                    mTrailerImageButton.setVisibility(View.VISIBLE);
                }else {
                    mTrailerImageButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void failure(RetrofitError error) {
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
                Log.d("RetrofitError", error.toString());
            }
        });
    }

    private void addReviewsToList(){
        if (mReviewList != null && mReviewList.size() > 0) {
            mReviewListView.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            for (ReviewList.Review review : mReviewList) {
                View view = inflater.inflate(R.layout.review_list_item, mReviewListView, false);
                ReviewViewHolder viewHolder = new ReviewViewHolder(view);
                viewHolder.userName.setText(review.author);
                viewHolder.reviewContent.setText(review.content);
                mReviewListView.addView(view);
            }
            mReviewListView.setVisibility(View.VISIBLE);
            mNoReviewTextView.setVisibility(View.GONE);
        } else {
            mReviewListView.setVisibility(View.GONE);
            mNoReviewTextView.setVisibility(View.VISIBLE);
        }
    }

    void showDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        android.support.v4.app.DialogFragment newFragment = TrailerDialogFragment.getInstance(mTrailerList);
        newFragment.show(fragmentManager, "dialog");
    }

    class ReviewViewHolder {

        public TextView userName;
        public TextView reviewContent;
        public ImageView userAvatar;

        public ReviewViewHolder(View itemView) {
            userName = (TextView) itemView.findViewById(R.id.user_name_text_view);
            reviewContent = (TextView) itemView.findViewById(R.id.review_content_text_view);
            userAvatar = (ImageView) itemView.findViewById(R.id.avatar_image);
        }
    }
}
