package com.example.pix.chat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.example.pix.R;
import com.example.pix.chat.fragments.ChatFragment;
import com.example.pix.home.models.Chat;

public class FriendActivity extends AppCompatActivity {


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        Chat chat = Chat.getChat(getIntent().getStringExtra("chat"));

        // As soon as we go into a FriendActivity, we summon a ChatFragment,
        // which can within itself change itself into a ProfileFragment
        getSupportFragmentManager().beginTransaction().add(R.id.friend_container, new ChatFragment()).commit();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}