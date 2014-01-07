package com.ac.tdl.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ac.tdl.SQL.DbContract;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task extends Model implements DbContract {

    // Stored in SQL
    private int taskId;
    private String taskTitle;
    private String taskDetails;
    private boolean priority;
    private long dateCreated;
    private long dateReminder;
    private long repetitionInMS;
    private long notifyBeforeReminderInMS;
    private boolean isComplete;
    private boolean archived;

    // Additional attributes
    private Hashtag[] hashtagArray;
    private SQLiteDatabase db;

    /**
     * Important: need DB is you want SQL cmds
     *
     * @param taskId
     * @param taskTitle
     * @param taskDetails
     * @param priority
     * @param dateCreated
     * @param dateReminder
     * @param repetitionInMS
     * @param notifyBeforeReminderInMS
     * @param isComplete
     * @param archived
     * @param hashtagArray
     * @param db
     */
    public Task(int taskId, String taskTitle, String taskDetails,
                boolean priority, long dateCreated, long dateReminder,
                long repetitionInMS, long notifyBeforeReminderInMS,
                boolean isComplete, boolean archived, Hashtag[] hashtagArray,
                SQLiteDatabase db) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskDetails = taskDetails;
        this.priority = priority;
        this.dateCreated = dateCreated;
        this.dateReminder = dateReminder;
        this.repetitionInMS = repetitionInMS;
        this.notifyBeforeReminderInMS = notifyBeforeReminderInMS;
        this.isComplete = isComplete;
        this.archived = archived;
        this.hashtagArray = hashtagArray;
        this.db = db;
    }

    /**
     * Constructor with at least the taskId, for this method to run
     */
    @Override
    public void getModelFromDb() {
        String[] projection = {TaskTable.COLUMN_NAME_TITLE,
                TaskTable.COLUMN_NAME_DETAILS, TaskTable.COLUMN_NAME_PRIORITY,
                TaskTable.COLUMN_NAME_DATE_CREATED,
                TaskTable.COLUMN_NAME_DATE_REMINDER,
                TaskTable.COLUMN_NAME_REPETITION_MS,
                TaskTable.COLUMN_NAME_NOTIFY_BEFORE_REMINDER_MS,
                TaskTable.COLUMN_NAME_IS_COMPLETE,
                TaskTable.COLUMN_NAME_ARCHIVED};
        String selection = TaskTable.COLUMN_NAME_ID + "=? ";
        String[] selectionArgs = null;
        try {
            selectionArgs = new String[]{String.valueOf(taskId)};
            Cursor cursor = db.query(TaskTable.TABLE_NAME, projection,
                    selection, selectionArgs, null, null, null);
            cursor.moveToFirst();
            setTaskTitle(cursor.getString(cursor
                    .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_TITLE)));
            setTaskDetails(cursor.getString(cursor
                    .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_DETAILS)));
            setPriority(getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_PRIORITY))));
            setDateCreated(cursor.getLong(cursor
                    .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_DATE_CREATED)));
            setDateReminder(cursor
                    .getLong(cursor
                            .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_DATE_REMINDER)));
            setRepetitionInMS(cursor
                    .getLong(cursor
                            .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_REPETITION_MS)));
            setNotifyBeforeReminderInMS(cursor
                    .getLong(cursor
                            .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_NOTIFY_BEFORE_REMINDER_MS)));
            setComplete(getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_IS_COMPLETE))));
            setArchived(getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_ARCHIVED))));
            getHashtagArrayFromDb();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
    }

    /**
     * Saves task into database, and parses out the hashtags and saves them in
     * the db
     */
    public void saveModel() {
        setModelInDb();
        saveHashtagList();
    }

    /**
     * Gets the hashtags saved in TaskTitle and TaskDetails in memory
     *
     * @return List<String>
     */
    public List<String> getHashtagsLabelsFromUpdatedFields() {
        List<String> hashtags = getHashtagList(taskDetails);
        hashtags.addAll(getHashtagList(taskTitle));
        return hashtags;
    }

    /**
     * Get hashtag labels from Hashtag objects in the hashtag array
     *
     * @return List<String>
     */
    public List<String> getHashtagLabels() {
        List<String> hashtagLabels = new ArrayList<String>();
        for (Hashtag hashtag : hashtagArray) {
            hashtagLabels.add(hashtag.getLabel());
        }
        return hashtagLabels;
    }

    public void updateTaskTitle(String taskTitle) {
        updateTask(taskId, taskTitle, TaskTable.COLUMN_NAME_TITLE);
        setTaskTitle(taskTitle);
        updateHashtagIfChanged();
    }

    public void updateTaskDetails(String taskDetails) {
        updateTask(taskId, taskDetails, TaskTable.COLUMN_NAME_DETAILS);
        setTaskDetails(taskDetails);
        updateHashtagIfChanged();
    }

    public void updateIsComplete(boolean isComplete) {
        updateTask(taskId, isComplete, TaskTable.COLUMN_NAME_IS_COMPLETE);
        setComplete(isComplete);
        //TODO : archive hashtags, when iscompleted is undone bring the hashtags back
    }

    public void updateArchived(boolean archived) {
        updateTask(taskId, archived, TaskTable.COLUMN_NAME_ARCHIVED);
        setArchived(archived);
        getHashtagArrayFromDb();
        if (hashtagArray == null || archived == false) return;
        for (Hashtag hashtag : hashtagArray) {
            hashtag.archiveHashtag();
        }
    }

    private void getHashtagArrayFromDb() {
        String[] projection = {HashtagTable.COLUMN_NAME_ID};
        String selection = HashtagTable.COLUMN_NAME_TASK_ID + "=? AND "
                + HashtagTable.COLUMN_NAME_ARCHIVED + "=?";
        String[] selectionArgs = {String.valueOf(taskId), "0"};
        Cursor cursor = db.query(HashtagTable.TABLE_NAME, projection,
                selection, selectionArgs, null, null, null);
        if (cursor.getCount() == 0) {
            return;
        }
        Hashtag[] hashtagList = new Hashtag[cursor.getCount()];
        int count = 0;
        while (cursor.moveToNext()) {
            Hashtag hashtag = new HashtagBuilder().withDb(db).build();
            hashtag.setHashtagId(cursor.getInt(cursor
                    .getColumnIndexOrThrow(HashtagTable.COLUMN_NAME_ID)));
            hashtag.getModelFromDb();
            hashtagList[count++] = hashtag;
        }
        hashtagArray = hashtagList;
    }

    private void saveHashtagList() {
        List<String> hashtags = new ArrayList<String>();
        hashtags.addAll(getHashtagList(taskDetails));
        hashtags.addAll(getHashtagList(taskTitle));
        for (String hashtag : hashtags) {
            Hashtag hashtagObject = new HashtagBuilder().withLabel(hashtag).withTaskId(taskId).withDb(db).build();
            hashtagObject.setModelInDb();
        }
    }

    @Override
    public void setModelInDb() {
        if (dateCreated == 0) {
            dateCreated = getCurrentTime();
        }
        ContentValues taskValues = new ContentValues();
        taskValues.put(TaskTable.COLUMN_NAME_TITLE, taskTitle);
        taskValues.put(TaskTable.COLUMN_NAME_DETAILS, taskDetails);
        taskValues
                .put(TaskTable.COLUMN_NAME_PRIORITY, getIntFromBool(priority));
        taskValues.put(TaskTable.COLUMN_NAME_DATE_CREATED, dateCreated);
        taskValues.put(TaskTable.COLUMN_NAME_DATE_REMINDER, dateReminder);
        taskValues.put(TaskTable.COLUMN_NAME_REPETITION_MS, repetitionInMS);
        taskValues.put(TaskTable.COLUMN_NAME_NOTIFY_BEFORE_REMINDER_MS,
                notifyBeforeReminderInMS);
        taskValues.put(TaskTable.COLUMN_NAME_IS_COMPLETE,
                getIntFromBool(isComplete));
        taskValues
                .put(TaskTable.COLUMN_NAME_ARCHIVED, getIntFromBool(archived));
        taskId = (int) db.insert(TaskTable.TABLE_NAME, null, taskValues);
    }

    /**
     * Updates the specified column with a value (value only supports String and
     * int)
     *
     * @param taskId
     * @param value
     * @param columnName
     */
    private void updateTask(int taskId, Object value, String columnName) {
        ContentValues values = new ContentValues();

        if (value instanceof String) {
            values.put(columnName, (String) value);
        } else {
            values.put(columnName, getIntFromBool((Boolean) value));
        }

        String selection = TaskTable.COLUMN_NAME_ID + "=? ";
        String[] selectionArgs = {String.valueOf(taskId)};
        db.update(TaskTable.TABLE_NAME, values, selection, selectionArgs);
    }

    /**
     * @param input
     * @return Array of valid hashtags
     */
    private List<String> getHashtagList(String input) {
        ArrayList<String> hashtagList = new ArrayList<String>();
        if (input == null || input.isEmpty()) return hashtagList;
        Pattern pattern = Pattern
                .compile("(?:\\s|\\A)[##]+([A-Za-z0-9-_]+[A-Za-z0-9-_])");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group(1);
            //Don't want numbers to be valid hashtags
            if(!isNumber(match))
                hashtagList.add(match);
        }
        return hashtagList;
    }

    public static boolean isNumber(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

    private void updateHashtagIfChanged() {
        Hashtag[] oldHashtagList = getHashtagArray();
        List<String> newHashtagList = getHashtagsLabelsFromUpdatedFields();
        if (oldHashtagList != null) {
            for (Hashtag oldHashtag : oldHashtagList) {
                // if oldhashtags don't exist anymore, set them as archived
                if (!newHashtagList.contains(oldHashtag.getLabel())) {
                    oldHashtag.archiveHashtag();
                }
            }
        }
        if (newHashtagList != null) {
            for (String newHashtag : newHashtagList) {
                // if newhashtags don't exist, create them
                if (!doesValueExistInHashtagList(newHashtag)) {
                    Hashtag hashtag = new HashtagBuilder().withLabel(newHashtag).withTaskId(taskId).withDb(db).build();
                    hashtag.setModelInDb();
                }
            }
        }
    }

    private boolean doesValueExistInHashtagList(String value) {
        for (Hashtag oldHashtag : getHashtagArray()) {
            if (oldHashtag.getLabel().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDetails() {
        return taskDetails;
    }

    public void setTaskDetails(String taskDetails) {
        this.taskDetails = taskDetails;
    }

    public long getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(long dateCreated) {
        this.dateCreated = dateCreated;
    }

    /**
     * Queries for hashtags if hashtag array is null
     *
     * @return hashtag array
     */
    public Hashtag[] getHashtagArray() {
        if (hashtagArray == null) {
            getHashtagArrayFromDb();
        }
        return hashtagArray;
    }

    public void setHashtagArray(Hashtag[] hashtagArray) {
        this.hashtagArray = hashtagArray;
    }

    public boolean isPriority() {
        return priority;
    }

    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    public long getDateReminder() {
        return dateReminder;
    }

    public void setDateReminder(long dateReminder) {
        this.dateReminder = dateReminder;
    }

    public long getRepetitionInMS() {
        return repetitionInMS;
    }

    public void setRepetitionInMS(long repetitionInMS) {
        this.repetitionInMS = repetitionInMS;
    }

    public long getNotifyBeforeReminderInMS() {
        return notifyBeforeReminderInMS;
    }

    public void setNotifyBeforeReminderInMS(long notifyBeforeReminderInMS) {
        this.notifyBeforeReminderInMS = notifyBeforeReminderInMS;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public SQLiteDatabase getDb() {
        return db;
    }

    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
