package com.ac.tdl.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.managers.HashtagManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Task extends Model implements DbContract {

    // Stored in SQL
    private long taskId;
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

    private static final String INCOMPLETE = "Incomplete";
    private static final String TODAY = "Today";
    private static SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();

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
     */
    public Task(long taskId, String taskTitle, String taskDetails,
                boolean priority, long dateCreated, long dateReminder,
                long repetitionInMS, long notifyBeforeReminderInMS,
                boolean isComplete, boolean archived, Hashtag[] hashtagArray) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        if (taskDetails == null || taskDetails.isEmpty())
            this.taskDetails = "";
        else
            this.taskDetails = taskDetails;
        this.priority = priority;
        this.dateCreated = dateCreated;
        this.dateReminder = dateReminder;
        this.repetitionInMS = repetitionInMS;
        this.notifyBeforeReminderInMS = notifyBeforeReminderInMS;
        this.isComplete = isComplete;
        this.archived = archived;
        this.hashtagArray = hashtagArray;
    }

    public Task() {

    }
    /**
     * SHOULD ONLY BE CALLED BY TASK MANAGER
     *
     * Saves task into database, and parses out the hashtags and saves them in
     * the db
     */
    public void saveModel() {
        setModelInDb();
        saveHashtagList();
    }

    private void saveHashtagList() {
        List<String> hashtags = new ArrayList<String>();
        hashtags.addAll(HashtagManager.getHashtagListFromString(taskDetails));
        hashtags.addAll(HashtagManager.getHashtagListFromString(taskTitle));
        if (hashtags.isEmpty())
            return;
        List<Hashtag> hashtagObjectList = new ArrayList<Hashtag>();

        for (String hashtag : hashtags) {
            Hashtag hashtagObject = new HashtagBuilder().withLabel(hashtag).withTaskId(taskId).build();
            hashtagObject.setModelInDb();
            hashtagObjectList.add(hashtagObject);
        }
        setHashtagArray(hashtagObjectList.toArray(new Hashtag[hashtags.size()]));
    }

    /**
     * Gets the hashtags saved in TaskTitle and TaskDetails in memory
     *
     * @return List<String>
     */
    public List<String> getHashtagsLabelsFromUpdatedFields() {
        List<String> hashtags = HashtagManager.getHashtagListFromString(taskDetails);
        hashtags.addAll(HashtagManager.getHashtagListFromString(taskTitle));
        if (hashtags.isEmpty())
            return null;
        return hashtags;
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
        long id = db.insert(TaskTable.TABLE_NAME, null, taskValues);
        setTaskId(id);
    }

    public void updateModel() {
        updateModelInDb();
        updateHashtagIfChanged();
//        if (!isArchived()) {
//            hashtagManager.getHashtagById();
//        }
    }

    private void updateModelInDb() {
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
        String selection = TaskTable.COLUMN_NAME_ID + "=? ";
        String[] selectionArgs = {String.valueOf(taskId)};
        db.update(TaskTable.TABLE_NAME, taskValues, selection, selectionArgs);
    }

    private void updateHashtagIfChanged() {

        Hashtag[] oldHashtagList = getHashtagArray();

        if (isArchived()) {
            if (oldHashtagList != null)
                for (Hashtag hashtag : oldHashtagList)
                    hashtag.archiveHashtag();
            setHashtagArray(null);
            return;
        }

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
            List<Hashtag> hashtagObjectList = new ArrayList<Hashtag>();
            for (String newHashtag : newHashtagList) {
                // if newhashtags don't exist, create them
                if (!doesValueExistInHashtagList(newHashtag)) {
                    Hashtag hashtag = new HashtagBuilder().withLabel(newHashtag).withTaskId(taskId).build();
                    hashtag.setModelInDb();
                    hashtagObjectList.add(hashtag);
                }
            }
            setHashtagArray(hashtagObjectList.toArray(new Hashtag[newHashtagList.size()]));
        } else
            setHashtagArray(null);
    }

    public boolean doesValueExistInHashtagList(String value) {
        Hashtag[] array = getHashtagArray();
        if (array == null)
            return false;
        for (Hashtag oldHashtag : array) {
            if (oldHashtag.getLabel().equals(value)) {
                return true;
            }
        }
        return false;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
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

    public Hashtag[] getHashtagArray() {
        return this.hashtagArray;
    }

    public List<String> getHashtagLabelsList() {
        if (getHashtagArray() == null)
            return null;
        List<String> list = new ArrayList<String>();
        for (Hashtag h : getHashtagArray())
            list.add(h.getLabel());
        return list;
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
