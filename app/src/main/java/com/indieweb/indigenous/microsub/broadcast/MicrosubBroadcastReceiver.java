package com.indieweb.indigenous.microsub.broadcast;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.MainActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;

public class MicrosubBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {

        final User user = new Accounts(context).getCurrentUser();
        if (user.getMicrosubEndpoint().length() == 0) {
            return;
        }

        if (!Preferences.getPreference(context, "indigenous_notification_microsub", false)) {
            return;
        }

        if (!new Connection(context).hasConnection()) {
            return;
        }

        final Integer[] unreadCount = {0};
        String microsubEndpoint = user.getMicrosubEndpoint();

        final boolean checkNotificationsOnly = Preferences.getPreference(context, "pref_key_check_new_posts_notifications", false);

        // TODO abstract this all in one helper request class.
        // We have the same in channelfragment
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

                                if (checkNotificationsOnly && !object.getString("uid").equals("notifications")) {
                                    continue;
                                }

                                if (object.has("unread")) {
                                    Object unreadCheck = object.get("unread");
                                    if (unreadCheck instanceof Integer) {
                                        unreadCount[0] += (Integer) unreadCheck;
                                    }
                                }
                            }

                            if (unreadCount[0] > 0) {

                                PendingIntent i= PendingIntent.getActivity(context, 0,
                                        new Intent(context, MainActivity.class), 0);

                                String title = "New posts in your channels";
                                String content = unreadCount[0].toString() + " to be exact, so quickly, go and check what's new!";
                                if (checkNotificationsOnly) {
                                    title = "New notifications";
                                    content = unreadCount[0].toString() + " to be exact, so go and check who pinged you!";
                                }

                                NotificationCompat.Builder mBuilder =
                                        new NotificationCompat.Builder(context, "indigenous_microsub")
                                                .setSmallIcon(R.drawable.button_feed_pressed)
                                                .setContentIntent(i)
                                                .setAutoCancel(true)
                                                .setVisibility(VISIBILITY_PUBLIC)
                                                .setContentTitle(title)
                                                .setContentText(content);

                                try {
                                    NotificationManager mNotificationManager =
                                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        NotificationChannel channel = new NotificationChannel("indigenous_microsub",
                                                "Microsub",
                                                NotificationManager.IMPORTANCE_DEFAULT);
                                        mNotificationManager.createNotificationChannel(channel);
                                    }
                                    mNotificationManager.notify(001, mBuilder.build());

                                    // Set notification to false.
                                    Preferences.setPreference(context, "indigenous_notification_microsub", false);
                                }
                                catch (NullPointerException ignored) { }
                                catch (Exception ignored) { }
                            }

                        }
                        catch (JSONException ignored) { }

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
