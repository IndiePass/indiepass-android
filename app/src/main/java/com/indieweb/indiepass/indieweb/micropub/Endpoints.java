package com.indieweb.indiepass.indieweb.micropub;

import android.accounts.AccountManager;
import android.content.Context;
import android.os.StrictMode;

import android.widget.RelativeLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indiepass.R;
import com.indieweb.indiepass.model.HCard;
import com.indieweb.indiepass.model.User;
import com.indieweb.indiepass.util.Utility;
import com.indieweb.indiepass.util.mf2.Mf2Parser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Endpoints {

    private final User user;
    private final Context context;
    private Document doc;
    private String url;
    private final RelativeLayout layout;

    public Endpoints(Context context, User user, RelativeLayout layout) {
        this.context = context;
        this.user = user;
        this.layout = layout;
    }

    /**
     * Refresh configuration.
     */
    public void refresh() {

        String micropubEndpoint = "";
        String microsubEndpoint = "";
        String micropubMediaEndpoint = "";

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            boolean foundInfo = false;
            url = user.getBaseUrl();

            org.jsoup.Connection connection = Jsoup.connect(url);
            org.jsoup.Connection.Response response = connection.execute();

            if (response.hasHeader("Link")) {
                String[] headers = response.header("Link").split(",");
                for (String link : headers) {
                    String[] split = link.split(";");
                    String endpoint = split[0].replace("<", "").replace(">", "").trim();
                    String rel = split[1].trim().replace("rel=", "").replace("\"", "");

                    endpoint = Utility.checkAbsoluteUrl(endpoint, url);

                    switch (rel) {
                        case "micropub":
                            foundInfo = true;
                            micropubEndpoint = endpoint;
                            break;
                        case "microsub":
                            foundInfo = true;
                            microsubEndpoint = endpoint;
                            break;
                        case "micropub_media":
                            foundInfo = true;
                            micropubMediaEndpoint = endpoint;
                            break;
                    }
                }
            }

            doc = connection.get();
            Elements links = doc.select("link[href]");
            for (Element link : links) {
                if (micropubEndpoint.length() == 0 && link.attr("rel").equals("micropub")) {
                    foundInfo = true;
                    micropubEndpoint = Utility.checkAbsoluteUrl(link.attr("abs:href"), url);
                }

                if (micropubMediaEndpoint.length() == 0 && link.attr("rel").equals("micropub_media")) {
                    foundInfo = true;
                    micropubMediaEndpoint = Utility.checkAbsoluteUrl(link.attr("abs:href"), url);
                }

                if (microsubEndpoint.length() == 0 && link.attr("rel").equals("microsub")) {
                    foundInfo = true;
                    microsubEndpoint = Utility.checkAbsoluteUrl(link.attr("abs:href"), url);
                }
            }

            if (foundInfo) {
                AccountManager am = AccountManager.get(context);
                am.setUserData(user.getAccount(), "micropub_endpoint", micropubEndpoint);
                am.setUserData(user.getAccount(), "microsub_endpoint", microsubEndpoint);
                am.setUserData(user.getAccount(), "micropub_media_endpoint", micropubMediaEndpoint);
                Snackbar.make(layout, R.string.account_sync_done, Snackbar.LENGTH_SHORT).show();
            }

            // Get token endpoint call to update avatar or name.
            checkProfileFromTokenEndpoint();

        }
        catch (IllegalArgumentException e) {
            Snackbar.make(layout, String.format(context.getString(R.string.account_sync_error), e.getMessage()), Snackbar.LENGTH_SHORT).show();
        }
        catch (IOException e) {
            Snackbar.make(layout, String.format(context.getString(R.string.domain_connect_error), e.getMessage()), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Use the verify token endpoint for profile.
     */
    private void checkProfileFromTokenEndpoint() {

        String TokenEndpoint = user.getTokenEndpoint();
        StringRequest getRequest = new StringRequest(Request.Method.GET, TokenEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        boolean foundInfo = false;
                        String authorAvatar = "";
                        String authorName = "";

                        try {
                            JSONObject indieAuthResponse = new JSONObject(response);

                            // Check profile key.
                            if (indieAuthResponse.has("profile")) {
                                JSONObject profile = indieAuthResponse.getJSONObject("profile");
                                if (profile.has("name")) {
                                    foundInfo = true;
                                    authorName = profile.getString("name");
                                }
                                if (profile.has("photo")) {
                                    foundInfo = true;
                                    authorAvatar = profile.getString("photo");
                                }
                            }
                        }
                        catch (JSONException ignored) { }

                        // If author name or avatar are still empty, try parsing the HTML.
                        if (authorName.length() == 0 || authorAvatar.length() == 0) {
                            String noProtocolUrl = user.getBaseUrlWithoutProtocol();
                            try {

                                Mf2Parser parser = new Mf2Parser();
                                ArrayList<HCard> cards = parser.parse(doc, new URI(url));

                                for (HCard c : cards) {
                                    if (c.getUrl() != null && c.getName() != null) {
                                        String HCardURL = c.getUrl().replace("https://", "").replace("http://", "");
                                        if (HCardURL.equals(noProtocolUrl) || HCardURL.equals(noProtocolUrl + "/")) {
                                            foundInfo = true;
                                            if (authorName.length() == 0) {
                                                authorName = c.getName();
                                            }
                                            if (authorAvatar.length() == 0) {
                                                authorAvatar = c.getAvatar();
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            catch (Exception ignored) { }
                        }

                        if (foundInfo) {
                            AccountManager am = AccountManager.get(context);
                            if (authorName.length() > 0) {
                                am.setUserData(user.getAccount(), "author_name", authorName);
                            }
                            if (authorAvatar.length() > 0) {
                                am.setUserData(user.getAccount(), "author_avatar", authorAvatar);
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
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(getRequest);
    }

}
