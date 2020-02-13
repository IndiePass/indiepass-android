package com.indieweb.indigenous.microsub;

import android.content.Context;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.HTTPRequest;
import com.indieweb.indigenous.util.Utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MicrosubAction {

    private final Context context;
    private final User user;
    private final RelativeLayout layout;

    public MicrosubAction(Context context, User user, RelativeLayout layout) {
        this.context = context;
        this.user = user;
        this.layout = layout;
    }

    /**
     * Do Microsub request.
     *
     * @param params
     *   The params to send.
     */
    private boolean doMicrosubRequest(Map<String, String> params) {

        if (!Utility.hasConnection(context)) {
            try {
                Snackbar.make(layout, context.getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
            }
            catch (Exception ignored) {}
            return false;
        }

        HTTPRequest r = new HTTPRequest(null, user, context);
        r.doPostRequest(user.getMicrosubEndpoint(), params);

        return true;
    }

    /**
     * Mark entries read.
     */
    public void markRead(String channelId, List<String> entries, boolean all) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "timeline");
        params.put("method", "mark_read");
        params.put("channel", channelId);
        if (all) {
            params.put("last_read_entry", entries.get(0));
        }
        else {
            int i = 0;
            for (String entry: entries) {
                params.put("entry[" + i + "]", entry);
                i++;
            }
        }
        doMicrosubRequest(params);
    }

    /**
     * Mark entries unread.
     */
    public void markUnread(String channelId, List<String> entries) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "timeline");
        params.put("method", "mark_unread");
        params.put("channel", channelId);
        int i = 0;
        for (String entry: entries) {
            params.put("entry[" + i + "]", entry);
            i++;
        }
        doMicrosubRequest(params);
    }

    /**
     * Delete post.
     */
    public void deletePost(String channelId, String postId) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "timeline");
        params.put("method", "remove");
        params.put("channel", channelId);
        params.put("entry", postId);
        doMicrosubRequest(params);
    }

    /**
     * Move post.
     */
    public void movePost(String channelId, String channelName, String postId) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "timeline");
        params.put("method", "move");
        params.put("channel", channelId);
        params.put("entry", postId);
        boolean success = doMicrosubRequest(params);
        if (success) {
            Snackbar.make(layout, String.format(context.getString(R.string.post_moved), channelName), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Create channel.
     */
    public void createChannel(final String channelName) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "channels");
        params.put("name", channelName);
        boolean success = doMicrosubRequest(params);
        if (success) {
            Snackbar.make(layout, context.getString(R.string.channel_created), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Update channel.
     */
    public void updateChannel(final String channelName, final String uid) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "channels");
        params.put("channel", uid);
        params.put("name", channelName);
        boolean success = doMicrosubRequest(params);
        if (success) {
            Snackbar.make(layout, context.getString(R.string.channel_updated), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Delete channel.
     */
    public void deleteChannel(final String channelId) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "channels");
        params.put("method", "delete");
        params.put("channel", channelId);
        boolean success = doMicrosubRequest(params);
        if (success) {
            Snackbar.make(layout, context.getString(R.string.channel_deleted), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Order channels.
     */
    public void orderChannels(final List<Channel> Channels) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "channels");
        params.put("method", "order");
        int i = 0;
        for (Channel c : Channels) {
            params.put("channels[" + i + "]", c.getUid());
            i++;
        }
        boolean success = doMicrosubRequest(params);
        if (success) {
            Snackbar.make(layout, context.getString(R.string.channels_order_updated), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Delete channel.
     */
    public void deleteFeed(final String url, final String channelId) {
        Map<String, String> params = new HashMap<>();
        params.put("action", "unfollow");
        params.put("url", url);
        params.put("channel", channelId);
        boolean success = doMicrosubRequest(params);
        if (success) {
            Snackbar.make(layout, context.getString(R.string.feed_deleted), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Subscribe.
     */
    public void subscribe(final String url, final String channelId, final boolean update) {
        Map<String, String> params = new HashMap<>();
        if (update) {
            params.put("method", "update");
        }
        params.put("action", "follow");
        params.put("url", url);
        params.put("channel", channelId);
        boolean success = doMicrosubRequest(params);
        if (success) {
            if (update) {
                Snackbar.make(layout, context.getString(R.string.feed_updated), Snackbar.LENGTH_SHORT).show();
            }
            else {
                Snackbar.make(layout, context.getString(R.string.feed_subscribed), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
