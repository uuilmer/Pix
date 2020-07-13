package com.example.pix.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.pix.R;
import com.example.pix.home.adapters.PagerAdapter;
import com.example.pix.home.fragments.ChatsFragment;
import com.example.pix.home.fragments.ComposeFragment;
import com.example.pix.home.fragments.ProfileFragment;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    float x1,x2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        List<Fragment> fragments = new ArrayList<>();
        fragments.add(new ChatsFragment());
        fragments.add(new ComposeFragment());
        fragments.add(new ProfileFragment());

        ViewPager viewPager = findViewById(R.id.vpPager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), fragments);

        viewPager.setAdapter(pagerAdapter);

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