package com.indieweb.indigenous.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.indieweb.indigenous.model.Cache;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.TimelineStyle;

import java.util.ArrayList;
import java.util.List;

import static com.indieweb.indigenous.model.TimelineStyle.TIMELINE_STYLE_SUMMARY;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 28;
    private static final String DATABASE_NAME = "indigenous";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Draft.CREATE_TABLE);
        db.execSQL(TimelineStyle.CREATE_TABLE);
        db.execSQL(Cache.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(TimelineStyle.CREATE_TABLE);
        db.execSQL(Draft.CREATE_TABLE);
        db.execSQL(Cache.CREATE_TABLE);

        // Remove obsolete tables.
        if (oldVersion < 27) {
            db.execSQL("DROP TABLE IF EXISTS tracks");
            db.execSQL("DROP TABLE IF EXISTS points");
        }

        // New fields.
        if (oldVersion < 28) {
            db.execSQL("ALTER TABLE " + Draft.TABLE_NAME + " ADD COLUMN " + Draft.COLUMN_SPOILER + " TEXT");
            db.execSQL("ALTER TABLE " + Draft.TABLE_NAME + " ADD COLUMN " + Draft.COLUMN_PUBLISHED + " INTEGER");
            db.execSQL("ALTER TABLE " + Draft.TABLE_NAME + " ADD COLUMN " + Draft.COLUMN_VISIBILITY + " TEXT");
            db.execSQL("ALTER TABLE " + Draft.TABLE_NAME + " ADD COLUMN " + Draft.COLUMN_SENSITIVITY + " INTEGER");
        }
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
            style = cursor.getInt(cursor.getColumnIndexOrThrow(TimelineStyle.COLUMN_TYPE));
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
     * Saves a draft.
     *
     * @param draft The draft to save.
     */
    public void saveDraft(Draft draft) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Draft.COLUMN_SEND_WHEN_ONLINE, draft.getSendWhenOnline());
        values.put(Draft.COLUMN_TYPE, draft.getType());
        values.put(Draft.COLUMN_ACCOUNT, draft.getAccount());
        values.put(Draft.COLUMN_NAME, draft.getName());
        values.put(Draft.COLUMN_BODY, draft.getBody());
        values.put(Draft.COLUMN_SPOILER, draft.getSpoiler());
        values.put(Draft.COLUMN_IMAGE, draft.getImage());
        values.put(Draft.COLUMN_CAPTION, draft.getCaption());
        values.put(Draft.COLUMN_VIDEO, draft.getVideo());
        values.put(Draft.COLUMN_AUDIO, draft.getAudio());
        values.put(Draft.COLUMN_TAGS, draft.getTags());
        values.put(Draft.COLUMN_URL, draft.getUrl());
        values.put(Draft.COLUMN_START_DATE, draft.getStartDate());
        values.put(Draft.COLUMN_END_DATE, draft.getEndDate());
        values.put(Draft.COLUMN_SYNDICATION_TARGETS, draft.getSyndicationTargets());
        values.put(Draft.COLUMN_PUBLISH_DATE, draft.getPublishDate());
        values.put(Draft.COLUMN_PUBLISHED, draft.getPublished());
        values.put(Draft.COLUMN_VISIBILITY, draft.getVisibility());
        values.put(Draft.COLUMN_SENSITIVITY, draft.getSensitivity());
        values.put(Draft.COLUMN_LOCATION_NAME, draft.getLocationName());
        values.put(Draft.COLUMN_LOCATION_URL, draft.getLocationUrl());
        values.put(Draft.COLUMN_LOCATION_VISIBILITY, draft.getLocationVisibility());
        values.put(Draft.COLUMN_SPINNER, draft.getSpinner());
        values.put(Draft.COLUMN_COORDINATES, draft.getCoordinates());

        if (draft.getId() > 0) {
            db.update(Draft.TABLE_NAME, values, Draft.COLUMN_ID + "=" + draft.getId(), null);
        } else {
            db.insert(Draft.TABLE_NAME, null, values);
        }
        db.close();
    }

    /**
     * Deletes a draft.
     *
     * @param id The draft id to delete.
     */
    public void deleteDraft(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(Draft.TABLE_NAME, Draft.COLUMN_ID + "=" + id, null);
        db.close();
    }

    /**
     * Gets a single draft.
     *
     * @param id The draft id.
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
                        Draft.COLUMN_SPOILER,
                        Draft.COLUMN_IMAGE,
                        Draft.COLUMN_CAPTION,
                        Draft.COLUMN_VIDEO,
                        Draft.COLUMN_AUDIO,
                        Draft.COLUMN_TAGS,
                        Draft.COLUMN_URL,
                        Draft.COLUMN_START_DATE,
                        Draft.COLUMN_END_DATE,
                        Draft.COLUMN_SYNDICATION_TARGETS,
                        Draft.COLUMN_PUBLISH_DATE,
                        Draft.COLUMN_PUBLISHED,
                        Draft.COLUMN_VISIBILITY,
                        Draft.COLUMN_SENSITIVITY,
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
     * @return <Draft>
     */
    public List<Draft> getDrafts() {
        List<Draft> drafts = new ArrayList<>();

        // Select query
        String selectQuery = "SELECT * FROM " + Draft.TABLE_NAME + " ORDER BY " + Draft.COLUMN_TIMESTAMP + " DESC";

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

        // return drafts list
        return drafts;
    }

    /**
     * Get the number of drafts.
     *
     * @return int
     * The number of drafts.
     */
    public int getDraftCount() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor dataCount;
        if (db != null) {
            dataCount = db.rawQuery("select " + Draft.COLUMN_ID + " from " + Draft.TABLE_NAME, null);
            count = dataCount.getCount();
            db.close();
        }
        return count;
    }

    /**
     * Set draft properties.
     *
     * @param draft  The draft.
     * @param cursor The cursor
     */
    private void setDraftProperties(Draft draft, Cursor cursor) {
        draft.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Draft.COLUMN_ID)));
        draft.setSendWhenOnline(cursor.getInt(cursor.getColumnIndexOrThrow(Draft.COLUMN_SEND_WHEN_ONLINE)));
        draft.setAccount(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_ACCOUNT)));
        draft.setType(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_TYPE)));
        draft.setName(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_NAME)));
        draft.setBody(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_BODY)));
        draft.setSpoiler(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_SPOILER)));
        draft.setImage(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_IMAGE)));
        draft.setCaption(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_CAPTION)));
        draft.setVideo(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_VIDEO)));
        draft.setAudio(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_AUDIO)));
        draft.setTags(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_TAGS)));
        draft.setUrl(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_URL)));
        draft.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_START_DATE)));
        draft.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_END_DATE)));
        draft.setSyndicationTargets(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_SYNDICATION_TARGETS)));
        draft.setPublishDate(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_PUBLISH_DATE)));
        draft.setPublished(cursor.getInt(cursor.getColumnIndexOrThrow(Draft.COLUMN_PUBLISHED)));
        draft.setVisibility(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_VISIBILITY)));
        draft.setSensitivity(cursor.getInt(cursor.getColumnIndexOrThrow(Draft.COLUMN_SENSITIVITY)));
        draft.setCoordinates(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_COORDINATES)));
        draft.setLocationName(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_LOCATION_NAME)));
        draft.setLocationUrl(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_LOCATION_URL)));
        draft.setLocationVisibility(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_LOCATION_VISIBILITY)));
        draft.setSpinner(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_SPINNER)));
        draft.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(Draft.COLUMN_TIMESTAMP)));
    }

    /**
     * Gets a cache item.
     *
     * @param account   The account to get the cache for
     * @param type      Type of cache item
     * @param channelId The channel id
     * @param page      The page to get from cache
     * @return Cache
     */
    public Cache getCache(String account, String type, String channelId, String page) {
        SQLiteDatabase db = this.getReadableDatabase();

        /*Cursor dataCount = db.rawQuery("select " + Cache.COLUMN_ID + " from " + Cache.TABLE_NAME, null);
        int count = dataCount.getCount();
        Log.d("indigenous_debug", "All caches: " + count);
        String selectQuery = "SELECT * FROM " + Cache.TABLE_NAME;
        Cursor all = db.rawQuery(selectQuery, null);
        if (all.moveToFirst()) {
            do {
                Log.d("indigenous_debug", "Cache: " + all.getInt(all.getColumnIndexOrThrow(Cache.COLUMN_ID)) + " - " + all.getString(all.getColumnIndexOrThrow(Cache.COLUMN_TYPE)));
            } while (all.moveToNext());
        }*/

        Cursor cursor = db.query(Cache.TABLE_NAME,
                new String[]{
                        Cache.COLUMN_ID,
                        Cache.COLUMN_ACCOUNT,
                        Cache.COLUMN_TYPE,
                        Cache.COLUMN_CHANNEL_ID,
                        Cache.COLUMN_PAGE,
                        Cache.COLUMN_DATA,
                },
                Cache.COLUMN_ACCOUNT + "=? AND " + Cache.COLUMN_TYPE + "=? AND " + Cache.COLUMN_CHANNEL_ID + "=? AND " + Cache.COLUMN_PAGE + "=?",
                new String[]{account, type, channelId, page}, null, null, null, null);

        Cache cache = new Cache();
        cache.setId(0);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            cache.setId(cursor.getInt(cursor.getColumnIndexOrThrow(Cache.COLUMN_ID)));
            cache.setAccount(cursor.getString(cursor.getColumnIndexOrThrow(Cache.COLUMN_ACCOUNT)));
            cache.setType(cursor.getString(cursor.getColumnIndexOrThrow(Cache.COLUMN_TYPE)));
            cache.setChannelId(cursor.getString(cursor.getColumnIndexOrThrow(Cache.COLUMN_CHANNEL_ID)));
            cache.setPage(cursor.getString(cursor.getColumnIndexOrThrow(Cache.COLUMN_PAGE)));
            cache.setData(cursor.getString(cursor.getColumnIndexOrThrow(Cache.COLUMN_DATA)));
            cursor.close();
        }

        return cache;
    }


    /**
     * Saves a cache.
     *
     * @param cache A cache object.
     */
    public void saveCache(Cache cache) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Cache.COLUMN_TYPE, cache.getType());
        values.put(Draft.COLUMN_ACCOUNT, cache.getAccount());
        values.put(Cache.COLUMN_CHANNEL_ID, cache.getChannelId());
        values.put(Cache.COLUMN_PAGE, cache.getPage());
        values.put(Cache.COLUMN_DATA, cache.getData());

        if (cache.getId() > 0) {
            db.update(Cache.TABLE_NAME, values, Draft.COLUMN_ID + "=" + cache.getId(), null);
        } else {
            db.insert(Cache.TABLE_NAME, null, values);
        }
        db.close();
    }

    /**
     * Deletes all cache.
     */
    public void clearCache() {
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("delete from " + Cache.TABLE_NAME);
    }

}
