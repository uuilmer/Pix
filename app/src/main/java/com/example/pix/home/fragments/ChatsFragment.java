package com.example.pix.home.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.home.adapters.ChatsAdapter;
import com.example.pix.home.models.Chat;
import com.example.pix.home.models.Message;
import com.example.pix.home.utils.EndlessRecyclerViewScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class ChatsFragment extends Fragment {

    private List<Chat> chats;
    private ChatsAdapter chatsAdapter;

    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView tvScore = view.findViewById(R.id.tv_score);
        RecyclerView rvChats = view.findViewById(R.id.rv_chats);

        // Get a List of this User's Chats and create an Adapter for it
        try {
            chats = Chat.getChats(ParseUser.getCurrentUser(), 0);
            chatsAdapter = new ChatsAdapter(getContext(), chats);
            rvChats.setAdapter(chatsAdapter);

            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            rvChats.setLayoutManager(linearLayoutManager);

            // When we scroll, get the next batch of chats
            EndlessRecyclerViewScrollListener scroll = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    try {
                        Chat.getChatsInBackground(ParseUser.getCurrentUser(), page, (objects, e) -> {
                            chats.addAll(objects);
                            chatsAdapter.notifyDataSetChanged();
                        });
                    } catch (ParseException e) {
                        Toast.makeText(getContext(), "Error retrieving more chats", Toast.LENGTH_SHORT).show();
                    }
                }
            };

            rvChats.addOnScrollListener(scroll);

            // Handle what happens when a Chat is swiped
            ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                    final int position = viewHolder.getAdapterPosition();
                    // Get the Chat that was swiped
                    Chat toDelete = chats.get(position);
                    try {
                        // Delete any messages from this Chat
                        deleteMessages(toDelete);
                        // Delete the Chat
                        toDelete.delete();
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error deleting Messages", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // Update our RecyclerView and Adapter
                    chats.remove(position);
                    chatsAdapter.notifyItemRemoved(position);
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
            itemTouchHelper.attachToRecyclerView(rvChats);
        } catch (ParseException e) {
            Log.e("Error", "Error getting List of Chats", e);
            Toast.makeText(getContext(), "Error retrieving chats", Toast.LENGTH_SHORT).show();
        }
    }

    // Get a List of Messages from this Chat then delete each one of them
    public void deleteMessages(Chat chat) throws ParseException {
        ParseQuery<Message> q = ParseQuery.getQuery(Message.class);
        q.include("chat");
        q.whereEqualTo("chat", chat);
        List<Message> res = q.find();
        for (Message m : res) {
            m.delete();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        chats.clear();
        try {
            Chat.getChatsInBackground(ParseUser.getCurrentUser(), 0, (objects, e) -> {
                chats.addAll(objects);
                chatsAdapter.notifyDataSetChanged();
            });
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Error retrieving more chats", Toast.LENGTH_SHORT).show();
        }
    }
}