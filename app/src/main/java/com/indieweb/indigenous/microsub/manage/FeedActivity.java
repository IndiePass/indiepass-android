package com.indieweb.indigenous.microsub.manage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.microsub.timeline.TimelineActivity;
import com.indieweb.indigenous.model.Feed;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedActivity extends AppCompatActivity implements View.OnClickListener {

    String channelId;
    String channelName;
    String shareUrl;
    EditText url;
    Button search;
    User user;
    TextView resultTitle;
    ListView feedResults;
    private List<Feed> Feeds = new ArrayList<>();
    private FeedsResultAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feed);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            channelId = extras.getString("channelId");
            channelName = extras.getString("channelName");
            shareUrl = extras.getString("url");
            this.setTitle("Add feed in " + channelName);
            user = new Accounts(this).getCurrentUser();

            feedResults = findViewById(R.id.feedResults);
            url = findViewById(R.id.url);
            search = findViewById(R.id.search);
            search.setOnClickListener(this);
            resultTitle = findViewById(R.id.resultsTitle);

            // Click search feed if url is added.
            if (shareUrl != null && shareUrl.length() > 0) {
                url.setText(shareUrl);
                search.performClick();
            }

            adapter = new FeedsResultAdapter(this, Feeds);
            feedResults.setAdapter(adapter);
        }
        else {
            Toast.makeText(this, "Channel not found", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                if (!TextUtils.isEmpty(url.getText())) {
                    searchFeeds(url.getText().toString());
                }
                else {
                    url.setError(getString(R.string.required_field));
                }
                break;
        }
    }

    /**
     * Search feeds.
     */
    public void searchFeeds(final String url) {

        if (!new Connection(getApplicationContext()).hasConnection()) {
            Toast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(getApplicationContext(), "Searching, stay tuned", Toast.LENGTH_LONG).show();

        String MicrosubEndpoint = user.getMicrosubEndpoint();
        StringRequest getRequest = new StringRequest(Request.Method.POST, MicrosubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object;
                            JSONObject microsubResponse = new JSONObject(response);
                            JSONArray itemList = microsubResponse.getJSONArray("results");

                            Feeds.clear();
                            for (int i = 0; i < itemList.length(); i++) {
                                object = itemList.getJSONObject(i);
                                Feed item = new Feed();
                                item.setUrl(object.getString("url"));
                                item.setType(object.getString("type"));
                                item.setChannel(channelId);
                                Feeds.add(item);
                            }

                            if (Feeds.size() > 0) {
                                resultTitle.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "No results found, please try again.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Something went wrong, please try again.", Toast.LENGTH_LONG).show();
                    }
                }
        )
        {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("action", "search");
                params.put("query", url);
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

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
    }

    /**
     * Feed items list adapter.
     */
    public class FeedsResultAdapter extends BaseAdapter implements View.OnClickListener {

        private final Context context;
        private final List<Feed> items;
        private LayoutInflater mInflater;

        FeedsResultAdapter(Context context, List<Feed> items) {
            this.context = context;
            this.items = items;
            this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return items.size();
        }

        public Feed getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public void onClick(View view) {}

        public class ViewHolder {
            public TextView url;
            public Button subscribe;
            public Button preview;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.list_item_feed_search, null);
                holder = new ViewHolder();
                holder.url = convertView.findViewById(R.id.url);
                holder.subscribe = convertView.findViewById(R.id.subscribe);
                holder.preview = convertView.findViewById(R.id.preview);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Feed item = items.get(position);
            if (item != null) {
                holder.url.setText(item.getType() + "\n" + item.getUrl());
                holder.subscribe.setOnClickListener(new FeedsResultAdapter.OnSubscribeClickListener(position));
                holder.preview.setOnClickListener(new FeedsResultAdapter.OnPreviewClickListener(position));
            }

            return convertView;
        }

        // Subscribe listener.
        class OnSubscribeClickListener implements View.OnClickListener {

            int position;

            OnSubscribeClickListener(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                final Feed feed = items.get(this.position);

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Subscribe to feed '"+ feed.getUrl() +"' ?");
                builder.setPositiveButton(context.getString(R.string.subscribe),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        new MicrosubAction(context, user).subscribe(feed.getUrl(), feed.getChannel());
                        finish();
                    }
                });
                builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        }

        // Preview listener.
        class OnPreviewClickListener implements View.OnClickListener {

            int position;

            OnPreviewClickListener(int position) {
                this.position = position;
            }

            @Override
            public void onClick(View v) {
                final Feed feed = items.get(this.position);
                Intent intent = new Intent(context, TimelineActivity.class);
                intent.putExtra("preview", true);
                intent.putExtra("previewUrl", feed.getUrl());
                context.startActivity(intent);
            }
        }

    }

}
