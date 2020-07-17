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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_musicroom, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SpotifyAppRemote remote = LoginActivity.getmSpotifyAppRemote();

        Toast.makeText(getContext(), "haha", Toast.LENGTH_SHORT).show();
        Chat chat = Chat.getChat(getActivity().getIntent().getStringExtra("chat"));
        ParseQuery<MusicRoom> q = ParseQuery.getQuery(MusicRoom.class);
        q.whereEqualTo("user", chat.getFriend(ParseUser.getCurrentUser()));
        final MusicRoom[] musicRoom = new MusicRoom[1];
        try {
            musicRoom[0] = q.getFirst();
            final Song[] nowPlaying = {musicRoom[0].getCurrentSong().fetch()};

            if (musicRoom[0].getUser().equals(ParseUser.getCurrentUser())) {
                final boolean[] isPlaying = {nowPlaying[0].isPlaying()};
                remote.getPlayerApi().subscribeToPlayerState().setEventCallback(playerState -> {
                    if(!playerState.track.uri.equals(nowPlaying[0].getURI())){
                        nowPlaying[0].setURI(playerState.track.uri);
                        nowPlaying[0].saveInBackground(e -> Toast.makeText(getContext(), "Error updating song", Toast.LENGTH_SHORT).show());
                    }
                    if (playerState.isPaused == isPlaying[0]) {
                        isPlaying[0] = !playerState.isPaused;
                        nowPlaying[0].setPlaying(isPlaying[0]);
                        nowPlaying[0].saveInBackground(e -> Toast.makeText(getContext(), "Error updating song", Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                final boolean[] isPlaying = {nowPlaying[0].isPlaying()};
                remote.getPlayerApi().play(nowPlaying[0].getURI());
                if (!isPlaying[0])
                    remote.getPlayerApi().pause();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            musicRoom[0] = q.getFirst();
                            Song checkForChange = musicRoom[0].getCurrentSong().fetch();
                            if (!checkForChange.getURI().equals(nowPlaying[0].getURI())) {
                                nowPlaying[0] = checkForChange;
                                remote.getPlayerApi().play(checkForChange.getURI());
                            }
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
            // spotify:track:4lMOQR1rRW5hJgJXOtKraL
            // spotify:track:5kbps8unnGH4raN5WduZuq
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}
