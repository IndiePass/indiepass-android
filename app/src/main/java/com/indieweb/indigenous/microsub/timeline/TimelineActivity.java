package com.indieweb.indigenous.microsub.timeline;

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
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.model.Cache;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.TimelineStyle;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

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
    private List<TimelineItem> TimelineItems = new ArrayList<>();
    SwipeRefreshLayout refreshLayout;
    ListView listView;
    Button loadMoreButton;
    boolean loadMoreButtonAdded = false;
    boolean loadMoreClicked = false;
    String[] olderItems;
    String debugResponse;
    User user;
    Integer style;
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
            sourceName = extras.getString("sourceName");
            searchQuery = extras.getString("search");
            if (searchQuery != null && searchQuery.length() > 2) {
                isSearch = true;
            }

            if (channelId != null && channelId.equals("global")) {
                isGlobalUnread = true;
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

                        if (markReadScroll && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
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

        boolean search = Preferences.getPreference(this, "pref_key_search", false);
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
                Utility.showDebugInfo(getApplicationContext(), debugResponse);
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
                new MicrosubAction(getApplicationContext(), user, layout).markRead(channelId, entries, clearAll, true);
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
        adapter = new TimelineListAdapter(this, TimelineItems, user, channelId, channelName, listView, isSourceView, style, layout);
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
        String MicrosubEndpoint = user.getMicrosubEndpoint();

        if (preview || isSearch) {
            method = Request.Method.POST;
        }
        else {

            MicrosubEndpoint += "?action=timeline&channel=" + channelId;

            // Global unread.
            if (isGlobalUnread || showUnread) {
                MicrosubEndpoint += "&is_read=false";
            }

            // Individual timeline.
            if (isSourceView) {
                MicrosubEndpoint += "&source=" + sourceId;
            }

            // Pager.
            if (pagerAfter != null && pagerAfter.length() > 0) {
                MicrosubEndpoint += "&after=" + pagerAfter;
            }
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        final String finalMicrosubEndpoint = MicrosubEndpoint;
        StringRequest getRequest = new StringRequest(method, finalMicrosubEndpoint,
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

                if (preview) {
                    params.put("action", "preview");
                    params.put("url", previewUrl);
                }

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
                if (user.isAnonymous() && finalMicrosubEndpoint.contains(getString(R.string.anonymous_microsub_endpoint))) {
                    accessToken = "";
                }

                headers.put("Authorization", "Bearer " + accessToken);
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

            JSONObject object;
            debugResponse = response;
            JSONObject microsubResponse = new JSONObject(response);
            JSONArray itemList = microsubResponse.getJSONArray("items");

            // Paging. Can be empty.
            if (microsubResponse.has("paging")) {
                try {
                    if (microsubResponse.getJSONObject("paging").has("after")) {
                        olderItems[0] = microsubResponse.getJSONObject("paging").getString("after");
                    }
                    else {
                        olderItems[0] = "";
                    }
                }
                catch (JSONException ignored) {
                    olderItems[0] = "";
                }
            }

            for (int i = 0; i < itemList.length(); i++) {
                object = itemList.getJSONObject(i);
                TimelineItem item = new TimelineItem();
                item.setJson(itemList.getString(i));

                boolean addContent = true;
                boolean isRead = false;
                String type = "entry";
                String url = "";
                String name = "";
                String textContent = "";
                String htmlContent = "";
                String authorName = "";

                // Type.
                if (object.has("type")) {
                    type = object.getString("type");
                }

                // Ignore 'card' type.
                if (type.equals("card")) {
                    continue;
                }

                // It's possible that _id is empty. Don't let readers choke on it.
                try {
                    item.setId(object.getString("_id"));
                } catch (Exception ignored) {}

                // Source id is experimental.
                if (object.has("_source")) {
                    try {
                        item.setSourceId(object.getString("_source"));
                    } catch (Exception ignored) {}
                }

                // Is read.
                if (object.has("_is_read")) {
                    isRead = object.getBoolean("_is_read");
                }
                item.setRead(isRead);
                if (!item.isRead() && item.getId() != null) {
                    entries.add(item.getId());

                    if (firstEntryId == null) {
                        firstEntryId = item.getId();
                    }

                }

                // Channel name and id.
                item.setChannelId(channelId);
                if (object.has("_channel") && isGlobalUnread) {
                    try {
                        String itemChannelName = object.getJSONObject("_channel").getString("name");
                        item.setChannelName(itemChannelName);
                    }
                    catch (Exception ignored) {}
                }
                else if (channelName != null && channelName.length() > 0) {
                    item.setChannelName(channelName);
                }

                // Author.
                if (object.has("author")) {

                    JSONObject author = object.getJSONObject("author");
                    if (author.has("name")) {
                        authorName = author.getString("name");
                    }
                    String authorUrl = "";
                    if (author.has("url")) {
                        authorUrl = author.getString("url");
                        item.setAuthorUrl(authorUrl);
                    }
                    if (authorName.equals("null") && authorUrl.length() > 0) {
                        authorName = authorUrl;
                    }

                    if (author.has("photo")) {
                        String authorPhoto = author.getString("photo");
                        if (!authorPhoto.equals("null") && authorPhoto.length() > 0) {
                            item.setAuthorPhoto(authorPhoto);
                        }
                    }
                }
                item.setAuthorName(authorName);

                // In reply to.
                if (object.has("in-reply-to")) {
                    type = "in-reply-to";
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, false, false, 0);
                    }
                }

                // Follow-of.
                if (object.has("follow-of")) {
                    type = "follow-of";
                    textContent = "Started following you!";
                }

                // Repost.
                if (object.has("repost-of")) {
                    type = "repost-of";
                    addContent = false;
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, true, recursiveReference, 0);
                    }
                }

                // Quotation of.
                if (object.has("quotation-of")) {
                    type = "quotation-of";
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, true, false, 0);
                    }
                }

                // Like.
                if (object.has("like-of")) {
                    type = "like-of";
                    addContent = false;
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, false, false, 0);
                    }
                }

                // Bookmark.
                if (object.has("bookmark-of")) {
                    type = "bookmark-of";
                    addContent = false;
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, false, false, 0);
                    }
                }

                // A checkin.
                if (object.has("checkin")) {
                    type = "checkin";
                    item.addToResponseType(type, object.getJSONObject("checkin").getString("name"));
                    String checkinUrl = "";
                    try {
                        checkinUrl = object.getJSONObject("checkin").getString("url");
                    }
                    catch (Exception ignored) {}
                    item.addToResponseType("checkin-url", checkinUrl);

                    try {
                        item.setLatitude(object.getJSONObject("checkin").getString("latitude"));
                        item.setLongitude(object.getJSONObject("checkin").getString("longitude"));
                    }
                    catch (Exception ignored) {}

                }

                // Location.
                if (object.has("location")) {
                    String location = getSingleJsonValueFromArrayOrString("location", object);
                    item.setLocation(location);
                }

                // Set type.
                item.setType(type);

                // Url.
                if (object.has("url")) {
                    url = object.getString("url");
                }
                item.setUrl(url);

                // Published
                String published = "";
                if (object.has("published")) {
                    published = object.getString("published");
                }
                item.setPublished(published);

                // Start
                String start = "";
                if (object.has("start")) {
                    start = object.getString("start");
                }
                item.setStart(start);

                // End
                String end = "";
                if (object.has("end")) {
                    end = object.getString("end");
                }
                item.setEnd(end);

                // Content.
                if (object.has("content") && addContent) {
                    JSONObject content = object.getJSONObject("content");

                    if (content.has("text")) {
                        addContent = false;
                        textContent = content.getString("text");
                    }

                    if (content.has("html")) {
                        addContent = false;
                        htmlContent = content.getString("html");

                        // Clean html, remove images and put them in photo.
                        try {
                            Document doc = Jsoup.parse(htmlContent);
                            Elements imgs = doc.select("img");
                            for (Element img : imgs) {
                                String photoUrl = img.absUrl("src");
                                if (!photoUrl.contains("spacer.gif") && !photoUrl.contains("spacer.png")) {
                                    item.addPhoto(photoUrl);
                                }
                            }
                            htmlContent = Jsoup.clean(htmlContent, Whitelist.basic());
                        }
                        catch (Exception ignored) {}
                    }

                }
                else if (object.has("summary") && addContent) {
                    addContent = false;
                    textContent = object.getString("summary");
                }

                // RSVP
                if (object.has("rsvp") && addContent) {
                    try {
                        textContent = "RSVP: " + object.getString("rsvp");
                    }
                    catch (JSONException ignored) {}
                }

                // Name.
                if (object.has("name")) {
                    name = object.getString("name").replace("\n", "").replace("\r", "");
                    if (name.equals(textContent)) {
                        name = "";
                    }
                }
                else if (object.has("summary") && addContent) {
                    name = object.getString("summary").replace("\n", "").replace("\r", "");
                }

                // Photos.
                if (object.has("photo")) {
                    try {
                        Object photoObject = object.get("photo");
                        if (photoObject instanceof JSONArray) {
                            JSONArray photos = object.getJSONArray("photo");
                            for (int p = 0; p < photos.length(); p++) {
                                item.addPhoto(photos.getString(p));
                            }
                        }
                        else {
                            item.addPhoto(object.getString("photo"));
                        }
                    }
                    catch (JSONException ignored) {}
                }

                // Audio.
                if (object.has("audio")) {
                    String audio = getSingleJsonValueFromArrayOrString("audio", object);
                    item.setAudio(audio);
                }

                // Video.
                if (object.has("video")) {
                    String video = getSingleJsonValueFromArrayOrString("video", object);
                    item.setVideo(video);
                }

                // Detect YouTube video.
                if (item.getUrl().length() > 0 && item.getUrl().contains("youtube.com")) {
                    item.setVideo(item.getUrl());
                }

                //Log.d("indigenous_debug", "text before: " + item.getTextContent());
                //Log.d("indigenous_debug", "html before: " + item.getHtmlContent());
                //Log.d("indigenous_debug", "reference before: " + item.getReference());

                // Swap reference and content if content is empty.
                if (item.swapReference() && Preferences.getPreference(getApplicationContext(), "pref_key_timeline_author_original", false) && textContent.length() == 0 && htmlContent.length() == 0 && item.getReference().length() > 0) {
                    //Log.d("indigenous_debug", "swapping ref / content");
                    item.setTextContent(item.getReference());
                    item.setReference("");
                }

                // Set values of name, text and html content.
                item.setName(name);
                if (item.getTextContent().length() == 0) {
                    item.setTextContent(textContent);
                }
                item.setHtmlContent(htmlContent);

                //Log.d("indigenous_debug", "text after: " + item.getTextContent());
                //Log.d("indigenous_debug", "html after: " + item.getHtmlContent());
                //Log.d("indigenous_debug", "reference after: " + item.getReference());

                TimelineItems.add(item);
            }

            adapter.notifyDataSetChanged();

            // Notify
            if (!fromCache && !isGlobalUnread && !isSourceView && !preview && (unread > 0 || unread == -1) && entries.size() > 0
                    && Preferences.getPreference(getApplicationContext(), "pref_key_mark_read", MARK_READ_CHANNEL_CLICK) == MARK_READ_CHANNEL_CLICK
                    && !readLater.equals(channelId)) {
                new MicrosubAction(TimelineActivity.this, user, layout).markRead(channelId, entries, false, false);
            }

            // Add mark read.
            if ((!isSourceView && !allReadVisible && firstEntryId != null && entries.size() == 20) || (!allReadVisible && isGlobalUnread && entries.size() > 0)) {
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
        catch (JSONException e) {
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
     * Returns a string from either an array or string property.
     *
     * @param property
     *   The property on the object to get a value from.
     * @param object
     *   The json object.
     *
     * @return value
     */
    protected String getSingleJsonValueFromArrayOrString(String property, JSONObject object) {
        String value = "";

        try {
            Object temp = object.get(property);
            if (temp instanceof JSONArray) {
                value = object.getJSONArray(property).get(0).toString();
            }
            else {
                value = object.getString(property);
            }
        }
        catch (JSONException ignored) { }

        return value;
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
                //noinspection ConstantConditions
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
                new MicrosubAction(getApplicationContext(), user, layout).markRead(channelId, readEntries, false, false);
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

    /**
     * Returns the reference content.
     *  @param object
     *   A JSON object.
     * @param url
     *   The url to find in references
     * @param item
     *   The current timeline item.
     * @param swapAuthor
     *   Whether to swap the author or not.
     * @param checkRecursive
     *   Whether to check further recursive.
     * @param level
     *   The recursive level
     */
    private void checkReference(JSONObject object, String url, TimelineItem item, boolean swapAuthor, boolean checkRecursive, int level) {

        if (object.has("refs")) {
            try {
                JSONObject references = object.getJSONObject("refs");
                if (references.has(url)) {
                    JSONObject ref = references.getJSONObject(url);

                    // Content.
                    if (ref.has("content")) {
                        JSONObject content = ref.getJSONObject("content");
                        if (content.has("text")) {
                            if (level == 1 && item.getReference().length() > 0) {
                                //Log.d("indigenous_debug", "swap on level 1: " + item.getReference());
                                item.setSwapReference(false);
                                item.setTextContent(item.getReference());
                            }
                            //Log.d("indigenous_debug", "content: " + content.getString("text"));
                            item.setReference(content.getString("text"));
                        }
                    }
                    else if (ref.has("summary")) {
                        if (level == 1 && item.getReference().length() > 0) {
                            //Log.d("indigenous_debug", "swap on level 1: " + item.getReference());
                            item.setSwapReference(false);
                            item.setTextContent(item.getReference());
                        }
                        item.setReference(ref.getString("summary"));
                    }

                    // Photo.
                    if (ref.has("photo")) {
                        JSONArray photos = ref.getJSONArray("photo");
                        for (int p = 0; p < photos.length(); p++) {
                            item.addPhoto(photos.getString(p));
                        }
                    }

                    // Video.
                    if (ref.has("video")) {
                        String video = ref.getJSONArray("video").getString(0);
                        item.setVideo(video);
                    }

                    // Swap actor and author.
                    if (swapAuthor && Preferences.getPreference(getApplicationContext(), "pref_key_timeline_author_original", false) && ref.has("author")) {
                        String authorName = "";
                        JSONObject author = ref.getJSONObject("author");
                        if (author.has("name")) {
                            authorName = author.getString("name");
                        }
                        String authorUrl = "";
                        if (author.has("url")) {
                            authorUrl = author.getString("url");
                            item.setAuthorUrl(authorUrl);
                        }
                        if (authorName.equals("null") && authorUrl.length() > 0) {
                            authorName = authorUrl;
                        }

                        if (author.has("photo")) {
                            String authorPhoto = author.getString("photo");
                            if (!authorPhoto.equals("null") && authorPhoto.length() > 0) {
                                item.setAuthorPhoto(authorPhoto);
                            }
                        }

                        if (authorName.length() > 0) {
                            item.setActor(item.getAuthorName());
                            item.setAuthorName(authorName);
                        }
                    }

                    if (checkRecursive && ref.has("quotation-of")) {
                        //Log.d("indigenous_debug", "going recursive");
                        String secondType = "quotation-of";
                        String value = getSingleJsonValueFromArrayOrString(secondType, ref);
                        if (value.length() > 0) {
                            checkReference(ref, value, item, false, false, 1);
                        }
                    }
                }
            }
            catch (JSONException ignored) { }
        }
    }
}
