package com.indieweb.indigenous.pixelfed;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PixelfedAuth extends AuthBase {

    public PixelfedAuth(Context context, User user) {
        super(context, user);
    }

    @Override
    public void syncAccount(RelativeLayout layout) {

        String endpoint = getUser().getMe() + "/api/v1/accounts/verify_credentials";
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

}
