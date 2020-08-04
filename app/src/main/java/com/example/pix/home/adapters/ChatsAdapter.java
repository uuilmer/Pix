package com.example.pix.home.adapters;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.activities.FriendActivity;
import com.example.pix.home.models.Chat;
import com.example.pix.home.models.Like;
import com.example.pix.home.models.Message;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static com.example.pix.home.models.Chat.CHAT;
import static com.example.pix.home.models.Chat.USER_PROFILE_CODE;


public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ViewHolder> {

    private Context context;
    private List<Chat> chats;

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

    protected class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivProfile;
        private TextView tvName;
        private TextView tvStatus;
        private TextView tvTime;
        private TextView tvPix;
        private LinearLayout llSelect;

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
            this.llSelect.setOnClickListener(unusedView -> {
                Intent i = new Intent(context, FriendActivity.class);
                i.putExtra(CHAT, chat.getObjectId());
                context.startActivity(i);
            });
            try {
                ParseUser friend = chat.getFriend(ParseUser.getCurrentUser()).fetchIfNeeded();
                ParseFile pic = friend.getParseFile(USER_PROFILE_CODE);
                if (pic != null) {
                    Glide.with(context)
                            .load(pic
                                    .getUrl())
                            .circleCrop()
                            .into(this.ivProfile);
                }
                this.tvName.setText("" + friend.getUsername());
                this.tvStatus.setText("" + chat.getStatusText());
                Message recent = chat.getFirstMessage();
                this.tvTime.setText("" + (recent != null ? getRelativeTime(recent.getTime().toString()) : ""));
                tvPix.setText("" + Like.getPix(friend) + "P");
            } catch (ParseException e) {
                Log.e("Error", "Failed getting status text and/or time", e);
            }
        }

        public String getRelativeTime(String json_response) {
            //Define the given format
            String format = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
            SimpleDateFormat sf = new SimpleDateFormat(format, Locale.ENGLISH);
            sf.setLenient(true);

            String relativeDate = "";
            try {
                //Get Unix Epoch and get relative from today, then call toString to get readable difference
                long dateMillis = sf.parse(json_response).getTime();
                relativeDate = DateUtils.getRelativeTimeSpanString(dateMillis,
                        System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS).toString();
            } catch (java.text.ParseException e) {
                e.printStackTrace();
            }

            return relativeDate;
        }
    }
}
