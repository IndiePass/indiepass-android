package com.indieweb.indigenous.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.indieweb.indigenous.model.Draft;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 7;
    private static final String DATABASE_NAME = "indigenous";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Draft.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + Draft.TABLE_NAME);

        // Create tables again
        onCreate(db);
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
        values.put(Draft.COLUMN_TYPE, draft.getType());
        values.put(Draft.COLUMN_ACCOUNT, draft.getAccount());
        values.put(Draft.COLUMN_NAME, draft.getName());
        values.put(Draft.COLUMN_BODY, draft.getBody());
        values.put(Draft.COLUMN_IMAGE, draft.getImage());
        values.put(Draft.COLUMN_TAGS, draft.getTags());
        values.put(Draft.COLUMN_URL, draft.getUrl());

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
                new String[]{Draft.COLUMN_ID, Draft.COLUMN_ACCOUNT, Draft.COLUMN_TYPE, Draft.COLUMN_NAME, Draft.COLUMN_BODY, Draft.COLUMN_IMAGE, Draft.COLUMN_TAGS, Draft.COLUMN_URL, Draft.COLUMN_TIMESTAMP},
                Draft.COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        Draft draft = new Draft();
        draft.setId(0);
        if (cursor != null) {
            cursor.moveToFirst();

            draft.setId(cursor.getInt(cursor.getColumnIndex(Draft.COLUMN_ID)));
            draft.setAccount(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_ACCOUNT)));
            draft.setType(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TYPE)));
            draft.setName(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_NAME)));
            draft.setBody(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_BODY)));
            draft.setImage(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_IMAGE)));
            draft.setTags(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TAGS)));
            draft.setUrl(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_URL)));
            draft.setPublished(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TIMESTAMP)));

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

        // Select All Query
        String selectQuery = "SELECT  * FROM " + Draft.TABLE_NAME + " WHERE " + Draft.COLUMN_ACCOUNT + "='" + account + "' " +
                "ORDER BY " + Draft.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list.
        if (cursor.moveToFirst()) {
            do {
                Draft draft = new Draft();
                draft.setId(cursor.getInt(cursor.getColumnIndex(Draft.COLUMN_ID)));
                draft.setAccount(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_ACCOUNT)));
                draft.setType(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TYPE)));
                draft.setName(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_NAME)));
                draft.setBody(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_BODY)));
                draft.setImage(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_IMAGE)));
                draft.setTags(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TAGS)));
                draft.setUrl(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_URL)));
                draft.setPublished(cursor.getString(cursor.getColumnIndex(Draft.COLUMN_TIMESTAMP)));

                drafts.add(draft);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return drafts;
    }
}