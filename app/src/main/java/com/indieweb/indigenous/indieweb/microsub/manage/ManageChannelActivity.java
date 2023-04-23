package com.indieweb.indigenous.indieweb.microsub.manage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.indieweb.microsub.MicrosubAction;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageChannelActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, StartDragListener {

    RecyclerView listChannel;
    private ManageChannelListAdapter adapter;
    private List<Channel> Channels = new ArrayList<>();
    SwipeRefreshLayout refreshLayout;
    ItemTouchHelper touchHelper;
    User user;
    String incomingText = "";
    boolean isShare = false;
    boolean showRefreshMessage;
    private RelativeLayout layout;
    private LinearLayout noConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channels);
        listChannel = findViewById(R.id.channel_list);
        user = new Accounts(this).getDefaultUser();

        layout = findViewById(R.id.channel_manage_root);
        noConnection = findViewById(R.id.noConnection);
        refreshLayout = findViewById(R.id.refreshChannels);
        refreshLayout.setOnRefreshListener(this);

        // Listen to incoming data.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras != null) {
            if (Intent.ACTION_SEND.equals(action)) {
                try {
                    if (extras.containsKey(Intent.EXTRA_TEXT)) {
                        incomingText = extras.get(Intent.EXTRA_TEXT).toString();
                        if (incomingText.length() > 0) {
                            isShare = true;
                        }
                    }
                }
                catch (NullPointerException ignored) {}
            }
        }

        if (isShare) {
            TextView createFeedTitle = findViewById(R.id.createFeedTitle);
            createFeedTitle.setVisibility(View.VISIBLE);
            TextView feedPreview = findViewById(R.id.previewFeed);
            feedPreview.setVisibility(View.VISIBLE);
            feedPreview.setText(incomingText);
        }

        startChannels();
    }

    @Override
    public void onRefresh() {
        startChannels();
    }

    /**
     * Checks the state of the pull to refresh.
     */
    public void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            Snackbar.make(layout, getString(R.string.channels_refreshed), Snackbar.LENGTH_SHORT).show();
            refreshLayout.setRefreshing(false);
        }
    }

    /**
     * Start channels.
     */
    public void startChannels() {
        noConnection.setVisibility(View.GONE);

        Channels = new ArrayList<>();
        adapter = new ManageChannelListAdapter(this, Channels, user, this, isShare, incomingText, layout);

        ItemTouchHelper.Callback callback = new ItemMoveCallback(adapter);
        touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(listChannel);

        listChannel.setAdapter(adapter);
        loadChannels();
    }

    /**
     * Load channels.
     */
    public void loadChannels() {

        if (!Utility.hasConnection(getApplicationContext())) {
            showRefreshMessage = false;
            checkRefreshingStatus();
            noConnection.setVisibility(View.VISIBLE);
            return;
        }

        String microsubEndpoint = user.getMicrosubEndpoint();

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

                                // Skip notifications channel.
                                String uid = object.getString("uid");
                                if (uid.equals("notifications")) {
                                    continue;
                                }

                                Channel channel = new Channel();
                                channel.setUid(uid);
                                channel.setName(object.getString("name"));
                                channel.setUnread(0);
                                Channels.add(channel);
                            }

                            adapter.notifyDataSetChanged();
                            checkRefreshingStatus();
                        }
                        catch (JSONException e) {
                            showRefreshMessage = false;
                            String message = String.format(getString(R.string.channel_list_parse_error), e.getMessage());
                            final Snackbar snack = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE);
                            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snack.dismiss();
                                    }
                                }
                            );
                            snack.show();
                            checkRefreshingStatus();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        showRefreshMessage = false;
                        checkRefreshingStatus();
                        String message = Utility.parseNetworkError(error, getApplicationContext(), R.string.request_failed, R.string.request_failed_unknown);
                        final Snackbar snack = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE);
                        snack.setAction(getString(R.string.close), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snack.dismiss();
                                }
                            }
                        );
                        snack.show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!isShare) {
            getMenuInflater().inflate(R.menu.manage_channel_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.channel_list_refresh:
                showRefreshMessage = true;
                refreshLayout.setRefreshing(true);
                startChannels();
                return true;
            case R.id.channel_add:
                createChannel();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void requestDrag(RecyclerView.ViewHolder viewHolder) {
        touchHelper.startDrag(viewHolder);
    }

    /**
     * Create channel dialog.
     *
     * @todo reload the fragment when the channel is created
     *       do the same when subscribing to a feed.
     */
    public void createChannel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ManageChannelActivity.this);
        builder.setTitle("Add channel");

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_single_input, null);
        final EditText input = view.findViewById(R.id.editText);
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                input.post(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager inputMethodManager = (InputMethodManager) ManageChannelActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                    }
                });
            }
        });
        builder.setView(view);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!TextUtils.isEmpty(input.getText())) {
                    String channelName = input.getText().toString();
                    new MicrosubAction(getApplicationContext(), user, layout).createChannel(channelName);
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
        input.requestFocus();
    }
}
