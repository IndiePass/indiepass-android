package com.indieweb.indigenous.reader;

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
import com.indieweb.indigenous.General;
import com.indieweb.indigenous.GeneralFactory;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.users.AuthActivity;
import com.indieweb.indigenous.indieweb.microsub.manage.ManageChannelActivity;
import com.indieweb.indigenous.model.Cache;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.general.BaseFragment;
import com.indieweb.indigenous.util.HTTPRequest;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ChannelFragment extends BaseFragment implements View.OnClickListener {

    private ListView listChannel;
    private ChannelListAdapter adapter;
    private List<Channel> Channels = new ArrayList<>();
    private String readLater;
    private boolean hideRead = false;
    private boolean showSources = false;
    private Reader reader;
    private General general;

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
        reader = ReaderFactory.getReader(user, null, requireContext());
        general = GeneralFactory.getGeneral(user, null, requireContext());

        requireActivity().setTitle(getString(R.string.nav_reader));

        if (reader.hasEndpoint()) {
            setHasOptionsMenu(true);
            if (general.supports(General.FEATURE_CHANNELS_REFRESH)) {
                enableRefresh();
                setLayoutRefreshing(true);
                setOnRefreshListener();
                showRefreshLayout();
            }
            else {
                disableRefresh();
            }
            listChannel.setVisibility(View.VISIBLE);
            readLater = Preferences.getPreference(requireContext(), "pref_key_read_later", "");
            hideRead = Preferences.getPreference(this.getContext(), "channel_hide_read", false);
            showSources = Preferences.getPreference(this.getContext(), "channel_show_sources", false);
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

        if (!Utility.hasConnection(requireContext()) && reader.supports(Reader.READER_CHANNEL_CAN_CACHE)) {
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

        String channelEndpoint = reader.getChannelEndpoint(showSources);
        if (channelEndpoint != null) {
            HTTPRequest r = new HTTPRequest(this.volleyRequestListener, user, requireContext());
            r.doGetRequest(channelEndpoint);
        }
        else {
            List<Channel> channelList = reader.getChannels();
            Channels.addAll(channelList);
            adapter.notifyDataSetChanged();
        }

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

        debugResponse = data;
        List<Channel> parsed = reader.parseChannelResponse(data, fromCache);
        Channels.addAll(parsed);

        List<Channel> additional = reader.getAdditionalChannels();
        Channels.addAll(additional);

        for (Channel c : Channels) {
            if (c.getUnread() > 0) {
                hasUnread = true;
                break;
            }
        }

        // Set channels globally, make a copy as we need the full list for moving items, or managing
        // feeds.
        Indigenous app = Indigenous.getInstance();
        app.setChannels(new ArrayList<>(Channels));

        // Remove channels if needed.
        if (hideRead && !fromCache && hasUnread) {
            for (int j = Channels.size()-1; j >= 0; j--) {
                Channel c = Channels.get(j);
                if (c.getUnread() == 0 || (c.getSourceId().length() > 0 && c.getUnreadSources() < 2)) {
                    Channels.remove(j);
                }
            }
        }

        adapter.notifyDataSetChanged();
        checkRefreshingStatus();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.actionButton) {
            general.handlePostActionButtonClick();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.channel_menu, menu);

        if (!general.supports(General.FEATURE_CHANNELS_REFRESH)) {
            MenuItem item = menu.findItem(R.id.channel_list_refresh);
            if (item != null) {
                item.setVisible(false);
            }
        }

        if (user.isAnonymous() || !general.supports(General.FEATURE_CHANNELS_MANAGE)) {
            MenuItem item = menu.findItem(R.id.channels_manage);
            if (item != null) {
                item.setVisible(false);
            }

            if (user.isAnonymous()) {
                MenuItem userAdd = menu.findItem(R.id.user_add);
                if (userAdd != null) {
                    userAdd.setVisible(true);
                }
            }
        }

        if (user.isAuthenticated() && reader.supports(Reader.READER_DEBUG_CHANNELS)) {
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

        if (general.supports(General.FEATURE_CHANNELS_HIDE_READ)) {
            if (hideRead) {
                MenuItem item = menu.findItem(R.id.channel_hide_read);
                if (item != null) {
                    item.setTitle(getString(R.string.channel_show_all));
                }
            }
        }
        else {
            MenuItem item = menu.findItem(R.id.channel_hide_read);
            if (item != null) {
                item.setVisible(false);
            }
        }

        if (general.supports(General.FEATURE_CHANNELS_SHOW_SOURCES)) {
            if (showSources) {
                MenuItem item = menu.findItem(R.id.channel_show_sources);
                if (item != null) {
                    item.setTitle(getString(R.string.channel_hide_sources));
                }
            }
        }
        else {
            MenuItem item = menu.findItem(R.id.channel_show_sources);
            if (item != null) {
                item.setVisible(false);
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
                Intent addUser = new Intent(getContext(), AuthActivity.class);
                startActivity(addUser);
                return true;
        }

        searchView.setOnQueryTextListener(queryTextListener);

        return super.onOptionsItemSelected(item);
    }

}