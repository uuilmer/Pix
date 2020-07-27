package com.example.pix.chat.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Song")
public class Song extends ParseObject {

    public String getAlbumName(){
        return getString("album");
    }

    public void setAlbumName(String name){
        put("album", name);
    }

    public String getArtistName(){
        return getString("artist");
    }

    public void setArtistName(String name){
        put("artist", name);
    }

    public String getImage(){
        return getString("artwork");
    }

    public void setImage(String artwork){
        put("artwork", artwork);
    }

    public String getName(){
        return getString("name");
    }

    public void setName(String name){
        put("name", name);
    }

    public String getURI(){
        return getString("uri");
    }

    public void setURI(String uri){
        put("uri", uri);
    }

    public boolean isPlaying(){
        return getBoolean("isPlaying");
    }
    public void setPlaying(boolean playing){
        put("isPlaying", playing);
    }
}
