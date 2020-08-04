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
        (view.findViewById(R.id.profile_back)).setOnClickListener(unusedView -> getActivity().onBackPressed());

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
            // If we have a listenerTimer in MusicRoomFragment, we must make it possible to end it.
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
        pix.setText("" + Like.getPix(user));

        // Check every 2 seconds for how many "Pix"(Likes) this person has
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(() -> pix.setText("" + Like.getPix(user)));
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

    // We need to make sure to stop the Timer that updates the number of "Pix"(Likes) when this Fragment is ended
    @Override
    public void onDetach() {
        super.onDetach();
        timer.cancel();
    }
}