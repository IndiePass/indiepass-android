package com.indieweb.indigenous.microsub.timeline;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

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
    String[] olderItems;
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.timeline_list_refresh:
                refreshLayout.setRefreshing(true);
                startTimeline();
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

    public void startTimeline() {
        TimelineItems = new ArrayList<>();
        adapter = new TimelineListAdapter(this, TimelineItems);
        listView.setAdapter(adapter);
        getTimeLineItems("");
    }

    /**
     * Notify the server that all is read.
     */
    public void notifyAllRead() {
        String MicrosubEndpoint = user.getMicrosubEndpoint();

        StringRequest getRequest = new StringRequest(Request.Method.POST, MicrosubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {}
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
                }
        )
        {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("action", "timeline");
                params.put("method", "mark_read");
                params.put("channel", channelId);
                params.put("last_read_entry", entryId);

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
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

                            // TODO refactor this code to crash less.
                            for (int i = 0; i < itemList.length(); i++) {
                                object = itemList.getJSONObject(i);
                                TimelineItem item = new TimelineItem();

                                Boolean isRead = false;
                                String type = "entry";
                                String url = "";
                                String name = "";
                                String textContent = "";
                                String htmlContent = "";
                                String photo = "";
                                String audio = "";
                                String authorName = "";
                                String authorPhoto = "";

                                // Type.
                                if (object.has("type")) {
                                    type = object.getString("type");
                                }

                                // Ignore 'card' type.
                                if (type.equals("card")) {
                                    continue;
                                }

                                item.setId(object.getString("_id"));
                                if (i == 0) {
                                    entryId = item.getId();
                                }

                                if (object.has("_is_read")) {
                                    isRead = object.getBoolean("_is_read");
                                }
                                item.setRead(isRead);

                                // In reply to.
                                // TODO there can me more than one
                                if (object.has("in-reply-to")) {
                                    type = "in-reply-to";
                                    item.addToSubType(type, object.getJSONArray("in-reply-to").get(0).toString());
                                }

                                // Like.
                                if (object.has("like-of")) {
                                    type = "like-of";
                                    item.addToSubType(type, object.getJSONArray("like-of").get(0).toString());
                                }

                                // Like.
                                if (object.has("bookmark-of")) {
                                    type = "bookmark-of";
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
                                        authorPhoto = author.getString("photo");
                                        if (!authorPhoto.equals("null") && authorPhoto.length() > 0) {
                                            item.setAuthorPhoto(authorPhoto);
                                        }
                                    }
                                }
                                item.setAuthorName(authorName);

                                // Content.
                                if (object.has("content")) {
                                    JSONObject content = object.getJSONObject("content");

                                    if (content.has("text")) {
                                        textContent = content.getString("text");
                                    }

                                    if (content.has("html")) {
                                        htmlContent = content.getString("html");

                                        // Clean html, remove images and put them in photo.
                                        // No fully ideal, but it's a good start.
                                        try {
                                            Document doc = Jsoup.parse(htmlContent);
                                            Elements imgs = doc.select("img");
                                            for (Element img : imgs) {
                                                photo = img.absUrl("src");
                                            }
                                            htmlContent = Jsoup.clean(htmlContent, Whitelist.basic());
                                        }
                                        catch (Exception ignored) {}
                                    }

                                }
                                else if(object.has("summary")) {
                                    textContent = object.getString("summary");
                                }

                                // Name.
                                if (object.has("name")) {
                                    name = object.getString("name").replace("\n", "").replace("\r", "");
                                }
                                else if (object.has("summary")) {
                                    name = object.getString("summary").replace("\n", "").replace("\r", "");
                                }

                                // Photo.
                                if (object.has("photo")) {
                                    photo = object.getJSONArray("photo").getString(0);
                                }
                                item.setPhoto(photo);

                                // audio.
                                if (object.has("audio")) {
                                    audio = object.getJSONArray("audio").getString(0);
                                }
                                item.setAudio(audio);

                                // Set values of name, text and html content.
                                item.setName(name);
                                item.setTextContent(textContent);
                                item.setHtmlContent(htmlContent);

                                TimelineItems.add(item);
                            }

                            adapter.notifyDataSetChanged();

                            if (unread > 0) {
                                notifyAllRead();
                            }

                            if (olderItems[0] != null && olderItems[0].length() > 0) {

                                if (!loadMoreButtonAdded) {
                                    loadMoreButtonAdded = true;
                                    listView.addFooterView(loadMoreButton);
                                }

                                loadMoreButton.setOnTouchListener(loadMoreTouch);
                            }
                            else {
                                if (loadMoreButtonAdded) {
                                    listView.removeFooterView(loadMoreButton);
                                }
                            }

                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("indigenous_debug", e.getMessage());
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
                    int downColor = getResources().getColor(R.color.loadMoreButtonBackgroundColor);
                    loadMoreButton.setBackgroundColor(downColor);
                    getTimeLineItems(olderItems[0]);
                    break;

            }
            return true;
        }
    };

}
