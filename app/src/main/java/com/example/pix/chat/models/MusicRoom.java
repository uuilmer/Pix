package com.example.pix.chat.models;

import com.example.pix.login.LoginActivity;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.util.List;

@ParseClassName("MusicRoom")
public class MusicRoom extends ParseObject {

    public ParseUser getUser() {
        return getParseUser("user");
    }

    public void setUser(ParseUser user) {
        put("user", user);
    }

    public int getPix() {
        return getInt("pix");
    }

    public void setPix(int pix) {
        put("pix", pix);
    }

    public Song getCurrentSong() {
        return (Song) getParseObject("nowPlaying");
    }

    public void setCurrentSong(Song song) {
        put("nowPlaying", song);
    }
}
