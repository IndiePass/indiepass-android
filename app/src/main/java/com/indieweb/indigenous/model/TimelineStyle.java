package com.indieweb.indigenous.model;

public class TimelineStyle {

    public static final int TIMELINE_STYLE_COMPACT = 0;
    public static final int TIMELINE_STYLE_SUMMARY = 1;

    public static final String TABLE_NAME = "timeline_style";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CHANNEL_ID = "channel";

    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                    + COLUMN_TYPE + " INTEGER,"
                    + COLUMN_CHANNEL_ID + " TEXT"
                    + ")";

    private Integer type;
    private String channel_id = "";

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getChannelId() {
        return channel_id;
    }

    public void setChannelId(String channel_id) {
        this.channel_id = channel_id;
    }

}
