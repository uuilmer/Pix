package com.example.pix.chat.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Song")
public class Song extends ParseObject {

    public String getURI() {
        return getString("uri");
    }

    public void setURI(String uri) {
        put("uri", uri);
    }

    public boolean isPlaying() {
        return getBoolean("isPlaying");
    }

    public void setPlaying(boolean playing) {
        put("isPlaying", playing);
    }
}
