package com.indieweb.indiepass.reader;

import android.content.Context;
import android.view.MenuItem;

import com.indieweb.indiepass.R;
import com.indieweb.indiepass.model.Channel;
import com.indieweb.indiepass.model.TimelineItem;
import com.indieweb.indiepass.model.User;
import com.indieweb.indiepass.util.Preferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.indieweb.indiepass.mastodon.MastodonReader.CHANNEL_NAME_MASTODON_ANONYMOUS;
import static com.indieweb.indiepass.pixelfed.PixelfedReader.CHANNEL_NAME_PIXELFED_ANONYMOUS;
import static com.indieweb.indiepass.pleroma.PleromaReader.CHANNEL_NAME_PLEROMA_ANONYMOUS;

abstract public class ReaderBase implements Reader {

    private final Context context;
    private final User user;

    public ReaderBase(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    public Context getContext() {
        return context;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean hasEndpoint() {
        return true;
    }

    @Override
    public boolean supports(String feature) {
        boolean supported = true;

        if (READER_DEBUG_CHANNELS.equals(feature)) {
            supported = Preferences.getPreference(getContext(), "pref_key_debug_microsub_channels", false);
        }

        return supported;
    }

    @Override
    public List<Channel> parseChannelResponse(String data, boolean fromCache) {
        return null;
    }

    @Override
    public List<Channel> getAdditionalChannels() {
        List<Channel> Channels = new ArrayList<>();

        if (getUser().isAnonymous()) {

            Channel channel = new Channel();
            channel.setUid(CHANNEL_NAME_MASTODON_ANONYMOUS);
            channel.setName(getContext().getString(R.string.channel_mastodon));
            channel.setUnread(0);
            Channels.add(channel);

            channel = new Channel();
            channel.setUid(CHANNEL_NAME_PIXELFED_ANONYMOUS);
            channel.setName(getContext().getString(R.string.channel_pixelfed));
            channel.setUnread(0);
            Channels.add(channel);

            channel = new Channel();
            channel.setUid(CHANNEL_NAME_PLEROMA_ANONYMOUS);
            channel.setName(getContext().getString(R.string.channel_pleroma));
            channel.setUnread(0);
            Channels.add(channel);
        }

        return Channels;
    }

    @Override
    public boolean hideDelete(String channelId) {
        return false;
    }

    @Override
    public String getChannelEndpoint(boolean showSources) {
        return null;
    }

    @Override
    public List<Channel> getChannels() {
        return null;
    }

    @Override
    public boolean sendTimelineAccessToken(String channelId) {
        return true;
    }

    @Override
    public Map<String, String> getTimelineParams(boolean isPreview, boolean isSearch, String channelId, String previewUrl, String searchQuery) {
        return null;
    }

    @Override
    public void markRead(String channelId, List<String> entries, boolean markAllRead, boolean showMessage) { }

    @Override
    public void setContactLabel(MenuItem menuItem, TimelineItem item) { }

    @Override
    public boolean canContact(String channelId) {
        return true;
    }

    @Override
    public String getTag(String url, TimelineItem item) {
        return "";
    }
}
