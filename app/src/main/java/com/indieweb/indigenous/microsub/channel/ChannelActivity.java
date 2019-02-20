package com.indieweb.indigenous.microsub.channel;


import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    ListView listChannel;
    SwipeRefreshLayout refreshLayout;
    private ChannelListAdapter adapter;
    private List<Channel> Channels = new ArrayList<>();
    User user;
    String debugResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);
        listChannel = findViewById(R.id.channel_list);
        refreshLayout = findViewById(R.id.refreshChannels);
        refreshLayout.setOnRefreshListener(this);

        user = new Accounts(this).getCurrentUser();
        this.setTitle(user.getMeWithoutProtocol());

        startChannels();
    }

    /**
     * Start channels.
     */
    public void startChannels() {
        Channels = new ArrayList<>();
        listChannel.setVisibility(View.VISIBLE);
        adapter = new ChannelListAdapter(this, Channels);
        listChannel.setAdapter(adapter);
        getChannels();
    }

    @Override
    public void onRefresh() {
        startChannels();
    }

    /**
     * Get channels.
     */
    public void getChannels() {

        String microsubEndpoint = user.getMicrosubEndpoint();

        // TODO abstract this all in one helper request class.
        // probably use jsonArrayRequest too, will be faster, but we'll see once we get all
        // kind of calls more or less ready.
        microsubEndpoint += "?action=channels";
        StringRequest getRequest = new StringRequest(Request.Method.GET, microsubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object;
                            debugResponse = response;
                            JSONObject microsubResponse = new JSONObject(response);
                            JSONArray channelList = microsubResponse.getJSONArray("channels");

                            for (int i = 0; i < channelList.length(); i++) {
                                object = channelList.getJSONObject(i);
                                Channel channel = new Channel();
                                channel.setUid(object.getString("uid"));
                                channel.setName(object.getString("name"));
                                Integer unread = 0;
                                if (object.has("unread")) {
                                    Object unreadCheck = object.get("unread");
                                    if (unreadCheck instanceof Integer) {
                                        unread = (Integer) unreadCheck;
                                    }
                                    if (unreadCheck instanceof Boolean) {
                                        if ((Boolean) unreadCheck) {
                                            unread = -1;
                                        }
                                    }
                                }
                                channel.setUnread(unread);
                                Channels.add(channel);
                            }

                            adapter.notifyDataSetChanged();
                            checkRefreshingStatus();

                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            checkRefreshingStatus();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.channels_not_found), Toast.LENGTH_SHORT).show();
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

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            Toast.makeText(getApplicationContext(), getString(R.string.channels_refreshed), Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
        }
    }

}
