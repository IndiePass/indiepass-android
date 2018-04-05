package com.indieweb.indigenous;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LikeActivity extends AppCompatActivity {

    Button createLike;
    EditText url;
    EditText tags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_like);

        createLike = findViewById(R.id.createLikeButton);
        createLike.setOnClickListener(doCreateLike);

    }

    /**
     * OnClickListener for the 'create like' button.
     */
    private final View.OnClickListener doCreateLike = new View.OnClickListener() {
        public void onClick(View v) {

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            createLike.setEnabled(false);

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

                            createLike.setEnabled(true);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Like posting failed", Toast.LENGTH_LONG).show();
                            Log.d("indigenous_debug", error.getMessage());
                            createLike.setEnabled(true);
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
    };

}
