package com.indieweb.indigenous.micropub.post;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.URLUtil;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.indieweb.indigenous.micropub.MicropubAction;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class UpdateActivity extends AppCompatActivity implements SendPostInterface {

    private EditText url;
    private CheckBox postStatus;
    private EditText title;
    private EditText body;
    private MenuItem sendItem;
    private User user;

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
        getMenuInflater().inflate(R.menu.post_update_action_menu, menu);

        boolean deleteEnabled = Preferences.getPreference(this, "pref_key_experimental_delete", false);
        if (!deleteEnabled) {
            MenuItem itemDelete = menu.findItem(R.id.deletePost);
            itemDelete.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deletePost:
                if (TextUtils.isEmpty(url.getText())) {
                    url.setError(getString(R.string.required_field));
                }
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this);
                    builder.setTitle("Are you sure you want to delete this post ?");
                    builder.setPositiveButton(getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int id) {
                            // TODO see if we can use an activity for result here
                            new MicropubAction(UpdateActivity.this, user).deletePost(url.getText().toString());
                            finish();
                        }
                    });
                    builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        // TODO move this to MicropubActionUpdate
        sendItem = item;

        if (!new Connection(this).hasConnection()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if (sendItem != null) {
            sendItem.setEnabled(false);
        }

        Toast.makeText(getApplicationContext(), "Sending, please wait", Toast.LENGTH_SHORT).show();

        String endpoint = user.getMicropubEndpoint();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getApplicationContext(), "Update success", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO refactor this out, we have too many of these.
                        try {
                            NetworkResponse networkResponse = error.networkResponse;
                            if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                                Integer code = networkResponse.statusCode;
                                String result = new String(networkResponse.data);
                                Toast.makeText(getApplicationContext(), "Update failed. Status code: " + code + "; message: " + result, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        if (sendItem != null) {
                            sendItem.setEnabled(true);
                        }
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
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

    }

}
