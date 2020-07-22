package com.example.pix.chat.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.models.MusicRoom;
import com.example.pix.chat.models.Song;
import com.example.pix.login.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.Timer;
import java.util.TimerTask;


public class MusicRoomFragment extends Fragment {

    ParseUser ownerOfRoom;
    public static Timer listenerTimer;
    ImageView ivPlay;
    SpotifyAppRemote remote;
    ParseQuery<MusicRoom> q;
    public static boolean isOwner;
    ImageView playingGif;
    final MusicRoom[] musicRoom = new MusicRoom[1];

    public MusicRoomFragment(ParseUser ownerOfRoom) {
        this.ownerOfRoom = ownerOfRoom;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_musicroom, container, false);
    }

    // We will be entering this Fragment with access to the above variable, ownerOfRoom.
    // We need to determine if this user if the ownerOfRoom or listener of this Musicroom
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        remote = LoginActivity.getmSpotifyAppRemote();

        // Get this Musicroom from the ownerOfRoom
        q = ParseQuery.getQuery(MusicRoom.class);
        q.whereEqualTo("user", ownerOfRoom);

        try {
            musicRoom[0] = q.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error getting Current Song!", Toast.LENGTH_SHORT).show();
        }
        /*  If this ownerOfRoom doesn't yet have a MusicRoom, create one.
            As of now, I am creating a new one even if this User isn't the owner of the room.
            I can even make it so that in the future
            a User need to pay to have their own MusicRoom. */
        try {
            if (musicRoom[0] == null) {
                musicRoom[0] = new MusicRoom();
                musicRoom[0].setUser(ownerOfRoom);
                musicRoom[0].save();
            }
            // Check to see if this User is the owner of the MusicRoom
            isOwner = musicRoom[0].getUser().fetch().getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating MusicRoom", Toast.LENGTH_SHORT).show();
        }

        // Retrieve the button we will click to start streaming and stop (play)
        ivPlay = view.findViewById(R.id.musicroom_play);
        playingGif = view.findViewById(R.id.musicroom_playing);
        Glide.with(getContext()).load(Uri.parse("android.resource://com.example.pix/" + R.drawable.musicroom_playing)).into(playingGif);
        playingGif.setVisibility(View.GONE);

        // Try to get the current Song
        nowPlayingInParse[0] = musicRoom[0].getCurrentSong();

        // Case where the current user needs to create a Song for this MusicRoom(None playing)
        if (nowPlayingInParse[0] == null) {
            seekSong();
        }
        // Case where there is already a Song playing (This is mainly for listeners)
        else
            setup(musicRoom);
    }

    // Method where this MusicRoom has no Song, and we therefore need to create one
    private void seekSong() {
        // If this User is the owner of the room, he can create on himself and move on
        if (isOwner) {
            nowPlayingInParse[0] = new Song();
            nowPlayingInParse[0].setPlaying(false);
            nowPlayingInParse[0].saveInBackground(e -> {
                if (e != null) {
                    Toast.makeText(getContext(), "Error starting song!", Toast.LENGTH_SHORT).show();
                    return;
                }
                // The MusicRoom needs to point at this new Song
                musicRoom[0].setCurrentSong(nowPlayingInParse[0]);
                musicRoom[0].saveInBackground(e1 -> {
                    if (e1 != null) {
                        Toast.makeText(getContext(), "Error updating MusicRoom!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    setup(musicRoom);
                });
            });
            // If the user is a listener and there is no Song, they must wait until the MusicRoom has a Song
        } else
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        musicRoom[0] = q.getFirst();
                        nowPlayingInParse[0] = musicRoom[0].getCurrentSong();
                        if (nowPlayingInParse[0] != null) {
                            setup(musicRoom);
                            this.cancel();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error looking for new Song!", Toast.LENGTH_SHORT).show();
                        this.cancel();
                    }
                }
            }, 0, 50);
    }

    final boolean[] isPlayingLocally = new boolean[1];

    private void setup(final MusicRoom[] musicRoom) {
        try {
            nowPlayingInParse[0].fetchIfNeeded();

            isPlayingLocally[0] = false;

            // Setup proper image
            if (listenerTimer != null) {
                isPlayingLocally[0] = true;
                ivPlay.setImageResource(R.drawable.musicroom_pause);
                playingGif.setVisibility(View.VISIBLE);
            } else
                ivPlay.setImageResource(R.drawable.musicroom_play);

            ivPlay.setOnClickListener(view1 -> startSync(musicRoom));
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Cannot set up Stream!", Toast.LENGTH_SHORT).show();
        }
    }

    final Song[] nowPlayingInParse = new Song[1];

    private void startSync(final MusicRoom[] musicRoom) {
        // If the nowPlayingInParse Song in Parse for this db is null, we must have stopped before
        // (See ending ALL CAPS of this method)
        if (nowPlayingInParse[0] == null) {
            Toast.makeText(getContext(), "Tap again to start Stream!", Toast.LENGTH_SHORT).show();
            // Go back to somehow get a valid Song
            seekSong();
            return;
        }
        // If the Song was not playing, begin either a stream or begin listening to the stream
        if (!isPlayingLocally[0]) {
            ivPlay.setImageResource(R.drawable.musicroom_pause);
            playingGif.setVisibility(View.VISIBLE);
            try {
                musicRoom[0] = q.getFirst();
                // Fetch the song that's playing at the moment
                nowPlayingInParse[0] = musicRoom[0].getCurrentSong().fetch();

                // Case where the User is the owner
                final boolean[] isPlayingInParse = {nowPlayingInParse[0].isPlaying()};
                if (isOwner) {
                    // Check if the song is playing

                    remote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                        // Check if the Song changed, in which case we update it in Parse
                        if (!playerState.track.uri.equals(nowPlayingInParse[0].getURI())) {
                            nowPlayingInParse[0].setURI(playerState.track.uri);
                            nowPlayingInParse[0].saveInBackground(e -> {
                                if (e != null)
                                    Toast.makeText(getContext(), "Error updating song", Toast.LENGTH_SHORT).show();
                            });
                        }
                        // Check if the playing/paused status changed, if so update it
                        if (playerState.isPaused == isPlayingInParse[0]) {
                            isPlayingInParse[0] = !playerState.isPaused;
                            nowPlayingInParse[0].setPlaying(isPlayingInParse[0]);
                            nowPlayingInParse[0].saveInBackground(e -> {
                                if (e != null)
                                    Toast.makeText(getContext(), "Error updating song", Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
                    // Case where User is a listener
                } else {
                    // Check if the song is currently playing
                    remote.getPlayerApi().play(nowPlayingInParse[0].getURI());
                    if (!isPlayingInParse[0])
                        remote.getPlayerApi().pause();
                    // Every fixed interval, check for updates to Parse
                    listenerTimer = new Timer();
                    listenerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                // Update our local Parse objects
                                musicRoom[0] = q.getFirst();
                                Song checkForChange = musicRoom[0].getCurrentSong().fetch();
                                // If the song changes, play this new song
                                if (!checkForChange.getURI().equals(nowPlayingInParse[0].getURI())) {
                                    nowPlayingInParse[0] = checkForChange;
                                    remote.getPlayerApi().play(checkForChange.getURI());
                                }
                                // If the song's play/pause status changes, update it with the remote
                                if (checkForChange.isPlaying() != isPlayingInParse[0]) {
                                    isPlayingInParse[0] = checkForChange.isPlaying();
                                    if (isPlayingInParse[0])
                                        remote.getPlayerApi().resume();
                                    else
                                        remote.getPlayerApi().pause();
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }, 0, 50);
                    // If the song this User played is not the same as the one in Parse, they must have manually changed it
                    // in which case cancel our controls
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            // If the Song was playing, stop the stream or stop listening to the stream
        } else {
            Toast.makeText(getContext(), "one", Toast.LENGTH_SHORT).show();
            playingGif.setVisibility(View.GONE);
            if (listenerTimer != null) {
                Toast.makeText(getContext(), "two", Toast.LENGTH_SHORT).show();
                listenerTimer.cancel();
            }
            if (isOwner) {
                remote.getPlayerApi().subscribeToPlayerState().cancel();
                try {
                    // Delete the Song that was playing
                    nowPlayingInParse[0].delete();
                    // Delete the reference to the Song that was playing from the MusicRoom
                    musicRoom[0].remove("nowPlayingInParse");
                    musicRoom[0].save();
                    // THIS ALERTS THIS METHOD THAT WE HAVE DELETED THE SONG, AKA WE NEED TO SOMEHOW FIND A NEW ONE
                    // We need this alert to the method because this method is run every time we start/stop the stream,
                    // and we thus need to make sure the Song we use in this method is valid.
                    nowPlayingInParse[0] = null;
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error deleting Sync Connection", Toast.LENGTH_SHORT).show();
                }
            }
            ivPlay.setImageResource(R.drawable.musicroom_play);
        }
        // Update since we switched
        isPlayingLocally[0] = !isPlayingLocally[0];
    }
}
