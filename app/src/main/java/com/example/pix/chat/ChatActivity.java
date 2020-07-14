package com.example.pix.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.home.models.Chat;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Use the objectId we passed to get this Chat
        String chatId = getIntent().getStringExtra("chat");
        Chat chat = Chat.getChat(chatId);
        Toast.makeText(ChatActivity.this, "" + chat.getPix(), Toast.LENGTH_SHORT).show();
    }
}