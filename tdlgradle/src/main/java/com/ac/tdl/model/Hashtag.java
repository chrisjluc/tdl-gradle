package com.ac.tdl.model;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;

public class Hashtag extends Model implements DbContract , Cloneable{

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
    protected Hashtag(long hashtagId, String label, long dateCreated, long taskId,
                   boolean archived) {
        this.hashtagId = hashtagId;
        this.label = label;
        this.dateCreated = dateCreated;
        this.taskId = taskId;
        this.archived = archived;
    }

    public Hashtag() {
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

    public Hashtag clone() throws CloneNotSupportedException {
        Hashtag clone=(Hashtag)super.clone();
        return clone;
    }

    public boolean equals(Object o){
        if (!(o instanceof Hashtag))
            return false;
        Hashtag obj = (Hashtag) o;
        if(hashtagId == obj.getHashtagId())
            return true;

        return false;
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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
