// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.mastodon;

import android.accounts.AccountManager;
import android.content.Context;
import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.AuthBase;
import com.indieweb.indigenous.util.HTTPRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MastodonAuth extends AuthBase {

    public MastodonAuth(Context context, User user) {
        super(context, user);
    }

    @Override
    public void syncAccount(RelativeLayout layout) {

        String endpoint = getUser().getBaseUrl() + "/api/v1/accounts/verify_credentials";
        StringRequest getRequest = new StringRequest(Request.Method.GET, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        boolean foundInfo = false;
                        String externalId = "";
                        String authorAvatar = "";
                        String authorName = "";

                        try {
                            JSONObject Response = new JSONObject(response);
                            if (Response.has("id")) {
                                foundInfo = true;
                                externalId = Response.getString("id");
                            }
                            if (Response.has("display_name")) {
                                foundInfo = true;
                                authorName = Response.getString("display_name");
                            }
                            else if (Response.has("username")) {
                                foundInfo = true;
                                authorName = Response.getString("username");
                            }
                            if (Response.has("avatar")) {
                                foundInfo = true;
                                authorAvatar = Response.getString("avatar");
                            }
                        }
                        catch (JSONException ignored) { }

                        if (foundInfo) {
                            AccountManager am = AccountManager.get(getContext());
                            if (externalId.length() > 0) {
                                am.setUserData(getUser().getAccount(), "external_id", externalId);
                            }
                            if (authorName.length() > 0) {
                                am.setUserData(getUser().getAccount(), "author_name", authorName);
                            }
                            if (authorAvatar.length() > 0) {
                                am.setUserData(getUser().getAccount(), "author_avatar", authorAvatar);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + getUser().getAccessToken());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(getRequest);
    }

    @Override
    public void revokeToken(User user) {
        String endpoint = user.getBaseUrl() + "/oauth/revoke";
        Map<String, String> params = new HashMap<>();
        params.put("client_id", user.getClientId());
        params.put("client_secret", user.getClientSecret());
        params.put("token", user.getAccessToken());
        HTTPRequest r = new HTTPRequest(null, user, getContext());
        r.doPostRequest(endpoint, params);
    }
}
