package org.ogasimli.MovieBox.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
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
    private Toolbar mToolbar;
    private MovieList.Movie mMovie;
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    ImageButton trailerImageButton;
    private ArrayList<TrailerList.Trailer> mTrailerList;
    private ArrayList<ReviewList.Review> mReviewList;
    private static final String TRAILER_STATE_KEY = "trailer_state";
    private static final String REVIEW_STATE_KEY = "review_state";
    private static final String IMAGE_BUTTON_VIEW_STATE_KEY = "view_state";
    private static final int IMAGE_BUTTON_VIEW_STATE_VISIBLE = 0;
    private static final int IMAGE_BUTTON_VIEW_STATE_GONE = 1;

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

        int state = IMAGE_BUTTON_VIEW_STATE_GONE;
        if (trailerImageButton.getVisibility() == View.VISIBLE) {
            state = IMAGE_BUTTON_VIEW_STATE_VISIBLE;
        }

        outState.putInt(IMAGE_BUTTON_VIEW_STATE_KEY, state);
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
        trailerImageButton = (ImageButton) rootView.findViewById(R.id.play_image);
        final CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);
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
        int state = savedInstanceState.getInt(IMAGE_BUTTON_VIEW_STATE_KEY, IMAGE_BUTTON_VIEW_STATE_GONE);
        switch (state) {
            case IMAGE_BUTTON_VIEW_STATE_GONE:
                trailerImageButton.setVisibility(View.GONE);
                break;
            case IMAGE_BUTTON_VIEW_STATE_VISIBLE:
                mTrailerList = savedInstanceState.getParcelableArrayList(TRAILER_STATE_KEY);
                mReviewList = savedInstanceState.getParcelableArrayList(REVIEW_STATE_KEY);
                trailerImageButton.setVisibility(View.VISIBLE);
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

        trailerImageButton.setOnClickListener(new View.OnClickListener() {
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
        mToolbar = (Toolbar) getActivity().findViewById(R.id.detail_toolbar);
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
                    trailerImageButton.setVisibility(View.VISIBLE);
                }else {
                    trailerImageButton.setVisibility(View.GONE);
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
            }

            @Override
            public void failure(RetrofitError error) {

                Log.d("RetrofitError", error.toString());
            }
        });
    }

    void showDialog() {
        FragmentManager fragmentManager = getFragmentManager();
        android.support.v4.app.DialogFragment newFragment = TrailerDialog.getInstance(mTrailerList);
        newFragment.show(fragmentManager, "dialog");
    }

    public static class TrailerDialog extends android.support.v4.app.DialogFragment {

        private ArrayList<TrailerList.Trailer> mTrailerList;
        private TrailerList.Trailer mTrailer;

        public static TrailerDialog getInstance(ArrayList<TrailerList.Trailer> trailers) {
            TrailerDialog dialog = new TrailerDialog();
            Bundle args = new Bundle();
            args.putParcelableArrayList(MainActivity.PACKAGE_NAME, trailers);
            dialog.setArguments(args);
            return dialog;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTrailerList = getArguments().getParcelableArrayList(MainActivity.PACKAGE_NAME);
            if (mTrailerList == null) {
                throw new NullPointerException("Trailer object should be put into dialog arguments.");
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            String[] trailerNames = new String[mTrailerList.size()];
            for (int i = 0; i < mTrailerList.size(); i++) {
                mTrailer = mTrailerList.get(i);
                trailerNames[i] = mTrailer.name + " (" + mTrailer.size + "p)";
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Pick a trailer")
                    .setItems(trailerNames, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            openTrailer(mTrailerList.get(which).getYoutubeLink());
                        }
                    });
            return builder.create();
        }

        private void openTrailer(String url) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
    }

}
