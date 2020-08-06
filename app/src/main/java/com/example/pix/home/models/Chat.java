package com.example.pix.home.models;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ParseClassName("Chat")
public class Chat extends ParseObject {

    public static final String[] statuses = new String[]{"New Chat", "Opened", "Delivered"};
    public static final String USER_PROFILE_CODE = "profile";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String CHAT = "chat";
    public static final String RECIPIENT = "to";
    public static final String USER_ONE = "userOne";
    public static final String USER_TWO = "userTwo";
    public static final String VISIBLE_ONE = "visibleOne";
    public static final String VISIBLE_TWO = "visibleTwo";
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
        // Make sure this User has not archived this Chat
        q.whereEqualTo(VISIBLE_ONE, true);
        queries.add(q);

        ParseQuery<Chat> p = ParseQuery.getQuery(Chat.class);
        p.whereEqualTo(USER_TWO, user);
        p.whereEqualTo(VISIBLE_TWO, true);
        queries.add(p);

        // Combine the queries as an OR
        ParseQuery<Chat> res = ParseQuery.or(queries);

        res.orderByDescending(UPDATED_AT);
        res.setLimit(NUM_PER_PAGE * page + NUM_PER_PAGE);
        res.setSkip(NUM_PER_PAGE * page);
        return res.find();
    }

    // Combine Chats where this user is the sender OR ones where they are recipient, but in background
    public static void getChatsInBackground(ParseUser user, int page, FindCallback<Chat> handler) throws ParseException {
        List<ParseQuery<Chat>> queries = new ArrayList<>();
        ParseQuery<Chat> q = ParseQuery.getQuery(Chat.class);
        q.whereEqualTo(USER_ONE, user);
        // Make sure this User has not archived this Chat
        q.whereEqualTo(VISIBLE_ONE, true);
        queries.add(q);

        ParseQuery<Chat> p = ParseQuery.getQuery(Chat.class);
        p.whereEqualTo(USER_TWO, user);
        p.whereEqualTo(VISIBLE_TWO, true);
        queries.add(p);

        // Combine the queries as an OR
        ParseQuery<Chat> res = ParseQuery.or(queries);

        res.orderByDescending(UPDATED_AT);
        res.setLimit(NUM_PER_PAGE * page + NUM_PER_PAGE);
        res.setSkip(NUM_PER_PAGE * page);
        res.findInBackground(handler);
    }

    // Combine Chats where this user is the sender OR ones where they are recipient, but in background
    public static void getChatsInBackground(ParseUser user, int page, FindCallback<Chat> handler, Date lowerLimit) throws ParseException {
        List<ParseQuery<Chat>> queries = new ArrayList<>();
        ParseQuery<Chat> q = ParseQuery.getQuery(Chat.class);
        q.whereEqualTo(USER_ONE, user);
        q.whereEqualTo(VISIBLE_ONE, true);
        queries.add(q);

        ParseQuery<Chat> p = ParseQuery.getQuery(Chat.class);
        p.whereEqualTo(USER_TWO, user);
        p.whereEqualTo(VISIBLE_TWO, true);
        queries.add(p);

        // Combine the queries as an OR
        ParseQuery<Chat> res = ParseQuery.or(queries);

        res.orderByDescending(UPDATED_AT);
        res.setLimit(NUM_PER_PAGE * page + NUM_PER_PAGE);
        res.setSkip(NUM_PER_PAGE * page);
        if (lowerLimit != null) {
            res.whereGreaterThan(UPDATED_AT, lowerLimit);
        }
        res.findInBackground(handler);
    }

    // Get a List of Messages from this Chat then delete each one of them
    public static void deleteMessages(Chat chat) throws ParseException {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include(CHAT);
        q.whereEqualTo(CHAT, chat);
        List<Message> res = q.find();
        for (Message m : res) {
            m.delete();
        }
    }

    public static void archiveChat(Context context, Chat chat) {
        // Our Use doesn't want to see this Chat anymore
        chat.setVisible(false);
        try {
            chat.save();
            // If the friend also doesn't want to see it, delete the Chat history
            if (!chat.isVisible(chat.getFriend(ParseUser.getCurrentUser()))) {
                try {
                    // First delete the messages
                    deleteMessages(chat);
                    // Then the Chat itself
                    chat.deleteInBackground(e -> {
                        if (e != null) {
                            Toast.makeText(context, "Error deleting chat", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(context, "Chat was deleted", Toast.LENGTH_SHORT).show();
                    });
                } catch (ParseException e) {
                    Toast.makeText(context, "Error deleting messages", Toast.LENGTH_SHORT).show();

                    e.printStackTrace();
                }
            }
        } catch (ParseException e) {
            Toast.makeText(context, "Error updating chat", Toast.LENGTH_SHORT).show();

            e.printStackTrace();
        }
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
        return getInt(USER_PIX);
    }

    public void setPix(int pix) {
        put(USER_PIX, pix);
    }

    public boolean isVisible(ParseUser user) {
        ParseUser userOne = getParseUser(USER_ONE);
        if (userOne.getObjectId().equals(user.getObjectId())) {
            return getBoolean(VISIBLE_ONE);
        }
        return getBoolean(VISIBLE_TWO);
    }

    public void setVisible(boolean isVisible) {
        ParseUser userOne = getParseUser(USER_ONE);
        if (userOne.getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
            put(VISIBLE_ONE, isVisible);
            return;
        }
        put(VISIBLE_TWO, isVisible);
    }

    // This method is for the purposes of a chat preview (Doesn't edit read receipts)
    public Message getFirstMessage() {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include(CHAT);
        q.whereEqualTo(CHAT, this);
        q.orderByDescending(UPDATED_AT);
        try {
            return q.getFirst();
        } catch (ParseException ignored) {
        }
        return null;
    }

    // This is for the purposes of entering a chat (Edits read receipts)
    public List<Message> getMessages(int page, ParseUser requester) throws ParseException {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include(CHAT);
        q.whereEqualTo(CHAT, this);
        q.orderByDescending(CREATED_AT);
        q.setLimit(NUM_PER_PAGE * page + NUM_PER_PAGE);
        q.setSkip(NUM_PER_PAGE * page);

        // If the user that requested these Messages is the one who they were sent to, mark as read
        if (this.getStatus() == 1) {
            Message latestMessage = getFirstMessage();
            if (latestMessage != null) {
                ParseUser recipient = latestMessage.getTo().fetchIfNeeded();
                if (recipient.getObjectId().equals(requester.getObjectId())) {
                    this.setStatus(0);
                    this.save();
                }
            }
        }

        return q.find();
    }

    // This is for the purposes of entering a chat (Edits read receipts), but in background
    public void getMessagesInBackground(int page, ParseUser requester, FindCallback<Message> handler) throws ParseException {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include(CHAT);
        q.whereEqualTo(CHAT, this);
        q.orderByDescending(CREATED_AT);
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
