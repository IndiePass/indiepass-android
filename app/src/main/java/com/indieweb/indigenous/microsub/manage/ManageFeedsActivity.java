package com.indieweb.indigenous.microsub.manage;

import android.content.Intent;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Feed;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageFeedsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    String channelId;
    String channelName;
    private ManageFeedsListAdapter adapter;
    private List<Feed> FeedItems = new ArrayList<>();
    SwipeRefreshLayout refreshLayout;
    ListView listView;
    User user;
    boolean showRefreshMessage;
    private RelativeLayout layout;
    private LinearLayout noConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_feed);

        layout = findViewById(R.id.feed_manage_root);
        noConnection = findViewById(R.id.noConnection);
        listView = findViewById(R.id.feed_list);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            channelId = extras.getString("channelId");
            channelName = extras.getString("channelName");
            this.setTitle("Feeds in " + channelName);
            refreshLayout = findViewById(R.id.refreshFeed);
            refreshLayout.setOnRefreshListener(this);
            user = new Accounts(this).getDefaultUser();
            startFeed();
        }
        else {
            Snackbar.make(layout, getString(R.string.channel_not_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.manage_feed_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.feed_list_refresh:
                showRefreshMessage = true;
                refreshLayout.setRefreshing(true);
                startFeed();
                return true;
            case R.id.feed_add:
                Intent intent = new Intent(getApplicationContext(), FeedActivity.class);
                intent.putExtra("channelId", channelId);
                intent.putExtra("channelName", channelName);
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        startFeed();
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            if (showRefreshMessage) {
                Snackbar.make(layout, getString(R.string.feed_items_refreshed), Snackbar.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        }
    }

    /**
     * Start with the feed.
     */
    public void startFeed() {
        noConnection.setVisibility(View.GONE);
        FeedItems = new ArrayList<>();
        adapter = new ManageFeedsListAdapter(this, FeedItems, user, layout);
        listView.setAdapter(adapter);
        getFeeds();
    }

    /**
     * Get feeds in channel.
     */
    public void getFeeds() {

        if (!Utility.hasConnection(getApplicationContext())) {
            showRefreshMessage = false;
            checkRefreshingStatus();
            Snackbar.make(layout, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
            noConnection.setVisibility(View.VISIBLE);
            return;
        }

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
                            showRefreshMessage = false;
                            String message = String.format(getString(R.string.feed_parse_error), e.getMessage());
                            final Snackbar snack = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE);
                            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snack.dismiss();
                                    }
                                }
                            );
                            snack.show();

                        }

                        checkRefreshingStatus();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showRefreshMessage = false;
                        Snackbar.make(layout, getString(R.string.no_feeds_found), Snackbar.LENGTH_SHORT).show();
                        checkRefreshingStatus();
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
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
