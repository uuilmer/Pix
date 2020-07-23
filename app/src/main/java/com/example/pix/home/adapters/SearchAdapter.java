package com.example.pix.home.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.example.pix.chat.ChatActivity;
import com.example.pix.home.models.Chat;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

import static com.example.pix.home.models.Chat.USER_ONE;
import static com.example.pix.home.models.Chat.USER_PROFILE_CODE;
import static com.example.pix.home.models.Chat.USER_TWO;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {

    public static final String NEW_PIC_CODE = "newPic";
    private Context context;
    private List<ParseUser> users;
    private boolean newPic;

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

        public void bind(ParseUser user) {
            this.tvPix.setText("" + user.getInt("pix") + "P");
            this.llSelect.setOnClickListener(view -> {
                // If we select a result, we will want to go to ChatActivity
                Intent i = new Intent(context, ChatActivity.class);

                // Look to see if a Chat with the selected User already exists
                Chat chat = findChat(user);

                // If we indicated that this Adapter was made with intent to send pic,
                if (newPic) i.putExtra(NEW_PIC_CODE, newPic);

                // If we couldnt find an existing CHat, make a new one
                if (chat == null) {
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
            ParseFile pic = user.getParseFile(USER_PROFILE_CODE);
            if (pic != null) {
                Glide.with(context)
                        .load(pic
                                .getUrl())
                        .circleCrop()
                        .into(this.ivProfile);
            }
            this.tvName.setText("" + user.getUsername());
            this.tvStatus.setVisibility(View.GONE);
            this.tvTime.setVisibility(View.GONE);
        }
    }

    // Try to find a Chat with the given ordering of the two Users
    private Chat findChatHelper(ParseUser userOne, ParseUser userTwo) {
        ParseQuery<Chat> q = ParseQuery.getQuery(Chat.class);
        q.include(USER_ONE);
        q.include(USER_TWO);
        q.whereEqualTo(USER_ONE, userOne);
        q.whereEqualTo(USER_TWO, userTwo);
        try {
            return q.getFirst();
        } catch (ParseException e) {
            return null;
        }
    }

    // Note: userOne and userTwo in Chat table is both people in the chat in no particular order,
    // this we need to check both cases:
    // User is userOne and friend is userTwo
    // friend is userOne and user is userTwo
    private Chat findChat(ParseUser user) {
        Chat chat = findChatHelper(user, ParseUser.getCurrentUser());
        if (chat != null) return chat;
        return findChatHelper(ParseUser.getCurrentUser(), user);
    }
}
