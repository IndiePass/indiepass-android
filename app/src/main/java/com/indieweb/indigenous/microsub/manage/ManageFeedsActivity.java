package com.indieweb.indigenous.microsub.manage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Feed;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageFeedsActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    String channelId;
    String channelName;
    private ManageFeedsListAdapter adapter;
    private List<Feed> FeedItems = new ArrayList<>();
    SwipeRefreshLayout refreshLayout;
    ListView listView;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_feed);

        listView = findViewById(R.id.feed_list);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            findViewById(R.id.actionButton).setOnClickListener(this);
            channelId = extras.getString("channelId");
            channelName = extras.getString("channelName");
            this.setTitle("Feeds in " + channelName);
            refreshLayout = findViewById(R.id.refreshFeed);
            refreshLayout.setOnRefreshListener(this);
            user = new Accounts(this).getCurrentUser();
            startFeed();
        }
        else {
            Toast.makeText(this, "Channel not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_feed_top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feed_list_refresh:
                refreshLayout.setRefreshing(true);
                startFeed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        startFeed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionButton:
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("channelId", channelId);
                intent.putExtra("channelName", channelName);
                startActivity(intent);
                break;
        }
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            Toast.makeText(getApplicationContext(), getString(R.string.feed_items_refreshed), Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
        }
    }

    /**
     * Start with the feed.
     */
    public void startFeed() {
        FeedItems = new ArrayList<>();
        adapter = new ManageFeedsListAdapter(this, FeedItems, user);
        listView.setAdapter(adapter);
        getFeeds();
    }

    /**
     * Get feeds in channel.
     */
    public void getFeeds() {

        String MicrosubEndpoint = user.getMicrosubEndpoint();
        MicrosubEndpoint += "?action=follow&channel=" + channelId;

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest getRequest = new StringRequest(Request.Method.GET, MicrosubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object;
                            JSONObject microsubResponse = new JSONObject(response);
                            JSONArray itemList = microsubResponse.getJSONArray("items");

                            for (int i = 0; i < itemList.length(); i++) {
                                object = itemList.getJSONObject(i);
                                Feed item = new Feed();
                                item.setUrl(object.getString("url"));
                                item.setChannel(channelId);
                                FeedItems.add(item);
                            }

                            adapter.notifyDataSetChanged();
                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        checkRefreshingStatus();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_feeds_found), Toast.LENGTH_SHORT).show();
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

        getRequest.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getRequest);

    }
}
