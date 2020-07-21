package com.example.pix.home.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.ChatActivity;
import com.example.pix.home.models.Chat;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    Context context;
    List<ParseUser> users;
    boolean newPic;

    public SearchAdapter(Context context, List<ParseUser> users) {
        this.context = context;
        this.users = users;
    }
    public SearchAdapter(boolean newPic, Context context, List<ParseUser> users) {
        this.context = context;
        this.users = users;
        this.newPic = newPic;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View to_add = LayoutInflater.from(context).inflate(R.layout.chat, parent, false);
        return new ViewHolder(to_add);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;
        TextView tvStatus;
        TextView tvTime;
        TextView tvPix;
        LinearLayout llSelect;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.ivProfile = itemView.findViewById(R.id.chats_profile);
            this.tvName = itemView.findViewById(R.id.chats_name);
            this.tvStatus = itemView.findViewById(R.id.chats_status);
            this.tvTime = itemView.findViewById(R.id.chats_time);
            this.tvPix = itemView.findViewById(R.id.chats_pix);
            this.llSelect = itemView.findViewById(R.id.chats_select);
        }

        public void bind(ParseUser user) {
            this.tvPix.setText("" + user.getInt("pix") + "P");
            this.llSelect.setOnClickListener(view -> {
                Intent i = new Intent(context, ChatActivity.class);

                Chat chat = findChat(true, user);
                Toast.makeText(context, "" + newPic, Toast.LENGTH_SHORT).show();
                if (newPic) {
                    Toast.makeText(context, "good", Toast.LENGTH_SHORT).show();
                    i.putExtra("newPic", newPic);
                }
                if(chat == null){
                    Chat newChat = new Chat();
                    newChat.setUser(ParseUser.getCurrentUser());
                    newChat.setFriend(user);
                    newChat.saveInBackground(e -> {
                        i.putExtra("chat", newChat.getObjectId());
                        context.startActivity(i);
                    });
                    return;
                }
                i.putExtra("chat", chat.getObjectId());
                context.startActivity(i);
            });
            Glide.with(context)
                    .load(user
                            .getParseFile("profile")
                            .getUrl())
                    .circleCrop()
                    .into(this.ivProfile);
            this.tvName.setText("" + user.getUsername());
            this.tvStatus.setVisibility(View.GONE);
            this.tvTime.setVisibility(View.GONE);
        }
    }
    private Chat findChat(boolean userOne, ParseUser user){
        ParseQuery<Chat> q = ParseQuery.getQuery(Chat.class);
        q.include("userOne");
        q.include("userTwo");
        if(userOne) {
            q.whereEqualTo("userOne", ParseUser.getCurrentUser());
            q.whereEqualTo("userTwo", user);
        }
        else{
            q.whereEqualTo("userOne", user);
            q.whereEqualTo("userTwo", ParseUser.getCurrentUser());
        }
        try {
            Chat res = q.getFirst();
            if (res != null)
                return res;
            if(userOne)
                return findChat(false, user);
            return null;
        } catch (ParseException e) {
            Toast.makeText(context, "Error searching!", Toast.LENGTH_SHORT).show();
        }
        return null;
    }
}
