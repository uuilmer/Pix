package com.example.pix.chat.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.pix.R;
import com.example.pix.chat.models.MusicRoom;
import com.example.pix.chat.models.Song;
import com.example.pix.home.models.Chat;
import com.example.pix.login.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

import java.util.Timer;
import java.util.TimerTask;


public class MusicRoomFragment extends Fragment {

    ParseUser user;

    public MusicRoomFragment(ParseUser user) {
        this.user = user;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_musicroom, container, false);
    }

    // We will be entering this Fragment with access to the above variable, user.
    // We need to determine if this user if the owner or listener of this Musicroom
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SpotifyAppRemote remote = LoginActivity.getmSpotifyAppRemote();

        // Get this Musicroom from the User
        ParseQuery<MusicRoom> q = ParseQuery.getQuery(MusicRoom.class);
        q.whereEqualTo("user", user);
        final MusicRoom[] musicRoom = new MusicRoom[1];

        try {
            musicRoom[0] = q.getFirst();
            // Fetch the song that's playing at the moment
            final Song[] nowPlaying = {musicRoom[0].getCurrentSong().fetch()};

            // Case where the User is the owner
            if (musicRoom[0].getUser().fetch().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                // Check if the song is playing
                final boolean[] isPlaying = {nowPlaying[0].isPlaying()};

                remote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                    // Check if the Song changed, in which case we update it in Parse
                    if (!playerState.track.uri.equals(nowPlaying[0].getURI())) {
                        nowPlaying[0].setURI(playerState.track.uri);
                        nowPlaying[0].saveInBackground(e -> {
                            if (e != null)
                                Toast.makeText(getContext(), "Error updating song", Toast.LENGTH_SHORT).show();
                        });
                    }
                    // Check if the playing/paused status changed, if so update it
                    if (playerState.isPaused == isPlaying[0]) {
                        isPlaying[0] = !playerState.isPaused;
                        nowPlaying[0].setPlaying(isPlaying[0]);
                        nowPlaying[0].saveInBackground(e -> {
                            if (e != null)
                                Toast.makeText(getContext(), "Error updating song", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
                // Case where User is a listener
            } else {
                // Check if the song is currently playing
                final boolean[] isPlaying = {nowPlaying[0].isPlaying()};
                remote.getPlayerApi().play(nowPlaying[0].getURI());
                if (!isPlaying[0])
                    remote.getPlayerApi().pause();
                // Every fixed interval, check for updates to Parse
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            // Update our local Parse objects
                            musicRoom[0] = q.getFirst();
                            Song checkForChange = musicRoom[0].getCurrentSong().fetch();
                            // If the song changes, play this new song
                            if (!checkForChange.getURI().equals(nowPlaying[0].getURI())) {
                                nowPlaying[0] = checkForChange;
                                remote.getPlayerApi().play(checkForChange.getURI());
                            }
                            // If the song's play/pause status changes, update it with the remote
                            if (checkForChange.isPlaying() != isPlaying[0]) {
                                isPlaying[0] = checkForChange.isPlaying();
                                if (isPlaying[0])
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
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
