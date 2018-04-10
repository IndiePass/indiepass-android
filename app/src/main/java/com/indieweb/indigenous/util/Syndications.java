package com.indieweb.indigenous.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public class Syndications {

    private final Context context;

    public Syndications(Context context) {
        this.context = context;
    }

    /**
     * Refresh syndication targets.
     */
    public void refresh() {

        final SharedPreferences preferences = context.getSharedPreferences("indigenous", MODE_PRIVATE);
        String microPubEndpoint = preferences.getString("micropub_endpoint", "");
        microPubEndpoint += "?q=syndicate-to";

        StringRequest getRequest = new StringRequest(Request.Method.GET, microPubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject micropubResponse = new JSONObject(response);
                            JSONArray itemList = micropubResponse.getJSONArray("syndicate-to");
                            if (itemList.length() > 0) {
                                SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                                editor.putString("syndications", response).apply();
                                Toast.makeText(context, "Syndications reloaded", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(context, "No syndications found", Toast.LENGTH_LONG).show();
                            }

                        }
                        catch (JSONException e) {
                            Log.d("indigenous_debug", e.getMessage());
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Request failed", Toast.LENGTH_LONG).show();
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
                SharedPreferences preferences = context.getSharedPreferences("indigenous", MODE_PRIVATE);
                String AccessToken = preferences.getString("access_token", "");
                headers.put("Authorization", "Bearer " + AccessToken);

                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(getRequest);

    }

}
