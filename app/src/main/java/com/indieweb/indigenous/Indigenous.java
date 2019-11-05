package com.indieweb.indigenous;

import android.app.Application;

import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.TimelineItem;

import java.util.ArrayList;
import java.util.List;

public class Indigenous extends Application {

    private static Indigenous singleton;

    public static String debug;

    public static TimelineItem timelineItem;

    public static Indigenous getInstance(){
        return singleton;
    }

    public static List<Channel> channelsList = new ArrayList<>();

    public void setDebug(String text) {
        debug = text;
    }

    public String getDebug() {
        return debug;
    }

    public void setTimelineItem(TimelineItem item) {
        timelineItem = item;
    }

    public TimelineItem getTimelineItem() {
        return timelineItem;
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
