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
import com.example.pix.home.fragments.ComposeFragment;

import static com.example.pix.home.adapters.SearchAdapter.NEW_PIC_CODE;

public class FriendActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);

        // Check if we took a picture from HomeFragment's ComposeFragment
        if (getIntent().hasExtra(NEW_PIC_CODE)) {
            getSupportFragmentManager().beginTransaction().add(R.id.friend_container, new ChatFragment(ComposeFragment.image)).commit();
        }
        else {
            getSupportFragmentManager().beginTransaction().add(R.id.friend_container, new ChatFragment()).commit();
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