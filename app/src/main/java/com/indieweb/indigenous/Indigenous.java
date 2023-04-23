package com.indieweb.indigenous;

import android.app.Application;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.Contact;
import com.indieweb.indigenous.model.TimelineItem;

import java.util.ArrayList;
import java.util.List;

public class Indigenous extends Application {

    public static String debug;
    public static TimelineItem timelineItem;
    public static Contact contactItem;
    public static boolean refreshChannels;
    public static List<Channel> channelsList = new ArrayList<>();
    private static Indigenous singleton;

    public static Indigenous getInstance() {
        return singleton;
    }

    public String getDebug() {
        return debug;
    }

    public void setDebug(String text) {
        debug = text;
    }

    public boolean isRefreshChannels() {
        return refreshChannels;
    }

    public void setRefreshChannels(boolean refresh) {
        refreshChannels = refresh;
    }

    public TimelineItem getTimelineItem() {
        return timelineItem;
    }

    public void setTimelineItem(TimelineItem item) {
        timelineItem = item;
    }

    public Contact getContact() {
        return contactItem;
    }

    public void setContact(Contact contact) {
        contactItem = contact;
    }

    public void setChannels(List<Channel> channels) {
        channelsList = channels;
    }

    public List<Channel> getChannelsList() {
        return channelsList;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
