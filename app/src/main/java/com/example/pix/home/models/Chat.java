package com.example.pix.home.models;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

@ParseClassName("Chat")
public class Chat extends ParseObject {
    public Chat(){}

    // Combine Chats where this user is the sender and ones where they are recipient
    public static List<Chat> getChats(ParseUser user, int page) throws ParseException {
        ParseQuery<Chat> q = ParseQuery.getQuery(Chat.class);
        q.whereEqualTo("userOne", user);
        q.orderByDescending("updatedAt");
        q.setLimit(5 * page + 5);
        q.setSkip(5 * page);
        List<Chat> caseOne = q.find();

        ParseQuery<Chat> p = ParseQuery.getQuery(Chat.class);
        p.whereEqualTo("userTwo", user);
        p.orderByDescending("updatedAt");
        p.setLimit(5 * page + 5);
        p.setSkip(5 * page);
        caseOne.addAll(p.find());
        return caseOne;
    }

    public String getStatus(){ // Take into account that for one user its delivered and opened and for other its new chat and opened
        // Maybe make a new column to keep track of this status as a number?
        return getString("status");
    }

    public void setStatus(String status){
        put("status", status);
    }

    public void setUser(ParseUser user){
        put("userOne", user);
    }

    // Return the User that is not the current User as the "friend"
    public ParseUser getFriend(ParseUser currUser){
        ParseUser one = getParseUser("userOne");
        ParseUser two = getParseUser("userTwo");
        return one.getObjectId() == currUser.getObjectId() ? two : one;
    }

    public void setFriend(ParseUser friend){
        put("userTwo", friend);
    }

    public int getPix(){
        return getInt("pix");
    }

    public void setPix(int pix){
        put("pix", pix);
    }

    public List<Message> getMessages(int page) throws ParseException {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include("chat");
        q.whereEqualTo("chat", this);
        q.orderByDescending("createdAt");
        q.setLimit(5 * page + 5);
        q.setSkip(5 * page);
        return q.find();
    }
}

//this.ivProfile.setImageResource(((ParseFile)chat.getFriendImage()).getUrl());
//this.tvName.setText("" + chat.getFriend.getUsername());
//this.tvPix.setText(chat.getPix());
