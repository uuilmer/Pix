package com.example.pix.home.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.home.models.Chat;
import com.example.pix.home.models.Message;
import com.parse.ParseException;
import com.parse.ParseUser;

import java.util.List;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    Context context;
    List<Chat> chats;

    public ChatsAdapter(Context context, List<Chat> chats){
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

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivProfile;
        TextView tvName;
        TextView tvStatus;
        TextView tvTime;
        TextView tvPix;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.ivProfile = itemView.findViewById(R.id.chats_profile);
            this.tvName = itemView.findViewById(R.id.chats_name);
            this.tvStatus = itemView.findViewById(R.id.chats_status);
            this.tvTime = itemView.findViewById(R.id.chats_time);
            this.tvPix = itemView.findViewById(R.id.chats_pix);
        }
        public void bind(Chat chat){
            Glide.with(context)
                    .load(
                            chat.getFriend(ParseUser.getCurrentUser())
                                    .getParseFile("profile")
                                    .getUrl())
                    .into(this.ivProfile);
            this.tvName.setText("" + chat.getFriend(ParseUser.getCurrentUser()).getUsername());
            this.tvPix.setText("" + chat.getPix());
            this.tvStatus.setText("" + chat.getStatus());
            try {
                Message recent = chat.getMessages(0).get(0);
                this.tvTime.setText("" + recent.getTime().toString());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }
}
