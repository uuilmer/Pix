package com.example.pix.home.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import android.graphics.Color;
import android.os.Bundle;

import com.example.pix.R;
import com.example.pix.home.adapters.PagerAdapter;
import com.example.pix.home.fragments.ChatsFragment;
import com.example.pix.home.fragments.ComposeFragment;
import com.example.pix.home.fragments.ProfileFragment;
import com.example.pix.login.LoginActivity;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    SpotifyAppRemote mSpotifyAppRemote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mSpotifyAppRemote = LoginActivity.getmSpotifyAppRemote();

        // Setup the PagerView and the colors we want for each tab
        PagerTabStrip pagerTabStrip = findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(false);

        List<Fragment> fragments = new ArrayList<>();
        List<String> fragmentNames = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        fragments.add(new ChatsFragment());
        fragmentNames.add("Chats");
        colors.add(Color.BLUE);
        fragments.add(new ComposeFragment());
        fragmentNames.add("Compose");
        colors.add(Color.GREEN);
        fragments.add(new ProfileFragment());
        fragmentNames.add("Profile");
        colors.add(Color.YELLOW);


        ViewPager viewPager = findViewById(R.id.vpPager);
        // Link the colors to each page
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pagerTabStrip.setTabIndicatorColor(colors.get(position));
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments, fragmentNames);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

//        LinearLayout homeContainer = findViewById(R.id.home_container);

//        View.OnTouchListener onTouchListener = (view, motionEvent) -> {
//            switch(motionEvent.getAction())
//            {
//                case MotionEvent.ACTION_DOWN:
//                    x1 = motionEvent.getX();
//                    return true;
//                case MotionEvent.ACTION_UP:
//                    x2 = motionEvent.getX();
//                    float deltaX = x2 - x1;
//                    if (deltaX > 0)
//                    {
//                        Toast.makeText(HomeActivity.this, "left2right swipe", Toast.LENGTH_SHORT).show ();
//                    }
//                    else
//                    {
//                        Toast.makeText(HomeActivity.this, "right2left swipe", Toast.LENGTH_SHORT).show ();
//                    }
//                    return true;
//            }
//            return super.onTouchEvent(motionEvent);
//        };
//
//        homeContainer.setOnTouchListener(onTouchListener);
    }
}