package com.indieweb.indigenous;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelsActivity extends AppCompatActivity implements View.OnClickListener, BottomSheetListener {

    private ChannelsListAdapter adapter;
    private List<Channel> Channels = new ArrayList<Channel>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);
        findViewById(R.id.actionButton).setOnClickListener(this);

        ListView listView = findViewById(R.id.channel_list);
        adapter = new ChannelsListAdapter(this, Channels);
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
        String microbSubEndPoint = preferences.getString("microsub_endpoint", "");
        microbSubEndPoint += "?action=channels";

        StringRequest getRequest = new StringRequest(Request.Method.GET, microbSubEndPoint,
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionButton:
                new BottomSheet.Builder(this)
                        .setSheet(R.menu.menu)
                        .setListener(this)
                        .show();
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSheetShown(@NonNull BottomSheet bottomSheet, @Nullable Object object) {}

    @Override
    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem, @Nullable Object o) {
        switch (menuItem.getItemId()) {
            case R.id.createArticle:
                Intent CreateArticle = new Intent(getBaseContext(), ArticleActivity.class);
                startActivity(CreateArticle);
                break;
            case R.id.createNote:
                Intent CreateNote = new Intent(getBaseContext(), NoteActivity.class);
                startActivity(CreateNote);
                break;
            case R.id.createLike:
                Intent CreateLike = new Intent(getBaseContext(), LikeActivity.class);
                startActivity(CreateLike);
                break;
        }
    }

    @Override
    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @Nullable Object o, int i) {}
}
