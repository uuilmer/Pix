package com.example.pix.home.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.example.pix.chat.activities.FriendActivity;
import com.example.pix.home.models.Chat;
import com.example.pix.home.models.Message;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    Context context;
    List<Chat> chats;

    public ChatsAdapter(Context context, List<Chat> chats) {
        this.context = context;
        this.chats = chats;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View to_add = LayoutInflater.from(context).inflate(R.layout.chat, parent, false);
        return new ViewHolder(to_add);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(chats.get(position));
    }

    @Override
    public int getItemCount() {
        return chats.size();
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

        public void bind(Chat chat) {
            this.tvPix.setText("" + chat.getPix() + "P");
            this.llSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, FriendActivity.class);
                    i.putExtra("chat", chat.getObjectId());
                    context.startActivity(i);
                }
            });
            try {
                ParseUser friend = chat.getFriend(ParseUser.getCurrentUser()).fetchIfNeeded();
                Glide.with(context)
                        .load(friend
                                .getParseFile("profile")
                                .getUrl())
                        .circleCrop()
                        .into(this.ivProfile);
                this.tvName.setText("" + friend.getUsername());
                this.tvStatus.setText("" + chat.getStatusText());
                Message recent = chat.getFirstMessage();
                this.tvTime.setText("" + (recent != null ? recent.getTime().toString() : ""));
            } catch (ParseException e) {
                Log.e("Error", "Failed getting status text and/or time", e);
                Toast.makeText(context, "Error getting Chat status", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
