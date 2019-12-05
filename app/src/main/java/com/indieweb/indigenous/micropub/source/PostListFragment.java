package com.indieweb.indigenous.micropub.source;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.model.PostListItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class PostListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    boolean showRefreshMessage = false;
    private PostListAdapter adapter;
    private List<PostListItem> PostListItems = new ArrayList<>();
    SwipeRefreshLayout refreshLayout;
    ListView listView;
    User user;
    Button loadMoreButton;
    boolean loadMoreButtonAdded = false;
    String[] olderItems;
    String debugResponse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_source_post_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        listView = view.findViewById(R.id.source_post_list);
        refreshLayout = view.findViewById(R.id.refreshSourcePostList);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshing(true);
        loadMoreButton = new Button(getContext());
        loadMoreButton.setText(R.string.load_more);
        loadMoreButton.setTextColor(getResources().getColor(R.color.textColor));
        loadMoreButton.setBackgroundColor(getResources().getColor(R.color.loadMoreButtonBackgroundColor));
        user = new Accounts(getContext()).getCurrentUser();
        requireActivity().setTitle(R.string.source_post_list);
        startPostList();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
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
                showRefreshMessage = true;
                refreshLayout.setRefreshing(true);
                startPostList();
                return true;
            case R.id.source_post_list_filter:
                Intent PostListFilter = new Intent(getActivity(), PostListFilterActivity.class);
                startActivityForResult(PostListFilter, 1);
                return true;
            case R.id.source_list_debug:
                Intent i = new Intent(getActivity(), DebugActivity.class);
                Indigenous app = Indigenous.getInstance();
                app.setDebug(debugResponse);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        showRefreshMessage = true;
        startPostList();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                boolean refresh = data.getBooleanExtra("refresh", false);
                if (refresh) {
                    refreshLayout.setRefreshing(true);
                    startPostList();
                }
            }
        }
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            if (showRefreshMessage) {
                Toast.makeText(getContext(), getString(R.string.source_post_list_items_refreshed), Toast.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        }
    }

    /**
     * Start with post list.
     */
    public void startPostList() {
        boolean updateEnabled = Preferences.getPreference(getContext(), "pref_key_source_update", false);
        boolean deleteEnabled = Preferences.getPreference(getContext(), "pref_key_source_delete", false);
        PostListItems = new ArrayList<>();
        adapter = new PostListAdapter(requireContext(), PostListItems, user, updateEnabled, deleteEnabled);
        listView.setAdapter(adapter);
        getSourcePostListItems("");
    }

    /**
     * Get items in channel.
     */
    public void getSourcePostListItems(String pagerAfter) {

        if (!new Connection(requireContext()).hasConnection()) {
            showRefreshMessage = false;
            checkRefreshingStatus();
            Toast.makeText(requireContext(), requireContext().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
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

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        StringRequest getRequest = new StringRequest(Request.Method.GET, MicropubEndpoint,
                new Response.Listener<String>() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object;
                            debugResponse = response;
                            JSONObject micropubResponse = new JSONObject(response);
                            JSONArray itemList = micropubResponse.getJSONArray("items");

                            // Paging. Can be empty.
                            if (micropubResponse.has("paging")) {
                                try {
                                    if (micropubResponse.getJSONObject("paging").has("after")) {
                                        olderItems[0] = micropubResponse.getJSONObject("paging").getString("after");
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
                                    content = object.getJSONArray("content").get(0).toString();
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
                                if (loadMoreButtonAdded) {
                                    listView.removeFooterView(loadMoreButton);
                                }
                            }

                        }
                        catch (JSONException e) {
                            showRefreshMessage = false;
                            Toast.makeText(getContext(), String.format(getString(R.string.post_list_parse_error), e.getMessage()), Toast.LENGTH_LONG).show();
                        }

                        checkRefreshingStatus();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Context context = getContext();
                        if (context != null) {
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                                int code = networkResponse.statusCode;
                                String result = new String(networkResponse.data).trim();
                                Toast.makeText(context, String.format(context.getString(R.string.posts_network_fail), code, result), Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(context, context.getString(R.string.posts_fail), Toast.LENGTH_LONG).show();
                            }
                        }
                        showRefreshMessage = false;
                        checkRefreshingStatus();
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }

        };

        getRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getRequest);
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
