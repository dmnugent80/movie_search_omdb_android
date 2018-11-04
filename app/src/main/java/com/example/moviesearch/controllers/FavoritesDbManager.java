package com.example.moviesearch.controllers;

import android.os.Handler;
import android.os.Looper;

import com.example.moviesearch.App;
import com.example.moviesearch.model.FavoritesDbHelper;
import com.example.moviesearch.model.Movie;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller class for the Favorites List, backed by a SQLite DB
 */

public class FavoritesDbManager {

    public static Set<String> sFavoritesIdSet = new HashSet<>();

    private boolean mFavoritesRequestCancelled = false;
    private Thread mFavoritesThread;
    private WeakReference<FavoritesCallback> mCallback;

    public interface FavoritesCallback {
        void onGotFavoritesDbResult(List<Movie> favoritesList);
    }

    public static void updateFavorites() {
        new Thread() {
            @Override
            public void run() {
                sFavoritesIdSet.clear();
                sFavoritesIdSet.addAll(FavoritesDbHelper.getInstance(App.getContext()).getFavoritesSet(App.getContext()));
            }
        }.start();
    }

    public void getFavorites(FavoritesCallback listener) {
        mFavoritesRequestCancelled = false;
        mCallback = new WeakReference<>(listener);
        mFavoritesThread = new Thread() {
            @Override
            public void run() {
                try {
                    //Get the favorites list from the DB
                    final List<Movie> favoriteList = FavoritesDbHelper.getInstance(App.getContext()).getFavoritesList(App.getContext());
                    //Put the callback on the UI thread
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            FavoritesCallback callback = mCallback.get();
                            if (callback != null && !mFavoritesRequestCancelled) {
                                callback.onGotFavoritesDbResult(favoriteList);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mFavoritesThread.start();
    }

    public void unregisterFavoritesCallback() {
        mFavoritesRequestCancelled = true;
        if (mFavoritesThread != null && mFavoritesThread.isAlive()) {
            mFavoritesThread.interrupt();
        }
    }
}
