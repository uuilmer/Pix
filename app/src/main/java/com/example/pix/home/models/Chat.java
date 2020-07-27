package com.example.pix.home.models;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Chat")
public class Chat extends ParseObject {

    public static final String[] statuses = new String[]{"New Chat", "Opened", "Delivered"};
    public static final String USER_PROFILE_CODE = "profile";
    public static final String USER_ONE = "userOne";
    public static final String USER_TWO = "userTwo";
    public static final String USER_PROFILE_CODE = "profile";
    public static final String USER_PIX = "pix";
    public static final int NUM_PER_PAGE = 20;

    public Chat() {
    }

    public static Chat getChat(String chatId) {
        ParseQuery<Chat> q = ParseQuery.getQuery(Chat.class);
        q.whereEqualTo("objectId", chatId);
        try {
            return q.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.e("Error", "Failed getting Chat from objectId", e);
        }
        return null;
    }

    // Combine Chats where this user is the sender OR ones where they are recipient
    public static List<Chat> getChats(ParseUser user, int page) throws ParseException {
        List<ParseQuery<Chat>> queries = new ArrayList<>();
        ParseQuery<Chat> q = ParseQuery.getQuery(Chat.class);
        q.whereEqualTo(USER_ONE, user);
        queries.add(q);

        ParseQuery<Chat> p = ParseQuery.getQuery(Chat.class);
        p.whereEqualTo(USER_TWO, user);
        queries.add(p);

        // Combine the queries as an OR
        ParseQuery<Chat> res = ParseQuery.or(queries);

        res.orderByDescending("createdAt");
        res.setLimit(NUM_PER_PAGE * page + NUM_PER_PAGE);
        res.setSkip(NUM_PER_PAGE * page);
        return res.find();
    }

    // Combine Chats where this user is the sender OR ones where they are recipient, but in background
    public static void getChatsInBackground(ParseUser user, int page, FindCallback<Chat> handler) throws ParseException {
        List<ParseQuery<Chat>> queries = new ArrayList<>();
        ParseQuery<Chat> q = ParseQuery.getQuery(Chat.class);
        q.whereEqualTo(USER_ONE, user);
        queries.add(q);

        ParseQuery<Chat> p = ParseQuery.getQuery(Chat.class);
        p.whereEqualTo(USER_TWO, user);
        queries.add(p);

        // Combine the queries as an OR
        ParseQuery<Chat> res = ParseQuery.or(queries);

        res.orderByDescending("createdAt");
        res.setLimit(NUM_PER_PAGE * page + NUM_PER_PAGE);
        res.setSkip(NUM_PER_PAGE * page);
        res.findInBackground(handler);
    }

    public int getStatus() {
        return getInt("status");
    }

    public String getStatusText() { // Take into account that for one user its delivered and opened and for other its new chat and opened
        // Maybe make a new column to keep track of this status as a number?
        int status = getInt("status");
        Message latestMessage = getFirstMessage();

        if (latestMessage == null) return "New chat!";

        boolean userSentThis = latestMessage.getFrom().getObjectId().equals(ParseUser.getCurrentUser().getObjectId());
        // If the current user sent this message, return either opened or delivered

        if (userSentThis) return statuses[1 + status];

        // Else return opened or new chat
        return statuses[1 - status];
    }

    public void setStatus(int status) {
        put("status", status);
    }

    public void setUser(ParseUser user) {
        put(USER_ONE, user);
    }

    // Return the User that is not the current User as the "friend"
    public ParseUser getFriend(ParseUser currUser) {
        ParseUser one = getParseUser(USER_ONE);
        ParseUser two = getParseUser(USER_TWO);
        return one.getObjectId().equals(currUser.getObjectId()) ? two : one;
    }

    public void setFriend(ParseUser friend) {
        put(USER_TWO, friend);
    }

    public int getPix() {
        return getInt("pix");
    }

    public void setPix(int pix) {
        put("pix", pix);
    }

    // This method is for the purposes of a chat preview (Doesn't edit read receipts)
    public Message getFirstMessage() {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include("chat");
        q.whereEqualTo("chat", this);
        q.orderByDescending("createdAt");
        try {
            return q.getFirst();
        } catch (ParseException ignored) {
        }
        return null;
    }

    // This is for the purposes of entering a chat (Edits read receipts)
    public List<Message> getMessages(int page, ParseUser requester) throws ParseException {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include("chat");
        q.whereEqualTo("chat", this);
        q.orderByDescending("createdAt");
        q.setLimit(NUM_PER_PAGE * page + NUM_PER_PAGE);
        q.setSkip(NUM_PER_PAGE * page);

        // If the user that requested these Messages is the one who they were sent to, mark as read
        if (this.getStatus() == 1) {
            ParseUser recipient = getFirstMessage().getTo().fetchIfNeeded();
            if (recipient.getObjectId().equals(requester.getObjectId())) {
                this.setStatus(0);
                this.save();
            }
        }

        return q.find();
    }

    // This is for the purposes of entering a chat (Edits read receipts), but in background
    public void getMessagesInBackground(int page, ParseUser requester, FindCallback<Message> handler) throws ParseException {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include("chat");
        q.whereEqualTo("chat", this);
        q.orderByDescending("createdAt");
        q.setLimit(NUM_PER_PAGE * page + NUM_PER_PAGE);
        q.setSkip(NUM_PER_PAGE * page);

        if (this.getStatus() == 1) {
            ParseUser recipient = getFirstMessage().getTo().fetchIfNeeded();
            if (recipient.getObjectId().equals(requester.getObjectId())) {
                this.setStatus(0);
                this.save();
            }
        }

        q.findInBackground(handler);
    }
}
