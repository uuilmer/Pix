package com.example.pix.home.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.Explode;
import android.transition.Fade;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerTabStrip;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.pix.R;
import com.example.pix.home.adapters.PagerAdapter;
import com.example.pix.home.adapters.SearchAdapter;
import com.example.pix.login.LoginActivity;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import java.util.ArrayList;
import java.util.List;

import static com.example.pix.home.models.Chat.USER_PROFILE_CODE;

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

        ImageView profile = view.findViewById(R.id.profile_pic);

        // Set the User profile picture
        ParseFile image = ParseUser.getCurrentUser().getParseFile(USER_PROFILE_CODE);
        if (image != null) {
            Glide.with(getContext()).load(image).into(profile);
        }

        ProfileFragment profileFragment = new ProfileFragment(ParseUser.getCurrentUser());


        profileFragment.setSharedElementEnterTransition(new Explode());
        profileFragment.setEnterTransition(new Explode());
        setExitTransition(new Explode());
        profileFragment.setSharedElementReturnTransition(new Explode());

        // When we click on the edit profile Toolbar button, replace this screen with a ProfileFragment
        profile.setOnClickListener(unusedView -> {
            getActivity()
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .addSharedElement(profile, "profile")
                    .addToBackStack(null)
                    .replace(R.id.home_profile, profileFragment)
                    .commit();
        });

        PagerTabStrip pagerTabStrip = view.findViewById(R.id.pager_header);
        pagerTabStrip.setDrawFullUnderline(false);

        Button svChats = view.findViewById(R.id.search_user);

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


        ViewPager viewPager = view.findViewById(R.id.vpPager);
        // Link the colors to each page
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                pagerTabStrip.setTabIndicatorColor(colors.get(position));
                System.out.println(positionOffset);
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
