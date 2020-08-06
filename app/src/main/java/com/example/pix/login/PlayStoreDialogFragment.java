package com.example.pix.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.DialogFragment;

import com.example.pix.R;

public class PlayStoreDialogFragment extends DialogFragment {

    private LoginActivity loginActivity;
    private boolean isSpotifyInstalled;

    public PlayStoreDialogFragment(LoginActivity loginActivity, boolean isSpotifyInstalled) {
        this.loginActivity = loginActivity;
        this.isSpotifyInstalled = isSpotifyInstalled;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Show the correct message
        builder.setMessage("You need to install & login to Spotify for the Music feature.")
                .setPositiveButton(isSpotifyInstalled ? "Login" : "Install it", (dialog, id) -> {
                    // If we haven installed Spotify, launch Spotify to login
                    if (isSpotifyInstalled) {
                        Intent launchIntent = loginActivity.getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                        if (launchIntent != null) {
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                        return;
                    }
                    // If we haven't, launch the Play Store
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(
                            "https://play.google.com/store/apps/details?id=com.spotify.music"));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                })
                .setNegativeButton("Disable it", (dialog, id) -> {
                    // If we choose to disable the Music feature, allow us to move on from the LoginActivity but mark
                    // the MUSIC_FEATURE_ENABLED as false
                    loginActivity.authenticated = true;
                    (loginActivity.findViewById(R.id.auth_spotify)).setVisibility(View.GONE);

                    loginActivity.MUSIC_FEATURE_ENABLED = false;
                    loginActivity.checkIfDone();
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
