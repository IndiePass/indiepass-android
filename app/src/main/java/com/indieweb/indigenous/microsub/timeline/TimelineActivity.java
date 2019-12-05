package com.indieweb.indigenous.microsub.timeline;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
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
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.TimelineStyle;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;

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

    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    public static int MARK_READ_CHANNEL_CLICK = 1;
    public static int MARK_READ_MANUAL = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        listView = findViewById(R.id.timeline_list);
        refreshLayout = findViewById(R.id.refreshTimeline);
        refreshLayout.setRefreshing(true);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            readLater = Preferences.getPreference(TimelineActivity.this, "pref_key_read_later", "");
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
            user = new Accounts(this).getCurrentUser();

            // Get style.
            DatabaseHelper db = new DatabaseHelper(TimelineActivity.this);
            style = db.getStyle(channelId);

            // Autoload more posts.
            if (Preferences.getPreference(TimelineActivity.this, "pref_key_timeline_autoload_more", false)) {
                listView.setOnScrollListener(new AbsListView.OnScrollListener() {

                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {
                        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE
                                && (listView.getLastVisiblePosition() - listView.getHeaderViewsCount() -
                                listView.getFooterViewsCount()) >= (adapter.getCount() - 1)) {
                            if (!loadMoreClicked && loadMoreButtonAdded) {
                                loadMoreItems();
                            }
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
                });
            }

            startTimeline();
        }
        else {
            Toast.makeText(this, getString(R.string.channel_not_found), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timeline_menu, menu);
        mainMenu = menu;

        boolean search = Preferences.getPreference(this, "pref_key_search", false);
        if (search && !preview && !isSearch && !isSourceView && !isGlobalUnread) {
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

        if (search && isSearch) {
            MenuItem item = menu.findItem(R.id.timeline_list_refresh);
            if (item != null) {
                item.setVisible(false);
            }
        }

        boolean debugJson = Preferences.getPreference(this, "pref_key_debug_microsub_timeline", false);
        if (debugJson && !preview) {
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
                Intent i = new Intent(this, DebugActivity.class);
                Indigenous app = Indigenous.getInstance();
                app.setDebug(debugResponse);
                startActivity(i);
                return true;

            case R.id.timeline_mark_all_read:
                boolean clearAll = false;
                if (!channelId.equals("global")) {
                    clearAll = true;
                    entries.clear();
                    entries.add(firstEntryId);
                }
                new MicrosubAction(getApplicationContext(), user).markRead(channelId, entries, clearAll);
                Toast.makeText(getApplicationContext(), getString(R.string.marked_as_read), Toast.LENGTH_SHORT).show();
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
                builder.setSingleChoiceItems(styleOptions, db.getStyle(channelId), new DialogInterface.OnClickListener() {
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
                Toast.makeText(getApplicationContext(), getString(R.string.timeline_items_refreshed), Toast.LENGTH_SHORT).show();
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
        TimelineItems = new ArrayList<>();
        adapter = new TimelineListAdapter(this, TimelineItems, user, channelId, channelName, listView, isSourceView, style);
        listView.setAdapter(adapter);
        getTimeLineItems("");
    }

    /**
     * Get items.
     */
    public void getTimeLineItems(String pagerAfter) {

        if (!new Connection(getApplicationContext()).hasConnection()) {
            showRefreshMessage = false;
            checkRefreshingStatus();
            Toast.makeText(getApplicationContext(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        entries.clear();
        int method = Request.Method.GET;
        String MicrosubEndpoint = user.getMicrosubEndpoint();
        olderItems = new String[1];

        if (preview || isSearch) {
            method = Request.Method.POST;
        }
        else {

            MicrosubEndpoint += "?action=timeline&channel=" + channelId;

            // Global unread.
            if (isGlobalUnread) {
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
        StringRequest getRequest = new StringRequest(method, MicrosubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
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

                                // In reply to.
                                if (object.has("in-reply-to")) {
                                    type = "in-reply-to";
                                    String value = getSingleJsonValueFromArrayOrString(type, object);
                                    if (value.length() > 0) {
                                        item.addToResponseType(type, value);
                                        checkReference(object, value, item);
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
                                        checkReference(object, value, item);
                                    }
                                }

                                // Quotation of.
                                if (object.has("quotation-of")) {
                                    type = "quotation-of";
                                    String value = getSingleJsonValueFromArrayOrString(type, object);
                                    if (value.length() > 0) {
                                        item.addToResponseType(type, value);
                                        checkReference(object, value, item);
                                    }
                                }

                                // Like.
                                if (object.has("like-of")) {
                                    type = "like-of";
                                    addContent = false;
                                    String value = getSingleJsonValueFromArrayOrString(type, object);
                                    if (value.length() > 0) {
                                        item.addToResponseType(type, value);
                                        checkReference(object, value, item);
                                    }
                                }

                                // Bookmark.
                                if (object.has("bookmark-of")) {
                                    type = "bookmark-of";
                                    addContent = false;
                                    String value = getSingleJsonValueFromArrayOrString(type, object);
                                    if (value.length() > 0) {
                                        item.addToResponseType(type, value);
                                        checkReference(object, value, item);
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

                                // Author.
                                if (object.has("author")) {

                                    JSONObject author = object.getJSONObject("author");
                                    if (author.has("name")) {
                                        authorName = author.getString("name");
                                    }
                                    String authorUrl = "";
                                    if (author.has("url")) {
                                        authorUrl = author.getString("url");
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

                                // Set values of name, text and html content.
                                item.setName(name);
                                item.setTextContent(textContent);
                                item.setHtmlContent(htmlContent);

                                TimelineItems.add(item);
                            }

                            adapter.notifyDataSetChanged();

                            // Notify
                            if (!isGlobalUnread && !isSourceView && (unread > 0 || unread == -1) && entries.size() > 0
                                    && Preferences.getPreference(getApplicationContext(), "pref_key_mark_read", MARK_READ_CHANNEL_CLICK) == MARK_READ_CHANNEL_CLICK
                                    && !readLater.equals(channelId)) {
                                new MicrosubAction(TimelineActivity.this, user).markRead(channelId, entries, false);
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
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.timeline_parse_error), e.getMessage()), Toast.LENGTH_LONG).show();
                        }

                        checkRefreshingStatus();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                            int code = networkResponse.statusCode;
                            String result = new String(networkResponse.data).trim();
                            Toast.makeText(TimelineActivity.this, String.format(getString(R.string.timeline_network_fail), code, result), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(TimelineActivity.this, getString(R.string.timeline_fail), Toast.LENGTH_LONG).show();
                        }

                        showRefreshMessage = false;
                        checkRefreshingStatus();
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
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }

        };

        getRequest.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(getRequest);

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
     * Returns the reference content.
     *
     * @param object
     *   A JSON object.
     * @param url
     *   The url to find in references
     * @param item
     *   The current timeline item.
     */
    private void checkReference(JSONObject object, String url, TimelineItem item) {

        if (object.has("refs")) {
            try {
                JSONObject references = object.getJSONObject("refs");
                if (references.has(url)) {
                    JSONObject ref = references.getJSONObject(url);

                    // Content.
                    if (ref.has("content")) {
                        JSONObject content = ref.getJSONObject("content");
                        if (content.has("text")) {
                            item.setReference(content.getString("text"));
                        }
                    }
                    else if (ref.has("summary")) {
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

                }
            }
            catch (JSONException ignored) { }
        }
    }
}
