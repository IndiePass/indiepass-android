package com.indieweb.indigenous.micropub.source;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.BaseFragment;
import com.indieweb.indigenous.model.PostListItem;
import com.indieweb.indigenous.util.HTTPRequest;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class PostListFragment extends BaseFragment {

    private PostListAdapter adapter;
    private List<PostListItem> PostListItems = new ArrayList<>();
    private ListView listView;
    private Button loadMoreButton;
    private boolean loadMoreButtonAdded = false;
    private String[] olderItems;
    private static final int FILTER_REQUEST_CODE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_source_post_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        layout = view.findViewById(R.id.source_post_list_root);
        listView = view.findViewById(R.id.source_post_list);
        setRefreshedMessage(R.string.source_post_list_items_refreshed);
        setOnRefreshListener();
        setLayoutRefreshing(true);
        loadMoreButton = new Button(getContext());
        loadMoreButton.setText(R.string.load_more);
        loadMoreButton.setTextColor(getResources().getColor(R.color.textColor));
        loadMoreButton.setBackgroundColor(getResources().getColor(R.color.loadMoreButtonBackgroundColor));
        requireActivity().setTitle(R.string.source_post_list);
        startPostList();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.source_list_menu, menu);

        boolean debugJson = Preferences.getPreference(getActivity(), "pref_key_debug_source_list", false);
        if (debugJson) {
            MenuItem item = menu.findItem(R.id.source_list_debug);
            if (item != null) {
                item.setVisible(true);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.source_post_list_refresh:
                setShowRefreshedMessage(false);
                setLayoutRefreshing(true);
                startPostList();
                return true;
            case R.id.source_post_list_filter:
                Intent PostListFilter = new Intent(getActivity(), PostListFilterActivity.class);
                startActivityForResult(PostListFilter, FILTER_REQUEST_CODE);
                return true;
            case R.id.source_list_debug:
                Utility.showDebugInfo(requireContext(), debugResponse);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        setShowRefreshedMessage(true);
        startPostList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILTER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                boolean refresh = data.getBooleanExtra("refresh", false);
                if (refresh) {
                    setLayoutRefreshing(true);
                    Snackbar.make(layout, getString(R.string.applying_filter), Snackbar.LENGTH_SHORT).show();
                    startPostList();
                }
            }
        }
    }

    @Override
    public void OnSuccessRequest(String response) {
        parseSourceResponse(response);
    }

    /**
     * Hide footer view.
     */
    private void hideFooterView(boolean resetLoadMoreButtonAdded) {
        if (loadMoreButtonAdded) {
            listView.removeFooterView(loadMoreButton);
        }

        if (resetLoadMoreButtonAdded) {
            loadMoreButtonAdded = false;
        }
    }

    /**
     * Start with post list.
     */
    private void startPostList() {
        hideNoConnection();
        boolean updateEnabled = Preferences.getPreference(getContext(), "pref_key_source_update", false);
        boolean deleteEnabled = Preferences.getPreference(getContext(), "pref_key_source_delete", false);
        PostListItems = new ArrayList<>();
        adapter = new PostListAdapter(requireContext(), PostListItems, user, updateEnabled, deleteEnabled, layout);
        listView.setAdapter(adapter);
        getSourcePostListItems("");
    }

    /**
     * Get source items.
     */
    private void getSourcePostListItems(String pagerAfter) {

        if (!Utility.hasConnection(requireContext())) {
            setShowRefreshedMessage(false);
            checkRefreshingStatus();
            showNoConnection();
            hideFooterView(true);
            return;
        }

        String MicropubEndpoint = user.getMicropubEndpoint();
        // Some endpoints already contain GET params. Instead of overriding the getParams method, we
        // just check it here.
        if (MicropubEndpoint.contains("?")) {
            MicropubEndpoint += "&q=source";
        }
        else {
            MicropubEndpoint += "?q=source";
        }

        if (pagerAfter.length() > 0) {
            MicropubEndpoint += "&after=" + pagerAfter;
        }
        olderItems = new String[1];

        // Filter on post type.
        String postType = Preferences.getPreference(getContext(), "source_post_list_filter_post_type", "all_source_post_types");
        if (!postType.equals("all_source_post_types")) {
            MicropubEndpoint += "&post-type=" + postType;
        }

        // Limit.
        String limit = Preferences.getPreference(getContext(), "source_post_list_filter_post_limit", "10");
        MicropubEndpoint += "&limit=" + limit;

        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, user, requireContext());
        r.doGetRequest(MicropubEndpoint);
    }

    /**
     * Parse source response.
     *
     * @param data
     *   The data to parse.
     */
    private void parseSourceResponse(String data) {
        try {
            JSONObject object;
            debugResponse = data;
            JSONObject root = new JSONObject(data);
            JSONArray itemList = root.getJSONArray("items");

            // Paging. Can be empty.
            if (root.has("paging")) {
                try {
                    if (root.getJSONObject("paging").has("after")) {
                        olderItems[0] = root.getJSONObject("paging").getString("after");
                    }
                }
                catch (JSONException ignored) {}
            }

            for (int i = 0; i < itemList.length(); i++) {
                object = itemList.getJSONObject(i).getJSONObject("properties");
                PostListItem item = new PostListItem();

                String url = "";
                String name = "";
                String content = "";
                String published = "";
                String postStatus = "";

                // url.
                if (object.has("url")) {
                    url = object.getJSONArray("url").get(0).toString();
                }
                item.setUrl(url);

                // post status.
                if (object.has("post-status")) {
                    postStatus = object.getJSONArray("post-status").get(0).toString();
                }
                item.setPostStatus(postStatus);

                // published.
                if (object.has("published")) {
                    published = object.getJSONArray("published").get(0).toString();
                }
                item.setPublished(published);

                // content.
                if (object.has("content")) {
                    boolean hasContent = false;
                    try {
                        // Use text first, as the overview is simple, and not a full overview.
                        JSONObject c = object.getJSONArray("content").getJSONObject(0);
                        if (c.has("text")) {
                            hasContent = true;
                            content = c.getString("text");
                        }
                        else if (c.has("html")) {
                            hasContent = true;
                            content = c.getString("html");
                        }
                    }
                    catch (JSONException ignored) {}

                    // No content yet, content might be just a string in the first key.
                    if (!hasContent) {
                        try {
                            content = object.getJSONArray("content").get(0).toString();
                        }
                        catch (JSONException ignored) {}
                    }
                }
                item.setContent(content);

                // name.
                if (object.has("name")) {
                    name = object.getJSONArray("name").get(0).toString();
                }
                item.setName(name);

                PostListItems.add(item);
            }

            adapter.notifyDataSetChanged();

            if (olderItems[0] != null && olderItems[0].length() > 0) {

                if (!loadMoreButtonAdded) {
                    loadMoreButtonAdded = true;
                    listView.addFooterView(loadMoreButton);
                }

                loadMoreButton.setOnTouchListener(loadMoreTouch);
            }
            else {
                hideFooterView(false);
            }

        }
        catch (JSONException e) {
            setShowRefreshedMessage(false);
            Snackbar.make(layout, String.format(getString(R.string.post_list_parse_error), e.getMessage()), Snackbar.LENGTH_SHORT).show();
        }

        checkRefreshingStatus();
    }

    /**
     * Load more touch button.
     */
    private View.OnTouchListener loadMoreTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            switch(motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int downColorTouch = getResources().getColor(R.color.loadMoreButtonBackgroundColorTouched);
                    loadMoreButton.setBackgroundColor(downColorTouch);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    loadMoreButton.setBackgroundColor(getResources().getColor(R.color.loadMoreButtonBackgroundColor));
                    break;
                case MotionEvent.ACTION_UP:
                    int downColor = getResources().getColor(R.color.loadMoreButtonBackgroundColor);
                    loadMoreButton.setBackgroundColor(downColor);
                    getSourcePostListItems(olderItems[0]);
                    break;

            }
            return true;
        }
    };

}
