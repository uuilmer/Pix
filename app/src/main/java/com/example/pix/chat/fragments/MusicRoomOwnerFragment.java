package com.example.pix.chat.fragments;

import android.app.MediaRouteButton;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.pix.R;
import com.example.pix.chat.models.MusicRoom;
import com.example.pix.chat.models.Song;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

public class MusicRoomOwnerFragment extends MusicRoomBaseFragment {

    public static Subscription<PlayerState> subscription;

    public MusicRoomOwnerFragment(MusicRoom musicRoom, ParseUser ownerOfRoom) {
        super(musicRoom, ownerOfRoom);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        albLike.setVisibility(View.GONE);
    }

    @Override
    protected void setup() {
        super.setup();

        if (subscription != null) {
            isPlayingLocally = true;
            ivPlay.setImageResource(R.drawable.musicroom_pause);
            rippleBackground.startRippleAnimation();
        } else {
            ivPlay.setImageResource(R.drawable.musicroom_play);
        }
    }

    @Override
    protected void seekSong() {
        nowPlayingInParse = new Song();
        nowPlayingInParse.setPlaying(false);
        nowPlayingInParse.saveInBackground(e -> {
            if (e != null) {
                Toast.makeText(getContext(), "Error starting song!", Toast.LENGTH_SHORT).show();
                return;
            }
            // The MusicRoom needs to point at this new Song
            musicRoom.setCurrentSong(nowPlayingInParse);
            musicRoom.saveInBackground(e1 -> {
                if (e1 != null) {
                    Toast.makeText(getContext(), "Error updating MusicRoom!", Toast.LENGTH_SHORT).show();
                    return;
                }
                startSync();
            });
        });
    }

    @Override
    protected void startStream() {
        super.startStream();

        // Check if the song is playing
        subscription = remote.getPlayerApi().subscribeToPlayerState();

        Toast.makeText(getContext(), "You can now play music in the Spotify App!", Toast.LENGTH_SHORT).show();
        // Play music on Spotify in case no Song was playing
        remote.getPlayerApi().resume();
        subscription.setEventCallback(playerState -> {
            // Check if the Song changed, in which case we update it in Parse
            if (!playerState.track.uri.equals(nowPlayingInParse.getURI())) {
                nowPlayingInParse.setURI(playerState.track.uri);
                nowPlayingInParse.saveInBackground(e -> {
                    if (e != null)
                        Toast.makeText(getContext(), "Error updating song", Toast.LENGTH_SHORT).show();
                });
            }
            // Check if the playing/paused status changed, if so update it
            if (playerState.isPaused == isPlayingInParse) {
                isPlayingInParse = !playerState.isPaused;
                nowPlayingInParse.setPlaying(isPlayingInParse);
                nowPlayingInParse.saveInBackground(e -> {
                    if (e != null)
                        Toast.makeText(getContext(), "Error updating song", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    protected void stopStream() {
        super.stopStream();

        // Stop the Song in case in wasn't already paused
        remote.getPlayerApi().pause();
        subscription.cancel();
        subscription = null;
        try {
            // Delete the Song that was playing
            nowPlayingInParse.delete();
            // Delete the reference to the Song that was playing from the MusicRoom
            musicRoom.remove(NOW_PLAYING);
            musicRoom.save();
            Toast.makeText(getContext(), "Streaming Stopped", Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error deleting Sync Connection", Toast.LENGTH_SHORT).show();
        }

        // THIS ALERTS THIS METHOD THAT WE HAVE DELETED THE SONG, AKA WE NEED TO SOMEHOW FIND A NEW ONE
        // We need this alert to the method because this method is run every time we start/stop the stream,
        // and we thus need to make sure the Song we use in this method is valid.
        nowPlayingInParse = null;

        ivPlay.setImageResource(R.drawable.musicroom_play);
    }
}
