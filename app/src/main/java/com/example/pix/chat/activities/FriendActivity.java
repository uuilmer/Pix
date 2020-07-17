package com.example.pix.chat.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.chat.fragments.ChatFragment;
import com.example.pix.chat.fragments.MusicRoomFragment;
import com.example.pix.home.models.Chat;
import com.example.pix.login.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseUser;

public class FriendActivity extends AppCompatActivity {


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        Chat chat = Chat.getChat(getIntent().getStringExtra("chat"));

        // As of now, we are not considering displaying the ChatFragment
        // we are building the MusicRomFragment, so directly create it:
        try {
            getSupportFragmentManager().beginTransaction().add(R.id.friend_container, new MusicRoomFragment(chat.getFriend(ParseUser.getCurrentUser()).fetch())).commit();
        } catch (ParseException e) {
            e.printStackTrace();
        }
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