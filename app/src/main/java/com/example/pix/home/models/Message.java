package com.example.pix.home.models;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.Date;

@ParseClassName("Message")
public class Message extends ParseObject {

    public static final String MESSAGE_RECIPIENT = "to";
    public static final String MESSAGE_SENDER = "from";

    public Message() {
    }

    public static Message getNewestMessage() {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.whereEqualTo(MESSAGE_RECIPIENT, ParseUser.getCurrentUser());
        q.orderByDescending("createdAt");
        try {
            return q.getFirst();
        } catch (ParseException e) {
            return null;
        }
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
}
