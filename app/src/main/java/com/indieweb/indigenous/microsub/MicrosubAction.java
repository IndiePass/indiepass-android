package com.indieweb.indigenous.microsub;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Connection;

import java.util.HashMap;
import java.util.Map;

public class MicrosubAction {

    private final Context context;
    private final User user;

    public MicrosubAction(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    /**
     * Notify the server that all is read.
     */
    public void notifyAllRead(final String channelId, final String entryId) {
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
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(getRequest);
    }

    /**
     * Delete post.
     */
    public void deletePost(final String channelId, final String postId) {

        if (!new Connection(context).hasConnection()) {
            Toast.makeText(context, context.getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();

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
                params.put("method", "remove");
                params.put("channel", channelId);
                params.put("entry", postId);

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

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(getRequest);
    }
}
