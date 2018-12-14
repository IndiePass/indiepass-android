package com.indieweb.indigenous.microsub.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.DebugActivity;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
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

    String channelId;
    String channelName;
    String entryId;
    Integer unread;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        listView = findViewById(R.id.timeline_list);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            channelId = extras.getString("channelId");
            unread = extras.getInt("unread");
            channelName = extras.getString("channelName");
            this.setTitle(channelName);
            loadMoreButton = new Button(this);
            loadMoreButton.setText(R.string.load_more);
            loadMoreButton.setTextColor(getResources().getColor(R.color.textColor));
            loadMoreButton.setBackgroundColor(getResources().getColor(R.color.loadMoreButtonBackgroundColor));
            refreshLayout = findViewById(R.id.refreshTimeline);
            refreshLayout.setOnRefreshListener(this);
            user = new Accounts(this).getCurrentUser();
            startTimeline();
        }
        else {
            Toast.makeText(this, "Channel not found", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_top_menu, menu);

        boolean debugJson = Preferences.getPreference(this, "pref_key_debug_microsub_timeline", false);
        if (debugJson) {
            MenuItem item = menu.findItem(R.id.timeline_debug);
            if (item != null) {
                item.setVisible(true);
            }
        }

        return super.onCreateOptionsMenu(menu);
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
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRefresh() {
        startTimeline();
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            Toast.makeText(getApplicationContext(), getString(R.string.timeline_items_refreshed), Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
        }
    }

    /**
     * Start with the timline.
     */
    public void startTimeline() {
        TimelineItems = new ArrayList<>();
        adapter = new TimelineListAdapter(this, TimelineItems, user, channelId);
        listView.setAdapter(adapter);
        getTimeLineItems("");
    }

    /**
     * Get items in channel.
     */
    public void getTimeLineItems(String pagerAfter) {

        String MicrosubEndpoint = user.getMicrosubEndpoint();
        MicrosubEndpoint += "?action=timeline&channel=" + channelId;
        if (pagerAfter.length() > 0) {
            MicrosubEndpoint += "&after=" + pagerAfter;
        }

        olderItems = new String[1];

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        StringRequest getRequest = new StringRequest(Request.Method.GET, MicrosubEndpoint,
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
                                }
                                catch (JSONException ignored) {}
                            }

                            for (int i = 0; i < itemList.length(); i++) {
                                object = itemList.getJSONObject(i);
                                TimelineItem item = new TimelineItem();
                                item.setJson(itemList.getString(i));

                                Boolean addContent = true;
                                Boolean isRead = false;
                                String type = "entry";
                                String url = "";
                                String name = "";
                                String textContent = "";
                                String htmlContent = "";
                                String audio = "";
                                String video = "";
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
                                // If no value is found, the notify all call will also be ignored.
                                try {
                                    item.setId(object.getString("_id"));
                                    if (entryId == null) {
                                        entryId = item.getId();
                                    }
                                }
                                catch (Exception ignored) {}

                                if (object.has("_is_read")) {
                                    isRead = object.getBoolean("_is_read");
                                }
                                item.setRead(isRead);

                                // In reply to.
                                if (object.has("in-reply-to")) {
                                    type = "in-reply-to";
                                    item.addToSubType(type, object.getJSONArray("in-reply-to").get(0).toString());
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
                                    item.addToSubType(type, object.getJSONArray("repost-of").get(0).toString());
                                }

                                // Like.
                                if (object.has("like-of")) {
                                    type = "like-of";
                                    addContent = false;
                                    item.addToSubType(type, object.getJSONArray("like-of").get(0).toString());
                                }

                                // Bookmark.
                                if (object.has("bookmark-of")) {
                                    type = "bookmark-of";
                                    addContent = false;
                                    item.addToSubType(type, object.getJSONArray("bookmark-of").get(0).toString());
                                }

                                // A checkin.
                                if (object.has("checkin")) {
                                    type = "checkin";
                                    item.addToSubType(type, object.getJSONObject("checkin").getString("name"));
                                    String checkinUrl = "";
                                    try {
                                        checkinUrl = object.getJSONObject("checkin").getString("url");
                                    }
                                    catch (Exception ignored) {}
                                    item.addToSubType("checkin-url", checkinUrl);
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
                                    JSONArray photos = object.getJSONArray("photo");
                                    for (int p = 0; p < photos.length(); p++) {
                                        item.addPhoto(photos.getString(p));
                                    }
                                }

                                // Audio.
                                if (object.has("audio")) {
                                    audio = object.getJSONArray("audio").getString(0);
                                }
                                item.setAudio(audio);

                                // Video.
                                if (object.has("video")) {
                                    video = object.getJSONArray("video").getString(0);
                                }
                                item.setVideo(video);

                                // Set values of name, text and html content.
                                item.setName(name);
                                item.setTextContent(textContent);
                                item.setHtmlContent(htmlContent);

                                TimelineItems.add(item);
                            }

                            adapter.notifyDataSetChanged();

                            // Notify
                            if ((unread > 0 || unread == -1) && entryId != null) {
                                new MicrosubAction(TimelineActivity.this, user).notifyAllRead(channelId, entryId);
                            }

                            if (olderItems[0] != null && olderItems[0].length() > 0) {

                                loadMoreClicked = false;

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
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        checkRefreshingStatus();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.no_posts_found), Toast.LENGTH_SHORT).show();
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
                        loadMoreClicked = true;
                        int downColor = getResources().getColor(R.color.loadMoreButtonBackgroundColor);
                        loadMoreButton.setBackgroundColor(downColor);
                        getTimeLineItems(olderItems[0]);
                    }
                    break;

            }
            return true;
        }
    };

}
