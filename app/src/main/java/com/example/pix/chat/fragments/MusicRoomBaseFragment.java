package com.example.pix.chat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pix.R;
import com.example.pix.chat.models.MusicRoom;
import com.example.pix.chat.models.Song;
import com.example.pix.login.LoginActivity;
import com.jackandphantom.androidlikebutton.AndroidLikeButton;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.skyfishjy.library.RippleBackground;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.Timer;

public abstract class MusicRoomBaseFragment extends Fragment {
    // Make the listenerTimer static because we can only be listening to ONE stream, regardless of how many FriendFragments
    // we open.
    public static Timer listenerTimer;
    protected ImageView ivPlay;
    // We don't need to worry about making the remote static and limiting that only one playerApi can be subscribed to,
    // because the only person that can subscribe to a playerapi is the owner, who can only do it in their
    // profile screen.
    protected SpotifyAppRemote remote;
    protected RippleBackground rippleBackground;
    protected MusicRoom musicRoom;
    protected boolean isPlayingLocally;
    protected Song nowPlayingInParse;
    protected boolean isPlayingInParse;
    public static final String NOW_PLAYING = "nowPlaying";
    protected AndroidLikeButton albLike;
    protected ParseUser ownerOfRoom;

    public MusicRoomBaseFragment(MusicRoom musicRoom, ParseUser ownerOfRoom) {
        this.musicRoom = musicRoom;
        this.ownerOfRoom = ownerOfRoom;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_musicroom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        remote = LoginActivity.getmSpotifyAppRemote();

        albLike = view.findViewById(R.id.like);

        // Retrieve the button we will click to start streaming and stop (play)
        ivPlay = view.findViewById(R.id.musicroom_play);
        rippleBackground = view.findViewById(R.id.ripple);

        // Try to get the current Song
        nowPlayingInParse = musicRoom.getCurrentSong();

        // Setup the views based on Parse
        setup();

    }

    protected void setup() {
        isPlayingLocally = false;

        ivPlay.setOnClickListener(view1 -> {
            // If we have no Song, make one
            if (nowPlayingInParse == null) {
                seekSong();
            } else {
                startSync();
            }
        });
    }

    protected abstract void seekSong();

    // This method is called whenever the PLAY/PAUSE button is clicked (It starts/stops the stream)
    protected void startSync() {
        try {
            nowPlayingInParse.fetchIfNeeded();

            if (!isPlayingLocally) {
                // If no Song is playing, start playing it(Or streaming it if owner)
                startStream();
            } else {
                // If the Song was playing, stop the stream or stop listening to the stream
                stopStream();
            }
            // Update since we switched whether we are streaming or not
            isPlayingLocally = !isPlayingLocally;
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Error fetching song", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    protected void startStream() {
        // Start the disk spinning to show that we are streaming/ listening
        getActivity().runOnUiThread(() -> ivPlay.setImageResource(R.drawable.musicroom_pause));
        rippleBackground.startRippleAnimation();

        // Fetch the song that's playing at the moment
        try {
            nowPlayingInParse = musicRoom.getCurrentSong().fetch();
            isPlayingInParse = nowPlayingInParse.isPlaying();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    protected void stopStream() {
        // Stop the spinning disk since we will no longer be playing/streaming a stream
        rippleBackground.stopRippleAnimation();

        // If we were listening to a stream via a listenerTimer, stop it
        if (listenerTimer != null) {
            listenerTimer.cancel();
            listenerTimer = null;
            Toast.makeText(getContext(), "Listening Stopped", Toast.LENGTH_SHORT).show();
        }
    }
}
