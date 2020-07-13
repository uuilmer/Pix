package com.example.pix.login;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pix.BuildConfig;
import com.example.pix.R;
import com.example.pix.home.HomeActivity;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

public class LoginActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1337;
    private static final String REDIRECT_URI = "yourcustomprotocol://callback";
    private boolean loggedIn = false;
    private boolean authenticated = false;

    private void checkIfDone(){
        if(loggedIn && authenticated){
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
        (findViewById(R.id.parse_login)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                ParseUser.logInInBackground(username, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(e != null){
                            Toast.makeText(LoginActivity.this, "Incorrect credentials", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(LoginActivity.this, "Successfully logged in!", Toast.LENGTH_SHORT).show();
                        loggedIn = true;
                        (findViewById(R.id.parse_container)).setVisibility(View.GONE);
                        checkIfDone();
                    }
                });
            }
        });

        // When user clicks register, we set up their account then go to MainActivity
        (findViewById(R.id.parse_signup)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
            }
        });

        // When button is hit, create an AuthenticationRequest and jump to the Spotify-provided LoginActivity
        (findViewById(R.id.auth_spotify)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(BuildConfig.SPOTIFY_KEY,
                        AuthenticationResponse.Type.TOKEN, REDIRECT_URI);
                builder.setScopes(new String[]{"streaming"});
                AuthenticationRequest request = builder.build();

                AuthenticationClient.openLoginActivity(LoginActivity.this, REQUEST_CODE, request);
            }
        });
    }

    // Return here from Spotify's LoginActivity with or without the Auth Token
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println("yup");

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, data);
            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    Toast.makeText(LoginActivity.this, "Successfully Authenticated!", Toast.LENGTH_SHORT).show();
                    authenticated = true;
                    (findViewById(R.id.spotify_auth_container)).setVisibility(View.GONE);
                    checkIfDone();
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Toast.makeText(LoginActivity.this, response.getError(), Toast.LENGTH_SHORT).show();
                    break;

                // Most likely auth flow was cancelled
                default:
                    System.out.println("janet");
                    // Handle other cases
            }
        }
    }
}