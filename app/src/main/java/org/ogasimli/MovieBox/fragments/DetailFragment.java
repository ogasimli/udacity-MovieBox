package org.ogasimli.MovieBox.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

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
    private MovieList.Movie mMovie;

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
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

        //Change the color of ratingBar
        LayerDrawable stars = (LayerDrawable) detailRatingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(rootView.getResources().getColor(R.color.accent_color), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(rootView.getResources().getColor(R.color.light_primary_color), PorterDuff.Mode.SRC_ATOP);


        detailMovieTitle.setText(mMovie.movieTitle);
        detailMovieGenre.setText(mMovie.getMovieGenre());
        detailMovieRelease.setText(mMovie.movieReleaseDate);
        String rating = String.format(rootView.getResources().getString(R.string.detail_rating),String.valueOf(mMovie.movieRating));
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
        detailMovieOverview.setText(mMovie.movieOverview);

        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.grow);
        fab.startAnimation(animation);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),
                        context.getString(R.string.fab_message),
                        Toast.LENGTH_SHORT).show();
            }
        });

        return rootView;
    }
}
