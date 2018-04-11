package com.indieweb.indigenous.microsub.timeline;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.R;

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

public class TimelineActivity extends AppCompatActivity {

    String channelId;
    String channelName;
    String entryId;
    Integer unread;
    private TimelineListAdapter adapter;
    private List<TimelineItem> TimelineItems = new ArrayList<TimelineItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        ListView listView = findViewById(R.id.timeline_list);
        adapter = new TimelineListAdapter(this, TimelineItems);
        listView.setAdapter(adapter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            channelId = extras.getString("channelId");
            unread = extras.getInt("unread");
            channelName = extras.getString("channelName");
            this.setTitle(channelName);
            getTimeLineItems();
        }
        else {
            Toast.makeText(this, "Channel not found", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Notify the server that all is read.
     */
    public void notifyAllRead() {
        final SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String MicrosubEndpoint = preferences.getString("microsub_endpoint", "");

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
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");

                // Add access token to header.
                SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
                String AccessToken = preferences.getString("access_token", "");
                headers.put("Authorization", "Bearer " + AccessToken);

                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
    }

    /**
     * Get items in channel.
     */
    public void getTimeLineItems() {

        // TODO abstract this all in one helper request class.
        // probably use jsonArrayRequest too, will be faster, but we'll see once we get all
        // kind of calls more or less ready.
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String MicrosubEndpoint = preferences.getString("microsub_endpoint", "");
        MicrosubEndpoint += "?action=timeline&channel=" + channelId;

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
                                TimelineItem item = new TimelineItem();

                                item.setId(object.getString("_id"));
                                if (i == 0) {
                                    entryId = item.getId();
                                }

                                String url = "";
                                String name = "";
                                String textContent = "";
                                String htmlContent = "";
                                String photo = "";
                                String audio = "";
                                String authorName = "";
                                String authorPhoto = "";

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

                                // Author name.
                                if (object.has("author")) {

                                    authorName = object.getJSONObject("author").getString("name");
                                    String authorUrl = object.getJSONObject("author").getString("url");
                                    if (authorName.equals("null") && authorUrl.length() > 0) {
                                        authorName = authorUrl;
                                    }
                                    authorPhoto = object.getJSONObject("author").getString("photo");
                                    if (!authorPhoto.equals("null")) {
                                        item.setAuthorPhoto(authorPhoto);
                                    }
                                }
                                item.setAuthorName(authorName);

                                // Content.
                                if (object.has("content")) {
                                    JSONObject content = object.getJSONObject("content");

                                    textContent = content.getString("text");

                                    // in-reply-to
                                    // TODO there can me more than one and fix this otherwise
                                    // as I think it's in the name as well
                                    if (object.has("in-reply-to")) {
                                        textContent += ", in reply to " + object.getJSONArray("in-reply-to").get(0);
                                    }

                                    if (content.has("html")) {
                                        htmlContent = content.getString("html");

                                        // Clean html, remove images and put them in photo.
                                        // No fully ideal, but it's a good start.
                                        Document doc = Jsoup.parse(htmlContent);
                                        Elements imgs = doc.select("img");
                                        for (Element img : imgs) {
                                            photo = img.absUrl("src");
                                        }
                                        htmlContent = Jsoup.clean(htmlContent, Whitelist.basic());
                                    }

                                }

                                // Name.
                                if (object.has("name")) {
                                    name = object.getString("name").replace("\n", "").replace("\r", "");
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

                                // A like.
                                // TODO there can me more than one.
                                // TODO it seems this is set in name, so we can probably remove this
                                /*if (object.has("like-of")) {
                                    text = "like " + object.getJSONArray("like-of").get(0);
                                    // reset name.
                                    name = "";
                                }*/

                                // A checkin.
                                // TODO store in different property
                                if (object.has("checkin")) {
                                    textContent = "Checked in at " + object.getJSONObject("checkin").getString("name");
                                }

                                item.setName(name);
                                item.setTextContent(textContent);
                                item.setHtmlContent(htmlContent);

                                TimelineItems.add(item);
                            }

                            adapter.notifyDataSetChanged();

                            if (unread > 0) {
                                notifyAllRead();
                            }

                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            Log.d("indigenous_debug", e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_LONG).show();
                        Log.d("indigenous_debug", error.getMessage());
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");

                // Add access token to header.
                SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
                String AccessToken = preferences.getString("access_token", "");
                headers.put("Authorization", "Bearer " + AccessToken);

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
