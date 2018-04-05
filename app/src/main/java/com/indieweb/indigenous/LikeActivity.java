package com.indieweb.indigenous;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikeActivity extends AppCompatActivity {

    EditText url;
    EditText tags;
    LinearLayout syndicationLayout;
    private List<Syndication> Syndications = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);

        // TODO make helper function.
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String syndicationsString = preferences.getString("syndications", "");
        if (syndicationsString.length() > 0) {
            JSONObject object;
            try {
                syndicationLayout = findViewById(R.id.syndicate);
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
                    ch.setId(i);
                    syndicationLayout.addView(ch);
                }

            }
            catch (JSONException e) {
                Log.d("indigenous_debug", e.getMessage());
            }
        }

        // Set incomingText in content.
        url = findViewById(R.id.likeUrl);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String incoming = extras.getString("incomingText");
            if (incoming != null && incoming.length() > 0) {
                url.setText(incoming);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                send();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Send like.
     */
    public void send() {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        url = findViewById(R.id.likeUrl);
        tags = findViewById(R.id.likeTags);
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String MicropubEndPoint = preferences.getString("micropub_endpoint", "");

        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, MicropubEndPoint,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                        Toast.makeText(getApplicationContext(), "Like success", Toast.LENGTH_LONG).show();

                        Intent Channels = new Intent(getBaseContext(), ChannelsActivity.class);
                        startActivity(Channels);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Like posting failed", Toast.LENGTH_LONG).show();
                        Log.d("indigenous_debug", error.getMessage());
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Url and entry.
                params.put("h", "entry");
                params.put("like-of", url.getText().toString());

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

                // Syndications.
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");

                // Add access token to header.
                SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
                String AccessToken = preferences.getString("access_token", "");
                headers.put("Authorization", "Bearer " + AccessToken);

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
