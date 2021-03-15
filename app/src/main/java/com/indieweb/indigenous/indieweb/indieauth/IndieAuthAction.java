// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.indieweb.indieauth;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.model.User;

import java.util.HashMap;
import java.util.Map;

public class IndieAuthAction {

    private final Context context;
    private final User user;

    public IndieAuthAction(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    /**
     * Revoke token.
     */
    public void revoke() {
        String TokenEndpoint = user.getTokenEndpoint();

        StringRequest getRequest = new StringRequest(Request.Method.POST, TokenEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) { }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("action", "revoke");
                params.put("token", user.getAccessToken());

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
