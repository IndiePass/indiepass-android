package com.indieweb.indigenous.microsub.channel;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.AboutActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.SettingsActivity;
import com.indieweb.indigenous.micropub.post.ArticleActivity;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.EventActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.MediaActivity;
import com.indieweb.indigenous.micropub.post.NoteActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.micropub.post.UpdateActivity;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.MicropubConfig;
import com.indieweb.indigenous.util.Preferences;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;
import com.kennyc.bottomsheet.menu.BottomSheetMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChannelActivity extends AppCompatActivity implements View.OnClickListener, BottomSheetListener, SwipeRefreshLayout.OnRefreshListener {

    String incomingText = "";
    String incomingImage = "";
    ListView listChannel;
    SwipeRefreshLayout refreshLayout;
    private ChannelListAdapter adapter;
    private List<Channel> Channels = new ArrayList<>();
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channels);
        findViewById(R.id.actionButton).setOnClickListener(this);
        listChannel = findViewById(R.id.channel_list);
        refreshLayout = findViewById(R.id.refreshChannels);
        refreshLayout.setOnRefreshListener(this);

        user = new Accounts(this).getCurrentUser();
        this.setTitle(user.getMeWithoutProtocol());

        startChannels();
    }

    /**
     * Start channels.
     */
    public void startChannels() {
        Channels = new ArrayList<>();
        listChannel.setVisibility(View.VISIBLE);
        adapter = new ChannelListAdapter(this, Channels);
        listChannel.setAdapter(adapter);
        getChannels();
    }

    @Override
    public void onRefresh() {
        startChannels();
    }

    /**
     * Get channels.
     */
    public void getChannels() {

        String microsubEndpoint = user.getMicrosubEndpoint();

        // TODO abstract this all in one helper request class.
        // probably use jsonArrayRequest too, will be faster, but we'll see once we get all
        // kind of calls more or less ready.
        microsubEndpoint += "?action=channels";
        StringRequest getRequest = new StringRequest(Request.Method.GET, microsubEndpoint,
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
                            checkRefreshingStatus();

                        }
                        catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            checkRefreshingStatus();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.channels_not_found), Toast.LENGTH_SHORT).show();
                        checkRefreshingStatus();
                    }
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

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            Toast.makeText(getApplicationContext(), getString(R.string.channels_refreshed), Toast.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
        }
    }

    /**
     * Opens the bottom sheet.
     */
    public void openBottomSheet() {

        Menu menu = new BottomSheetMenu(this);
        new MenuInflater(this).inflate(R.menu.micropub_post_menu, menu);

        // Hide Media if micropub media endpoint is empty.
        String micropubMediaEndpoint = user.getMicropubMediaEndpoint();
        if (micropubMediaEndpoint == null || micropubMediaEndpoint.length() == 0) {
            menu.removeItem(R.id.createMedia);
        }

        // Hide Update if setting is not enabled.
        boolean updateEnabled = Preferences.getPreference(this, "pref_key_experimental_update", false);
        if (!updateEnabled) {
            menu.removeItem(R.id.updatePost);
        }

        new BottomSheet.Builder(this, R.style.BottomSheet_StyleDialog)
                .setMenu(menu)
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
        getMenuInflater().inflate(R.menu.channel_top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO create helper method as we have the same in MicropubActivity
        switch (item.getItemId()) {
            case R.id.refreshConfiguration:
                new MicropubConfig(getApplicationContext(), user).refresh();
                return true;

            case R.id.channel_list_refresh:
                startChannels();
                return true;

            case R.id.settings:
                Intent goSettings = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(goSettings);
                return true;

            case R.id.about:
                Intent goAbout = new Intent(getBaseContext(), AboutActivity.class);
                startActivity(goAbout);
                return true;

            case R.id.accounts:
                new Accounts(this).switchAccount(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSheetShown(@NonNull BottomSheet bottomSheet, @Nullable Object object) { }

    @Override
    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem, @Nullable Object o) {
        // TODO create helper method, we have the same in MicropubActivity
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
            case R.id.createReply:
                Intent CreateReply = new Intent(getBaseContext(), ReplyActivity.class);
                if (incomingText.length() > 0) {
                    CreateReply.putExtra("incomingText", incomingText);
                }
                startActivity(CreateReply);
                break;
            case R.id.createBookmark:
                Intent CreateBookmark = new Intent(getBaseContext(), BookmarkActivity.class);
                if (incomingText.length() > 0) {
                    CreateBookmark.putExtra("incomingText", incomingText);
                }
                startActivity(CreateBookmark);
                break;
            case R.id.createRepost:
                Intent CreateRepost = new Intent(getBaseContext(), RepostActivity.class);
                if (incomingText.length() > 0) {
                    CreateRepost.putExtra("incomingText", incomingText);
                }
                startActivity(CreateRepost);
                break;
            case R.id.createEvent:
                Intent CreateEvent = new Intent(getBaseContext(), EventActivity.class);
                if (incomingText.length() > 0) {
                    CreateEvent.putExtra("incomingText", incomingText);
                }
                startActivity(CreateEvent);
                break;
            case R.id.createRSVP:
                Intent CreateRSVP = new Intent(getBaseContext(), RsvpActivity.class);
                if (incomingText.length() > 0) {
                    CreateRSVP.putExtra("incomingText", incomingText);
                }
                startActivity(CreateRSVP);
                break;
            case R.id.createMedia:
                Intent CreateMedia = new Intent(getBaseContext(), MediaActivity.class);
                if (incomingImage != null && incomingImage.length() > 0) {
                    CreateMedia.putExtra("incomingImage", incomingImage);
                }
                startActivity(CreateMedia);
                break;
            case R.id.updatePost:
                Intent UpdatePost = new Intent(getBaseContext(), UpdateActivity.class);
                startActivity(UpdatePost);
                break;
        }
    }

    @Override
    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @Nullable Object o, int i) {}
}
