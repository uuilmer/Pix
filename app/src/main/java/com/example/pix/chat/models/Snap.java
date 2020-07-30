package com.example.pix.chat.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

@ParseClassName("Snap")
public class Snap extends ParseObject {

    public static final String PICTURE = "picture";
    public static final String SENDER = "from";
    public static final String RECIPIENT = "to";

    public ParseFile getPicture() {
        return getParseFile(PICTURE);
    }

    public void setPicture(ParseFile picture) {
        put(PICTURE, picture);
    }

    public ParseUser getSender() {
        return getParseUser(SENDER);
    }

    public void setSender(ParseUser sender) {
        put(SENDER, sender);
    }

    public ParseUser getRecipient() {
        return getParseUser(RECIPIENT);
    }

    public void setRecipient(ParseUser recipient) {
        put(RECIPIENT, recipient);
    }
}
