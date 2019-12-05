package com.indieweb.indigenous.micropub.post;

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
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity implements SendPostInterface {

    private EditText url;
    private Switch postStatus;
    private EditText title;
    private EditText body;
    private MenuItem sendItem;
    private User user;
    public RelativeLayout progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_update);
        super.onCreate(savedInstanceState);

        // Get current user.
        user = new Accounts(this).getCurrentUser();

        url = findViewById(R.id.url);
        postStatus = findViewById(R.id.postStatus);
        title = findViewById(R.id.title);
        body = findViewById(R.id.body);
        progressBar = findViewById(R.id.progressBar);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String incomingText = extras.getString("incomingText");
            if (incomingText != null && incomingText.length() > 0) {
                if (URLUtil.isValidUrl(incomingText)) {
                    url.setText(incomingText);
                }
                else {
                    body.setText(incomingText);
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

        if (!new Connection(this).hasConnection()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        showProgressBar();

        if (sendItem != null) {
            sendItem.setEnabled(false);
        }

        String endpoint = user.getMicropubEndpoint();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), getString(R.string.post_update_success), Toast.LENGTH_SHORT).show();
                        hideProgressBar();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                                Integer code = networkResponse.statusCode;
                                String result = new String(networkResponse.data);
                                Toast.makeText(getApplicationContext(), String.format(getString(R.string.post_update_network_fail), code, result), Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), String.format(getString(R.string.post_update_fail), error.getMessage()), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.post_update_fail), error.getMessage()), Toast.LENGTH_LONG).show();
                        }

                        if (sendItem != null) {
                            sendItem.setEnabled(true);
                        }

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
     * Show progress bar.
     */
    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide progress bar.
     */
    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

}
