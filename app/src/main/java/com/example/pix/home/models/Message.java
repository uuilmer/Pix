package com.example.pix.home.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.File;
import java.util.Date;

@ParseClassName("Message")
public class Message extends ParseObject {

    public static final String CHAT = "chat";
    public static final String SENDER = "from";
    public static final String RECIPIENT = "to";
    public static final String TEXT = "text";
    public static final String PICTURE = "pic";
    public static final String IS_SNAP = "isSnap";

    public Message() {
    }

    public Chat getChat() {
        return (Chat) getParseObject("chat");
    }

    public void setChat(Chat chat) {
        put("chat", chat);
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

    public ParseFile getPic() {
        return getParseFile("pic");
    }

    public void setPic(ParseFile pic) {
        put("pic", pic);
    }

    public boolean isSnap() {
        return getBoolean(IS_SNAP);
    }

    public void setIsSnap(boolean isSnap) {
        put(IS_SNAP, isSnap);
    }
}
