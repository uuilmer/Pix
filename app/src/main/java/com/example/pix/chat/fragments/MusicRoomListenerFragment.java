package com.example.pix.chat.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pix.R;
import com.example.pix.chat.models.MusicRoom;
import com.example.pix.chat.models.Song;
import com.example.pix.home.models.Like;
import com.jackandphantom.androidlikebutton.AndroidLikeButton;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Timer;
import java.util.TimerTask;

public class MusicRoomListenerFragment extends MusicRoomBaseFragment {

    private Like likesThisStreamer;
    private ParseQuery<MusicRoom> q;

    public MusicRoomListenerFragment(MusicRoom musicRoom, ParseUser ownerOfRoom, ParseQuery<MusicRoom> q) {
        super(musicRoom, ownerOfRoom);
        this.q = q;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        likesThisStreamer = Like.checkIfLikes(ParseUser.getCurrentUser(), musicRoom.getUser());

        if (likesThisStreamer == null) {
            albLike.setCurrentlyLiked(false);
        } else {
            albLike.setCurrentlyLiked(true);
        }
        albLike.setOnLikeEventListener(new AndroidLikeButton.OnLikeEventListener() {
            @Override
            public void onLikeClicked(AndroidLikeButton androidLikeButton) {
                likesThisStreamer = new Like();
                likesThisStreamer.setListener(ParseUser.getCurrentUser());
                likesThisStreamer.setStreamer(ownerOfRoom);
                likesThisStreamer.saveInBackground(e -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error liking", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    albLike.setCurrentlyLiked(true);
                });
            }

            @Override
            public void onUnlikeClicked(AndroidLikeButton androidLikeButton) {
                // When we click unlike, delete the current Like
                likesThisStreamer.deleteInBackground(e -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Error liking", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    albLike.setCurrentlyLiked(false);

                    likesThisStreamer = null;
                });
            }
        });
    }

    @Override
    protected void setup() {
        super.setup();

        if (listenerTimer != null) {
            isPlayingLocally = true;
            ivPlay.setImageResource(R.drawable.musicroom_pause);
            rippleBackground.startRippleAnimation();
        } else {
            ivPlay.setImageResource(R.drawable.musicroom_play);
        }
    }

    @Override
    protected void seekSong() {
        try {
            musicRoom = q.getFirst();
            nowPlayingInParse = musicRoom.getCurrentSong();
            if (nowPlayingInParse != null) {
                startSync();
            } else {
                Toast.makeText(getContext(), ownerOfRoom.getUsername() + " is not Streaming", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error getting Current Song!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void startStream() {
        super.startStream();

        // Check if the song is currently playing
        remote.getPlayerApi().play(nowPlayingInParse.getURI());
        if (!isPlayingInParse)
            remote.getPlayerApi().pause();
        // Every fixed interval, check for updates to Parse
        listenerTimer = new Timer();
        // Keep updating the current song for this Listener
        listenerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    // Update our local Parse objects
                    musicRoom = q.getFirst();
                    Song checkForChange = musicRoom.getCurrentSong();
                    if (checkForChange == null) {
                        // We must run stopStream in the UiThread because it makes changes to views(Change the playing ImageView
                        // to not playing) that were defined in the main thread, and thus can only be changed there
                        getActivity().runOnUiThread(() -> {
                            stopStream();
                            this.cancel();
                        });
                        return;
                    }
                    checkForChange.fetch();
                    if (checkForChange.getURI() == null) {
                        nowPlayingInParse = checkForChange;
                        remote.getPlayerApi().pause();
                        return;
                    }
                    // If the song changes, play this new song
                    if (nowPlayingInParse == null || !checkForChange.getURI().equals(nowPlayingInParse.getURI())) {
                        nowPlayingInParse = checkForChange;
                        remote.getPlayerApi().play(checkForChange.getURI());
                    }
                    // If the song's play/pause status changes, update it with the remote
                    if (checkForChange.isPlaying() != isPlayingInParse) {
                        isPlayingInParse = checkForChange.isPlaying();
                        if (isPlayingInParse)
                            remote.getPlayerApi().resume();
                        else
                            remote.getPlayerApi().pause();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 50);
    }

    @Override
    protected void stopStream() {
        super.stopStream();

        // THIS ALERTS THIS METHOD THAT WE HAVE DELETED THE SONG, AKA WE NEED TO SOMEHOW FIND A NEW ONE
        // We need this alert to the method because this method is run every time we start/stop the stream,
        // and we thus need to make sure the Song we use in this method is valid.
        nowPlayingInParse = null;

        ivPlay.setImageResource(R.drawable.musicroom_play);
    }
}
