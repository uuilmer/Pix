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
            try {
                finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final int[] currCamera = {Camera.CameraInfo.CAMERA_FACING_FRONT};
        final Camera[] c = {Camera.open(currCamera[0])};

        // Set as Portrait
        c[0].setDisplayOrientation(90);

        // Create the preview and set as the FrameLayout
        final CameraPreview[] preview = {new CameraPreview(getContext(), c[0])};
        FrameLayout frameLayout = view.findViewById(R.id.compose_camera);
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
            getParentFragmentManager().beginTransaction().replace(R.id.friend_container, new ChatFragment(image)).commit();
        };

        // When we take a pic, do the above ^
        (view.findViewById(R.id.compose_take)).setOnClickListener(view1 -> c[0].takePicture(() -> {

        }, callback, callback));
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