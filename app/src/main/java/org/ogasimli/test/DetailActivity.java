package org.ogasimli.test;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;


public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.detail_container, new DetailFragment())
                    .commit();
        }
    }

    public static class DetailFragment extends Fragment {

        private String movieTitle;
        private String movieGenre;
        private String posterPath;
        private String backdropPath;
        private String movieId;
        private String movieOverview;
        private String movieReleaseDate;
        private double movieRating;
        private Context context;

        public DetailFragment() {
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

            LayerDrawable stars = (LayerDrawable) detailRatingBar.getProgressDrawable();
            stars.getDrawable(2).setColorFilter(rootView.getResources().getColor(R.color.accent_color), PorterDuff.Mode.SRC_ATOP);
            stars.getDrawable(0).setColorFilter(rootView.getResources().getColor(R.color.light_primary_color), PorterDuff.Mode.SRC_ATOP);

            String packageName = MainActivity.PACKAGE_NAME;
            Intent intent = getActivity().getIntent();
            if (intent!=null){
                movieTitle = intent.getStringExtra(packageName + ".movieTitle");
                movieGenre = intent.getStringExtra(packageName + ".movieGenre");
                posterPath = intent.getStringExtra(packageName + ".posterPath");
                backdropPath = intent.getStringExtra(packageName + ".backdropPath");
                movieId = intent.getStringExtra(packageName + ".movieId");
                movieOverview = intent.getStringExtra(packageName + ".movieOverview");
                movieReleaseDate = intent.getStringExtra(packageName + ".movieReleaseDate");
                movieRating = intent.getDoubleExtra(packageName + ".movieRating", 0);
            }


            detailMovieTitle.setText(movieTitle);
            detailMovieGenre.setText(movieGenre);
            detailMovieRelease.setText(movieReleaseDate);
            String rating = String.format(rootView.getResources().getString(R.string.detail_rating),String.valueOf(movieRating));
            detailMovieRating.setText(rating);
            detailRatingBar.setRating((float) movieRating);
            context = detailPosterImage.getContext();
            Glide.with(context).load("http://image.tmdb.org/t/p/w185/" + posterPath).into(detailPosterImage);
            context = detailBackdropImage.getContext();
            Glide.with(context).load("http://image.tmdb.org/t/p/w500/" + backdropPath).into(detailBackdropImage);
            detailMovieOverview.setText(movieOverview);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(),
                            "This button will save the movie to your favorite list",
                            Toast.LENGTH_SHORT).show();
                }
            });

            return rootView;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Reverse the scene transition by home button press
            case android.R.id.home:
                supportFinishAfterTransition();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
