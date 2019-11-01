package com.indieweb.indigenous.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.TimelineStyle;

import java.util.ArrayList;
import java.util.List;

import static com.indieweb.indigenous.model.TimelineStyle.TIMELINE_STYLE_SUMMARY;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 18;
    private static final String DATABASE_NAME = "indigenous";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Draft.CREATE_TABLE);
        db.execSQL(TimelineStyle.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TimelineStyle.CREATE_TABLE);
    }

    /**
     * Get a timeline style.
     */
    public int getStyle(String channelId) {
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

        db.delete(TimelineStyle.TABLE_NAME, TimelineStyle.COLUMN_CHANNEL_ID + "=" + t.getChannelId(), null);
        db.insert(TimelineStyle.TABLE_NAME, null, values);

        db.close();
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
        if (cursor != null) {
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

        // return notes list
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
