package com.example.pix.home.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.chat.activities.FriendActivity;
import com.example.pix.home.fragments.HomeFragment;
import com.example.pix.home.models.Message;
import com.parse.ParseException;
import com.example.pix.home.models.Message;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class HomeActivity extends AppCompatActivity {

    public static final int RESULT_LOAD_IMG = 100;
    public static HomeFragment homeFragment;
    public static final String HOME_FRAGMENT_TAG = "home";
    public static final String APP_NAME = "Pix";
    public static final String CHANNEL_ID = "pix";
    public static final String KEY_TEXT_REPLY = "key_text_reply";
    public static final int REPLY_CODE = 001;
    private Message currentLatestMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        homeFragment = new HomeFragment();

        // The home activity first contains the HomeFragment, which can potentially replace itself with a ProfileFragment
        getSupportFragmentManager().beginTransaction().replace(R.id.home_profile, homeFragment, HOME_FRAGMENT_TAG).commit();

        createNotificationChannel();

        currentLatestMessage = Message.getNewestMessage();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Keep checking for new messages in the background, even when we close the app
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Message latestMessage = Message.getNewestMessage();
                if (!latestMessage.getObjectId().equals(currentLatestMessage.getObjectId())) {
                    NotificationCompat.Builder builder = null;
                    try {
                        // If we click on the notification, retrieve this message's Chat and bring it to the FriendActivity
                        Intent i = new Intent(HomeActivity.this, FriendActivity.class);
                        i.putExtra("chat", latestMessage.getChat().fetch().getObjectId());

                        // Wrap our above intent into a PendingIntent, which is what the notification can trigger
                        PendingIntent intent = PendingIntent.getActivity(HomeActivity.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

                        // This is what we can access from the FriendActivity that we can use there to retrieve the reply
                        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                                .setLabel("Reply")
                                .build();

                        // We need to define this action for when they decide to enter text as opposed to click on notification
                        NotificationCompat.Action replyAction =
                                new NotificationCompat.Action.Builder(
                                        android.R.drawable.ic_dialog_info,
                                        "Reply", intent)
                                        .addRemoteInput(remoteInput)
                                        .build();

                        builder = new NotificationCompat.Builder(HomeActivity.this, CHANNEL_ID)
                                .setSmallIcon(R.drawable.spotify)
                                .setContentTitle(latestMessage.getFrom().fetch().getUsername())
                                .setContentText(latestMessage.getText())
                                // When they click:
                                .setContentIntent(intent)
                                // When they enter the reply instead:
                                .addAction(replyAction);

                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(HomeActivity.this, "Damn", Toast.LENGTH_SHORT).show();
                    }
                    mNotificationManager.notify(REPLY_CODE, builder.build());
                    currentLatestMessage = latestMessage;
                }
            }
        }, 0, 5000);

    }

    // Code provided by Google, necessary to be able to create Notifications
    // Link: https://developer.android.com/training/notify-user/build-notification
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = APP_NAME;
            String description = "New Message";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}