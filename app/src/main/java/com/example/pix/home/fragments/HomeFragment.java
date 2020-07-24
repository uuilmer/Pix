package com.example.pix.home.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import com.example.pix.R;
import com.example.pix.home.adapters.PagerAdapter;
import com.example.pix.login.LoginActivity;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    SpotifyAppRemote mSpotifyAppRemote;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSpotifyAppRemote = LoginActivity.getmSpotifyAppRemote();

        // When we click on the edit profile Toolbar button, replace this screen with a ProfileFragment
        (view.findViewById(R.id.home_profile_icon)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                        .addToBackStack("stack")
                        .replace(R.id.home_profile, new ProfileFragment())
                        .commit();
            }
        });

        PagerTabStrip pagerTabStrip = view.findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(false);

        SearchView svChats = view.findViewById(R.id.search_user);

        List<Fragment> fragments = new ArrayList<>();
        List<String> fragmentNames = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        fragments.add(new ChatsFragment());
        fragmentNames.add("Chats");
        colors.add(Color.BLUE);
        fragments.add(new ComposeContainerFragment());
        fragmentNames.add("Compose");
        colors.add(Color.GREEN);

        ImageView profile = view.findViewById(R.id.home_profile_icon);
        TextView pix = view.findViewById(R.id.tv_score);


        ViewPager viewPager = view.findViewById(R.id.vpPager);
        // Link the colors to each page
        LinearLayout header = view.findViewById(R.id.header);
        Drawable background = header.getBackground();
        background.setAlpha(0);
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
                    int scaled = (int) (positionOffset * 255);
                    background.setAlpha(255 - scaled);
                    profile.setColorFilter(Color.argb(255, scaled, scaled, scaled));
                    pix.setTextColor(Color.argb(255, scaled, scaled, scaled));
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
}
