package com.indieweb.indigenous.reader;

import android.content.Context;

import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Preferences;

import java.util.ArrayList;
import java.util.List;

abstract public class ReaderBase implements Reader {

    private Context context;
    private User user;

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
        return new ArrayList<>();
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
    public void markRead(String channelId, List<String> entries, boolean markAllRead, boolean showMessage) { }
}
