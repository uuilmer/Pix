package com.example.pix.network;

import android.app.Application;

import com.example.pix.chat.models.MusicRoom;
import com.example.pix.chat.models.Song;
import com.example.pix.home.models.Chat;
import com.example.pix.home.models.Message;
import com.parse.Parse;
import com.parse.ParseObject;

public class ParseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ParseObject.registerSubclass(Chat.class);
        ParseObject.registerSubclass(Message.class);
        ParseObject.registerSubclass(MusicRoom.class);
        ParseObject.registerSubclass(Song.class);
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("wilmer-pix") // should correspond to APP_ID env variable
                .clientKey("37DD5A0741")  // set explicitly unless clientKey is explicitly configured on Parse server
                .server("https://wilmer-pix.herokuapp.com/parse/").build());
    }
}

