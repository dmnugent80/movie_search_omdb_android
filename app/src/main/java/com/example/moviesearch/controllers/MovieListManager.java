package com.example.moviesearch.controllers;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.moviesearch.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Controller class for the main Movie List
 */

public class MovieListManager {

    private static final String TAG = MovieListManager.class.getSimpleName();

    private static final String MOVIE_URL = "http://www.omdbapi.com/";
    private static final String S = "s";
    private static final String API_KEY = "apikey";
    private static final String PAGE = "page";

    private static final String RESPONSE = "Response";
    private static final String TRUE = "True";
    private static final String FALSE = "False";
    private static final String SEARCH = "Search";
    private static final String TITLE = "Title";
    private static final String YEAR = "Year";
    private static final String POSTER = "Poster";
    private static final String TOTAL_RESULTS = "totalResults";

    public static final String MOVIE_LIST = "movie_list";

    public static final int NONE = Integer.MIN_VALUE;
    public static int sTotalResults = NONE;


    private static final String ID = "imdbID";
    private static final String IMAGE = "image";

    private boolean mMovieListRequestCancelled = false;
    private Thread mMovieListThread;
    private WeakReference<MovieListCallback> mCallback;

    public interface MovieListCallback {
        void onGotMovieList(List<Movie> movieList);
        void onMovieListFailure();
    }

    public void getMovieList(final String apiKey, final String searchString, final int page, MovieListCallback listener) {
        mCallback = new WeakReference<>(listener);
        mMovieListRequestCancelled = false;
        mMovieListThread = new Thread() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    StringBuilder sb = new StringBuilder();
                    sb.append(MOVIE_URL)
                            .append("?")
                            .append(S + "=" + searchString)
                            .append("&" + API_KEY + "=" + apiKey)
                            .append("&" + PAGE + "=" + page);

                    final Request request = new Request.Builder()
                            .url(sb.toString())
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            onMovieListFailure();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String responseBody = response.body().string();
                                Log.d(TAG, "response: " + responseBody);
                                try {
                                    JSONObject movieJsonObject = new JSONObject(responseBody);
                                    if (movieJsonObject != null) {
                                        String totalResultsString = movieJsonObject.getString(TOTAL_RESULTS);
                                        sTotalResults = Integer.valueOf(totalResultsString);

                                        List<Movie> movieList = getMovieListFromJsonObject(movieJsonObject);

                                        //set up the favorites
                                        for (Movie movie : movieList) {
                                            boolean isFavorite = FavoritesDbManager.sFavoritesIdSet.contains(movie.mId);
                                            if (isFavorite) {
                                                movie.mIsFavorite = true;
                                            }
                                        }
                                        onGotMovieList(movieList);
                                    }
                                } catch (JSONException e) {
                                    onMovieListFailure();
                                }
                            } else {
                                onMovieListFailure();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    onMovieListFailure();
                }
            }
        };
        mMovieListThread.start();
    }

    public List<Movie> getMovieListFromJsonObject(JSONObject movieJsonObject) {
        List<Movie> movieList = new ArrayList<>();
        try {
            JSONArray movieJsonArray = movieJsonObject.getJSONArray(SEARCH);
            for (int i = 0; i < movieJsonArray.length(); i++) {
                JSONObject movieObj = movieJsonArray.getJSONObject(i);
                String title = movieObj.getString(TITLE);
                String id = movieObj.getString(ID);
                String posterUrl = movieObj.getString(POSTER);
                Movie movie = new Movie(title, id, posterUrl);
                movieList.add(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieList;
    }

    /*
    public List<Movie> getMovieListFromJson(JSONArray movieJsonArray) {
        List<Movie> movieList = new ArrayList<>();
        try {
            for (int i = 0; i < movieJsonArray.length(); i++) {
                JSONObject movieObj = movieJsonArray.getJSONObject(i);
                String title = movieObj.getString(TITLE);
                String id = movieObj.getString(ID);
                String image = movieObj.getString(IMAGE);
                Movie movie = new Movie(title, id, image);
                movieList.add(movie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return movieList;
    }
    */

    public void unregisterMovieListCallback() {
        mMovieListRequestCancelled = true;
        if (mMovieListThread != null && mMovieListThread.isAlive()) {
            mMovieListThread.interrupt();
        }
    }

    private void onGotMovieList(final List<Movie> movieList) {
        //Put the callback on the UI thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                MovieListCallback callback = mCallback.get();
                if (callback != null && !mMovieListRequestCancelled) {
                    callback.onGotMovieList(movieList);
                }
            }
        });
    }

    private void onMovieListFailure() {
        //Put the callback on the UI thread
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                MovieListCallback callback = mCallback.get();
                if (callback != null && !mMovieListRequestCancelled) {
                    callback.onMovieListFailure();
                }
            }
        });
    }
}
