package com.indieweb.indiepass;

import android.app.Application;

import com.indieweb.indiepass.model.Channel;
import com.indieweb.indiepass.model.Contact;
import com.indieweb.indiepass.model.TimelineItem;

import java.util.ArrayList;
import java.util.List;

public class IndiePass extends Application {

    private static IndiePass singleton;

    public static String debug;

    public static TimelineItem timelineItem;

    public static Contact contactItem;

    public static boolean refreshChannels;

    public static IndiePass getInstance(){
        return singleton;
    }

    public static List<Channel> channelsList = new ArrayList<>();

    public void setDebug(String text) {
        debug = text;
    }

    public String getDebug() {
        return debug;
    }

    public void setRefreshChannels(boolean refresh) {
        refreshChannels = refresh;
    }

    public boolean isRefreshChannels() {
        return refreshChannels;
    }

    public void setTimelineItem(TimelineItem item) {
        timelineItem = item;
    }

    public TimelineItem getTimelineItem() {
        return timelineItem;
    }

    public void setContact(Contact contact) {
        contactItem = contact;
    }

    public Contact getContact() {
        return contactItem;
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
