package com.example.pix.home.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.chat.fragments.ChatFragment;
import com.example.pix.chat.activities.FriendActivity;
import com.example.pix.home.activities.HomeActivity;
import com.example.pix.home.adapters.SearchAdapter;
import com.example.pix.home.utils.CameraPreview;
import com.example.pix.home.utils.PopupHelper;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ComposeFragment extends Fragment {

    private FriendActivity mActivity;
    public static ParseFile image;
    private int currCamera;
    private Camera camera;
    private CameraPreview preview;
    private FrameLayout frameLayout;
    private Button take;

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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        frameLayout = view.findViewById(R.id.compose_camera);
        take = view.findViewById(R.id.compose_take);

        setup();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setup() {
        currCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;
        camera = Camera.open(currCamera);

        // Set as Portrait
        camera.setDisplayOrientation(90);

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
            image = new ParseFile(bytes);
            // Create popup to select who to send to
            if (getActivity() instanceof HomeActivity) {
                PopupHelper.createPopup(getActivity(), getContext(), true);
            } else {
                // Case where this ComposeFragment was called from ChatActivity so we know who to send it to
                // thus no need for popup
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.friend_container, new ChatFragment(image))
                        .commit();
            }
        };

        // When we take a pic, do the above ^
        take.setOnClickListener(view1 -> camera.takePicture(() -> {

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
        if (context instanceof FriendActivity) {
            mActivity = (FriendActivity) context;
        }
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