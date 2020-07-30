package com.example.pix.home.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baoyz.widget.PullRefreshLayout;
import com.example.pix.R;
import com.example.pix.home.adapters.ChatsAdapter;
import com.example.pix.home.models.Chat;
import com.example.pix.home.utils.EndlessRecyclerViewScrollListener;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class ChatsFragment extends Fragment {

    private List<Chat> chats;
    private ChatsAdapter chatsAdapter;
    private Date lowerLimit;

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
                public void onLoadMore(int page, int totalItemsCount, RecyclerView unusedView) {
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

            PullRefreshLayout layout = view.findViewById(R.id.swipeRefreshLayout);

            // Whenever we try to refresh, delete all Chats, and get them again
            layout.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);

            lowerLimit = chats.get(0).getUpdatedAt();
            layout.setOnRefreshListener(() -> {
                try {
                    Chat.getChatsInBackground(ParseUser.getCurrentUser(), 0, (objects, e) -> {
                        HashSet<String> toRemove = new HashSet<>();
                        // List the objectIds we need to remove from our chats
                        for (Chat c : objects) {
                            toRemove.add(c.getObjectId());
                        }
                        // Delete them
                        for (Chat c : chats){
                            if (toRemove.contains(c.getObjectId())) {
                                chats.remove(c);
                            }
                        }
                        // Add the to the top/add new chats
                        for (Chat c : objects) {
                            chats.add(0, c);
                        }
                        chatsAdapter.notifyDataSetChanged();
                        layout.setRefreshing(false);
                        // Our newest message is now newer
                        lowerLimit = chats.get(0).getUpdatedAt();
                    }, lowerLimit);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error refreshing Chats", Toast.LENGTH_SHORT).show();
                    layout.setRefreshing(false);
                }
            });

        } catch (ParseException e) {
            Log.e("Error", "Error getting List of Chats", e);
            Toast.makeText(getContext(), "Error retrieving chats", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chats == null) chats = new ArrayList<>();
        else chats.clear();
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