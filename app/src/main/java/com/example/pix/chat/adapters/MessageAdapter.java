package com.example.pix.chat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.home.models.Message;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.util.HashSet;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Message> messages;
    private ImageView imageContainer;
    private VideoView videoContainer;
    // This will keep track of Snaps we have already seen, to make it impossible to see a Snap twice
    private static HashSet<Message> snapsSeen = new HashSet<>();
    // Commented out code is attempt to combine two consecutive messages by the same sender into one
    //List<ViewHolder> viewHolders;

    public MessageAdapter(Context context, List<Message> messages, ImageView imageContainer, VideoView videoContainer) {
        this.context = context;
        this.messages = messages;
        this.imageContainer = imageContainer;
        this.videoContainer = videoContainer;
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
        holder.bind(position, messages, context, imageContainer, videoContainer);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView text;
        private Message message;
        private ImageView contentPic;
        private LinearLayout openSnap;
        private View snapIndicator;
        private TextView snapText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.message_name);
            this.text = itemView.findViewById(R.id.message_text);
            this.contentPic = itemView.findViewById(R.id.message_content_pic);
            this.openSnap = itemView.findViewById(R.id.message_snap);
            this.snapIndicator = itemView.findViewById(R.id.message_snap_indicator);
            this.snapText = itemView.findViewById(R.id.message_snap_text);
        }

        public void bind(int position, List<Message> messages, Context context, ImageView imageContainer, VideoView videoContainer) {
            this.contentPic.setVisibility(View.GONE);
            this.name.setVisibility(View.VISIBLE);
            this.openSnap.setVisibility(View.GONE);

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

            // Only show text attachment if not a Snap content
            this.text.setVisibility(View.GONE);
            if (message.getText() != null && !message.getText().equals("") && !message.isSnap()) {
                this.text.setVisibility(View.VISIBLE);
                this.text.setText("" + message.getText());
            }


            ParseFile content = message.getPic();
            // Only show content attachment if not a Snap content
            if (content != null && !message.isSnap()) {
                this.contentPic.setVisibility(View.VISIBLE);
                Glide.with(context).load(content.getUrl()).into(this.contentPic);

                // Allow the User to view a picture in full-screen
                this.contentPic.setOnClickListener(unusedView -> {
                    Glide.with(context).load(content.getUrl()).into(imageContainer);
                    imageContainer.setVisibility(View.VISIBLE);

                    // If we click this new Imageview...
                    imageContainer.setOnClickListener(unusedView1 -> {
                        // Get rid of the picture
                        imageContainer.setImageResource(0);
                        imageContainer.setVisibility(View.GONE);

                        // No more interaction with the full-screen ImageView
                        imageContainer.setOnClickListener(null);
                    });
                });
            }


            // If this is a Snap content...
            if (message.isSnap() && !snapsSeen.contains(message)) {
                // Show the snap layout(To represent a message)
                this.openSnap.setVisibility(View.VISIBLE);

                // If this User sent it, they can't open the Snap
                if (message.getFrom().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                    this.snapText.setText("Delivered");
                } else {
                    // If this User received the Snap content, if they click the layout...
                    this.openSnap.setOnClickListener(unusedView -> {
                        // Set up the content as an ImageView or VideoView that lives in ChatFragment
                        // Both of these are placed above Chat with a friend with visibility Gone,
                        // so we change it to visible and it will cover the screen

                        // This happens if we click the full screen content(For both pic and video)...
                        View.OnClickListener callback = unusedView1 -> {
                            // Get rid of the Snap image and hide the big ImageView
                            imageContainer.setImageResource(0);
                            imageContainer.setVisibility(View.GONE);

                            // Get rid of the SNap video and hide the big VideoView
                            videoContainer.suspend();
                            videoContainer.setVisibility(View.GONE);


                            Drawable buttonDrawable = this.snapIndicator.getBackground();
                            buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                            // Indicate to User that we have opened the Snap by changing indicator color
                            DrawableCompat.setTint(buttonDrawable, Color.WHITE);
                            this.snapIndicator.setBackground(buttonDrawable);

                            // Delete Snap
                            message.deleteInBackground();
                            snapsSeen.add(message);
                            this.snapText.setText("Opened");

                            // No more interaction with this empty Snap
                            imageContainer.setOnClickListener(null);
                            videoContainer.setOnClickListener(null);
                            this.openSnap.setOnClickListener(null);
                        };

                        // If the url ends with mp4, then this must be a video snap
                        if (content.getUrl().endsWith(".mp4")) {
                            // Show the big VideView
                            videoContainer.setVisibility(View.VISIBLE);

                            // Show the big ImageView(This was done to serve as a background,
                            // as attempting to cahnge the background of the VideoView was very complex)
                            imageContainer.setVisibility(View.VISIBLE);

                            // Set up the looping Snap video
                            Uri video = Uri.parse(content.getUrl());
                            videoContainer.setVideoURI(video);
                            videoContainer.setOnPreparedListener(mp -> {
                                mp.setLooping(true);
                                videoContainer.start();
                            });

                            // When we click the video, end it
                            videoContainer.setOnClickListener(callback);
                        } else {
                            // Case where Snap content is picture
                            Glide.with(context).load(content.getUrl()).into(imageContainer);
                            imageContainer.setVisibility(View.VISIBLE);

                            imageContainer.setOnClickListener(callback);
                        }
                    });
                }
            }

            this.message = message;
        }
    }
}
