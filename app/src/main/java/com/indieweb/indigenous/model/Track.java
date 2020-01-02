package com.indieweb.indigenous.model;

public class Track {

    public static final String TABLE_NAME = "tracks";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ACCOUNT = "account";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    public static final String COLUMN_START_LOCATION = "start_location";
    public static final String COLUMN_END_LOCATION = "end_location";
    public static final String COLUMN_INTERVAL = "interval";
    public static final String COLUMN_TRANSPORT = "transport";

    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_ACCOUNT + " TEXT,"
                    + COLUMN_TITLE + " TEXT,"
                    + COLUMN_START_LOCATION + " TEXT,"
                    + COLUMN_END_LOCATION + " TEXT,"
                    + COLUMN_INTERVAL + " INTEGER,"
                    + COLUMN_TRANSPORT + " TEXT,"
                    + COLUMN_START_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + COLUMN_END_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                    + ")";

    private int id = 0;
    private String account;
    private String title = "";
    private String startTime = "";
    private String endTime = "";
    private String startLocation = "";
    private String endLocation = "";
    private String transport = "";
    private int interval = 0;
    private int points;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public String getTransport() {
        return transport;
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getPointCount() {
        return points;
    }

    public void setPointCount(int points) {
        this.points = points;
    }
}
