package com.example.pix.spotifyAuth;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.pix.BuildConfig;
import com.example.pix.R;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class SpotifyAuthActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // When button is hit, create an AuthenticationRequest and jump to the Spotify-provided LoginActivity
        (findViewById(R.id.auth_spotify)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(BuildConfig.SPOTIFY_KEY,
                        AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
                builder.setScopes(new String[]{"streaming"});
                AuthenticationRequest request = builder.build();

                AuthenticationClient.openLoginActivity(SpotifyAuthActivity.this, REQUEST_CODE, request);
            }
        });
    }

    // Return here from Spotify's LoginActivity with or without the Auth Token
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("yup");
        Toast.makeText(SpotifyAuthActivity.this, "yea", Toast.LENGTH_SHORT).show();

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    System.out.println(response.getCode() + " " + response.getExpiresIn());
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    System.out.println("Error");
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
    }
}