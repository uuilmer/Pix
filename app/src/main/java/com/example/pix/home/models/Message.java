package com.example.pix.home.models;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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

    public static Message getNewestMessage() {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.whereEqualTo(RECIPIENT, ParseUser.getCurrentUser());
        q.orderByDescending("createdAt");
        try {
            return q.getFirst();
        } catch (ParseException e) {
            return null;
        }
    }


    public Chat getChat() {
        return (Chat) getParseObject(CHAT);
    }

    public void setChat(Chat chat) {
        put(CHAT, chat);
    }

    public ParseUser getFrom() {
        return getParseUser(SENDER);
    }

    public void setFrom(ParseUser user) {
        put(SENDER, user);
    }

    public ParseUser getTo() {
        return getParseUser(RECIPIENT);
    }

    public void setTo(ParseUser user) {
        put(RECIPIENT, user);
    }

    public Date getTime() {
        return getCreatedAt();
    }

    public String getText() {
        return getString(TEXT);
    }

    public void setText(String text) {
        put(TEXT, text);
    }

    public ParseFile getPic() {
        return getParseFile(PICTURE);
    }

    public void setPic(ParseFile pic) {
        put(PICTURE, pic);
    }

    public boolean isSnap() {
        return getBoolean(IS_SNAP);
    }

    public void setIsSnap(boolean isSnap) {
        put(IS_SNAP, isSnap);
    }
}
