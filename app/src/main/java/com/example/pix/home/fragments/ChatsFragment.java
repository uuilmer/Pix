package com.example.pix.home.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.example.pix.R;
import com.example.pix.home.adapters.ChatsAdapter;
import com.example.pix.home.models.Chat;
import com.example.pix.home.utils.EndlessRecyclerViewScrollListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class ChatsFragment extends Fragment {

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
            List<Chat> chats = Chat.getChats(ParseUser.getCurrentUser(), 0);
            ChatsAdapter chatsAdapter = new ChatsAdapter(getContext(), chats);
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

            PullRefreshLayout layout = view.findViewById(R.id.swipeRefreshLayout);

            // Whenever we try to refresh, delete all Chats, and get them again
            layout.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);
            layout.setOnRefreshListener(() -> {
                try {
                    Chat.getChatsInBackground(ParseUser.getCurrentUser(), 0, (objects, e) -> {
                        chats.clear();
                        chats.addAll(objects);
                        chatsAdapter.notifyDataSetChanged();
                        layout.setRefreshing(false);
                    });
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
}