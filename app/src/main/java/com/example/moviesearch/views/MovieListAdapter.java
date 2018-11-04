package com.example.moviesearch.views;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviesearch.App;
import com.example.moviesearch.R;
import com.example.moviesearch.controllers.FavoritesDbManager;
import com.example.moviesearch.model.FavoritesDbHelper;
import com.example.moviesearch.model.Movie;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Adapter class for the Movie RecyclerView
 */

public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieViewHolder> {
    private List<Movie> mMovieList;
    private WeakReference<MovieClickCallback> mMovieClickListener;

    public interface MovieClickCallback {
        void onMovieClicked(Movie movie);
    }

    public class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mPosterImage;
        private TextView mTitleTextView, mIdTextView;
        private ImageView mFavoriteImageView;
        public MovieViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            mTitleTextView = view.findViewById(R.id.tv_title);
            mIdTextView = view.findViewById(R.id.tv_id);
            mPosterImage = view.findViewById(R.id.iv_logo);
            mFavoriteImageView = view.findViewById(R.id.iv_favorite);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            Movie movie = mMovieList.get(position);
            MovieClickCallback callback = mMovieClickListener.get();
            if (movie != null && callback != null) {
                callback.onMovieClicked(movie);
            }
        }
    }

    public MovieListAdapter(List<Movie> movieList, MovieClickCallback listener) {
        mMovieList = movieList;
        mMovieClickListener = new WeakReference<>(listener);
    }

    @Override
    public MovieListAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MovieViewHolder holder, final int position) {
        Movie movie = mMovieList.get(position);
        holder.mTitleTextView.setText(movie.mTitle);
        holder.mIdTextView.setText(movie.mId);

        if (movie.mIsFavorite) {
            holder.mFavoriteImageView.setImageDrawable(App.getContext().getDrawable(R.drawable.baseline_favorite_white_24));
        } else {
            holder.mFavoriteImageView.setImageDrawable(App.getContext().getDrawable(R.drawable.baseline_favorite_border_white_24));
        }

        holder.mFavoriteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFavoriteClicked(holder, position);
            }
        });

        Glide.with(App.getContext())
                .load(movie.mPosterUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.baseline_local_movies_black_48)
                        .fitCenter())
                .into(holder.mPosterImage);
    }

    private void onFavoriteClicked(MovieViewHolder holder, int position) {
        Movie movie = mMovieList.get(position);
        if (movie.mIsFavorite) {
            movie.mIsFavorite = false;
            holder.mFavoriteImageView.setImageDrawable(App.getContext().getDrawable(R.drawable.baseline_favorite_border_white_24));
            //TODO: get this off the UI thread:
            FavoritesDbHelper.getInstance(App.getContext()).removeFavorite(App.getContext(), movie.mTitle);
            //Update the Set
            FavoritesDbManager.updateFavorites();
        } else {
            movie.mIsFavorite = true;
            holder.mFavoriteImageView.setImageDrawable(App.getContext().getDrawable(R.drawable.baseline_favorite_white_24));
            //TODO: get this off the UI thread:
            FavoritesDbHelper.getInstance(App.getContext()).addFavorite(App.getContext(), movie.mTitle, movie.mId, movie.mPosterUrl);
            //Update the Set
            FavoritesDbManager.updateFavorites();
        }
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }
}
