package com.indieweb.indigenous.reader;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
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
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Cache;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.TimelineStyle;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimelineActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public String firstEntryId;
    String channelId;
    String channelName;
    String sourceId;
    String sourceName;
    boolean isSearch = false;
    String searchQuery;
    boolean isTag = false;
    String tag;
    boolean allReadVisible = false;
    List<String> entries = new ArrayList<>();
    Integer unread;
    boolean showUnread = false;
    Menu mainMenu;
    boolean isSourceView = false;
    boolean isGlobalUnread = false;
    boolean preview = false;
    String previewUrl;
    boolean showRefreshMessage = false;
    private TimelineListAdapter adapter;
    public List<TimelineItem> TimelineItems = new ArrayList<>();
    SwipeRefreshLayout refreshLayout;
    ListView listView;
    Button loadMoreButton;
    boolean loadMoreButtonAdded = false;
    boolean loadMoreClicked = false;
    String[] olderItems;
    String debugResponse;
    User user;
    Integer style;
    private Reader reader;
    private String readLater;
    private LinearLayout noConnection;
    private RelativeLayout layout;
    private boolean hasCache = false;
    private boolean recursiveReference = false;

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    public static int MARK_READ_CHANNEL_CLICK = 1;
    public static int MARK_READ_MANUAL = 2;
    public static int MARK_READ_SCROLL = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        listView = findViewById(R.id.timeline_list);
        noConnection = findViewById(R.id.noConnection);
        refreshLayout = findViewById(R.id.refreshTimeline);
        layout = findViewById(R.id.timeline_root);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            refreshLayout.setRefreshing(true);
            readLater = Preferences.getPreference(TimelineActivity.this, "pref_key_read_later", "");
            recursiveReference = Preferences.getPreference(TimelineActivity.this, "pref_key_timeline_recursive", false);
            channelId = extras.getString("channelId");
            channelName = extras.getString("channelName");
            unread = extras.getInt("unread");
            preview = extras.getBoolean("preview");
            previewUrl = extras.getString("previewUrl");
            sourceId = extras.getString("sourceId");
            tag = extras.getString("tag");
            sourceName = extras.getString("sourceName");
            searchQuery = extras.getString("search");
            if (searchQuery != null && searchQuery.length() > 2) {
                isSearch = true;
            }

            if (channelId != null && channelId.equals("global")) {
                isGlobalUnread = true;
            }

            if (tag != null && tag.length() > 0) {
                isTag = true;
            }

            if (preview) {
                channelId = "preview";
                this.setTitle("Preview");
            }
            else {

                // Looking at source.
                if (sourceName != null && sourceName.length() > 0) {
                    isSourceView = true;
                    this.setTitle(sourceName);
                }
                // Initiating search
                else if (isSearch) {
                    this.setTitle(searchQuery);
                }
                // Tag view
                else if (isTag) {
                    this.setTitle("#" + tag);
                }
                else {
                    this.setTitle(channelName);
                }
                loadMoreButton = new Button(this);
                loadMoreButton.setText(R.string.load_more);
                loadMoreButton.setTextColor(getResources().getColor(R.color.textColor));
                loadMoreButton.setBackgroundColor(getResources().getColor(R.color.loadMoreButtonBackgroundColor));
                refreshLayout.setOnRefreshListener(this);
            }
            user = new Accounts(this).getDefaultUser();
            reader = ReaderFactory.getReader(user, channelId, TimelineActivity.this);

            // Get style.
            DatabaseHelper db = new DatabaseHelper(TimelineActivity.this);
            style = db.getTimelineStyle(channelId);

            // Autoload more posts or mark while scrolling.
            final boolean autoload = Preferences.getPreference(TimelineActivity.this, "pref_key_timeline_autoload_more", false);
            final boolean markReadScroll = Preferences.getPreference(getApplicationContext(), "pref_key_mark_read", MARK_READ_CHANNEL_CLICK) == MARK_READ_SCROLL && !readLater.equals(channelId);
            if (autoload || markReadScroll) {
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (autoload && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                                && (listView.getLastVisiblePosition() - listView.getHeaderViewsCount() -
                                listView.getFooterViewsCount()) >= (adapter.getCount() - 1)) {
                            if (!loadMoreClicked && loadMoreButtonAdded) {
                                loadMoreItems();
                            }
                        }

                        if (reader.supports(Reader.READER_MARK_READ) && markReadScroll && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                            markItemsReadWhileScrolling();
                         }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) { }
                });
            }

            startTimeline();
        }
        else {
            Snackbar.make(layout, getString(R.string.channel_not_found), Snackbar.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timeline_menu, menu);
        mainMenu = menu;

        boolean search = reader.supports(Reader.READER_SEARCH);
        if (user.isAuthenticated() && search && !preview && !isSearch && !isSourceView && !isGlobalUnread) {
            MenuItem item = menu.findItem(R.id.timeline_search);
            if (item != null) {
                item.setVisible(true);

                SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
                searchView = (SearchView) item.getActionView();

                if (searchView != null) {
                    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

                    queryTextListener = new SearchView.OnQueryTextListener() {
                        @Override
                        public boolean onQueryTextChange(String newText) {
                            return true;
                        }
                        @Override
                        public boolean onQueryTextSubmit(String query) {
                            if (query.length() > 0) {
                                Intent timelineActivity = new Intent(getApplicationContext(), TimelineActivity.class);
                                timelineActivity.putExtra("channelId", channelId);
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

        if (user.isAuthenticated() && search && isSearch) {
            MenuItem item = menu.findItem(R.id.timeline_list_refresh);
            if (item != null) {
                item.setVisible(false);
            }
        }

        boolean debugJson = Preferences.getPreference(this, "pref_key_debug_microsub_timeline", false);
        if (user.isAuthenticated() && debugJson && !preview) {
            MenuItem item = menu.findItem(R.id.timeline_debug);
            if (item != null) {
                item.setVisible(true);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.timeline_list_refresh:
                refreshLayout.setRefreshing(true);
                startTimeline();
                return true;

            case R.id.timeline_debug:
                Utility.showDebugInfo(TimelineActivity.this, debugResponse);
                return true;

            case R.id.timeline_item_status:
                showUnread = !showUnread;
                if (showUnread) {
                    item.setTitle(getString(R.string.timeline_item_status_all));
                }
                else {
                    item.setTitle(getString(R.string.timeline_item_status_unread));
                }
                refreshLayout.setRefreshing(true);
                startTimeline();
                return true;

            case R.id.timeline_mark_all_read:
                boolean clearAll = false;
                if (!channelId.equals("global")) {
                    clearAll = true;
                    entries.clear();
                    entries.add(firstEntryId);
                }
                reader.markRead(channelId, entries, clearAll, true);
                return true;

            case R.id.timeline_style:
                final AlertDialog.Builder builder = new AlertDialog.Builder(TimelineActivity.this);
                final DatabaseHelper db = new DatabaseHelper(TimelineActivity.this);
                final CharSequence[] styleOptions = {"Compact", "Summary"};
                builder.setTitle(getString(R.string.select_style));
                builder.setCancelable(true);
                builder.setNegativeButton(getApplicationContext().getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        dialog.dismiss();
                    }
                });
                builder.setSingleChoiceItems(styleOptions, db.getTimelineStyle(channelId), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        TimelineStyle s = new TimelineStyle();
                        s.setType(index);
                        s.setChannelId(channelId);
                        db.saveTimelineStyle(s);
                        style = index;
                        dialog.dismiss();
                        startTimeline();
                    }
                });
                builder.show();
                return true;
        }

        if (searchView != null) {
            searchView.setOnQueryTextListener(queryTextListener);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        showRefreshMessage = true;
        startTimeline();
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            if (showRefreshMessage) {
                Snackbar.make(layout, getString(R.string.timeline_items_refreshed), Snackbar.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);

            if (isSearch) {
                refreshLayout.setEnabled(false);
            }
        }
    }

    /**
     * Start with the timeline.
     */
    public void startTimeline() {
        noConnection.setVisibility(View.GONE);
        TimelineItems = new ArrayList<>();
        adapter = new TimelineListAdapter(this, reader, TimelineItems, user, channelId, channelName, listView, isSourceView, style, layout);
        listView.setAdapter(adapter);
        getTimeLineItems("");
    }

    /**
     * Get items.
     */
    public void getTimeLineItems(final String pagerAfter) {

        olderItems = new String[1];

        if (!Utility.hasConnection(getApplicationContext())) {
            showRefreshMessage = false;

            Cache cache = user.isAuthenticated() ? Utility.getCache(getApplicationContext(), user.getMeWithoutProtocol(), "timeline", channelId, pagerAfter) : null;
            if (cache != null && cache.getData().length() > 0) {
                parseTimelineResponse(cache.getData(), true);
            }
            else {
                if (hasCache) {
                    if (loadMoreButtonAdded) {
                        listView.removeFooterView(loadMoreButton);
                    }
                }
                else {
                    noConnection.setVisibility(View.VISIBLE);
                }
                checkRefreshingStatus();
            }

            return;
        }

        entries.clear();
        int method = Request.Method.GET;
        String readerEndpoint = reader.getTimelineEndpoint(user, channelId, isGlobalUnread, showUnread, isSourceView, sourceId, isTag, tag, isSearch, searchQuery, pagerAfter);
        if (preview || isSearch) {
            method = reader.getTimelineMethod(preview, isSearch);
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        final String finalReaderEndpoint = readerEndpoint;
        StringRequest getRequest = new StringRequest(method, finalReaderEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!showUnread && !preview) {
                            Utility.saveCache(getApplicationContext(), user.getMeWithoutProtocol(), "timeline", response, channelId, pagerAfter);
                        }
                        parseTimelineResponse(response, false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showRefreshMessage = false;
                        checkRefreshingStatus();
                        String message = Utility.parseNetworkError(error, getApplicationContext(), R.string.request_failed, R.string.request_failed_unknown);
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
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // TODO move
                if (preview) {
                    params.put("action", "preview");
                    params.put("url", previewUrl);
                }

                // TODO move
                if (isSearch) {
                    params.put("action", "search");
                    params.put("channel", channelId);
                    params.put("query", searchQuery);
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");

                String accessToken = user.getAccessToken();

                // Send empty access token in case the user is anonymous and the microsub endpoint
                // is still set to the Indigenous site.
                if (user.isAnonymous() && finalReaderEndpoint.contains(getString(R.string.anonymous_microsub_endpoint))) {
                    accessToken = "";
                }

                if (reader.sendTimelineAccessToken(channelId)) {
                    headers.put("Authorization", "Bearer " + accessToken);
                }
                return headers;
            }

        };

        getRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getRequest);

    }

    /**
     * Parse timeline response.
     *
     * @param response
     *   The response
     * @param fromCache
     *   Whether it came from cache
     */
    @SuppressLint("ClickableViewAccessibility")
    protected void parseTimelineResponse(String response, boolean fromCache) {

        try {

            if (fromCache) {
                hasCache = true;
            }

            debugResponse = response;
            List<TimelineItem> parseItems = reader.parseTimelineResponse(response, channelId, channelName, entries, isGlobalUnread, isSearch, recursiveReference, olderItems, getApplicationContext());
            TimelineItems.addAll(parseItems);
            if (firstEntryId == null) {
                firstEntryId = TimelineItems.get(0).getId();
            }
            adapter.notifyDataSetChanged();

            // Notify
            if (reader.supports(Reader.READER_MARK_READ) && !fromCache && !isGlobalUnread && !isSourceView && !preview && (unread > 0 || unread == -1) && entries.size() > 0
                    && Preferences.getPreference(getApplicationContext(), "pref_key_mark_read", MARK_READ_CHANNEL_CLICK) == MARK_READ_CHANNEL_CLICK
                    && !readLater.equals(channelId)) {
                reader.markRead(channelId, entries, false, false);
            }

            // Add mark read.
            if (reader.supports(Reader.READER_MARK_READ) && (!isSourceView && !allReadVisible && firstEntryId != null && entries.size() == 20) || (!allReadVisible && isGlobalUnread && entries.size() > 0)) {
                allReadVisible = true;
                MenuItem item = mainMenu.findItem(R.id.timeline_mark_all_read);
                if (item != null) {
                    item.setVisible(true);
                }
            }

            // Older items.
            if (olderItems != null && olderItems[0] != null && olderItems[0].length() > 0) {

                loadMoreClicked = false;
                loadMoreButton.setText(R.string.load_more);

                if (!loadMoreButtonAdded) {
                    loadMoreButtonAdded = true;
                    listView.addFooterView(loadMoreButton);
                    loadMoreButton.setOnTouchListener(loadMoreTouch);
                }

            }
            else {
                if (loadMoreButtonAdded) {
                    listView.removeFooterView(loadMoreButton);
                }
            }

        }
        catch (Exception e) {
            showRefreshMessage = false;
            String message = String.format(getString(R.string.timeline_parse_error), e.getMessage());
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

    /**
     * Load more touch button.
     */
    private View.OnTouchListener loadMoreTouch = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
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
                    if (!loadMoreClicked) {
                        loadMoreItems();
                    }
                    break;

            }
            return true;
        }
    };

    /**
     * Load more items.
     */
    private void loadMoreItems() {
        loadMoreClicked = true;
        int downColor = getResources().getColor(R.color.loadMoreButtonBackgroundColor);
        loadMoreButton.setBackgroundColor(downColor);
        loadMoreButton.setText(R.string.loading);
        getTimeLineItems(olderItems[0]);
    }

    /**
     * Mark items read while scrolling.
     */
    private void markItemsReadWhileScrolling() {
        try {
            // Determine the position to where we have to check. Switch to the last if the user has
            // scrolled to the end.
            int position = listView.getFirstVisiblePosition();
            //Log.d("indigenous_debug", "First visible: " + position + " - last visible: " + listView.getLastVisiblePosition());
            if (listView.getLastVisiblePosition() == (adapter.getCount() - 1)) {
                position = listView.getLastVisiblePosition();
                //Log.d("indigenous_debug", "Count: " + adapter.getCount() + " - Last position: " + listView.getLastVisiblePosition());
            }
            //Log.d("indigenous_debug", "position: " + position);

            int counter = 0;
            TimelineItem item;
            int itemPosition = 0;
            List<String> readEntries = new ArrayList<>();
            while (itemPosition <= position) {
                item = adapter.getItem(itemPosition);
                //Log.d("indigenous_debug", "Position:" + itemPosition + " - " + item.getName());
                if (item != null && !item.isRead()) {
                    //Log.d("indigenous_debug", "Marking as read: " + item.getName());
                    readEntries.add(item.getId());
                    item.setRead(true);
                    TimelineItems.set(itemPosition, item);
                    counter -= 1;
                }
                itemPosition++;
            }

            if (readEntries.size() > 0) {
                //Log.d("indigenous_debug", "Counter: " + counter);
                reader.markRead(channelId, readEntries, false, false);
                Utility.notifyChannels(channelId, counter, false);
                if (sourceId != null) {
                    Utility.notifyChannels(sourceId, counter, true);
                }
            }
        }
        catch (Exception e) {
            Log.d("indigenous_debug", "Exception marking read: " + e.getMessage());
        }
    }
}
