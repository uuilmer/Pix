package com.example.pix.home.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.chat.ChatActivity;
import com.example.pix.chat.ChatFragment;
import com.example.pix.home.utils.CameraPreview;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ComposeFragment extends Fragment {

    ChatActivity mActivity;

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

    final int[] currCamera = new int[1];
    final Camera[] c = new Camera[1];
    final CameraPreview[] preview = new CameraPreview[1];
    FrameLayout frameLayout;
    Button take;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        frameLayout = view.findViewById(R.id.compose_camera);
        take = view.findViewById(R.id.compose_take);

        setup();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setup(){
        currCamera[0] = Camera.CameraInfo.CAMERA_FACING_FRONT;
        c[0] = Camera.open(currCamera[0]);

        // Set as Portrait
        c[0].setDisplayOrientation(90);

        // Create the preview and set as the FrameLayout
        preview[0] = new CameraPreview(getContext(), c[0]);
        frameLayout.addView(preview[0]);

        // When we double-tap, create a new preview with the only difference being the Camera source, and set it as our FrameLayout's only child
        frameLayout.setOnTouchListener(new View.OnTouchListener() {
            // We will use this Object to recognize double-taps
            GestureDetector gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    c[0].stopPreview();
                    c[0].release();
                    if (currCamera[0] == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        currCamera[0] = Camera.CameraInfo.CAMERA_FACING_BACK;
                    else
                        currCamera[0] = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    c[0] = Camera.open(currCamera[0]);
                    c[0].setDisplayOrientation(90);
                    preview[0] = new CameraPreview(getContext(), c[0]);
                    frameLayout.removeAllViews();
                    frameLayout.addView(preview[0]);
                    return super.onDoubleTap(e);
                }
            });

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Use the Object to handle double-taps
                gestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        // When we are done taking a picture, go to a new ChatActivity with this new Image ParseFile
        Camera.PictureCallback callback = (bytes, camera) -> {
            ParseFile image = new ParseFile(bytes);
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.friend_container, new ChatFragment(image))
                    .commit();
        };

        // When we take a pic, do the above ^
        take.setOnClickListener(view1 -> c[0].takePicture(() -> {

        }, null, callback));
    }

    @Override
    public void onResume() {
        super.onResume();
        /*  When we resume the ComposeFragment that lives in Home, we may have used a ComposeFragment in ChatFragment
            to send a Snap that caused this (HomeFragment's ComposeFragment), which was still alive in the background,
            to have a reference to Camera which was invalidated the moment we opened it in ChatFragment. To fix this
            we need to re-setup the Camera whenever a ComposeFragment is resumed. */
        setup();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ChatActivity)
            mActivity = (ChatActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }
}