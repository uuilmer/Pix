package com.example.pix.home.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.util.Date;

@ParseClassName("Message")
public class Message extends ParseObject {

    public Message() {
    }

    public ParseUser getFrom() {
        return getParseUser("from");
    }

    public void setFrom(ParseUser user) {
        put("from", user);
    }

    public ParseUser getTo() {
        return getParseUser("to");
    }

    public void setTo(ParseUser user) {
        put("to", user);
    }

    public Date getTime() {
        return getCreatedAt();
    }

    public String getText() {
        return getString("text");
    }

    public void setText(String text) {
        put("text", text);
    }

    public ParseFile getPic(){
        return getParseFile("pic");
    }
    public void setPic(File pic){
        put("pic", pic);
    }
}
