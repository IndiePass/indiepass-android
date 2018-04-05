package com.indieweb.indigenous.Channel;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.MainActivity;
import com.indieweb.indigenous.Object.Channel;
import com.indieweb.indigenous.PostType.ArticleActivity;
import com.indieweb.indigenous.PostType.LikeActivity;
import com.indieweb.indigenous.PostType.NoteActivity;
import com.indieweb.indigenous.R;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelActivity extends AppCompatActivity implements View.OnClickListener, BottomSheetListener {

    String incomingText = "";
    String incomingImage = "";
    private ChannelListAdapter adapter;
    private List<Channel> Channels = new ArrayList<Channel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);
        findViewById(R.id.actionButton).setOnClickListener(this);

        // Listen to share menu.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (Intent.ACTION_SEND.equals(action)) {
            try {
                assert extras != null;
                if (extras.containsKey(Intent.EXTRA_TEXT)) {
                    incomingText = extras.get(Intent.EXTRA_TEXT).toString();
                }
                if (extras.containsKey(Intent.EXTRA_STREAM)) {
                    incomingImage = extras.get(Intent.EXTRA_STREAM).toString();
                }

                if (incomingText.length() > 0 || incomingImage.length() > 0) {
                    // Open selection.
                    openBottomSheet();
                }
                else {
                    startChannels();
                }
            }
            catch (NullPointerException ignored) {}
        }
        else {
            startChannels();
        }
    }

    /**
     * Start channels.
     */
    public void startChannels() {
        ListView listView = findViewById(R.id.channel_list);
        adapter = new ChannelListAdapter(this, Channels);
        listView.setAdapter(adapter);
        getChannels();
    }

    /**
     * Get channels.
     */
    public void getChannels() {

        // TODO abstract this all in one helper request class.
        // probably use jsonArrayRequest too, will be faster, but we'll see once we get all
        // kind of calls more or less ready.
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String MicrosubEndpoint = preferences.getString("microsub_endpoint", "");
        MicrosubEndpoint += "?action=channels";

        StringRequest getRequest = new StringRequest(Request.Method.GET, MicrosubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject object;
                            JSONObject microsubResponse = new JSONObject(response);
                            JSONArray channelList = microsubResponse.getJSONArray("channels");

                            for (int i = 0; i < channelList.length(); i++) {
                                object = channelList.getJSONObject(i);
                                Channel channel = new Channel();
                                channel.setUid(object.getString("uid"));
                                channel.setName(object.getString("name"));
                                channel.setUnread(object.getInt("unread"));
                                Channels.add(channel);
                            }

                            adapter.notifyDataSetChanged();

                        }
                        catch (JSONException ignored) {}

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_LONG).show();
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
                SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
                String AccessToken = preferences.getString("access_token", "");
                headers.put("Authorization", "Bearer " + AccessToken);

                return headers;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);

    }

    /**
     * Opens the bottom sheet.
     */
    public void openBottomSheet() {
        new BottomSheet.Builder(this)
                .setSheet(R.menu.menu)
                .setListener(this)
                .show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionButton:
                openBottomSheet();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Log out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Remove shared preferences.
                                SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                                editor.clear().apply();

                                // Go to main activity.
                                Intent main = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(main);
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;

            case R.id.refreshSyndications:
                refreshSyndications();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Refresh syndications.
     */
    public void refreshSyndications() {
        final SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
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
                                SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                                editor.putString("syndications", response).apply();
                                Toast.makeText(getApplicationContext(), "Syndications reloaded", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "No syndications found", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_LONG).show();
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
                SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
                String AccessToken = preferences.getString("access_token", "");
                headers.put("Authorization", "Bearer " + AccessToken);

                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
    }

    @Override
    public void onSheetShown(@NonNull BottomSheet bottomSheet, @Nullable Object object) {}

    @Override
    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem, @Nullable Object o) {
        switch (menuItem.getItemId()) {
            case R.id.createArticle:
                Intent CreateArticle = new Intent(getBaseContext(), ArticleActivity.class);
                if (incomingText != null && incomingText.length() > 0) {
                    CreateArticle.putExtra("incomingText", incomingText);
                }
                if (incomingImage != null && incomingImage.length() > 0) {
                    CreateArticle.putExtra("incomingImage", incomingImage);
                }
                startActivity(CreateArticle);
                break;
            case R.id.createNote:
                Intent CreateNote = new Intent(getBaseContext(), NoteActivity.class);
                if (incomingText != null && incomingText.length() > 0) {
                    CreateNote.putExtra("incomingText", incomingText);
                }
                if (incomingImage != null && incomingImage.length() > 0) {
                    CreateNote.putExtra("incomingImage", incomingImage);
                }
                startActivity(CreateNote);
                break;
            case R.id.createLike:
                Intent CreateLike = new Intent(getBaseContext(), LikeActivity.class);
                if (incomingText.length() > 0) {
                    CreateLike.putExtra("incomingText", incomingText);
                }
                startActivity(CreateLike);
                break;
        }
    }

    @Override
    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @Nullable Object o, int i) {}
}
