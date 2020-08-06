package com.example.pix.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
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

    private static final String REDIRECT_URI = "yourcustomprotocol://callback";
    public static boolean MUSIC_FEATURE_ENABLED = true;
    private boolean loggedIn = false;
    protected boolean authenticated = false;
    private static SpotifyAppRemote mSpotifyAppRemote;

    // We can access the Spotify Remote in later Activities
    public static SpotifyAppRemote getmSpotifyAppRemote() {
        return mSpotifyAppRemote;
    }

    // When one of (spotify, parse) is done, check if both are done
    protected void checkIfDone() {
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
        (findViewById(R.id.parse_login)).setOnClickListener(unusedView -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            // Fields cannot be blank
            if (username.length() == 0 || password.length() == 0) {
                Toast.makeText(this, "No field can be blank!", Toast.LENGTH_SHORT).show();
                return;
            }

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
        (findViewById(R.id.parse_signup)).setOnClickListener(unusedView -> {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            // Fields cannot be blank
            if (username.length() == 0 || password.length() == 0) {
                Toast.makeText(this, "No field can be blank!", Toast.LENGTH_SHORT).show();
                return;
            }

            ParseUser newUser = new ParseUser();
            newUser.setUsername(username);
            newUser.setPassword(password);
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
        (findViewById(R.id.auth_spotify)).setOnClickListener(unusedView -> {

            // If the USer doesn't have Spotify installed, let the USer decide to install it or disable the music feature
            if (!SpotifyAppRemote.isSpotifyInstalled(this)) {
                PlayStoreDialogFragment dialog = new PlayStoreDialogFragment(this, false);
                dialog.show(getSupportFragmentManager(), null);
                return;
            }

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
                            (findViewById(R.id.auth_spotify)).setVisibility(View.GONE);
                            authenticated = true;
                            checkIfDone();
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                            // If we failed to connect to Spotify, chances are the User hasn't signed in yet
                            Toast.makeText(LoginActivity.this, "Sign in with Spotify", Toast.LENGTH_SHORT).show();

                            // If we are not logged in to Spotify, give User the option to login or disable feature
                            PlayStoreDialogFragment dialogFragment = new PlayStoreDialogFragment(LoginActivity.this, true);
                            dialogFragment.show(getSupportFragmentManager(), null);
                        }
                    });
        });
    }

}