package com.example.moviesearch.views;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.moviesearch.model.*;
import com.example.moviesearch.controllers.*;
import com.example.moviesearch.*;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MovieListAdapter.MovieClickCallback,
        MovieListManager.MovieListCallback {

    private static final String TAG = MainActivity.class.getSimpleName();

    private LinearLayout mErrorLayout;
    private RecyclerView mMovieRecyclerView;
    private Button mSearchButton;
    private EditText mMovieSearchEditText;
    private MovieListAdapter mMovieAdapter;
    private List<Movie> mMovieList = new ArrayList<>();
    private MovieListManager mMovieListManager = new MovieListManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mErrorLayout = findViewById(R.id.movie_error_layout);
        mMovieRecyclerView = findViewById(R.id.movie_recycler_view);
        mMovieSearchEditText = findViewById(R.id.search_text);
        mSearchButton = findViewById(R.id.search_button);

        mMovieAdapter = new MovieListAdapter(mMovieList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mMovieRecyclerView.setLayoutManager(layoutManager);
        mMovieRecyclerView.setAdapter(mMovieAdapter);

        initListeners(layoutManager);

        if (savedInstanceState != null) {
            List<Movie> savedMovieList = savedInstanceState.getParcelableArrayList(MovieListManager.MOVIE_LIST);
            if (savedMovieList != null && !savedMovieList.isEmpty()) {
                loadSavedMovieData(savedMovieList);
            }
        }

        int options = mMovieSearchEditText.getImeOptions();
        mMovieSearchEditText.setImeOptions(options|EditorInfo.IME_FLAG_NO_EXTRACT_UI);
    }

    private EndlessRecyclerViewScrollListener mScrollListener;
    void initListeners(LinearLayoutManager layoutManager) {
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startMovieSearch();
            }
        });

        mMovieSearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    startMovieSearch();
                    return true;
                }
                return false;
            }
        });

        mScrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                if (totalItemsCount < MovieListManager.sTotalResults) {
                    loadMovieData(page);
                }
            }
        };
        mMovieRecyclerView.addOnScrollListener(mScrollListener);
    }

    private void startMovieSearch() {
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mMovieSearchEditText.getWindowToken(), 0);

        //Clear the data source and start loading results for new search
        MovieListManager.sTotalResults = MovieListManager.NONE;
        mMovieList.clear();
        mScrollListener.resetState();
        loadMovieData(1);
    }

    private void loadSavedMovieData(List<Movie> movieList) {
        Log.d(TAG, "load saved movie data");
        //Hide keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mMovieSearchEditText.getWindowToken(), 0);

        //Clear the data source and start loading results for new search
        MovieListManager.sTotalResults = MovieListManager.NONE;
        mMovieList.clear();
        mScrollListener.resetState();
        updateMovieList(movieList);
    }

    private void loadMovieData(int page) {
        //Get the search string
        String movieToSearch = mMovieSearchEditText.getText().toString();
        Log.d(TAG, "Movie search: " + movieToSearch + " page: " + page);

        mSearchButton.setEnabled(false);
        mMovieListManager.getMovieList(getString(R.string.movies_key), movieToSearch, page, this);
    }

    @Override
    public void onGotMovieList(List<Movie> movieList) {
        if (!isFinishing()) {
            mSearchButton.setEnabled(true);
            showResultsView();
            updateMovieList(movieList);
        }
    }

    @Override
    public void onMovieListFailure() {
        if (!isFinishing()) {
            mSearchButton.setEnabled(true);
            showErrorView();
        }
    }

    @Override
    public void onMovieClicked(Movie movie) {
        if (!isFinishing()) {
            MovieDetailFragment movieDetailFragment = MovieDetailFragment.newInstance(movie);
            movieDetailFragment.show(getSupportFragmentManager(), "MovieDetailFragment");
        }
    }

    private void updateMovieList(List<Movie> newMovieList) {
        mMovieList.addAll(newMovieList);
        mMovieAdapter.notifyDataSetChanged();
    }

    private void showErrorView() {
        mMovieRecyclerView.setVisibility(View.GONE);
        mErrorLayout.setVisibility(View.VISIBLE);
    }

    private void showResultsView() {
        mMovieRecyclerView.setVisibility(View.VISIBLE);
        mErrorLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_favorite:
                Intent i = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(i);
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
        if (mMovieListManager != null) {
            mMovieListManager.unregisterMovieListCallback();
        }
        super.onDestroy();
    }
}
