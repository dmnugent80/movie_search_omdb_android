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
import com.example.moviesearch.model.Movie;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Adapter class for the Favorites RecyclerView
 */

public class FavoriteListAdapter extends RecyclerView.Adapter<FavoriteListAdapter.FavoriteViewHolder> {
    private List<Movie> mMovieList;
    private WeakReference<FavoriteClickCallback> mMovieClickListener;

    public interface FavoriteClickCallback {
        void onFavoriteClicked(Movie movie);
    }

    public class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mPosterImage;
        private TextView mTitleTextView, mIdTextView;
        public FavoriteViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            mTitleTextView = view.findViewById(R.id.tv_title);
            mIdTextView = view.findViewById(R.id.tv_id);
            mPosterImage = view.findViewById(R.id.iv_logo);
        }

        @Override
        public void onClick(View view) {
            int position = getLayoutPosition();
            Movie movie = mMovieList.get(position);
            FavoriteClickCallback callback = mMovieClickListener.get();
            if (movie != null && callback != null) {
                callback.onFavoriteClicked(movie);
            }
        }
    }

    public FavoriteListAdapter(List<Movie> movieList, FavoriteClickCallback callback) {
        mMovieList = movieList;
        mMovieClickListener = new WeakReference<>(callback);
    }

    @Override
    public FavoriteListAdapter.FavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new FavoriteViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final FavoriteViewHolder holder, final int position) {
        Movie movie = mMovieList.get(position);
        holder.mTitleTextView.setText(movie.mTitle);
        holder.mIdTextView.setText(movie.mId);

        Glide.with(App.getContext())
                .load(movie.mPosterUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.baseline_local_movies_black_48)
                        .fitCenter())
                .into(holder.mPosterImage);
    }

    @Override
    public int getItemCount() {
        return mMovieList.size();
    }
}