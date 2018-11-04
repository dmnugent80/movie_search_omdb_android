package com.example.moviesearch.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.example.moviesearch.controllers.*;
import com.example.moviesearch.model.*;
import com.example.moviesearch.*;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity implements
        FavoritesDbManager.FavoritesCallback,
        FavoriteListAdapter.FavoriteClickCallback{

    private static final String TAG = FavoritesActivity.class.getSimpleName();

    private RecyclerView mMovieRecyclerView;
    private FavoriteListAdapter mMovieAdapter;
    private List<Movie> mMovieList = new ArrayList<>();

    FavoritesDbManager mFavoritesDbManager = new FavoritesDbManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMovieRecyclerView = findViewById(R.id.movie_recycler_view);

        mMovieAdapter = new FavoriteListAdapter(mMovieList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMovieRecyclerView.setLayoutManager(layoutManager);
        mMovieRecyclerView.setAdapter(mMovieAdapter);

        if (savedInstanceState != null) {
            List<Movie> savedMovieList = savedInstanceState.getParcelableArrayList(MovieListManager.MOVIE_LIST);
            if (savedMovieList != null && !savedMovieList.isEmpty()) {
                onGotFavoritesDbResult(savedMovieList);
            } else {
                mFavoritesDbManager.getFavorites(this);
            }
        } else {
            mFavoritesDbManager.getFavorites(this);
        }
    }

    @Override
    public void onGotFavoritesDbResult(List<Movie> favoritesList) {
        if (!isFinishing()) {
            mMovieList.clear();
            mMovieList.addAll(favoritesList);
            mMovieAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onFavoriteClicked(Movie movie) {
        if (!isFinishing()) {
            MovieDetailFragment movieDetailFragment = MovieDetailFragment.newInstance(movie);
            movieDetailFragment.show(getSupportFragmentManager(), "MovieDetailFragment");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!mMovieList.isEmpty()) {
            outState.putParcelableArrayList(MovieListManager.MOVIE_LIST, (ArrayList<Movie>) mMovieList);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (mFavoritesDbManager != null) {
            mFavoritesDbManager.unregisterFavoritesCallback();
        }
        super.onDestroy();
    }
}
