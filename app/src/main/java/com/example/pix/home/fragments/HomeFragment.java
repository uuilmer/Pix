package com.example.pix.home.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import com.example.pix.R;
import com.example.pix.home.adapters.PagerAdapter;
import com.example.pix.home.adapters.SearchAdapter;
import com.example.pix.login.LoginActivity;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

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

        SpotifyAppRemote mSpotifyAppRemote = LoginActivity.getmSpotifyAppRemote();

        // When we click on the edit profile Toolbar button, replace this screen with a ProfileFragment
        (view.findViewById(R.id.home_profile_icon)).setOnClickListener(view12 -> {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                    .addToBackStack("stack")
                    .replace(R.id.home_profile, new ProfileFragment())
                    .commit();
        });

        PagerTabStrip pagerTabStrip = view.findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(false);

        ImageView svChats = view.findViewById(R.id.home_search_user);

        // ALMOST ALL OF THIS CODE IS REPEATED FROM COMPOSE, SO LOOK TO REDUCE CODE REPETITION
        svChats.setOnClickListener(view1 -> {
            LinearLayout container = getActivity().findViewById(R.id.home_container);
            LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popup = layoutInflater.inflate(R.layout.popup_search, null);

            ImageView close = popup.findViewById(R.id.popup_close);
            SearchView search = popup.findViewById(R.id.popup_searchView);
            RecyclerView rvResults = popup.findViewById(R.id.popup_rv);

            PopupWindow popupWindow = new PopupWindow(popup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.showAtLocation(container, Gravity.CENTER, 0, 0);
            search.requestFocus();
            close.setOnClickListener(view2 -> popupWindow.dismiss());

            List<ParseUser> results = new ArrayList<>();
            SearchAdapter adapter = new SearchAdapter(getContext(), results);
            rvResults.setAdapter(adapter);
            rvResults.setLayoutManager(new LinearLayoutManager(getContext()));

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


        ImageView profile = view.findViewById(R.id.home_profile_icon);
        TextView pix = view.findViewById(R.id.tv_score);
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
                    profile.setColorFilter(Color.argb(MAX_ALPHA, scaled, scaled, scaled));
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
