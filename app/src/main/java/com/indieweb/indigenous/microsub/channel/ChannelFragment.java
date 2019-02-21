package com.indieweb.indigenous.microsub.channel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.MainActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.microsub.manage.ManageChannelActivity;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    TextView noMicrosubEndpoint;
    boolean showRefreshMessage = false;
    ListView listChannel;
    SwipeRefreshLayout refreshLayout;
    private ChannelListAdapter adapter;
    private List<Channel> Channels = new ArrayList<>();
    User user;
    String debugResponse;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_channels, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.actionButton).setOnClickListener(this);
        listChannel = view.findViewById(R.id.channel_list);
        refreshLayout = view.findViewById(R.id.refreshChannels);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setRefreshing(true);

        user = new Accounts(getContext()).getCurrentUser();
        requireActivity().setTitle("Reader");

        if (user.getMicrosubEndpoint().length() > 0) {
            setHasOptionsMenu(true);
            refreshLayout.setVisibility(View.VISIBLE);
            listChannel.setVisibility(View.VISIBLE);
            startChannels();
        }
        else {
            refreshLayout.setVisibility(View.GONE);
            listChannel.setVisibility(View.GONE);
            noMicrosubEndpoint = view.findViewById(R.id.noMicrosubEndpoint);
            noMicrosubEndpoint.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Start channels.
     */
    public void startChannels() {
        Channels = new ArrayList<>();
        listChannel.setVisibility(View.VISIBLE);
        adapter = new ChannelListAdapter(requireContext(), Channels);
        listChannel.setAdapter(adapter);
        loadChannels();
    }

    @Override
    public void onRefresh() {
        showRefreshMessage = true;
        startChannels();
    }

    /**
     * Get channels.
     */
    public void loadChannels() {

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
                            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            checkRefreshingStatus();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), getString(R.string.channels_not_found), Toast.LENGTH_SHORT).show();
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

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(getRequest);
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            if (showRefreshMessage) {
                Toast.makeText(getContext(), getString(R.string.channels_refreshed), Toast.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionButton:
                ((MainActivity) requireActivity()).openDrawer(R.id.nav_create);
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.channel_menu, menu);

        boolean debugJson = Preferences.getPreference(getActivity(), "pref_key_debug_microsub_channels", false);
        if (debugJson) {
            MenuItem item = menu.findItem(R.id.channels_debug);
            if (item != null) {
                item.setVisible(true);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.channels_manage:
                Intent manageChannels = new Intent(getContext(), ManageChannelActivity.class);
                startActivity(manageChannels);
                return true;

            case R.id.channel_list_refresh:
                showRefreshMessage = true;
                refreshLayout.setRefreshing(true);
                startChannels();
                return true;

            case R.id.channels_debug:
                Intent i = new Intent(getContext(), DebugActivity.class);
                Indigenous app = Indigenous.getInstance();
                app.setDebug(debugResponse);
                startActivity(i);
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

}
