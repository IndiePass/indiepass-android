package com.indieweb.indigenous.microsub.channel;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.MainActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.indieauth.IndieAuthActivity;
import com.indieweb.indigenous.microsub.manage.ManageChannelActivity;
import com.indieweb.indigenous.microsub.timeline.TimelineActivity;
import com.indieweb.indigenous.model.Cache;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.general.BaseFragment;
import com.indieweb.indigenous.util.HTTPRequest;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChannelFragment extends BaseFragment implements View.OnClickListener {

    private ListView listChannel;
    private ChannelListAdapter adapter;
    private List<Channel> Channels = new ArrayList<>();
    private String readLater;
    private boolean hideRead = false;
    private boolean showSources = false;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

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
        layout = view.findViewById(R.id.channel_root);
        setRefreshedMessage(R.string.channels_refreshed);

        requireActivity().setTitle(getString(R.string.nav_reader));

        if (user.getMicrosubEndpoint().length() > 0) {
            setHasOptionsMenu(true);
            setLayoutRefreshing(true);
            setOnRefreshListener();
            showRefreshLayout();
            readLater = Preferences.getPreference(requireContext(), "pref_key_read_later", "");
            listChannel.setVisibility(View.VISIBLE);
            hideRead = Preferences.getPreference(requireContext(), "channel_hide_read", false);
            showSources = Preferences.getPreference(requireContext(), "channel_show_sources", false);
            startChannels();
        }
        else {
            listChannel.setVisibility(View.GONE);
            hideRefreshLayout();
            TextView noMicrosubEndpoint = view.findViewById(R.id.noMicrosubEndpoint);
            noMicrosubEndpoint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Indigenous app = Indigenous.getInstance();
        if (app.isRefreshChannels()) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRefresh() {
        setShowRefreshedMessage(true);
        startChannels();
    }

    /**
     * Start channels.
     */
    private void startChannels() {
        hideNoConnection();
        Channels = new ArrayList<>();
        listChannel.setVisibility(View.VISIBLE);
        adapter = new ChannelListAdapter(requireContext(), Channels, readLater);
        listChannel.setAdapter(adapter);
        loadChannels();
    }

    /**
     * Load channels.
     */
    private void loadChannels() {

        if (!Utility.hasConnection(requireContext())) {
            setShowRefreshedMessage(false);
            Cache cache = user.isAuthenticated() ? Utility.getCache(requireContext(), user.getMeWithoutProtocol(), "channels", "", "") : null;
            if (cache != null && cache.getData().length() > 0) {
                parseChannelResponse(cache.getData(), true);
                Snackbar.make(layout, getString(R.string.reader_offline), Snackbar.LENGTH_SHORT).show();
            }
            else {
                checkRefreshingStatus();
                showNoConnection();
            }
            return;
        }

        String microsubEndpoint = user.getMicrosubEndpoint();
        if (microsubEndpoint.contains("?")) {
            microsubEndpoint += "&action=channels";
        }
        else {
            microsubEndpoint += "?action=channels";
        }

        if (showSources) {
            microsubEndpoint += "&method=tree";
        }

        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, user, requireContext());
        r.doGetRequest(microsubEndpoint);
    }

    @Override
    public void OnSuccessRequest(String response) {
        try {
            Utility.saveCache(requireContext(), user.getMeWithoutProtocol(), "channels", response, "", "");
        }
        catch (Exception ignored) {}
        parseChannelResponse(response, false);
    }

    /**
     * Parse the channel data.
     *
     * @param data
     *   The data to parse.
     * @param fromCache
     *   Whether the response came from cache or not.
     */
    private void parseChannelResponse(String data, boolean fromCache) {
        boolean hasUnread = false;

        try {
            JSONObject object;
            JSONObject source;
            debugResponse = data;
            JSONObject microsubResponse = new JSONObject(data);
            JSONArray channelList = microsubResponse.getJSONArray("channels");

            int index = 0;
            int unreadChannels = 0;
            int totalUnread = 0;
            for (int i = 0; i < channelList.length(); i++) {
                object = channelList.getJSONObject(i);
                Channel channel = new Channel();
                channel.setUid(object.getString("uid"));
                channel.setName(object.getString("name"));

                int unread = 0;
                if (!fromCache && object.has("unread")) {
                    Object unreadCheck = object.get("unread");
                    if (unreadCheck instanceof Integer) {
                        unread = (Integer) unreadCheck;
                        totalUnread += unread;
                        if (unread > 0) {
                            hasUnread = true;
                            unreadChannels++;
                        }
                    }
                    if (unreadCheck instanceof Boolean) {
                        if ((Boolean) unreadCheck) {
                            hasUnread = true;
                            unread = -1;
                        }
                    }
                }

                channel.setUnread(unread);
                Channels.add(index++, channel);

                // Sources.
                if (object.has("sources")) {
                    JSONArray sources = object.getJSONArray("sources");

                    if (sources.length() < 2) {
                        continue;
                    }

                    for (int s = 0; s < sources.length(); s++) {
                        source = sources.getJSONObject(s);
                        Channel channelSource = new Channel();
                        channelSource.setUid(object.getString("uid"));
                        channelSource.setSourceId(source.getString("uid"));
                        String label = source.getString("url");
                        if (source.getString("name").length() > 0) {
                            label = source.getString("name");
                        }
                        channelSource.setName(label);

                        int sourceUnread = 0;
                        if (!fromCache && source.has("unread")) {
                            Object unreadCheck = source.get("unread");
                            if (unreadCheck instanceof Integer) {
                                sourceUnread = (Integer) unreadCheck;
                            }
                            if (unreadCheck instanceof Boolean) {
                                if ((Boolean) unreadCheck) {
                                    sourceUnread = -1;
                                }
                            }
                        }

                        channelSource.setUnread(sourceUnread);
                        Channels.add(index++, channelSource);
                    }
                }

            }

            try {
                if (user.isAuthenticated() && Preferences.getPreference(getContext(), "pref_key_unread_items_channel", false) && unreadChannels > 1 && totalUnread > 0) {
                    Channel channel = new Channel();
                    channel.setUid("global");
                    channel.setName("Unread items");
                    channel.setUnread(totalUnread);
                    Channels.add(0, channel);
                }
            }
            catch (Exception ignored) {}

            // Set channels globally, make a copy as we need the full list for moving items, or
            // managing feeds
            Indigenous app = Indigenous.getInstance();
            app.setChannels(new ArrayList<>(Channels));

            // Remove channels if needed.
            if (hideRead && !fromCache && hasUnread) {
                for (int j = Channels.size()-1; j >= 0; j--) {
                    Channel c = Channels.get(j);
                    if (c.getUnread() == 0) {
                        Channels.remove(j);
                    }
                }
            }

            adapter.notifyDataSetChanged();
            checkRefreshingStatus();
        }
        catch (JSONException e) {
            setShowRefreshedMessage(false);
            String message = String.format(getString(R.string.channel_list_parse_error), e.getMessage());
            final Snackbar snack = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snack.dismiss();
                    }
                }
            );
            snack.show();
            checkRefreshingStatus();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.actionButton) {
            ((MainActivity) requireActivity()).openDrawer(R.id.nav_create);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.channel_menu, menu);

        if (user.isAnonymous()) {
            MenuItem item = menu.findItem(R.id.channels_manage);
            if (item != null) {
                item.setVisible(false);
            }

            MenuItem userAdd = menu.findItem(R.id.user_add);
            if (userAdd != null) {
                userAdd.setVisible(true);
            }
        }

        boolean debugJson = Preferences.getPreference(getActivity(), "pref_key_debug_microsub_channels", false);
        if (user.isAuthenticated() && debugJson) {
            MenuItem item = menu.findItem(R.id.channels_debug);
            if (item != null) {
                item.setVisible(true);
            }
        }

        boolean clearCache = Preferences.getPreference(getActivity(), "pref_key_reader_cache", false);
        if (user.isAuthenticated() && clearCache) {
            MenuItem item = menu.findItem(R.id.clear_cache);
            if (item != null) {
                item.setVisible(true);
            }
        }

        boolean search = Preferences.getPreference(getActivity(), "pref_key_search_global", false);
        if (search && user.isAuthenticated()) {
            MenuItem item = menu.findItem(R.id.channel_search);
            if (item != null) {
                item.setVisible(true);

                SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
                searchView = (SearchView) item.getActionView();

                if (searchView != null) {
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().getComponentName()));

                    queryTextListener = new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return true;
                        }
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (query.length() > 0) {
                                Intent timelineActivity = new Intent(getContext(), TimelineActivity.class);
                                timelineActivity.putExtra("channelId", "global");
                                timelineActivity.putExtra("search", query);
                                startActivity(timelineActivity);
                            }
                            return true;
                        }
                    };
                    searchView.setOnQueryTextListener(queryTextListener);
                }
            }
        }

        if (hideRead) {
            MenuItem item = menu.findItem(R.id.channel_hide_read);
            if (item != null) {
                item.setTitle(getString(R.string.channel_show_all));
            }
        }

        if (showSources) {
            MenuItem item = menu.findItem(R.id.channel_show_sources);
            if (item != null) {
                item.setTitle(getString(R.string.channel_hide_sources));
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
                setShowRefreshedMessage(true);
                setLayoutRefreshing(true);
                startChannels();
                return true;

            case R.id.channel_hide_read:
                hideRead = !hideRead;
                if (hideRead) {
                    item.setTitle(getString(R.string.channel_show_all));
                }
                else {
                    item.setTitle(getString(R.string.channel_hide_read));
                }
                Preferences.setPreference(requireContext(), "channel_hide_read", hideRead);
                startChannels();
                return true;

            case R.id.channel_show_sources:
                showSources = !showSources;
                if (showSources) {
                    item.setTitle(getString(R.string.channel_hide_sources));
                }
                else {
                    item.setTitle(getString(R.string.channel_show_sources));
                }
                Preferences.setPreference(requireContext(), "channel_show_sources", showSources);
                startChannels();
                return true;

            case R.id.channels_debug:
                Utility.showDebugInfo(getContext(), debugResponse);
                return true;

            case R.id.clear_cache:
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(getString(R.string.clear_cache_confirm));
                builder.setCancelable(true);
                builder.setPositiveButton(getString(R.string.clear),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Snackbar.make(layout, getString(R.string.cache_cleared), Snackbar.LENGTH_SHORT).show();
                        DatabaseHelper db = new DatabaseHelper(requireContext());
                        db.clearCache();
                    }
                });

                builder.setNegativeButton(requireContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;

            case R.id.user_add:
                Intent addUser = new Intent(getContext(), IndieAuthActivity.class);
                startActivity(addUser);
                return true;
        }

        searchView.setOnQueryTextListener(queryTextListener);

        return super.onOptionsItemSelected(item);
    }

}