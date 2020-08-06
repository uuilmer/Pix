package com.example.pix.home.models;

import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("Like")
public class Like extends ParseObject {

    public static final String STREAMER_CODE = "streamer";
    public static final String LISTENER_CODE = "listener";

    public ParseUser getListener() {
        return getParseUser(LISTENER_CODE);
    }

    public static Like checkIfLikes(ParseUser listener, ParseUser streamer){
        ParseQuery<Like> q = ParseQuery.getQuery(Like.class);
        q.whereEqualTo(LISTENER_CODE, listener);
        q.whereEqualTo(STREAMER_CODE, streamer);
        try {
            return q.getFirst();
        } catch (ParseException e) {
            return null;
        }
    }

    public static Integer getPix(ParseUser user) {
        ParseQuery<Like> q = ParseQuery.getQuery(Like.class);
        q.whereEqualTo(STREAMER_CODE, user);
        try {
            List<Like> res = q.find();
            return res.size();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void setListener(ParseUser listener) {
        put(LISTENER_CODE, listener);
    }

    public ParseUser getStreamer() {
        return getParseUser(STREAMER_CODE);
    }

    public void setStreamer(ParseUser streamer) {
        put(STREAMER_CODE, streamer);
    }
}
