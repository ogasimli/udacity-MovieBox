package org.ogasimli.MovieBox.objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.preference.PreferenceManager;
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
import org.ogasimli.MovieBox.fragments.MovieFragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Custom Adapter for movies
 * Created by ogasimli on 01.07.2015.
 */
public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    final private Context mContext;

    final private MovieFragment.MovieActionListener movieActionListener;

    final private boolean isDualPane;

    public int mSelectedPosition;

    private ArrayList<MovieList.Movie> mMovieList;

    private boolean isFavorite;

    private OnItemClickListener mItemClickListener;

    private View view;

    public MovieAdapter(Context mContext,
                        MovieFragment.MovieActionListener movieActionListener,
                        boolean isDualPane) {
        this.mContext = mContext;
        this.movieActionListener = movieActionListener;
        this.isDualPane = isDualPane;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        //Change the color of ratingBar
        tintRatingBar(view, viewHolder.mMovieRating);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        MovieList.Movie movie = mMovieList.get(i);

        viewHolder.mMovieTitle.setText(movie.movieTitle);

        if (movie.movieGenre != null) {
            viewHolder.mMovieGenre.setText(movie.movieGenre);
        } else {
            movie.movieGenre = determineGenre(view, movie.genreIds);
            viewHolder.mMovieGenre.setText(movie.movieGenre);
        }

        Glide.with(viewHolder.mMoviePoster.getContext()).
                load(movie.getPosterUrl()).
                placeholder(R.drawable.movie_placeholder).
                diskCacheStrategy(DiskCacheStrategy.ALL).
                into(viewHolder.mMoviePoster);

        viewHolder.mMovieRating.setRating(decreaseRating(movie.movieRating));

        if (isDualPane) {
            viewHolder.itemView.setSelected(mSelectedPosition == i);
        }
    }

    @Override
    public int getItemCount() {
        return mMovieList == null ? 0 : mMovieList.size();
    }

    @Override
    public long getItemId(int position) {
        return Long.parseLong(mMovieList.get(position).movieId);
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

    public void setMovieList(ArrayList<MovieList.Movie> movieList) {
        mMovieList = movieList;
        notifyDataSetChanged();
        if (isDualPane) {
            int position = (mSelectedPosition < mMovieList.size()) ? mSelectedPosition
                    : mMovieList.size() - 1;
            selectMovie(position, null);
        }
    }

    public void selectMovie(int position, View view) {
        mSelectedPosition = position;
        MovieList.Movie movie = mMovieList.get(position);
        ifMovieIsFavorite(movie.movieId);
        movieActionListener.onMovieSelected(movie, isFavorite, view);
    }

    /*Method to determine if movie is in favorites list*/
    private void ifMovieIsFavorite(String movieId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String favoritesString = prefs.getString("favorites", "");
        ArrayList<String> list = new ArrayList<>();
        if (favoritesString.length() > 0) {
            StringTokenizer st = new StringTokenizer(favoritesString, ",");
            while (st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
        }
        Collections.reverse(list);
        boolean favorite = false;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(movieId)) {
                favorite = true;
                break;
            }
        }
        isFavorite = favorite;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    /*Movie view holder class*/
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @InjectView(R.id.movie_title_text)
        TextView mMovieTitle;

        @InjectView(R.id.movie_genre_text)
        TextView mMovieGenre;

        @InjectView(R.id.movie_poster)
        ImageView mMoviePoster;

        @InjectView(R.id.rating_bar)
        RatingBar mMovieRating;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
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
