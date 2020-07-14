package com.example.pix.chat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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

        RecyclerView rvMessages = findViewById(R.id.chat_rv);
        ImageView ivProfile = findViewById(R.id.chat_profile);
        TextView tvName = findViewById(R.id.chat_name);
        ImageView ivBack = findViewById(R.id.chat_back);
        ImageView ivCamera = findViewById(R.id.chat_camera);
        EditText etText = findViewById(R.id.chat_text);
        ImageView ivPictures = findViewById(R.id.chat_pictures);



    }
}