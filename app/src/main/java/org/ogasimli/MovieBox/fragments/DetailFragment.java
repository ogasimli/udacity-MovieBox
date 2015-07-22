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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.ogasimli.MovieBox.MainActivity;
import org.ogasimli.MovieBox.R;
import org.ogasimli.MovieBox.movie.MovieList;

/**
 * Fragment containing movie details
 * Created by ogasimli on 11.07.2015.
 */
public class DetailFragment extends Fragment {

    private Context context;
    private Toolbar mToolbar;
    private MovieList.Movie mMovie;
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    public static DetailFragment getInstance(MovieList.Movie movie) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(MainActivity.PACKAGE_NAME, movie);
        fragment.setArguments(args);
        return fragment;
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
        final CoordinatorLayout mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_layout);
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        //Change the color of ratingBar
        LayerDrawable stars = (LayerDrawable) detailRatingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(rootView.getResources().getColor(R.color.accent_color),
                PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(rootView.getResources().getColor(R.color.light_primary_color),
                PorterDuff.Mode.SRC_ATOP);


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
        context = detailPosterImage.getContext();
        Glide.with(context).
                load(mMovie.getPosterUrl()).
                placeholder(R.drawable.movie_placeholder).
                diskCacheStrategy(DiskCacheStrategy.ALL).
                into(detailPosterImage);
        context = detailBackdropImage.getContext();
        Glide.with(context).
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
                Snackbar.make(mCoordinatorLayout,context.getString(R.string.fab_message),
                        Snackbar.LENGTH_SHORT)
                        .setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                            }
                        })
                        .show();
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
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.collapsing_toolbar_layout);
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
        mCollapsingToolbarLayout.setTitle(mMovie.movieTitle);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initToolbar() {
        mToolbar = (Toolbar) getActivity().findViewById(R.id.detail_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
    }
}
