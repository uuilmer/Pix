package com.example.pix.chat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
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
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public static final int RESULT_LOAD_IMG = 100;
    ImageView ivNewPic;
    File newPic;

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
        ivNewPic = findViewById(R.id.chat_image);

        ParseUser friend = chat.getFriend(ParseUser.getCurrentUser());

        ivPictures.setOnClickListener(view -> {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, RESULT_LOAD_IMG);
        });

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
            List<Message> messages = chat.getMessages(0, ParseUser.getCurrentUser());
            MessageAdapter messageAdapter = new MessageAdapter(ChatActivity.this, messages);
            rvMessages.setAdapter(messageAdapter);
            rvMessages.setLayoutManager(manager);
            EndlessRecyclerViewScrollListener scroll = new EndlessRecyclerViewScrollListener(manager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    try {
                        chat.getMessagesInBackground(page, ParseUser.getCurrentUser(), (objects, e) -> {
                            messages.addAll(objects);
                            messageAdapter.notifyDataSetChanged();
                        });

                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("Error", "Failed fetching more messages", e);
                    }
                }
            };
            rvMessages.addOnScrollListener(scroll);
            manager.scrollToPosition(0);

            // When we press enter, save this text as a new Message within this Chat and scroll to the latest message
            etText.setOnKeyListener((view, i, keyEvent) -> {
                if(keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER){
                    Message newMessage = new Message();
                    newMessage.setText(etText.getText().toString());
                    newMessage.setFrom(ParseUser.getCurrentUser());
                    newMessage.setTo(friend);
                    if(newPic != null)
                        newMessage.setPic(newPic);
                    newMessage.setChat(chat);
                    try {
                        newMessage.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                messages.add(0, newMessage);
                                messageAdapter.notifyDataSetChanged();
                                rvMessages.smoothScrollToPosition(0);
                            }
                        });
                        etText.setText("");
                        chat.setStatus(1);
                        chat.save();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.e("Error", "Failed saving new Message", e);
                    }
                    return true;
                }
                return false;
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == RESULT_LOAD_IMG){
                try {
                    final Uri imageUri = data.getData();
                    InputStream imageStream = getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ivNewPic.setImageBitmap(selectedImage);
                    ivNewPic.setVisibility(View.VISIBLE);
                    newPic = new File(imageUri.getPath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}