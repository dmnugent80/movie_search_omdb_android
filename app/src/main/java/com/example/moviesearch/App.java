package com.example.moviesearch;

import android.app.Application;
import android.content.Context;

import com.example.moviesearch.controllers.FavoritesDbManager;

public class App extends Application {

    private static final String TAG = App.class.getSimpleName();
    private static Context sAppContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this.getApplicationContext();
        FavoritesDbManager.updateFavorites();
    }

    public static Context getContext() {
        return sAppContext;
    }
}
