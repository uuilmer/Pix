package com.example.pix.home.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.chat.utils.CameraPermissionHelper;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedFace;
import com.google.ar.core.Config;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.EnumSet;

public class ComposeFragment extends Fragment {

    private boolean mUserRequestedInstall = true;
    private Session mSession;

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        maybeEnableArButton();
    }

    void maybeEnableArButton() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(getContext());
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(() -> maybeEnableArButton(), 200);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // ARCore requires camera permission to operate.
        if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
            CameraPermissionHelper.requestCameraPermission(getActivity());
            return;
        }

        // Make sure Google Play Services for AR is installed and up to date.
        try {
            if (mSession == null) {
                switch (ArCoreApk.getInstance().requestInstall(getActivity(), mUserRequestedInstall)) {
                    case INSTALLED:
                        // Success, create the AR session.
                        mSession = new Session(getContext());
                        break;
                    case INSTALL_REQUESTED:
                        // Ensures next invocation of requestInstall() will either return
                        // INSTALLED or throw an exception.
                        mUserRequestedInstall = false;
                        return;
                }
            }
        } catch (UnavailableUserDeclinedInstallationException | UnavailableDeviceNotCompatibleException e) {
            // Display an appropriate message to the user and return gracefully.
            Toast.makeText(getContext(), "TODO: handle exception " + e, Toast.LENGTH_LONG)
                    .show();
            return;
        } catch (UnavailableArcoreNotInstalledException
                | UnavailableApkTooOldException
                | UnavailableSdkTooOldException e) {
            e.printStackTrace();
        }

        try {
            mSession = new Session(getContext(), EnumSet.of(Session.Feature.FRONT_CAMERA));
            Config config = new Config(mSession);
            config.setAugmentedFaceMode(
                    Config.AugmentedFaceMode.MESH3D);
            mSession.configure(config);

            Collection<AugmentedFace> faces = mSession.getAllTrackables(AugmentedFace.class);

            for (AugmentedFace face : faces) {
                if (face.getTrackingState() == TrackingState.TRACKING) {
                    // UVs and indices can be cached as they do not change during the session.
                    FloatBuffer uvs = face.getMeshTextureCoordinates();
                    ShortBuffer indices = face.getMeshTriangleIndices();
                    // Center and region poses, mesh vertices, and normals are updated each frame.
                    Pose facePose = face.getCenterPose();
                    FloatBuffer faceVertices = face.getMeshVertices();
                    FloatBuffer faceNormals = face.getMeshNormals();
                    // Render the face using these values with OpenGL
                }
            }

        } catch (UnavailableArcoreNotInstalledException
                | UnavailableApkTooOldException
                | UnavailableSdkTooOldException
                | UnavailableDeviceNotCompatibleException e) {
            e.printStackTrace();
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        if (!CameraPermissionHelper.hasCameraPermission(getActivity())) {
            Toast.makeText(getContext(), "Camera permission is needed to run this application", Toast.LENGTH_LONG)
                    .show();
            if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(getActivity())) {
                // Permission denied with checking "Do not ask again".
                CameraPermissionHelper.launchPermissionSettings(getActivity());
            }
            getActivity().finish();
        }
    }
}