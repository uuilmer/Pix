package com.example.pix.home.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.transition.Explode;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.fragments.MusicRoomFragment;
import com.example.pix.chat.utils.FetchPath;
import com.example.pix.home.models.Like;
import com.example.pix.login.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static android.app.Activity.RESULT_OK;
import static com.example.pix.chat.activities.FriendActivity.FRIEND_FRAGMENT_TAG;
import static com.example.pix.home.activities.HomeActivity.HOME_FRAGMENT_TAG;
import static com.example.pix.home.activities.HomeActivity.RESULT_LOAD_IMG;
import static com.example.pix.home.models.Chat.USER_PROFILE_CODE;

public class ProfileFragment extends Fragment {

    private ImageView profile;
    private ParseUser user;
    private boolean isOwner;
    private Timer timer;

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
        if (isOwner)
            return inflater.inflate(R.layout.fragment_profile, container, false);
        else
            return inflater.inflate(R.layout.fragment_friend, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Clicking back ends this fragment
        // Will need to figure out how to end with animation
        setExitTransition(new Explode());
        (view.findViewById(R.id.profile_back)).setOnClickListener(unusedView -> {
            // If the User was looking at a friend's profile, there must have been a ChatFragment with the FRIEND_FRAGMENT_CHAT
            if (!isOwner) {
                Fragment friendFragment = getParentFragmentManager().findFragmentByTag(FRIEND_FRAGMENT_TAG);
                getParentFragmentManager().beginTransaction()
                        .show(friendFragment)
                        .hide(this)
                        .commit();
                return;
            }
            // If it was not a friend's profile, it must have been a User looking at their own profile,
            // in which case there must be a Fragment with the HOME_FRAGMENT_TAG
            Fragment home = getParentFragmentManager().findFragmentByTag(HOME_FRAGMENT_TAG);
            getParentFragmentManager().beginTransaction()
                    .hide(this)
                    .show(home)
                    .commit();
        });

        profile = view.findViewById(R.id.profile_pic);

        // Load profile pic
        Glide.with(getContext())
                .load(user
                        .getParseFile(USER_PROFILE_CODE).getUrl())
                .circleCrop()
                .into(profile);

        // We need to differentiate if this ProfileFragment is a friend or the user
        // because we use a different xml layout for each case.
        if (isOwner) {
            // The stopListening shortcut button is intended for the owner of the room to stop listening
            // to whichever friend the listenerTimer is attached to
            Button stopListening = view.findViewById(R.id.profile_stop);
            if (MusicRoomFragment.listenerTimer != null) {
                stopListening.setVisibility(View.VISIBLE);
                stopListening.setOnClickListener(view14 -> {
                    MusicRoomFragment.listenerTimer.cancel();
                    MusicRoomFragment.listenerTimer = null;
                    stopListening.setVisibility(View.GONE);
                });
            }

            // If we press "Enter" in the username EditText, update the username
            EditText name = view.findViewById(R.id.profile_name);
            name.setText(user.getUsername());

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
            name.setText(user.getUsername());// If we press "Enter" in the username EditText, update the username
        }
        // Set User's number of Pix
        TextView pix = view.findViewById(R.id.profile_pix);
        // Check every 2 seconds for how many "Pix"(Likes) this person has
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> pix.setText(Like.getPix(user) + "P"));
            }
        }, 0, 2000);

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
                // Assign it to the User
                ParseUser.getCurrentUser().put(USER_PROFILE_CODE, toSave);

                // Save the new profile pic in Parse
                toSave.saveInBackground((SaveCallback) e -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Unable to save Pic", Toast.LENGTH_SHORT).show();
                        System.out.println(e.getMessage());
                        return;
                    }
                    // Save the User to keep his assigned image
                    ParseUser.getCurrentUser().saveInBackground(e1 -> {
                        if (e1 != null) {
                            Toast.makeText(getContext(), "Unable to set Profile Pic", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Glide.with(getContext())
                                .load(toSave.getUrl())
                                .circleCrop()
                                .into(profile);
                    });
                });
            }
        }
    }

    // We need to make sure to stop the Timer that updates the number of "Pix"(Likes) when this Fragment is ended
    @Override
    public void onDetach() {
        super.onDetach();
        timer.cancel();
    }
}