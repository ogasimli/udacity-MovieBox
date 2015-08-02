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

    public int position;
    private ArrayList<MovieList.Movie> mMovieList;
    private OnItemClickListener mItemClickListener;
    private View view;

    public MovieAdapter() {
    }

    public ArrayList<MovieList.Movie> getMovieList() {
        return mMovieList;
    }

    public void setMovieList(ArrayList<MovieList.Movie> movieList) {
        this.mMovieList = movieList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        //Change the color of ratingBar
        tintRatingBar(view, viewHolder.movieRating);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        MovieList.Movie movie = mMovieList.get(i);

        viewHolder.movieTitle.setText(movie.movieTitle);

        if (movie.movieGenre != null) {
            viewHolder.movieGenre.setText(movie.movieGenre);
        } else {
            movie.movieGenre = determineGenre(view, movie.genreIds);
            viewHolder.movieGenre.setText(movie.movieGenre);
        }

        Glide.with(viewHolder.moviePoster.getContext()).
                load(movie.getPosterUrl()).
                placeholder(R.drawable.movie_placeholder).
                diskCacheStrategy(DiskCacheStrategy.ALL).
                into(viewHolder.moviePoster);

        viewHolder.movieRating.setRating(decreaseRating(movie.movieRating));
        position = i;
    }

    @Override
    public int getItemCount() {
        return mMovieList == null ? 0 : mMovieList.size();
    }

    /*Method to determine the genre string*/
    private String determineGenre(View view, ArrayList<String> genreId) {
        String resultGenre;
        ArrayList<String> genreList = new ArrayList<>();
        String[] id = view.getResources().getStringArray(R.array.genre_id);
        String[] value = view.getResources().getStringArray(R.array.genre_id_values);

        if (genreId != null && genreId.size() > 0) {
            for (int i = 0; i < genreId.size(); i++) {
                for (int j = 0; j < id.length; j++) {
                    if (id[j].equals(genreId.get(i))) {
                        genreList.add(value[j]);
                        break;
                    }
                    if (genreList.size() > 2) {
                        break;
                    }
                }
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < genreList.size(); i++) {
                stringBuilder.append(genreList.get(i));
                stringBuilder.append(", ");
            }
            stringBuilder.setLength(stringBuilder.length() - 2);
            resultGenre = stringBuilder.toString();
        } else {
            resultGenre = view.getResources().getString(R.string.unknown_genre_text);
        }

        return resultGenre;
    }

    /*Method to change the color of ratingBar*/
    private void tintRatingBar(View view, RatingBar ratingBar) {
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(2).setColorFilter(view.getResources().getColor(R.color.accent_color),
                PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(view.getResources().getColor(R.color.rating_bar_empty_color),
                PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(view.getResources().getColor(R.color.light_primary_color),
                PorterDuff.Mode.SRC_ATOP);
    }

    /*Method to get the half of movie ratings*/
    private float decreaseRating(double rating) {
        return (float) Math.round((rating * 5 / 10) * 100) / 100;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final TextView movieTitle;

        public final TextView movieGenre;

        public final ImageView moviePoster;

        public final RatingBar movieRating;

        public final String id[];

        public final String value[];

        public ViewHolder(View itemView) {
            super(itemView);
            movieTitle = (TextView) itemView.findViewById(R.id.movie_title_text);
            movieGenre = (TextView) itemView.findViewById(R.id.movie_genre_text);
            moviePoster = (ImageView) itemView.findViewById(R.id.movie_poster);
            movieRating = (RatingBar) itemView.findViewById(R.id.rating_bar);
            id = itemView.getResources().getStringArray(R.array.genre_id);
            value = itemView.getResources().getStringArray(R.array.genre_id_values);
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
}
