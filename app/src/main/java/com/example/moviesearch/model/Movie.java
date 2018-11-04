package com.example.moviesearch.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing an individual Movie
 */

public class Movie implements Parcelable {
    public String mTitle;
    public String mId;
    public String mPosterUrl;
    public boolean mIsFavorite = false;
    public Movie(String title, String id, String posterUrl) {
        mTitle = title;
        mId = id;
        mPosterUrl = posterUrl;
    }

    protected Movie(Parcel in) {
        mTitle = in.readString();
        mId = in.readString();
        mPosterUrl = in.readString();
        mIsFavorite = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mId);
        dest.writeString(mPosterUrl);
        dest.writeByte((byte) (mIsFavorite ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
