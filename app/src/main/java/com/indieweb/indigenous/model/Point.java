package com.indieweb.indigenous.model;

public class Point {

    public static final String TABLE_NAME = "points";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TRACK_ID = "track_id";
    public static final String COLUMN_POINT = "point";

    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_TRACK_ID + " INTEGER,"
                    + COLUMN_POINT + " TEXT"
                    + ")";

    private int id = 0;
    private int trackId;
    private String point = "";

    public int getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }

    public String getPoint() {
        return point;
    }

    public void setPoint(String point) {
        this.point = point;
    }
}
