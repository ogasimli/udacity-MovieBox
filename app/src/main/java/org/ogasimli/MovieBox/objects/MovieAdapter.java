package org.ogasimli.MovieBox.objects;

import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.ogasimli.MovieBox.R;

import java.util.ArrayList;

/**
 * Custom Adapter for movies
 * Created by ogasimli on 01.07.2015.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    private ArrayList<MovieList.Movie> mMovieList;

    private OnItemClickListener mItemClickListener;

    public MovieAdapter() {
    }

    public void setMovieList(ArrayList<MovieList.Movie> movieList) {
        this.mMovieList = movieList;
    }

    public ArrayList<MovieList.Movie> getMovieList() {
        return mMovieList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        RatingBar ratingBar = (RatingBar) view.findViewById(R.id.rating_bar);
        //Change the color of ratingBar
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(view.getResources().getColor(R.color.accent_color),
                PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(view.getResources().getColor(R.color.light_primary_color),
                PorterDuff.Mode.SRC_ATOP);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        MovieList.Movie movie = mMovieList.get(i);

        viewHolder.movieTitle.setText(movie.movieTitle);

        if (movie.genreIds.size() != 0) {
            viewHolder.movieGenre.setText(movie.getMovieGenre(movie.genreIds));
        } else {
            viewHolder.movieGenre.setText(R.string.unknown_genre_text);
        }

        Glide.with(viewHolder.moviePoster.getContext()).
                load(movie.getPosterUrl()).
                placeholder(R.drawable.movie_placeholder).
                diskCacheStrategy(DiskCacheStrategy.ALL).
                into(viewHolder.moviePoster);

        viewHolder.movieRating.setRating(decreaseRating(movie.movieRating));
    }

    @Override
    public int getItemCount() {
        return mMovieList == null ? 0 : mMovieList.size();
    }

    //Method to decrease movie ratings in order to be able to assign them into RatingBar
    // on card_item layout
    private float decreaseRating(double rating) {
        return (float) Math.round((rating * 5 / 10) * 100) / 100;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView movieTitle;
        public final TextView movieGenre;
        public final ImageView moviePoster;
        public final RatingBar movieRating;

        public ViewHolder(View itemView) {
            super(itemView);
            movieTitle = (TextView) itemView.findViewById(R.id.movie_title_text);
            movieGenre = (TextView) itemView.findViewById(R.id.movie_genre_text);
            moviePoster = (ImageView) itemView.findViewById(R.id.movie_poster);
            movieRating = (RatingBar) itemView.findViewById(R.id.rating_bar);
            itemView.setOnClickListener(this);
        }

        //Set OnItemClickListener to RecyclerView
        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                mItemClickListener.onItemClick(v, getAdapterPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }
}
