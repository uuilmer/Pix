package com.example.pix.chat.adapters;

import android.content.Context;
import android.util.DisplayMetrics;
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

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    Context context;
    List<Message> messages;
    //List<ViewHolder> viewHolders;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
        //this.viewHolders = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View toAdd = LayoutInflater.from(context).inflate(R.layout.message, parent, false);
        return new ViewHolder(toAdd);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(messages.get(position)/*, viewHolders, position*/);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView text;
        ViewGroup contentPics;
        Message message;
        ImageView contentPic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.message_name);
            this.text = itemView.findViewById(R.id.message_text);
            this.contentPics = itemView.findViewById(R.id.message_content_pics);
            this.contentPic = itemView.findViewById(R.id.message_content_pic);
        }

        public void bind(Message message/*, List<ViewHolder> viewHolders, int position*/) {
            this.contentPic.setVisibility(View.GONE);
//            this.itemView.setVisibility(View.VISIBLE);
//            this.contentPics.removeAllViews();
//            if(!viewHolders.contains(this))
//                viewHolders.add(this);
//            if(position > 0){
//                int currIndex = viewHolders.indexOf(this);
//                ViewHolder previous = viewHolders.get(currIndex -1);
//                while(previous.itemView.getVisibility() == View.GONE && currIndex > 0)
//                    previous = viewHolders.get(--currIndex);
//                Message content = previous.getMessage();
//                if(content.getFrom().equals(ParseUser.getCurrentUser())){
//                    previous.text.setText("" + previous.text.getText().toString() + "\n" + message.getText());
//                    newPic(previous.contentPics, message);
//                    this.itemView.setVisibility(View.GONE);
//                    return;
//                }
//            }
            ParseUser from = message.getFrom();
            try {
                this.name.setText("" + (from.fetchIfNeeded().equals(ParseUser.getCurrentUser()) ? "Me" : from.fetchIfNeeded().getUsername()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            this.text.setText("" + message.getText());

            ParseFile pic = message.getPic();
            if (pic != null) {
                this.contentPic.setVisibility(View.VISIBLE);
                Glide.with(this.contentPics.getContext()).load(pic.getUrl()).into(this.contentPic);
            }

            this.message = message;
        }
//        private void newPic(ViewGroup viewGroup, Message message){
//            ParseFile pic = message.getPic();
//            if(pic != null) {
//                ImageView newPic = new ImageView(viewGroup.getContext());
//                Glide.with(viewGroup.getContext()).load(pic.getUrl()).into(newPic);
//                newPic.getLayoutParams().height = (int) convertPixelsToDp(80, itemView.getContext());
//                newPic.getLayoutParams().width = (int) convertPixelsToDp(80, itemView.getContext());
//                viewGroup.addView(newPic);
//            }
//        }

//        public Message getMessage() {
//            return message;
//        }

        public static float convertPixelsToDp(float px, Context context) {
            return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        }
    }
}
