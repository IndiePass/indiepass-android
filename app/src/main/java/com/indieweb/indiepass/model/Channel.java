package com.indieweb.indiepass.model;

public class Channel {

    private String uid;
    private String name;
    private int unread;
    private String sourceId = "";
    private int unreadSources = 0;
    private boolean countInteger = false;
    private boolean countNew = false;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public boolean isCountInteger() {
        return countInteger;
    }

    public void setCountInteger(boolean countInteger) {
        this.countInteger = countInteger;
    }

    public boolean isCountNew() {
        return countNew;
    }

    public void setCountNew(boolean countNew) {
        this.countNew = countNew;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public int getUnreadSources() {
        return unreadSources;
    }

    public void setUnreadSources(int unreadSources) {
        this.unreadSources = unreadSources;
    }
}
