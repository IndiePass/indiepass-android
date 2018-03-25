package com.indieweb.indigenous;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeLineActivity extends AppCompatActivity {

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
        assert extras != null;
        String channelId = extras.getString("channelId");

        getTimeLineItems(channelId);
    }

    /**
     * Get items in channel.
     */
    public void getTimeLineItems(String channelId) {

        // TODO abstract this all in one helper request class.
        // probably use jsonArrayRequest too, will be faster, but we'll see once we get all
        // kind of calls more or less ready.
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String microbSubEndPoint = preferences.getString("microsub_endpoint", "");
        microbSubEndPoint += "?action=timeline&channel=" + channelId;

        StringRequest getRequest = new StringRequest(Request.Method.GET, microbSubEndPoint,
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

                                String name = "";
                                String text = "";
                                String photo = "";
                                String authorName = "Swentel";

                                // Author name.
                                if (object.has("author")) {

                                    authorName = object.getJSONObject("author").getString("name");
                                    String authorUrl = object.getJSONObject("author").getString("url");
                                    Log.d("indi_author_name", authorName);
                                    Log.d("indi_author_url", authorUrl);
                                    if (authorName.equals("null") && authorUrl.length() > 0) {
                                        Log.d("indi_author_replace", "yes");
                                        authorName = authorUrl;
                                    }
                                }
                                item.setAuthorName(authorName);

                                // Content.
                                if (object.has("content")) {
                                    JSONObject content = object.getJSONObject("content");

                                    text = content.getString("text");

                                    // in-reply-to
                                    // TODO there can me more than one.
                                    if (object.has("in-reply-to")) {
                                        text += ", in reply to " + object.getJSONArray("in-reply-to").get(0);
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

                                // A like.
                                // TODO there can me more than one.
                                // TODO it seems this is set in name, so we can probably remove this
                                /*if (object.has("like-of")) {
                                    text = "like " + object.getJSONArray("like-of").get(0);
                                    // reset name.
                                    name = "";
                                }*/

                                // A checkin.
                                if (object.has("checkin")) {
                                    text = "Checked in at " + object.getJSONObject("checkin").getString("name");
                                }

                                item.setName(name);
                                item.setContent(text);
                                TimelineItems.add(item);
                            }

                            adapter.notifyDataSetChanged();

                        }
                        catch (JSONException e) {
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

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);

    }

}
