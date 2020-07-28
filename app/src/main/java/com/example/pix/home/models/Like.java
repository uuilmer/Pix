package com.example.pix.home.models;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

@ParseClassName("Like")
public class Like extends ParseObject {

    public ParseUser getListener() {
        return getParseUser("listener");
    }

    public static Like checkIfLikes(ParseUser listener, ParseUser streamer){
        ParseQuery<Like> q = ParseQuery.getQuery(Like.class);
        q.whereEqualTo("listener", listener);
        q.whereEqualTo("streamer", streamer);
        try {
            return q.getFirst();
        } catch (ParseException e) {
            return null;
        }
    }

    public void setListener(ParseUser listener) {
        put("listener", listener);
    }

    public ParseUser getStreamer() {
        return getParseUser("streamer");
    }

    public void setStreamer(ParseUser streamer) {
        put("streamer", streamer);
    }
}
