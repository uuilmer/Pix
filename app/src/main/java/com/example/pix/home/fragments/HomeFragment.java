package com.example.pix.home.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.Explode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.home.adapters.PagerAdapter;
import com.example.pix.home.models.Like;
import com.example.pix.home.utils.PopupHelper;
import com.example.pix.login.LoginActivity;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.pix.home.models.Chat.USER_PROFILE_CODE;

public class HomeFragment extends Fragment {

    public static final int MAX_ALPHA = 255;

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView profile = view.findViewById(R.id.home_profile_icon);
        TextView pix = view.findViewById(R.id.tv_score);

        // Set the current Pix Score
        pix.setText("" + Like.getPix(ParseUser.getCurrentUser()));

        // Set the User profile picture
        ParseFile image = ParseUser.getCurrentUser().getParseFile(USER_PROFILE_CODE);
        if (image != null) {
            Glide.with(view).load(image.getUrl()).circleCrop().into(profile);
        }

        ProfileFragment profileFragment = new ProfileFragment(ParseUser.getCurrentUser());

        profileFragment.setSharedElementEnterTransition(new Explode());
        profileFragment.setEnterTransition(new Explode());
        setExitTransition(new Explode());
        profileFragment.setSharedElementReturnTransition(new Explode());

        // When we click on the edit profile Toolbar button, replace this screen with a ProfileFragment
        profile.setOnClickListener(unusedView -> {
            // If we have previously created a ProfileFragment, get it..
            if (getParentFragmentManager().getFragments().contains(profileFragment)) {
                getParentFragmentManager()
                        .beginTransaction()
                        // ..show it..
                        .show(profileFragment)
                        // ..and hide this HomeFragment
                        .hide(this)
                        .commit();
                return;
            }
            // If we haven't, add it
            getParentFragmentManager()
                    .beginTransaction()
                    .add(R.id.home_profile, profileFragment)
                    .hide(this)
                    .commit();
        });

        PagerTabStrip pagerTabStrip = view.findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(false);

        ImageView svChats = view.findViewById(R.id.home_search_user);

        // Create popup to search for friends
        svChats.setOnClickListener(unusedView -> {
            PopupHelper.createPopup(getActivity(), getContext(), false);

        });

        List<Fragment> fragments = new ArrayList<>();
        List<String> fragmentNames = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        fragments.add(new ChatsFragment());
        fragmentNames.add("Chats");
        colors.add(Color.BLUE);
        fragments.add(new ComposeFragment());
        fragmentNames.add("Compose");
        colors.add(Color.GREEN);


        ViewPager viewPager = view.findViewById(R.id.vpPager);
        // Link the colors to each page
        LinearLayout header = view.findViewById(R.id.header);
        Drawable headerBackground = header.getBackground();
        headerBackground.setAlpha(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pagerTabStrip.setTabIndicatorColor(colors.get(position));
                // Only when not 0 because for some reason, when we are exactly at ComposeFragment(Index 1),
                // The offset jumps to 0 from 0.9999
                if (positionOffset != 0) {
                    /*  When we scroll the PagerView, use the offset(The fraction of what index page we are on:
                            Example: ChatsFragment is 0, ComposeFragment is 1, and in between them is 0.5
                        We scale this index up to 255 and assign it to our header's background and the opposite
                        as the icon tints. */
                    int scaled = (int) (positionOffset * MAX_ALPHA);
                    headerBackground.setAlpha(MAX_ALPHA - scaled);
                    profile.setAlpha((float) (1 - (positionOffset * 0.8)));
                    svChats.setColorFilter(Color.argb(MAX_ALPHA, scaled, scaled, scaled));
                    pix.setTextColor(Color.argb(MAX_ALPHA, scaled, scaled, scaled));
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });
        PagerAdapter pagerAdapter = new PagerAdapter(getChildFragmentManager(), fragments, fragmentNames);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
