package com.example.pix.home.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.chat.utils.FetchPath;
import com.example.pix.home.activities.HomeActivity;
import com.example.pix.login.LoginActivity;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;

import static android.app.Activity.RESULT_OK;
import static com.example.pix.home.activities.HomeActivity.RESULT_LOAD_IMG;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    ImageView profile;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toast.makeText(getContext(), "lool", Toast.LENGTH_SHORT).show();

        // Clicking back ends this fragment
        // Will need to figure out how to end with animation
        (view.findViewById(R.id.profile_back)).setOnClickListener(unusedView -> getParentFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                .replace(R.id.home_profile, HomeActivity.homeFragment)
                .commit());

        (view.findViewById(R.id.profile_signout)).setOnClickListener(unusedView -> {
            ParseUser.logOut();
            Intent i = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivity(i);
            getActivity().finish();
        });
        LinearLayout selectNewPic = view.findViewById(R.id.profile_change_pic);
        // When we click the plus, go to add a pic
        selectNewPic.setOnClickListener(unusedView -> {
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, RESULT_LOAD_IMG);
        });

        profile = view.findViewById(R.id.profile_pic);

        // Load profile pic
        Glide.with(getContext())
                .load(ParseUser.getCurrentUser()
                        .getParseFile("profile").getUrl())
                .circleCrop()
                .into(profile);

        // If we press "Enter" in the username EditText, update the username
        EditText name = view.findViewById(R.id.profile_name);
        name.setText("" + ParseUser.getCurrentUser().getUsername());
        name.setOnKeyListener((unusedView, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_ENTER) {
                // Create new message
                ParseUser.getCurrentUser().setUsername(name.getText().toString());
                ParseUser.getCurrentUser().saveInBackground(e -> {
                    if (e != null) {
                        Toast.makeText(getContext(), "Username is taken", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Toast.makeText(getContext(), "Updated name!", Toast.LENGTH_SHORT).show();
                });
                return true;
            }
            return false;
        });

        // Set User's number of Pix
        TextView pix = view.findViewById(R.id.profile_pix);
        pix.setText("" + ParseUser.getCurrentUser().getInt("pix"));
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_LOAD_IMG) {
                final Uri imageUri = data.getData();
                // When we return from choosing a picture, save it as a ParseFile THEN update the current ImageView
                ParseFile toSave = new ParseFile(new File(FetchPath.getPath(getContext(), imageUri)));

                ParseUser.getCurrentUser().put("profile", toSave);
                ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Glide.with(getContext())
                                .load(toSave.getUrl())
                                .circleCrop()
                                .into(profile);
                    }
                });
            }
        }
    }
}