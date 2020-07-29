package com.example.pix.chat.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
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
    private ImageView snapContainer;
    // Commented out code is attempt to combine two consecutive messages by the same sender into one
    //List<ViewHolder> viewHolders;

    public MessageAdapter(Context context, List<Message> messages, ImageView snapContainer) {
        this.context = context;
        this.messages = messages;
        this.snapContainer = snapContainer;
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
        holder.bind(messages.get(position), context, snapContainer);
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

        public void bind(Message message, Context context, ImageView snapContainer) {
            this.contentPic.setVisibility(View.GONE);

            // Attempted to combine consecutive messages but failed:

            /*this.itemView.setVisibility(View.VISIBLE);
            this.contentPics.removeAllViews();
            if(!viewHolders.contains(this))
                viewHolders.add(this);
            if(position > 0){
                int currIndex = viewHolders.indexOf(this);
                ViewHolder previous = viewHolders.get(currIndex -1);
                while(previous.itemView.getVisibility() == View.GONE && currIndex > 0)
                    previous = viewHolders.get(--currIndex);
                Message content = previous.getMessage();
                if(content.getFrom().equals(ParseUser.getCurrentUser())){
                    previous.text.setText("" + previous.text.getText().toString() + "\n" + message.getText());
                    newPic(previous.contentPics, message);
                    this.itemView.setVisibility(View.GONE);
                    return;
                }
            }*/
            ParseUser from = message.getFrom();
            try {
                this.name.setText("" + (from.fetchIfNeeded().getObjectId().equals(ParseUser.getCurrentUser().getObjectId()) ? "Me" : from.fetchIfNeeded().getUsername()));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            // Only show text attachment if not a Snap pic
            this.text.setVisibility(View.GONE);
            if (message.getText() != null && !message.getText().equals("") && !message.isSnap()) {
                this.text.setVisibility(View.VISIBLE);
                this.text.setText("" + message.getText());
            }


            ParseFile pic = message.getPic();
            // Only show pic attachment if not a Snap pic
            if (pic != null && !message.isSnap()) {
                this.contentPic.setVisibility(View.VISIBLE);
                Glide.with(context).load(pic.getUrl()).into(this.contentPic);
            }


            // If this is a Snap picture...
            if (message.isSnap()) {
                // Show the snap layout
                this.openSnap.setVisibility(View.VISIBLE);

                // If this User sent it, they can't open the Snap
                if (message.getFrom().getObjectId().equals(ParseUser.getCurrentUser().getObjectId())) {
                    this.snapText.setText("Delivered");
                } else {
                    // If this User received the Snap picture, if they click the layout...
                    this.openSnap.setOnClickListener(unusedView -> {
                        // Set up the picture as an ImageView that lives in ChatFragment
                        // This ImageView is placed above the who Chat with a friend with visibility Gone,
                        // so we change it to visible and it will cover the screen
                        ParseFile picture = message.getPic();
                        Glide.with(context).load(picture.getUrl()).into(snapContainer);
                        snapContainer.setVisibility(View.VISIBLE);

                        // If we click this new Imageview...
                        snapContainer.setOnClickListener(unusedView1 -> {
                            // Get rid of the Snap picture and hide the big ImageView
                            snapContainer.setImageResource(0);
                            snapContainer.setVisibility(View.GONE);


                            Drawable buttonDrawable = this.snapIndicator.getBackground();
                            buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                            // Indicate to User that we have opened the Snap by changing indicator color
                            DrawableCompat.setTint(buttonDrawable, Color.WHITE);
                            this.snapIndicator.setBackground(buttonDrawable);

                            // Delete Snap
                            message.deleteInBackground();
                            this.snapText.setText("Opened");

                            // No more interaction with this empty Snap
                            snapContainer.setOnClickListener(null);
                            this.openSnap.setOnClickListener(null);
                        });
                    });
                }
            }

            this.message = message;
        }
        /*private void newPic(ViewGroup viewGroup, Message message){
            ParseFile pic = message.getPic();
            if(pic != null) {
                ImageView newPic = new ImageView(viewGroup.getContext());
                Glide.with(viewGroup.getContext()).load(pic.getUrl()).into(newPic);
                newPic.getLayoutParams().height = (int) convertPixelsToDp(80, itemView.getContext());
                newPic.getLayoutParams().width = (int) convertPixelsToDp(80, itemView.getContext());
                viewGroup.addView(newPic);
            }
        }

        public Message getMessage() {
            return message;
        }*/

        public static float convertPixelsToDp(float px, Context context) {
            return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        }
    }
}
