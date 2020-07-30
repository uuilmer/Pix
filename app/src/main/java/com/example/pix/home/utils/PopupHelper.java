package com.example.pix.home.utils;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pix.R;
import com.example.pix.home.adapters.SearchAdapter;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class PopupHelper {

    public static void createPopup(Activity activity, Context context, boolean newPic) {
        LinearLayout container = activity.findViewById(R.id.home_container);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popup = layoutInflater.inflate(R.layout.popup_search, null);

        ImageView close = popup.findViewById(R.id.popup_close);
        SearchView search = popup.findViewById(R.id.popup_searchView);
        RecyclerView rvResults = popup.findViewById(R.id.popup_rv);

        // Position popup
        PopupWindow popupWindow = new PopupWindow(popup, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.showAtLocation(container, Gravity.CENTER, 0, 0);
        search.requestFocus();
        close.setOnClickListener(unusedView -> popupWindow.dismiss());

        List<ParseUser> results = new ArrayList<>();
        // Tell the adapter whether this adapter will need to handle saved pics(New Snap from ComposeFragment)
        SearchAdapter adapter;
        if (newPic) {
            adapter = new SearchAdapter(true, context, results);
        } else {
            adapter = new SearchAdapter(context, results);
        }
        rvResults.setAdapter(adapter);
        rvResults.setLayoutManager(new LinearLayoutManager(context));

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
                        Toast.makeText(context, "Error searching!", Toast.LENGTH_SHORT).show();
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
    }
}
