package com.example.pix.chat.fragments;

import android.app.Activity;
import android.app.ActivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.activities.FriendActivity;
import com.example.pix.chat.models.MusicRoom;
import com.example.pix.chat.models.Song;
import com.example.pix.home.activities.HomeActivity;
import com.example.pix.home.models.Like;
import com.example.pix.login.LoginActivity;
import com.jackandphantom.androidlikebutton.AndroidLikeButton;
import com.parse.DeleteCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.skyfishjy.library.RippleBackground;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;

import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.ACTIVITY_SERVICE;


public class MusicRoomFragment extends Fragment {

    private ParseUser ownerOfRoom;
    // Make the listenerTimer static because we can only be listening to ONE stream, regardless of how many FriendFragments
    // we open.
    public static Timer listenerTimer;
    private ImageView ivPlay;
    public static Subscription<PlayerState> subscription;
    // We don't need to worry about making the remote static and limiting that only one playerApi can be subscribed to,
    // because the only person that can subscribe to a playerapi is the owner, who can only do it in their
    // profile screen.
    private SpotifyAppRemote remote;
    private ParseQuery<MusicRoom> q;
    public static boolean isOwner;
    private RippleBackground rippleBackground;
    private MusicRoom musicRoom;
    private boolean isPlayingLocally;
    private Song nowPlayingInParse;
    private boolean isPlayingInParse;
    public static final String NOW_PLAYING = "nowPlaying";
    private Like likesThisStreamer;

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
            musicRoom = q.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error getting Current Song!", Toast.LENGTH_SHORT).show();
        }
        /*  If this ownerOfRoom doesn't yet have a MusicRoom, create one.
            As of now, I am creating a new one even if this User isn't the owner of the room.
            I can even make it so that in the future
            a User need to pay to have their own MusicRoom. */
        try {
            if (musicRoom == null) {
                musicRoom = new MusicRoom();
                musicRoom.setUser(ownerOfRoom);
                musicRoom.save();
            }
            // Check to see if this User is the owner of the MusicRoom
            isOwner = musicRoom.getUser().fetch().getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating MusicRoom", Toast.LENGTH_SHORT).show();
        }

        AndroidLikeButton albLike = view.findViewById(R.id.like);
        likesThisStreamer = Like.checkIfLikes(ParseUser.getCurrentUser(), musicRoom.getUser());

        if (likesThisStreamer == null) {
            albLike.setCurrentlyLiked(false);
        } else {
            albLike.setCurrentlyLiked(true);
        }

        if (isOwner) {
            albLike.setVisibility(View.GONE);
        } else {
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

        // Retrieve the button we will click to start streaming and stop (play)
        ivPlay = view.findViewById(R.id.musicroom_play);
        rippleBackground = view.findViewById(R.id.ripple);

        // Try to get the current Song
        nowPlayingInParse = musicRoom.getCurrentSong();

        // Setup the views based on Parse
        setup();
    }

    private void setup() {
        isPlayingLocally = false;

        // Setup proper image
        if (isOwner && subscription != null || !isOwner && listenerTimer != null) {
            isPlayingLocally = true;
            ivPlay.setImageResource(R.drawable.musicroom_pause);
            rippleBackground.startRippleAnimation();
        } else {
            ivPlay.setImageResource(R.drawable.musicroom_play);
        }
        ivPlay.setOnClickListener(view1 -> {
            // If we have no Song, make one
            if (nowPlayingInParse == null) seekSong();
            else startSync();
        });
    }

    // Method where this MusicRoom has no Song, and we therefore need to create one
    private void seekSong() {
        // If this User is the owner of the room, he can create on himself and move on
        if (isOwner) {
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
            // If the user is a listener and there is no Song, they must wait until the MusicRoom has a Song
        } else {
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
    }

    // This method is called whenever the PLAY/PAUSE button is clicked (It starts/stops the stream)
    private void startSync() {
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

    private void startStream() {
        // Start the disk spinning to show that we are streaming/ listening
        getActivity().runOnUiThread(() -> ivPlay.setImageResource(R.drawable.musicroom_pause));
        rippleBackground.startRippleAnimation();
        try {
            musicRoom = q.getFirst();
            // Fetch the song that's playing at the moment
            nowPlayingInParse = musicRoom.getCurrentSong().fetch();

            isPlayingInParse = nowPlayingInParse.isPlaying();
            // Case where the User is the owner
            if (isOwner) {
                // Check if the song is playing
                subscription = remote.getPlayerApi().subscribeToPlayerState();
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
                // Case where User is a listener
            } else {
                // Check if the song is currently playing
                remote.getPlayerApi().play(nowPlayingInParse.getURI());
                if (!isPlayingInParse)
                    remote.getPlayerApi().pause();
                // Every fixed interval, check for updates to Parse
                listenerTimer = new Timer();
                // I need to save this Activity, because I won't be able to call getActivity() if we
                // change from FriendActivity back to HomeActivity, and our listenerTimer would throw an error
                Activity musicRoomActivity = getActivity();
                listenerTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            // Update our local Parse objects
                            musicRoom = q.getFirst();
                            Song checkForChange = musicRoom.getCurrentSong();
                            if (checkForChange == null) {
                                musicRoomActivity.runOnUiThread(() -> {
                                    stopStream();
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
                            if (nowPlayingInParse.getURI() == null || !checkForChange.getURI().equals(nowPlayingInParse.getURI())) {
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
                // If the song this User played is not the same as the one in Parse, they must have manually changed it
                // in which case cancel our controls
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void stopStream() {
        // Stop the spinning disk since we will no longer be playing/streaming a stream
        rippleBackground.stopRippleAnimation();

        // If we were listening to a stream via a listenerTimer, stop it
        if (listenerTimer != null) {
            listenerTimer.cancel();
            listenerTimer = null;
        }
        // If we were streaming as the owner, stop
        if (isOwner) {
            subscription.cancel();
            subscription = null;
            try {
                // Delete the Song that was playing
                nowPlayingInParse.delete();
                // Delete the reference to the Song that was playing from the MusicRoom
                musicRoom.remove(NOW_PLAYING);
                musicRoom.save();
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error deleting Sync Connection", Toast.LENGTH_SHORT).show();
            }
        }
        // THIS ALERTS THIS METHOD THAT WE HAVE DELETED THE SONG, AKA WE NEED TO SOMEHOW FIND A NEW ONE
        // We need this alert to the method because this method is run every time we start/stop the stream,
        // and we thus need to make sure the Song we use in this method is valid.
        nowPlayingInParse = null;

        ivPlay.setImageResource(R.drawable.musicroom_play);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopStream();
    }
}
