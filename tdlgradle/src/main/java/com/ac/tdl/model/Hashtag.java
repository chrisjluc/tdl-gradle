package com.ac.tdl.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;

import java.util.ArrayList;
import java.util.List;

public class Hashtag extends Model implements DbContract {

    private long hashtagId;
    private String label;
    private long dateCreated;
    private long taskId;
    private boolean archived;
    private static SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();

    /**
     * Important: Need DB if you want to perform SQL cmds
     *
     * @param hashtagId
     * @param label
     * @param dateCreated
     * @param taskId
     * @param archived
     */
    public Hashtag(long hashtagId, String label, long dateCreated, long taskId,
                   boolean archived) {
        this.hashtagId = hashtagId;
        this.label = label;
        this.dateCreated = dateCreated;
        this.taskId = taskId;
        this.archived = archived;
    }

    public Hashtag() {
    }

    public static List<String> getHashtagLabelsInDb() {
        String[] projection = {DbContract.HashtagTable.COLUMN_NAME_HASHTAG_LABEL};
        String selection = HashtagTable.COLUMN_NAME_ARCHIVED + "=?";
        String[] selectionArgs = new String[]{"0"};
        Cursor cursor = db.query(true, DbContract.HashtagTable.TABLE_NAME, projection,
                selection, selectionArgs, null, null, null, null);
        List<String> hashtagList = new ArrayList<String>();
        while (cursor.moveToNext()) {
            hashtagList.add(cursor.getString(0));
        }
        return hashtagList;
    }

    public void getModelFromDb() {
        String[] projection = {HashtagTable.COLUMN_NAME_HASHTAG_LABEL,
                HashtagTable.COLUMN_NAME_DATE_CREATED,
                HashtagTable.COLUMN_NAME_TASK_ID,
                HashtagTable.COLUMN_NAME_ARCHIVED
        };
        String selection = HashtagTable.COLUMN_NAME_ID + "=? AND "
                + HashtagTable.COLUMN_NAME_ARCHIVED + "=?";
        try {
            String[] selectionArgs = new String[]{String.valueOf(hashtagId), "0"};
            Cursor cursor = db.query(HashtagTable.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, null);
            cursor.moveToFirst();
            setLabel(cursor
                    .getString(cursor
                            .getColumnIndexOrThrow(HashtagTable.COLUMN_NAME_HASHTAG_LABEL)));
            setDateCreated(cursor
                    .getLong(cursor
                            .getColumnIndexOrThrow(HashtagTable.COLUMN_NAME_DATE_CREATED)));
            setTaskId(cursor.getInt(cursor
                    .getColumnIndexOrThrow(HashtagTable.COLUMN_NAME_TASK_ID)));
            setArchived(getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(HashtagTable.COLUMN_NAME_ARCHIVED))));
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
    }

    public static String createHashtagString(List<String> array) {
        StringBuilder hashtagBuilder = new StringBuilder();

        if (array == null) {
            return "";
        }

        for (String s : array) {
            hashtagBuilder.append(" #" + s);
        }
        return hashtagBuilder.toString();
    }

    @Override
    public void setModelInDb() {
        ContentValues hashtagValues = new ContentValues();
        hashtagValues.put(HashtagTable.COLUMN_NAME_HASHTAG_LABEL, label);
        if (dateCreated == 0) {
            this.dateCreated = getCurrentTime();
        }
        hashtagValues.put(HashtagTable.COLUMN_NAME_DATE_CREATED, dateCreated);
        hashtagValues.put(HashtagTable.COLUMN_NAME_TASK_ID, taskId);
        hashtagValues.put(HashtagTable.COLUMN_NAME_ARCHIVED, getIntFromBool(archived));
        long id = db.insert(HashtagTable.TABLE_NAME, null, hashtagValues);
        setHashtagId(id);
    }

    public void archiveHashtag() {
        ContentValues values = new ContentValues();
        values.put(HashtagTable.COLUMN_NAME_ARCHIVED, 1);
        String selection = HashtagTable.COLUMN_NAME_ID + "=? ";
        String[] selectionArgs = {String.valueOf(hashtagId)};
        db.update(HashtagTable.TABLE_NAME, values, selection, selectionArgs);
    }

    public long getHashtagId() {
        return hashtagId;
    }

    public void setHashtagId(long hashtagId) {
        this.hashtagId = hashtagId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public boolean getArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
