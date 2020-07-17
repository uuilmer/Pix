package com.example.pix.chat.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.adapters.MessageAdapter;
import com.example.pix.chat.utils.FetchPath;
import com.example.pix.home.models.Chat;
import com.example.pix.home.models.Message;
import com.example.pix.home.utils.EndlessRecyclerViewScrollListener;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    public static final int RESULT_LOAD_IMG = 100;
    public static final int REQUEST_PERM = 101;
    ImageView ivNewPic;
    ParseFile newPic;
    MessageAdapter messageAdapter;
    List<Message> messages;
    RecyclerView rvMessages;
    EditText etText;
    Chat chat;
    ImageView ivPictures;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERM);

        // Use the objectId we passed to get this Chat
        String chatId = getActivity().getIntent().getStringExtra("chat");
        chat = Chat.getChat(chatId);


        rvMessages = view.findViewById(R.id.chat_rv);
        ImageView ivProfile = view.findViewById(R.id.chat_profile);
        TextView tvName = view.findViewById(R.id.chat_name);
        ImageView ivBack = view.findViewById(R.id.chat_back);
        ImageView ivCamera = view.findViewById(R.id.chat_camera);
        etText = view.findViewById(R.id.chat_text);
        ivPictures = view.findViewById(R.id.chat_pictures);
        ivNewPic = view.findViewById(R.id.chat_image);

        ivBack.setOnClickListener(view1 -> getActivity().finish());

        ParseUser friend = chat.getFriend(ParseUser.getCurrentUser());

        // When we click the plus, go to add a pic
        ivPictures.setOnClickListener(view1 -> {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, RESULT_LOAD_IMG);
        });

        // Get this friend's profile pic
        ParseFile profile;
        try {
            profile = friend.fetchIfNeeded().getParseFile("profile");
            Glide.with(getActivity()).load(profile.getUrl()).circleCrop().into(ivProfile);
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Error retrieving more chats", Toast.LENGTH_SHORT).show();
        }

        tvName.setText("" + friend.getUsername());

        LinearLayoutManager manager = new LinearLayoutManager(getContext());

        // Snapchat scrolls up instead of down, so reverse
        manager.setReverseLayout(true);

        try {
            messages = chat.getMessages(0, ParseUser.getCurrentUser());
            messageAdapter = new MessageAdapter(getContext(), messages);
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
                        Log.e("Error", "Failed fetching more messages", e);
                        Toast.makeText(getContext(), "Error retrieving more messages", Toast.LENGTH_SHORT).show();
                    }
                }
            };
            rvMessages.addOnScrollListener(scroll);
            manager.scrollToPosition(0);

            // When we press enter, save this text as a new Message within this Chat and scroll to the latest message
            etText.setOnKeyListener((view1, i, keyEvent) -> {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                    // Create new message
                    Message newMessage = new Message();
                    newMessage.setText(etText.getText().toString());
                    newMessage.setFrom(ParseUser.getCurrentUser());
                    newMessage.setTo(friend);
                    newMessage.setChat(chat);
                    if (newPic != null) {
                        // Case where we need to save a picture to DB first
                        newPic.saveInBackground((SaveCallback) e -> {
                            if (e != null) {
                                Toast.makeText(getContext(), "Error saving picture", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            newMessage.setPic(newPic);
                            saveMessage(newMessage);
                        });
                    } else
                        // Case where there is no picture
                        saveMessage(newMessage);
                    return true;
                }
                return false;
            });
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Error retrieving messages", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMessage(Message newMessage) {
        newMessage.saveInBackground(e -> {
            if (e != null) {
                Log.e("Error", "Failed saving message", e);
                Toast.makeText(getContext(), "Error sending message", Toast.LENGTH_SHORT).show();
                return;
            }
            messages.add(0, newMessage);
            messageAdapter.notifyDataSetChanged();
            rvMessages.smoothScrollToPosition(0);
            etText.setText("");
            chat.setStatus(1);
            ivNewPic.setImageResource(0);
            ivNewPic.setVisibility(View.GONE);
            newPic = null;
            chat.saveInBackground(e1 -> {
                if (e1 != null) {
                    Toast.makeText(getContext(), "Error sending read receipt", Toast.LENGTH_SHORT).show();
                    Log.e("Error", "Failed saving chat", e1);
                }
            });
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMG) {
                try {
                    final Uri imageUri = data.getData();
                    InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                    final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                    ivNewPic.setImageBitmap(selectedImage);
                    ivNewPic.setVisibility(View.VISIBLE);

                    newPic = new ParseFile(new File(FetchPath.getPath(getContext(), imageUri)));
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Error retrieving image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "You can now send pictures!", Toast.LENGTH_SHORT).show();
        } else {
            // INSERT HERE BETTER DISABLING OF SENDING PICS SINCE USER DID NOT ALLOW IT.(MAYBE)
            ivPictures.setVisibility(View.GONE);
        }
    }
}
