package com.example.pix.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.home.activities.HomeActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";
    private boolean loggedIn = false;
    private boolean authenticated = false;
    private static SpotifyAppRemote mSpotifyAppRemote;

    // We can access the Spotify Remote in later Activities
    public static SpotifyAppRemote getmSpotifyAppRemote() {
        return mSpotifyAppRemote;
    }

    // When one of (spotify, parse) is done, check if both are done
    private void checkIfDone() {
        if (loggedIn && authenticated) {
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        final EditText etUsername = findViewById(R.id.entered_username);
        final EditText etPassword = findViewById(R.id.entered_password);


        // Attempt to login the User
        (findViewById(R.id.parse_login)).setOnClickListener(view -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            ParseUser.logInInBackground(username, password, new LogInCallback() {
                @Override
                public void done(ParseUser user, ParseException e) {
                    if (e != null) {
                        Toast.makeText(LoginActivity.this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(LoginActivity.this, "Successfully logged in!", Toast.LENGTH_SHORT).show();
                    loggedIn = true;
                    (findViewById(R.id.parse_container)).setVisibility(View.GONE);
                    checkIfDone();
                }
            });
        });

        // When user clicks register, we set up their account then go to MainActivity
        (findViewById(R.id.parse_signup)).setOnClickListener(view -> {
            ParseUser newUser = new ParseUser();
            newUser.setUsername(etUsername.getText().toString());
            newUser.setPassword(etPassword.getText().toString());
            try {
                newUser.signUp();
                Toast.makeText(LoginActivity.this, "Successfully signed up!", Toast.LENGTH_SHORT).show();
                loggedIn = true;
                (findViewById(R.id.parse_container)).setVisibility(View.GONE);
                checkIfDone();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        // When button is hit, create an AuthenticationRequest and jump to the Spotify-provided LoginActivity
        (findViewById(R.id.auth_spotify)).setOnClickListener(view -> {

            // Set the connection parameters
            ConnectionParams connectionParams =
                    new ConnectionParams.Builder("cf5e6393a07f442ab4f22d05650071ec") // Client ID
                            .setRedirectUri(REDIRECT_URI)
                            .showAuthView(true)
                            .build();

            SpotifyAppRemote.connect(LoginActivity.this, connectionParams,
                    new Connector.ConnectionListener() {

                        @Override
                        public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                            mSpotifyAppRemote = spotifyAppRemote;

                            // We are done Authenticating Spotify
                            (findViewById(R.id.spotify_auth_container)).setVisibility(View.GONE);
                            checkIfDone();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            Log.e("MainActivity", throwable.getMessage(), throwable);
                        }
                    });
            authenticated = true;
        });
    }

}