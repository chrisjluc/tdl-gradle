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

	private int hashtagId;
	private String label;
	private long dateCreated;
	private int taskId;
	private boolean archived;
	private static SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();

	/**
	 * Important: Need DB if you want to perform SQL cmds
	 * @param hashtagId
	 * @param label
	 * @param dateCreated
	 * @param taskId
	 * @param archived
	 */
	public Hashtag(int hashtagId, String label, long dateCreated, int taskId,
			boolean archived) {
		this.hashtagId = hashtagId;
		this.label = label;
		this.dateCreated = dateCreated;
		this.taskId = taskId;
		this.archived = archived;
	}

    public Hashtag(){}

    public static String[] getHashtagLabelsInDb() {
        String[] projection = {DbContract.HashtagTable.COLUMN_NAME_HASHTAG_LABEL};
        Cursor cursor = db.query(true, DbContract.HashtagTable.TABLE_NAME, projection,
                null, null, null, null, null, null);
        ArrayList<String> hashtagArray = new ArrayList<String>();
        while (cursor.moveToNext()) {
            hashtagArray.add(cursor.getString(0));
        }
        return hashtagArray.toArray(new String[hashtagArray.size()]);
    }

	@Override
	public void getModelFromDb() {
		String[] projection = { HashtagTable.COLUMN_NAME_HASHTAG_LABEL,
				HashtagTable.COLUMN_NAME_DATE_CREATED,
				HashtagTable.COLUMN_NAME_TASK_ID,
				HashtagTable.COLUMN_NAME_ARCHIVED
				};
		String selection = HashtagTable.COLUMN_NAME_ID + "=? AND "
				+ HashtagTable.COLUMN_NAME_ARCHIVED + "=?";
		String[] selectionArgs = null;
		Cursor cursor = null;
		try {
			selectionArgs = new String[] { String.valueOf(hashtagId), "0" };
			cursor = db.query(HashtagTable.TABLE_NAME, projection, selection,
					selectionArgs, null, null, null);

			if (cursor.getCount() == 0) {
				return;
			}
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
   public static String createHashtagString(Hashtag[] array){
            StringBuilder hashtagBuilder = new StringBuilder();

            if (array == null) {
                return "";
            }

            for (Hashtag hashtag : array){
                hashtagBuilder.append(" #" + hashtag.getLabel());
            }
            return hashtagBuilder.toString();
        }
    public static String createHashtagString(List<String> array){
        StringBuilder hashtagBuilder = new StringBuilder();

        if (array == null) {
            return "";
        }

        for (String s: array){
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
		db.insert(HashtagTable.TABLE_NAME, null, hashtagValues);
	}

	public void archiveHashtag() {
		ContentValues values = new ContentValues();
		values.put(HashtagTable.COLUMN_NAME_ARCHIVED, 1);
		String selection = HashtagTable.COLUMN_NAME_ID + "=? ";
		String[] selectionArgs = { String.valueOf(hashtagId) };
		db.update(HashtagTable.TABLE_NAME, values, selection, selectionArgs);
	}

	public int getHashtagId() {
		return hashtagId;
	}

	public void setHashtagId(int hashtagId) {
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

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public boolean getArchived() {
		return archived;
	}

	public void setArchived(boolean archived) {
		this.archived = archived;
	}
}
