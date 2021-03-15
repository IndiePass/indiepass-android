// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.indieweb.micropub.source;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.PostType;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PostListFilterActivity extends AppCompatActivity {

    String defaultPostType;
    String selectedPostType;
    PostType defaultPostTypeItem;
    Spinner postTypesSpinner;
    Spinner postLimitSpinner;
    Button filterSourcePostListButton;
    ScrollView layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_source_post_list_filter);
        super.onCreate(savedInstanceState);

        layout = findViewById(R.id.source_post_filter_root);

        // Get default user.
        User user = new Accounts(this).getDefaultUser();

        // Get post limit spinner.
        postLimitSpinner = findViewById(R.id.postLimit);
        String limit = Preferences.getPreference(getApplicationContext(), "source_post_list_filter_post_limit", "10");
        int selectedItem = (Integer.parseInt(limit) / 10) - 1;
        postLimitSpinner.setSelection(selectedItem);

        // Set post type spinner values.
        defaultPostType = Preferences.getPreference(getApplicationContext(), "source_post_list_filter_post_type", "all_source_post_types");
        ArrayList<PostType> postTypeList = new ArrayList<>();
        postTypeList.add(new PostType("all_source_post_types", getString(R.string.post_types_all)));
        postTypesSpinner = findViewById(R.id.postTypes);
        String postTypes = user.getPostTypes();
        if (postTypes != null && postTypes.length() > 0) {
            try {
                JSONObject object;
                JSONArray itemList = new JSONArray(postTypes);

                for (int i = 0; i < itemList.length(); i++) {
                    object = itemList.getJSONObject(i);
                    PostType item = new PostType(object.getString("type"), object.getString("name"));
                    postTypeList.add(item);

                    if (defaultPostType.equals(object.getString("type"))) {
                        defaultPostTypeItem = item;
                    }
                }

            }
            catch (JSONException e) {
                Snackbar.make(layout, String.format(getString(R.string.post_types_parse_error), e.getMessage()), Snackbar.LENGTH_LONG).show();
            }
        }

        ArrayAdapter<PostType> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, postTypeList);
        postTypesSpinner.setAdapter(adapter);
        if (defaultPostTypeItem != null) {
            postTypesSpinner.setSelection(adapter.getPosition(defaultPostTypeItem));
        }
        postTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                PostType postType = (PostType) parent.getSelectedItem();
                selectedPostType = postType.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        filterSourcePostListButton = findViewById(R.id.filterSourcePostListButton);
        filterSourcePostListButton.setOnClickListener(new filterSourcePostListOnClickListener());

    }

    // Filter
    class filterSourcePostListOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Preferences.setPreference(PostListFilterActivity.this, "source_post_list_filter_post_type", selectedPostType);
            Preferences.setPreference(PostListFilterActivity.this, "source_post_list_filter_post_limit", postLimitSpinner.getSelectedItem().toString());
            Intent returnIntent = new Intent();
            returnIntent.putExtra("refresh", true);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }


}
