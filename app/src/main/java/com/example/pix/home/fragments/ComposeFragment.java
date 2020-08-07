package com.example.pix.home.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.chat.fragments.ChatFragment;
import com.example.pix.chat.activities.FriendActivity;
import com.example.pix.home.activities.HomeActivity;
import com.example.pix.home.utils.CameraPreview;
import com.example.pix.home.utils.PopupHelper;
import com.parse.ParseFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class ComposeFragment extends Fragment {

    private static final int MAX_CLICK_DURATION = 200;
    public static ParseFile contentToSave;
    private int currCamera;
    private Camera camera;
    private CameraPreview preview;
    private FrameLayout frameLayout;
    private Button take;
    private MediaRecorder recorder;
    private long timeclicked;
    private Timer startRecord;;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkCameraHardware(getContext())) {
            Toast.makeText(getContext(), "No camera available!", Toast.LENGTH_SHORT).show();
            System.exit(0);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        frameLayout = view.findViewById(R.id.compose_camera);
        take = view.findViewById(R.id.compose_take);

        setup();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    private void setup() {
        currCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
        camera = Camera.open(currCamera);

        // Set as Portrait
        camera.setDisplayOrientation(90);
        // I needed to change the Camera itself's rotation, because it was displaying correctly,
        // but the pictures it was taking were rotated
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRotation(270);
        camera.setParameters(parameters);

        // Create the preview and set as the FrameLayout
        preview = new CameraPreview(getContext(), camera);
        frameLayout.addView(preview);

        // When we double-tap, create a new preview with the only difference being the Camera source, and set it as our FrameLayout's only child
        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            // We will use this Object to recognize double-taps
            GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    camera.stopPreview();
                    camera.release();
                    if (currCamera == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        currCamera = Camera.CameraInfo.CAMERA_FACING_BACK;
                    } else {
                        currCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    }
                    camera = Camera.open(currCamera);
                    camera.setDisplayOrientation(90);
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setRotation(270);
                    camera.setParameters(parameters);
                    preview = new CameraPreview(getContext(), camera);
                    frameLayout.removeAllViews();
                    frameLayout.addView(preview);
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View unusedView, MotionEvent motionEvent) {
                // Use the Object to handle double-taps
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        // When we are done taking a picture, go to a new ChatActivity with this new Image ParseFile
        Camera.PictureCallback callback = (bytes, camera) -> {
            contentToSave = new ParseFile(bytes);
            // Create popup to select who to send to
            if (getActivity() instanceof HomeActivity) {
                PopupHelper.createPopup(getActivity(), getContext(), true);
            } else {
                // Case where this ComposeFragment was called from ChatActivity so we know who to send it to
                // thus no need for popup
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.friend_container, new ChatFragment(contentToSave))
                        .commit();
            }
        };

        // Create a File in our external storage(We can't write into local storage)
        File path = Environment.getExternalStorageDirectory();
        File video = new File(path, "/" + "video.mp4");

        Animation animation = new AlphaAnimation(1, 0);
        animation.setDuration(500);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);

        // When we touch the take snap content icon...
        take.setOnTouchListener((unusedView, motionEvent) -> {
            // If this is us clicking down...
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                // Note the current time
                timeclicked = System.currentTimeMillis();
                take.startAnimation(animation);
                // In 200 milliseconds(MAX_CLICK_DURATION) start recording...
                startRecord = new Timer();
                startRecord.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        recorder = new MediaRecorder();
                        camera.lock();
                        camera.unlock();
                        // Set the camera, rotation, video and audio source, and our new output File
                        recorder.setCamera(camera);
                        recorder.setOrientationHint(270);
                        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
                        recorder.setOutputFile(video);
                        try {
                            recorder.prepare();
                            recorder.start();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error starting Video", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, MAX_CLICK_DURATION);
            } else if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Stop blinking the Take button
                take.clearAnimation();
                // If it hasn't been 200 millis yet, and we just let go of the take button, it was a single click..
                if (System.currentTimeMillis() - timeclicked < MAX_CLICK_DURATION) {
                    // We cancel the video, take a pic and call our callback
                    startRecord.cancel();
                    camera.takePicture(() -> {
                    }, null, callback);
                    return true;
                }
                // If we let go of the take button sometime after 200 millis, the video must have started...
                // Stop it, save it as ParseFile..
                recorder.stop();
                contentToSave = new ParseFile(video);
                recorder.release();

                // If we are in HomeActivity, we need to figure out who to send the Snap to, so create a popup
                if (getActivity() instanceof HomeActivity) {
                    PopupHelper.createPopup(getActivity(), getContext(), true);
                } else {
                    // Case where this ComposeFragment was called from ChatActivity so we know who to send it to
                    // thus no need for popup
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.friend_container, new ChatFragment(contentToSave))
                            .commit();
                }
                return true;
            }
            return false;
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onResume() {
        super.onResume();
        /*  When we resume the ComposeFragment that lives in Home, we may have used a ComposeFragment in ChatFragment
            to send a Snap that caused this (HomeFragment's ComposeFragment), which was still alive in the background,
            to have a reference to Camera which was invalidated the moment we opened it in ChatFragment. To fix this
            we need to re-setup the Camera whenever a ComposeFragment is resumed. */
        setup();

        // If we came back to a Compose Fragment and the Popup is active, close it
        if (PopupHelper.isActive()) PopupHelper.closePopup();
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}