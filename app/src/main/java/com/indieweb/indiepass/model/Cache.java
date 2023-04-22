package com.indieweb.indiepass.model;

public class Cache {

    public static final String TABLE_NAME = "cache";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_TYPE = "type";
    public static final String COLUMN_CHANNEL_ID = "channel_id";
    public static final String COLUMN_PAGE = "page";
    public static final String COLUMN_DATA = "data";

    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_ACCOUNT + " TEXT,"
                    + COLUMN_TYPE + " TEXT,"
                    + COLUMN_CHANNEL_ID + " TEXT,"
                    + COLUMN_PAGE + " TEXT,"
                    + COLUMN_DATA + " TEXT"
                    + ")";

    private int id = 0;
    private String account;
    private String type = "";
    private String channelId = "";
    private String page = "";
    private String data = "";

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String page) {
        this.page = page;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
