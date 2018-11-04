package com.example.moviesearch.views;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.moviesearch.App;
import com.example.moviesearch.model.Movie;
import com.example.moviesearch.R;

public class MovieDetailFragment extends DialogFragment {
    private TextView mTitleTv, mYearTv;
    private ImageView mPosterImage;
    private static final String MOVIE = "movie";
    private Movie mMovie;

    public static MovieDetailFragment newInstance(Movie movie) {
        MovieDetailFragment fragment = new MovieDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(MOVIE, movie);
        fragment.setArguments(bundle);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Get the Movie
        mMovie = getArguments().getParcelable(MOVIE);

        //Set up the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MovieDialogTheme);
        View view = View.inflate(getActivity(), R.layout.fragment_movie_detail, null);
        builder.setView(view);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        initViews(view);
        return builder.create();
    }

    private void initViews(View rootView) {
        mTitleTv = rootView.findViewById(R.id.movie_title_txt);
        mYearTv = rootView.findViewById(R.id.id_txt);
        mPosterImage = rootView.findViewById(R.id.poster_iv);

        if (mMovie != null) {
            mTitleTv.setText(mMovie.mTitle);
            mYearTv.setText(mMovie.mId);

            Glide.with(App.getContext())
                    .load(mMovie.mPosterUrl)
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.baseline_local_movies_black_48)
                            .fitCenter())
                    .into(mPosterImage);
        }
    }
}
