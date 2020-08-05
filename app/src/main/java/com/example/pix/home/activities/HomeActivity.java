package com.example.pix.home.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.pix.R;
import com.example.pix.home.fragments.HomeFragment;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class HomeActivity extends AppCompatActivity {

    public static final int RESULT_LOAD_IMG = 100;
    public static HomeFragment homeFragment;
    public static final String HOME_FRAGMENT_TAG = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeFragment = new HomeFragment();

        // The home activity first contains the HomeFragment, which can potentially replace itself with a ProfileFragment
        getSupportFragmentManager().beginTransaction().replace(R.id.home_profile, homeFragment, HOME_FRAGMENT_TAG).commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}