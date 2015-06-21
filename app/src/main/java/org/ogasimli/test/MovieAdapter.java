package org.ogasimli.test;

import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.List;

/**
 * Created by ogasimli on 20.06.2015.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private List<Movie> movieList;

    public MovieAdapter(List<Movie> movieList) {
        this.movieList = movieList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(view.getResources().getColor(R.color.accent_color), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(view.getResources().getColor(R.color.light_primary_color), PorterDuff.Mode.SRC_ATOP);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Movie movie = movieList.get(i);
        viewHolder.movieTitle.setText(movie.getMovieTitle());
        viewHolder.movieGenre.setText(movie.getMovieGenre());
        viewHolder.movieThumbnail.setImageResource(movie.getMovieThumbnail());
        viewHolder.movieRating.setRating((float) movie.getMovieRating());
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView movieTitle;
        public TextView movieGenre;
        public ImageView movieThumbnail;
        public RatingBar movieRating;

        public ViewHolder(View itemView) {
            super(itemView);
            movieTitle = (TextView)itemView.findViewById(R.id.movie_title_text);
            movieGenre = (TextView) itemView.findViewById(R.id.movie_genre_text);
            movieThumbnail = (ImageView)itemView.findViewById(R.id.film_image);
            movieRating = (RatingBar) itemView.findViewById(R.id.rating_bar);
        }
    }
}
