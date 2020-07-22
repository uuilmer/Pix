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
import com.example.pix.chat.ChatActivity;
import com.example.pix.chat.ChatFragment;
import com.example.pix.home.activities.HomeActivity;
import com.example.pix.home.adapters.SearchAdapter;
import com.example.pix.home.utils.CameraPreview;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ComposeFragment extends Fragment {

    private ChatActivity mActivity;
    public static ParseFile image;
    private final int[] currCamera = new int[1];
    private final Camera[] c = new Camera[1];
    private final CameraPreview[] preview = new CameraPreview[1];
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
                    if (currCamera[0] == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        currCamera[0] = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                    else {
                        currCamera[0] = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    }
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
            image = new ParseFile(bytes);
            if (getActivity() instanceof HomeActivity) {
                LinearLayout container = getActivity().findViewById(R.id.home_container);
                LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View popup = layoutInflater.inflate(R.layout.popup_search, null);

                ImageView close = popup.findViewById(R.id.popup_close);
                SearchView search = popup.findViewById(R.id.popup_searchView);
                RecyclerView rvResults = popup.findViewById(R.id.popup_rv);

                // Position popup
                PopupWindow popupWindow = new PopupWindow(popup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.showAtLocation(container, Gravity.CENTER, 0, 0);
                search.requestFocus();
                close.setOnClickListener(view2 -> popupWindow.dismiss());

                List<ParseUser> results = new ArrayList<>();
                // Tell the adapter whether this adapter will need to handle saved pics(New Snap from ComposeFragment)
                SearchAdapter adapter = new SearchAdapter(true, getContext(), results);
                rvResults.setAdapter(adapter);
                rvResults.setLayoutManager(new LinearLayoutManager(getContext()));

                // When we search, query all users
                // Once we select a User, the Adapter will handle finding the current User's chat with them,
                // and if it doesn't exist it will create a new Chat
                search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        ParseQuery<ParseUser> q = ParseQuery.getQuery(ParseUser.class);
                        q.whereStartsWith("username", s);
                        q.findInBackground((objects, e) -> {
                            if (e != null) {
                                Toast.makeText(getContext(), "Error searching!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            results.clear();
                            results.addAll(objects);
                            adapter.notifyDataSetChanged();
                        });
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        return false;
                    }
                });
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
        if (context instanceof ChatActivity) {
            mActivity = (ChatActivity) context;
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