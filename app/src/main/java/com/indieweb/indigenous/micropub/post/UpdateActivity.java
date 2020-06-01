package com.indieweb.indigenous.micropub.post;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.PostListItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.HTTPRequest;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.util.VolleyRequestListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity implements SendPostInterface, VolleyRequestListener {

    private TextView url;
    private Switch postStatus;
    private EditText title;
    private EditText body;
    private MenuItem sendItem;
    private User user;
    public RelativeLayout progressBar;
    private RelativeLayout layout;
    protected VolleyRequestListener volleyRequestListener;
    private PostListItem item = new PostListItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_update);
        super.onCreate(savedInstanceState);

        // Get default user.
        user = new Accounts(this).getDefaultUser();

        layout = findViewById(R.id.update_root);
        url = findViewById(R.id.url);
        postStatus = findViewById(R.id.postStatus);
        title = findViewById(R.id.title);
        body = findViewById(R.id.body);
        progressBar = findViewById(R.id.progressBar);

        // Set listener.
        VolleyRequestListener(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String status = extras.getString("status");
            if (status != null && status.equals("draft")) {
                postStatus.setChecked(false);
            }

            String urlToUpdate = extras.getString("url");
            if (urlToUpdate != null && urlToUpdate.length() > 0) {
                if (URLUtil.isValidUrl(urlToUpdate)) {
                    url.setText(urlToUpdate);
                    getPostFromServer(urlToUpdate);
                }
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_update_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        if (TextUtils.isEmpty(url.getText())) {
            url.setError(getString(R.string.required_field));
        }
        else {
            updatePost(item);
        }
    }

    /**
     * Send update post.
     */
    public void updatePost(MenuItem item) {
        sendItem = item;

        if (!Utility.hasConnection(getApplicationContext())) {
            Snackbar.make(layout, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();

        String endpoint = user.getMicropubEndpoint();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressBar();
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = Utility.parseNetworkError(error, getApplicationContext(), R.string.post_update_network_fail, R.string.post_update_fail);
                        Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
                        hideProgressBar();
                    }
                }
        )
        {
            @Override
            public byte[] getBody() {

                try {
                    JSONObject root = new JSONObject();
                    root.put("action", "update");
                    root.put("url", url.getText().toString());

                    // Replace.
                    JSONObject replace = new JSONObject();

                    // Title
                    if (!TextUtils.isEmpty(title.getText())) {
                        JSONArray titleArray = new JSONArray();
                        titleArray.put(title.getText().toString());
                        replace.put("name", titleArray);
                    }

                    // Content
                    if (!TextUtils.isEmpty(body.getText())) {
                        JSONArray bodyArray = new JSONArray();
                        bodyArray.put(body.getText().toString());
                        replace.put("content", bodyArray);
                    }

                    // Post status.
                    if (postStatus != null) {
                        String postStatusValue = "draft";
                        if (postStatus.isChecked()) {
                            postStatusValue = "published";
                        }
                        JSONArray statusArray = new JSONArray();
                        statusArray.put(postStatusValue);
                        replace.put("post-status", statusArray);
                    }

                    root.put("replace", replace);
                    return root.toString().getBytes();
                }
                catch (JSONException ignored) { }

                String root = "{}";
                return root.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

    }

    /**
     * Show progress bar and disable send menu item.
     */
    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (sendItem != null) {
            sendItem.setEnabled(false);
        }

    }

    /**
     * Hide progress bar and enable send menu item.
     */
    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (sendItem != null) {
            sendItem.setEnabled(true);
        }

    }

    /**
     * Get data from server.
     *
     * @param url
     *   The url to fetch data from.
     */
    public void getPostFromServer(String url) {
        String MicropubEndpoint = user.getMicropubEndpoint();
        if (MicropubEndpoint.contains("?")) {
            MicropubEndpoint += "&q=source";
        }
        else {
            MicropubEndpoint += "?q=source";
        }

        MicropubEndpoint += "&url=" + url;

        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, user, getApplicationContext());
        r.doGetRequest(MicropubEndpoint);
    }

    @Override
    public void OnSuccessRequest(String response) {
        JSONObject object;

        try {
            JSONObject root = new JSONObject(response);
            object = root.getJSONObject("properties");

            String url = "";
            String name = "";
            String content = "";
            String published = "";
            String postStatus = "";

            // url.
            if (object.has("url")) {
                url = object.getJSONArray("url").get(0).toString();
            }
            item.setUrl(url);

            // post status.
            if (object.has("post-status")) {
                postStatus = object.getJSONArray("post-status").get(0).toString();
            }
            item.setPostStatus(postStatus);

            // published.
            if (object.has("published")) {
                published = object.getJSONArray("published").get(0).toString();
            }
            item.setPublished(published);

            // content.
            if (object.has("content")) {
                boolean hasContent = false;
                try {
                    // Use text first, as the overview is simple, and not a full overview.
                    JSONObject c = object.getJSONArray("content").getJSONObject(0);
                    if (c.has("text")) {
                        hasContent = true;
                        content = c.getString("text");
                    }
                    else if (c.has("html")) {
                        hasContent = true;
                        content = c.getString("html");
                    }
                }
                catch (JSONException ignored) {}

                // No content yet, content might be just a string in the first key.
                if (!hasContent) {
                    try {
                        content = object.getJSONArray("content").get(0).toString();
                    }
                    catch (JSONException ignored) {}
                }
            }
            item.setContent(content);

            // name.
            if (object.has("name")) {
                name = object.getJSONArray("name").get(0).toString();
            }
            item.setName(name);
        }
        catch (JSONException ignored) {}

        if (item.getName().length() > 0 || item.getContent().length() > 0) {
            if (item.getName().length() > 0) {
                title.setText(item.getName());
            }
            if (item.getContent().length() > 0) {
                body.setText(item.getContent());
            }
            if (!item.getPostStatus().equals("published")) {
                postStatus.setChecked(false);
            }
            Snackbar.make(layout, getString(R.string.source_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void OnFailureRequest(VolleyError error) {
        String message = getString(R.string.request_failed_unknown);
        try {
            message = Utility.parseNetworkError(error, getApplicationContext(), R.string.request_failed, R.string.request_failed_unknown);
        }
        catch (Exception ignored) {}
        Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Set request listener.
     *
     * @param volleyRequestListener
     *   The volley request listener.
     */
    private void VolleyRequestListener(VolleyRequestListener volleyRequestListener) {
        this.volleyRequestListener = volleyRequestListener;
    }

}
