package com.example.pix.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.adapters.MessageAdapter;
import com.example.pix.home.models.Chat;
import com.example.pix.home.models.Message;
import com.example.pix.home.utils.EndlessRecyclerViewScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Use the objectId we passed to get this Chat
        String chatId = getIntent().getStringExtra("chat");
        Chat chat = Chat.getChat(chatId);


        RecyclerView rvMessages = findViewById(R.id.chat_rv);
        ImageView ivProfile = findViewById(R.id.chat_profile);
        TextView tvName = findViewById(R.id.chat_name);
        ImageView ivBack = findViewById(R.id.chat_back);
        ImageView ivCamera = findViewById(R.id.chat_camera);
        EditText etText = findViewById(R.id.chat_text);
        ImageView ivPictures = findViewById(R.id.chat_pictures);

        ParseUser friend = chat.getFriend(ParseUser.getCurrentUser());

        ParseFile profile = null;
        try {
            profile = friend.fetchIfNeeded().getParseFile("profile");
            Glide.with(ChatActivity.this).load(profile.getUrl()).circleCrop().into(ivProfile);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvName.setText("" + friend.getUsername());

        LinearLayoutManager manager = new LinearLayoutManager(ChatActivity.this);

        // Snapchat scrolls up instead of down, so reverse
        manager.setReverseLayout(true);

        try {
            List<Message> messages = chat.getMessages(0);
            MessageAdapter messageAdapter = new MessageAdapter(ChatActivity.this, messages);
            rvMessages.setAdapter(messageAdapter);
            rvMessages.setLayoutManager(manager);
            EndlessRecyclerViewScrollListener scroll = new EndlessRecyclerViewScrollListener(manager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    try {
                        chat.getMessagesInBackground(page, new FindCallback<Message>() {
                            @Override
                            public void done(List<Message> objects, ParseException e) {
                                messages.addAll(objects);
                                messageAdapter.notifyDataSetChanged();
                            }
                        });

                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("Error", "Failed fetching more messages", e);
                    }
                }
            };
            rvMessages.addOnScrollListener(scroll);
            manager.scrollToPosition(0);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}