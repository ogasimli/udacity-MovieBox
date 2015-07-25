package org.ogasimli.MovieBox.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import org.ogasimli.MovieBox.MainActivity;
import org.ogasimli.MovieBox.objects.TrailerList;

import java.util.ArrayList;

/**
 * Dialog fragment to show the list of trailers
 * Created by ogasimli on 24.07.2015.
 */
public class TrailerDialogFragment extends android.support.v4.app.DialogFragment {

    private ArrayList<TrailerList.Trailer> mTrailerList;

    public static TrailerDialogFragment getInstance(ArrayList<TrailerList.Trailer> trailers) {
        TrailerDialogFragment dialog = new TrailerDialogFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(MainActivity.PACKAGE_NAME, trailers);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTrailerList = getArguments().getParcelableArrayList(MainActivity.PACKAGE_NAME);
        if (mTrailerList == null) {
            throw new NullPointerException("Trailer object should be put into dialog arguments.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] trailerNames = new String[mTrailerList.size()];
        for (int i = 0; i < mTrailerList.size(); i++) {
            TrailerList.Trailer mTrailer = mTrailerList.get(i);
            trailerNames[i] = mTrailer.name + " (" + mTrailer.size + "p)";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick a trailer")
                .setItems(trailerNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        openTrailer(mTrailerList.get(which).getYoutubeLink());
                    }
                });
        return builder.create();
    }

    private void openTrailer(String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}