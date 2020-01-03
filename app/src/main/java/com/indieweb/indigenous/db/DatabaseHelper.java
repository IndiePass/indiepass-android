package com.indieweb.indigenous.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.Point;
import com.indieweb.indigenous.model.TimelineStyle;
import com.indieweb.indigenous.model.Track;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.indieweb.indigenous.model.TimelineStyle.TIMELINE_STYLE_SUMMARY;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 23;
    private static final String DATABASE_NAME = "indigenous";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Draft.CREATE_TABLE);
        db.execSQL(TimelineStyle.CREATE_TABLE);
        db.execSQL(Track.CREATE_TABLE);
        db.execSQL(Point.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Point.CREATE_TABLE);
        db.execSQL(Track.CREATE_TABLE);
        db.execSQL(TimelineStyle.CREATE_TABLE);
    }

    /**
     * Get a timeline style.
     */
    public int getTimelineStyle(String channelId) {
        int style = TIMELINE_STYLE_SUMMARY;

        // Select query
        String selectQuery = "SELECT " + TimelineStyle.COLUMN_TYPE + " FROM " + TimelineStyle.TABLE_NAME + " WHERE " + TimelineStyle.COLUMN_CHANNEL_ID + "='" + channelId + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Get result
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            style = cursor.getInt(cursor.getColumnIndex(TimelineStyle.COLUMN_TYPE));
            cursor.close();
        }

        // close db connection
        db.close();

        return style;
    }

    /**
     * Saves a Timeline style.
     */
    public void saveTimelineStyle(TimelineStyle t) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TimelineStyle.COLUMN_TYPE, t.getType());
        values.put(TimelineStyle.COLUMN_CHANNEL_ID, t.getChannelId());

        db.delete(TimelineStyle.TABLE_NAME, TimelineStyle.COLUMN_CHANNEL_ID + "=?", new String[]{t.getChannelId()});
        db.insert(TimelineStyle.TABLE_NAME, null, values);

        db.close();
    }

    /**
     * Saves a track.
     *
     * @param track
     *   The track to save.
     */
    public void saveTrack(Track track, boolean setEnd) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Values which will always be set.
        values.put(Track.COLUMN_TITLE, track.getTitle());
        values.put(Track.COLUMN_TRANSPORT, track.getTransport());

        if (track.getId() > 0) {
            if (setEnd) {
                values.put(Track.COLUMN_END_TIME,dateFormat.format(System.currentTimeMillis()));
            }
            db.update(Track.TABLE_NAME, values, Track.COLUMN_ID + "=" + track.getId(), null);
        }
        else {
            values.put(Track.COLUMN_INTERVAL, track.getInterval());
            values.put(Track.COLUMN_ACCOUNT, track.getAccount());
            values.put(Track.COLUMN_START_TIME,dateFormat.format(System.currentTimeMillis()));
            db.insert(Track.TABLE_NAME, null, values);
        }
        db.close();
    }

    /**
     * Saves a point.
     *
     * @param point
     *   The point to save.
     */
    public void savePoint(Point point) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Point.COLUMN_POINT, point.getPoint());
        values.put(Point.COLUMN_TRACK_ID, point.getTrackId());

        db.insert(Point.TABLE_NAME, null, values);
        db.close();
    }

    /**
     * Get points for a track.
     *
     * @param trackId
     *   The track id.
     *
     * @return points
     */
    public Map<Integer, Point> getPoints(int trackId) {
        @SuppressLint("UseSparseArrays")
        Map<Integer, Point> points = new LinkedHashMap<>();

        // Select query
        String selectQuery = "SELECT * FROM " + Point.TABLE_NAME + " WHERE " + Point.COLUMN_TRACK_ID + "=" + trackId + " " +
                "ORDER BY " + Point.COLUMN_ID + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Point point = new Point();
                point.setId(cursor.getInt(cursor.getColumnIndex(Point.COLUMN_ID)));
                point.setPoint(cursor.getString(cursor.getColumnIndex(Point.COLUMN_POINT)));
                points.put(point.getId(), point);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        return points;
    }

    /**
     * Gets a single track.
     *
     * @param id
     *   The track id.
     *
     * @return Track
     */
    public Track getTrack(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Track.TABLE_NAME,
                new String[]{
                        Track.COLUMN_ID,
                        Track.COLUMN_ACCOUNT,
                        Track.COLUMN_TITLE,
                        Track.COLUMN_START_LOCATION,
                        Track.COLUMN_END_LOCATION,
                        Track.COLUMN_INTERVAL,
                        Track.COLUMN_TRANSPORT,
                        Track.COLUMN_START_TIME,
                        Track.COLUMN_END_TIME
                },
                Track.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Track track = new Track();
        track.setId(0);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            setTrackProperties(track, cursor);
            cursor.close();
        }

        return track;
    }


    /**
     * Get all tracks.
     *
     * @param account
     *   The account to get the tracks for.
     *
     * @return <Track>
     */
    public List<Track> getTracks(String account) {
        List<Track> tracks = new ArrayList<>();

        // Select query
        String selectQuery = "SELECT * FROM " + Track.TABLE_NAME + " WHERE " + Track.COLUMN_ACCOUNT + "='" + account + "' " +
                "ORDER BY " + Track.COLUMN_START_TIME + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Track track = new Track();
                setTrackProperties(track, cursor);
                tracks.add(track);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return tracks list
        return tracks;
    }

    /**
     * Gets the latest track id.
     *
     * @return int id
     */
    public int getLatestTrackId(String account) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Select query
        String selectQuery = "SELECT " + Track.COLUMN_ID + " FROM " + Track.TABLE_NAME + "" +
                " WHERE " + Track.COLUMN_ACCOUNT + "='" + account + "' " +
                "ORDER BY " + Track.COLUMN_START_TIME + " DESC LIMIT 1";

        int id = 0;
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndex(Track.COLUMN_ID));
            cursor.close();
        }

        return id;
    }

    /**
     * Deletes all tracker data.
     */
    public void deleteAllTrackerData() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from " + Track.TABLE_NAME);
        db.execSQL("delete from " + Point.TABLE_NAME);
    }

    /**
     * Set track properties.
     *
     * @param track
     *   The track.
     * @param cursor
     *   The cursor
     */
    private void setTrackProperties(Track track, Cursor cursor) {
        track.setId(cursor.getInt(cursor.getColumnIndex(Track.COLUMN_ID)));
        track.setAccount(cursor.getString(cursor.getColumnIndex(Track.COLUMN_ACCOUNT)));
        track.setTitle(cursor.getString(cursor.getColumnIndex(Track.COLUMN_TITLE)));
        track.setStartTime(cursor.getString(cursor.getColumnIndex(Track.COLUMN_START_TIME)));
        track.setEndTime(cursor.getString(cursor.getColumnIndex(Track.COLUMN_END_TIME)));
        track.setInterval(cursor.getInt(cursor.getColumnIndex(Track.COLUMN_INTERVAL)));
        track.setStartLocation(cursor.getString(cursor.getColumnIndex(Track.COLUMN_START_LOCATION)));
        track.setEndLocation(cursor.getString(cursor.getColumnIndex(Track.COLUMN_END_LOCATION)));
        track.setTransport(cursor.getString(cursor.getColumnIndex(Track.COLUMN_TRANSPORT)));
        track.setPointCount(this.getPointCount(track.getId()));
    }

    /**
     * Deletes a track.
     *
     * @param id
     *   The track id to delete.
     */
    public void deleteTrack(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Track.TABLE_NAME, Track.COLUMN_ID + "=" + id, null);
        db.delete(Point.TABLE_NAME, Point.COLUMN_TRACK_ID + "=" + id, null);
        db.close();
    }

    /**
     * Get the number of points for a track.
     *
     * @param trackId
     *   The track id.
     *
     * @return int
     *   The number of drafts.
     */
    private int getPointCount(int trackId) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor dataCount;
        if (db != null) {
            dataCount = db.rawQuery("select " + Point.COLUMN_ID + " from " + Point.TABLE_NAME + " where " + Point.COLUMN_TRACK_ID + "=" + trackId, null);
            count = dataCount.getCount();
            //Log.d("indigenous_debug", "Points for " + trackId + ": " + count);
            //dataCount = db.rawQuery("select " + Point.COLUMN_ID + " from " + Point.TABLE_NAME, null);
            //count = dataCount.getCount();
            //Log.d("indigenous_debug", "All points: " + count);
            db.close();
        }
        return count;
    }


    /**
     * Saves a draft.
     *
     * @param draft
     *   The draft to save.
     */
    public void saveDraft(Draft draft) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Draft.COLUMN_SEND_WHEN_ONLINE, draft.getSendWhenOnline());
        values.put(Draft.COLUMN_TYPE, draft.getType());
        values.put(Draft.COLUMN_ACCOUNT, draft.getAccount());
        values.put(Draft.COLUMN_NAME, draft.getName());
        values.put(Draft.COLUMN_BODY, draft.getBody());
        values.put(Draft.COLUMN_IMAGES, draft.getImages());
        values.put(Draft.COLUMN_CAPTIONS, draft.getCaptions());
        values.put(Draft.COLUMN_TAGS, draft.getTags());
        values.put(Draft.COLUMN_URL, draft.getUrl());
        values.put(Draft.COLUMN_START_DATE, draft.getStartDate());
        values.put(Draft.COLUMN_END_DATE, draft.getEndDate());
        values.put(Draft.COLUMN_SYNDICATION_TARGETS, draft.getSyndicationTargets());
        values.put(Draft.COLUMN_PUBLISH_DATE, draft.getPublishDate());
        values.put(Draft.COLUMN_LOCATION_NAME, draft.getLocationName());
        values.put(Draft.COLUMN_LOCATION_URL, draft.getLocationUrl());
        values.put(Draft.COLUMN_LOCATION_VISIBILITY, draft.getLocationVisibility());
        values.put(Draft.COLUMN_SPINNER, draft.getSpinner());
        values.put(Draft.COLUMN_COORDINATES, draft.getCoordinates());

        if (draft.getId() > 0) {
            db.update(Draft.TABLE_NAME, values, Draft.COLUMN_ID + "=" + draft.getId(), null);
        }
        else {
            db.insert(Draft.TABLE_NAME, null, values);
        }
        db.close();
    }

    /**
     * Deletes a draft.
     *
     * @param id
     *   The draft id to delete.
     */
    public void deleteDraft(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Draft.TABLE_NAME, Draft.COLUMN_ID + "=" + id, null);
        db.close();
    }

    /**
     * Gets a single draft.
     *
     * @param id
     *   The draft id.
     *
     * @return Draft
     */
    public Draft getDraft(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(Draft.TABLE_NAME,
                new String[]{
                        Draft.COLUMN_ID,
                        Draft.COLUMN_SEND_WHEN_ONLINE,
                        Draft.COLUMN_ACCOUNT,
                        Draft.COLUMN_TYPE,
                        Draft.COLUMN_NAME,
                        Draft.COLUMN_BODY,
                        Draft.COLUMN_IMAGES,
                        Draft.COLUMN_CAPTIONS,
                        Draft.COLUMN_TAGS,
                        Draft.COLUMN_URL,
                        Draft.COLUMN_START_DATE,
                        Draft.COLUMN_END_DATE,
                        Draft.COLUMN_SYNDICATION_TARGETS,
                        Draft.COLUMN_PUBLISH_DATE,
                        Draft.COLUMN_COORDINATES,
                        Draft.COLUMN_LOCATION_NAME,
                        Draft.COLUMN_LOCATION_URL,
                        Draft.COLUMN_LOCATION_VISIBILITY,
                        Draft.COLUMN_SPINNER,
                        Draft.COLUMN_TIMESTAMP
                },
                Draft.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Draft draft = new Draft();
        draft.setId(0);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            setDraftProperties(draft, cursor);
            cursor.close();
        }

        return draft;
    }

    /**
     * Get all drafts
     *
     * @param account
     *   The account to get the drafts for.
     *
     * @return <Draft>
     */
    public List<Draft> getDrafts(String account) {
        List<Draft> drafts = new ArrayList<>();

        // Select query
        String selectQuery = "SELECT * FROM " + Draft.TABLE_NAME + " WHERE " + Draft.COLUMN_ACCOUNT + "='" + account + "' " +
                "ORDER BY " + Draft.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Draft draft = new Draft();
                setDraftProperties(draft, cursor);
                drafts.add(draft);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return tracks list
        return drafts;
    }

    /**
     * Get the number of drafts.
     *
     * @return int
     *   The number of drafts.
     */
    public int getDraftCount() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor dataCount;
        if (db != null) {
            dataCount = db.rawQuery("select "+ Draft.COLUMN_ID + " from " + Draft.TABLE_NAME, null);
            count = dataCount.getCount();
            db.close();
        }
        return count;
    }

    /**
     * Set draft properties.
     *
     * @param draft
     *   The draft.
     * @param cursor
     *   The cursor
     */
    private void setDraftProperties(Draft draft, Cursor cursor) {
        draft.setId(cursor.getInt(cursor.getColumnIndex(Draft.COLUMN_ID)));
        draft.setSendWhenOnline(cursor.getInt(cursor.getColumnIndex(Draft.COLUMN_SEND_WHEN_ONLINE)));
        draft.setAccount(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_ACCOUNT)));
        draft.setType(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TYPE)));
        draft.setName(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_NAME)));
        draft.setBody(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_BODY)));
        draft.setImages(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_IMAGES)));
        draft.setCaptions(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_CAPTIONS)));
        draft.setTags(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TAGS)));
        draft.setUrl(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_URL)));
        draft.setStartDate(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_START_DATE)));
        draft.setEndDate(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_END_DATE)));
        draft.setSyndicationTargets(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_SYNDICATION_TARGETS)));
        draft.setPublishDate(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_PUBLISH_DATE)));
        draft.setCoordinates(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_COORDINATES)));
        draft.setLocationName(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_LOCATION_NAME)));
        draft.setLocationUrl(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_LOCATION_URL)));
        draft.setLocationVisibility(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_LOCATION_VISIBILITY)));
        draft.setSpinner(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_SPINNER)));
        draft.setTimestamp(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TIMESTAMP)));
    }
}
