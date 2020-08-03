package com.example.pix.chat.adapters;

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
import com.example.pix.home.models.Message;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messages;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View toAdd = LayoutInflater.from(context).inflate(R.layout.message, parent, false);
        return new ViewHolder(toAdd);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(position, messages);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView text;
        private ViewGroup contentPics;
        private Message message;
        private ImageView contentPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.message_name);
            this.text = itemView.findViewById(R.id.message_text);
            this.contentPics = itemView.findViewById(R.id.message_content_pics);
            this.contentPic = itemView.findViewById(R.id.message_content_pic);
        }

        public void bind(int position, List<Message> messages) {
            this.contentPic.setVisibility(View.GONE);
            this.name.setVisibility(View.VISIBLE);

            Message message = messages.get(position);

            // Check if the previous message was sent by the same person, if so there is no need to display the username again
            if (position < messages.size() - 1) {
                if (messages.get(position + 1).getFrom().getObjectId().equals(message.getFrom().getObjectId())) {
                    this.name.setVisibility(View.GONE);
                }
            }

            ParseUser from = message.getFrom();
            try {
                this.name.setText("" + (from.fetchIfNeeded().getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) ? "Me" : from.fetchIfNeeded().getUsername()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.text.setVisibility(View.GONE);
            if (message.getText() != null && !message.getText().equals("")) {
                this.text.setVisibility(View.VISIBLE);
                this.text.setText("" + message.getText());
            }


            ParseFile pic = message.getPic();
            if (pic != null) {
                this.contentPic.setVisibility(View.VISIBLE);
                Glide.with(this.contentPics.getContext()).load(pic.getUrl()).into(this.contentPic);
            }

            this.message = message;
        }
    }
}
