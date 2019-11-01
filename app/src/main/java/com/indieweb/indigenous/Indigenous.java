package com.indieweb.indigenous;

import android.app.Application;

import com.indieweb.indigenous.model.TimelineItem;

public class Indigenous extends Application {

    private static Indigenous singleton;

    public static String debug;

    public static TimelineItem timelineItem;

    public static Indigenous getInstance(){
        return singleton;
    }

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

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
