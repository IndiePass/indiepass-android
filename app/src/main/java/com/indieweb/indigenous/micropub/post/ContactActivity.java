package com.indieweb.indigenous.micropub.post;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.BaseCreate;
import com.indieweb.indigenous.model.Contact;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ContactActivity extends BaseCreate {

    TextView contactName;
    TextView contactNickname;
    TextView contactUrl;
    TextView contactPhoto;
    boolean update = false;
    protected Contact contact;
    private MenuItem sendItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Contact";
        hType = "card";

        setContentView(R.layout.activity_contact);

        contactName = findViewById(R.id.contactName);
        contactNickname = findViewById(R.id.contactNickname);
        contactUrl = findViewById(R.id.contactUrl);
        contactPhoto = findViewById(R.id.contactPhoto);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            boolean addContact = extras.getBoolean("addContact");
            boolean updateContact = extras.getBoolean("updateContact");
            if (addContact || updateContact) {
                Indigenous app = Indigenous.getInstance();
                contact = app.getContact();
                if (contact != null) {

                    if (updateContact) {
                        update = true;
                    }

                    contactName.setText(contact.getName());
                    contactNickname.setText(contact.getNickname());
                    contactUrl.setText(contact.getUrl());
                    contactPhoto.setText(contact.getPhoto());
                }
            }
        }

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (TextUtils.isEmpty(contactName.getText()) && !update) {
            hasErrors = true;
            contactName.setError(getString(R.string.required_field));
        }

        if (!hasErrors) {

            if (update) {
                updateContact(item);
            }
            else {

                bodyParams.put("name", contactName.getText().toString());

                if (!TextUtils.isEmpty(contactNickname.getText())) {
                    bodyParams.put("nickname", contactNickname.getText().toString());
                }
                if (!TextUtils.isEmpty(contactUrl.getText())) {
                    bodyParams.put("url", contactUrl.getText().toString());
                }
                if (!TextUtils.isEmpty(contactPhoto.getText())) {
                    bodyParams.put("photo", contactPhoto.getText().toString());
                }

                sendBasePost(item);
            }
        }
    }

    /**
     * Update contact.
     */
    public void updateContact(MenuItem item) {
        sendItem = item;

        if (!Utility.hasConnection(getApplicationContext())) {
            Snackbar.make(layout, getString(R.string.no_connection), Snackbar.LENGTH_LONG).show();
            return;
        }

        showProgressBar();

        if (sendItem != null) {
            sendItem.setEnabled(false);
        }

        String endpoint = user.getMicropubEndpoint();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest request = new StringRequest(Request.Method.POST, endpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        hideProgressBar();
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (sendItem != null) {
                            sendItem.setEnabled(true);
                        }
                        hideProgressBar();
                        String message = Utility.parseNetworkError(error, getApplicationContext(), R.string.post_update_network_fail, R.string.post_update_fail);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }
                }
        )
        {
            @Override
            public byte[] getBody() {

                try {
                    JSONObject root = new JSONObject();
                    root.put("action", "update");
                    root.put("url", contact.getInternalUrl());

                    // Replace.
                    JSONObject replace = new JSONObject();

                    // Name
                    if (!TextUtils.isEmpty(contactName.getText())) {
                        JSONArray nameArray = new JSONArray();
                        nameArray.put(contactName.getText().toString());
                        replace.put("name", nameArray);
                    }

                    // Nickname
                    if (!TextUtils.isEmpty(contactNickname.getText())) {
                        JSONArray nicknameArray = new JSONArray();
                        nicknameArray.put(contactNickname.getText().toString());
                        replace.put("nickname", nicknameArray);
                    }

                    // Url
                    if (!TextUtils.isEmpty(contactUrl.getText())) {
                        JSONArray nameArray = new JSONArray();
                        nameArray.put(contactUrl.getText().toString());
                        replace.put("url", nameArray);
                    }

                    // Photo
                    if (!TextUtils.isEmpty(contactPhoto.getText())) {
                        JSONArray nameArray = new JSONArray();
                        nameArray.put(contactPhoto.getText().toString());
                        replace.put("photo", nameArray);
                    }

                    root.put("replace", replace);
                    return root.toString().getBytes();
                }
                catch (JSONException ignored) { }

                String root = "{}";
                return root.getBytes();
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Content-type", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

}
