package com.indieweb.indigenous.micropub;

import android.accounts.AccountManager;
import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Contact;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicropubAction {

    private final Context context;
    private final User user;

    public MicropubAction(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    /**
     * Delete a post.
     *
     * @param url
     *   The url to delete.
     */
    public void deletePost(final String url) {

        if (!new Connection(context).hasConnection()) {
            Toast.makeText(context, context.getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, R.string.sending_please_wait, Toast.LENGTH_SHORT).show();
        String MicropubEndpoint = user.getMicropubEndpoint();
        StringRequest getRequest = new StringRequest(Request.Method.POST, MicropubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, R.string.post_delete_success, Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(context, String.format(context.getString(R.string.delete_post_network_error), code, result), Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(context, String.format(context.getString(R.string.delete_post_error), error.getMessage()), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(context, String.format(context.getString(R.string.delete_post_error), e.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Send along access token if configured.
                if (Preferences.getPreference(context, "pref_key_access_token_body", false)) {
                    params.put("access_token", user.getAccessToken());
                }

                // Put url and action.
                params.put("url", url);
                params.put("action", "delete");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");

                // Send access token in header by default.
                if (!Preferences.getPreference(context, "pref_key_access_token_body", false)) {
                    headers.put("Authorization", "Bearer " + user.getAccessToken());
                }

                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(getRequest);

    }

    /**
     * Prepare tags autocomplete.
     *
     * @param tags
     *   The tags widget.
     */
    void prepareTagsAutocomplete(final MultiAutoCompleteTextView tags) {

        // If there's no connection, get it from local.
        if (!new Connection(context).hasConnection()) {
            AccountManager am = AccountManager.get(context);
            String response = am.getUserData(user.getAccount(), "tags_list");
            parseTagsResponse(response, tags, false, null);
            return;
        }

        // Get tags from the endpoint.
        String MicropubEndpoint = user.getMicropubEndpoint();
        if (MicropubEndpoint.contains("?")) {
            MicropubEndpoint += "&q=category";
        }
        else {
            MicropubEndpoint += "?q=category";
        }

        StringRequest getRequest = new StringRequest(Request.Method.GET, MicropubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseTagsResponse(response, tags, true, user);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
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

    /**
     * Parse tags response.
     *
     * @param response
     *   Parse tags response.
     * @param tags
     *   The autocomplete field
     * @param saveInAccount
     *   Whether to set the tags list or not.
     * @param user
     *   The user.
     */
    private void parseTagsResponse(String response, MultiAutoCompleteTextView tags, boolean saveInAccount, User user) {
        ArrayList<String> items = new ArrayList<>();

        try {
            JSONObject categoryResponse = new JSONObject(response);
            if (categoryResponse.has("categories")) {
                JSONArray tagsList = categoryResponse.getJSONArray("categories");
                if (tagsList.length() > 0) {
                    for (int i = 0; i < tagsList.length(); i++) {
                        items.add(tagsList.getString(i));
                    }
                }
            }
        }
        catch (JSONException ignored) {}

        if (items.size() > 0) {
            setTagsAutocomplete(tags, items);

            if (saveInAccount) {
                AccountManager am = AccountManager.get(context);
                am.setUserData(user.getAccount(), "tags_list", response);
            }
        }

    }

    /**
     * Sets tags autcomplete.
     */
    private void setTagsAutocomplete(MultiAutoCompleteTextView tags, ArrayList<String> items) {
        tags.setThreshold(1);
        tags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        tags.setAdapter(new ArrayAdapter<>(context, R.layout.popup_item, items));
    }

    /**
     * Prepare contact autocomplete.
     *
     * @param body
     *   The body widget.
     */
    void prepareContactAutocomplete(final MultiAutoCompleteTextView body) {

        // If there's no connection, get it from local.
        if (!new Connection(context).hasConnection()) {
            AccountManager am = AccountManager.get(context);
            String response = am.getUserData(user.getAccount(), "contact_list");
            parseContactsResponse(response, body, false, null);
            return;
        }

        // Get contacts from the endpoint.
        String MicropubEndpoint = user.getMicropubEndpoint();
        if (MicropubEndpoint.contains("?")) {
            MicropubEndpoint += "&q=contact";
        }
        else {
            MicropubEndpoint += "?q=contact";
        }

        StringRequest getRequest = new StringRequest(Request.Method.GET, MicropubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        parseContactsResponse(response, body, true, user);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {}
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

    /**
     * Parse contacts response.
     *
     * @param response
     *   Parse tags response.
     * @param body
     *   The autocomplete field
     * @param saveInAccount
     *   Whether to set the tags list or not.
     * @param user
     *   The user.
     */
    private void parseContactsResponse(String response, MultiAutoCompleteTextView body, boolean saveInAccount, User user) {
        ArrayList<Contact> contactItems = new ArrayList<>();

        try {
            JSONObject categoryResponse = new JSONObject(response);
            if (categoryResponse.has("contacts")) {
                JSONObject contactObject;
                JSONArray contactList = categoryResponse.getJSONArray("contacts");
                if (contactList.length() > 0) {
                    for (int i = 0; i < contactList.length(); i++) {
                        contactObject = contactList.getJSONObject(i);
                        if (contactObject.has("name")) {
                            Contact contact = new Contact();
                            contact.setName(contactObject.getString("name"));

                            if (contactObject.has("nickname")) {
                                if (Preferences.getPreference(context, "pref_key_contact_body_autocomplete_value", false)) {
                                    contact.setName(contactObject.getString("nickname"));
                                }
                                contact.setNickname(contactObject.getString("nickname"));
                            }

                            if (contactObject.has("url")) {
                                contact.setUrl(contactObject.getString("url"));
                            }

                            if (contactObject.has("photo")) {
                                contact.setPhoto(contactObject.getString("photo"));
                            }

                            contactItems.add(contact);
                        }
                    }
                }
            }
        }
        catch (JSONException ignored) {}

        if (contactItems.size() > 0) {
            setContactsAutocomplete(body, contactItems);

            if (saveInAccount) {
                AccountManager am = AccountManager.get(context);
                am.setUserData(user.getAccount(), "contact_list", response);
            }
        }

    }


    /**
     * Sets contact autocomplete.
     */
    private void setContactsAutocomplete(MultiAutoCompleteTextView body, List<Contact> contactItems) {
        body.setThreshold(1);
        ArrayAdapter contactAdapter = new ArrayAdapter<>(context, R.layout.popup_item, contactItems);
        body.setAdapter(contactAdapter);

        body.setTokenizer(new MultiAutoCompleteTextView.Tokenizer() {
            @Override
            public CharSequence terminateToken(CharSequence text) {
                int i = text.length();

                while (i > 0 && text.charAt(i - 1) == ' ') {
                    i--;
                }

                if (i > 0 && text.charAt(i - 1) == ' ') {
                    return text;
                } else {
                    if (text instanceof Spanned) {
                        SpannableString sp = new SpannableString(text + " ");
                        TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
                        return sp;
                    } else {
                        return text + " ";
                    }
                }
            }

            @Override
            public int findTokenStart(CharSequence text, int cursor) {
                int i = cursor;

                while ((i > 0 && text.charAt(i - 1) != '@')) {
                    i--;
                }


                // Check if token really started with @, else we don't have a valid token.
                if (i < 1 || text.charAt(i - 1) != '@') {
                    return cursor;
                }

                return i;
            }

            @Override
            public int findTokenEnd(CharSequence text, int cursor) {
                int i = cursor;
                int len = text.length();

                while (i < len) {
                    if (text.charAt(i) == ' ') {
                        return i;
                    } else {
                        i++;
                    }
                }

                return len;
            }
        });
    }

}
