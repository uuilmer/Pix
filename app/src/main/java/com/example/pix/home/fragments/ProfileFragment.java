package com.example.pix.home.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.fragments.MusicRoomFragment;
import com.example.pix.chat.utils.FetchPath;
import com.example.pix.login.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

import static android.app.Activity.RESULT_OK;
import static com.example.pix.home.activities.HomeActivity.RESULT_LOAD_IMG;
import static com.example.pix.home.models.Chat.USER_PROFILE_CODE;
import static com.example.pix.home.models.Chat.USER_PIX;

public class ProfileFragment extends Fragment {

    private ImageView profile;
    private ParseUser user;
    private boolean isOwner;

    public ProfileFragment(ParseUser user) {
        this.user = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Check if this ProfileFragment's user is the current user
        isOwner = user.getObjectId().equals(ParseUser.getCurrentUser().getObjectId());

        // Assign a layout based on the above
        if (isOwner) {
            return inflater.inflate(R.layout.fragment_profile, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_friend, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Clicking back ends this fragment
        (view.findViewById(R.id.profile_back)).setOnClickListener(unusedView -> getActivity().onBackPressed());

        profile = view.findViewById(R.id.profile_pic);

        ParseFile profilePic = user.getParseFile(USER_PROFILE_CODE);
        // Load profile pic
        if (profilePic != null) {
            Glide.with(getContext())
                    .load(profilePic.getUrl())
                    .circleCrop()
                    .into(profile);
        }

        // If we have a listenerTimer in MusicRoomFragment, we must make it possible to end it.
        Button stopListening = view.findViewById(R.id.profile_stop);
        if (MusicRoomFragment.listenerTimer != null) {
            stopListening.setVisibility(View.VISIBLE);
            stopListening.setOnClickListener(unusedView -> {
                MusicRoomFragment.listenerTimer.cancel();
                MusicRoomFragment.listenerTimer = null;
                stopListening.setVisibility(View.GONE);
            });
        }

        // We need to differentiate if this ProfileFragment is a friend or the user
        // because we use a different xml layout for each case.
        if (isOwner) {
            // If we press "Enter" in the username EditText, update the username
            EditText name = view.findViewById(R.id.profile_name);
            name.setText("" + user.getUsername());

            (view.findViewById(R.id.profile_signout)).setOnClickListener(unusedView -> {
                ParseUser.logOut();
                Intent i = new Intent(getActivity(), LoginActivity.class);
                getActivity().startActivity(i);
                getActivity().finish();
            });
            LinearLayout selectNewPic = view.findViewById(R.id.profile_change_pic);
            // When we click the plus, go to add a pic
            selectNewPic.setOnClickListener(unusedView -> {
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, RESULT_LOAD_IMG);
            });

            name.setOnKeyListener((unusedView, i, keyEvent) -> {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                    // Create new message
                    ParseUser.getCurrentUser().setUsername(name.getText().toString());
                    ParseUser.getCurrentUser().saveInBackground(e -> {
                        if (e != null) {
                            Toast.makeText(getContext(), "Username is taken", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(getContext(), "Updated name!", Toast.LENGTH_SHORT).show();
                    });
                    return true;
                }
                return false;
            });
        } else {
            // The friend's name is a TextView, so that the user cannot edit it
            TextView name = view.findViewById(R.id.profile_name);
            name.setText("" + user.getUsername());// If we press "Enter" in the username EditText, update the username
        }
        // Set User's number of Pix
        TextView pix = view.findViewById(R.id.profile_pix);
        pix.setText("" + user.getInt(USER_PIX));

        // Insert a MusicRoomFragment(Currently has no layout) to monitor this User's Spotify
        // and update their personal Musicroom accordingly
        getChildFragmentManager().beginTransaction().add(R.id.profile_musicroom, new MusicRoomFragment(user)).commit();
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMG) {
                final Uri imageUri = data.getData();
                // When we return from choosing a picture, save it as a ParseFile THEN update the current ImageView
                ParseFile toSave = new ParseFile(new File(FetchPath.getPath(getContext(), imageUri)));

                ParseUser.getCurrentUser().put(USER_PROFILE_CODE, toSave);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Glide.with(getContext())
                                .load(toSave.getUrl())
                                .circleCrop()
                                .into(profile);
                    }
                });
            }
        }
    }
}