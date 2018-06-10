package com.indieweb.indigenous.micropub.post;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Syndication;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookmarkActivity extends AppCompatActivity {

    EditText title;
    EditText body;
    EditText url;
    EditText tags;
    LinearLayout syndicationLayout;
    private List<Syndication> Syndications = new ArrayList<>();
    private MenuItem sendItem;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        title = findViewById(R.id.bookmarkTitle);
        body = findViewById(R.id.bookmarkText);
        user = new Accounts(this).getCurrentUser();

        // TODO make helper function.
        int index = 0;
        syndicationLayout = findViewById(R.id.syndicate);
        String syndicationsString = user.getSyndicationTargets();
        if (syndicationsString.length() > 0) {
            JSONObject object;
            try {
                JSONObject s = new JSONObject(syndicationsString);
                JSONArray itemList = s.getJSONArray("syndicate-to");
                for (int i = 0; i < itemList.length(); i++) {
                    object = itemList.getJSONObject(i);
                    Syndication syndication = new Syndication();
                    syndication.setUid(object.getString("uid"));
                    syndication.setName(object.getString("name"));
                    Syndications.add(syndication);

                    CheckBox ch = new CheckBox(this);
                    ch.setText(syndication.getName());
                    ch.setId(index);
                    syndicationLayout.addView(ch);
                    index++;
                }

            }
            catch (JSONException e) {
                Log.d("indigenous_debug", e.getMessage());
            }
        }

        // Check incoming text or image.
        Intent intent = getIntent();
        url = findViewById(R.id.bookmarkUrl);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action)) {
                try {
                    if (extras.containsKey(Intent.EXTRA_TEXT)) {
                        String urlIncoming = extras.get(Intent.EXTRA_TEXT).toString();
                        if (urlIncoming != null && urlIncoming.length() > 0) {
                            url.setText(urlIncoming);
                            send();
                        }
                    }
                }
                catch (NullPointerException ignored) {}
            }
            else {
                String urlIncoming = extras.getString("incomingText");
                if (urlIncoming != null && urlIncoming.length() > 0) {
                    setUrlAndFocusOnTitle(urlIncoming);
                }
            }
        }

    }

    /**
     * Sets the incoming text as URL and puts focus on title field.
     *
     * @param incomingUrl
     *   The incoming URL.
     */
    public void setUrlAndFocusOnTitle(String incomingUrl) {
        url.setText(incomingUrl);
        title.requestFocus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu_send, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.send:
                sendItem = item;
                send();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * OnClickListener for the 'create post' button.
     */
    public void send() {

        if (sendItem != null) {
            sendItem.setEnabled(false);
        }
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        tags = findViewById(R.id.bookmarkTags);

        Toast.makeText(getApplicationContext(), "Sending, please wait", Toast.LENGTH_SHORT).show();

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, user.getMicropubEndpoint(),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                        Toast.makeText(getApplicationContext(), "Post success", Toast.LENGTH_LONG).show();
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
                                Toast.makeText(getApplicationContext(), "Bookmark posting failed. Status code: " + code + "; message: " + result, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        if (sendItem != null) {
                            sendItem.setEnabled(true);
                        }
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Post access token too, Wordpress scans for the token in the body.
                params.put("access_token", user.getAccessToken());

                // name, content, entry and bookmark-of.
                params.put("h", "entry");
                params.put("name", title.getText().toString());
                params.put("content", body.getText().toString());
                params.put("bookmark-of", url.getText().toString());

                // Tags.
                // TODO make sure the UI is ok
                List<String> tagsList = new ArrayList<>(Arrays.asList(tags.getText().toString().split(",")));
                int i = 0;
                for (String tag: tagsList) {
                    tag = tag.trim();
                    if (tag.length() > 0) {
                        params.put("category["+ i +"]", tag);
                        i++;
                    }
                }

                // SyndicationTargets.
                if (Syndications.size() > 0) {
                    CheckBox checkbox;
                    for (int j = 0; j < Syndications.size(); j++) {

                        checkbox = findViewById(j);
                        if (checkbox.isChecked()) {
                            params.put("mp-syndicate-to[" + j + "]", Syndications.get(j).getUid());
                        }
                    }
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

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

    }

}
