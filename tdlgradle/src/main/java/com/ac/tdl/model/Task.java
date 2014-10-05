package com.ac.tdl.model;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ac.tdl.MainActivity;
import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.managers.HashtagManager;

import java.util.ArrayList;
import java.util.List;

public class Task extends Model implements DbContract, Cloneable {

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
    private List<Hashtag> hashtagList;

    private static final String INCOMPLETE = "Incomplete";
    private static final String TODAY = "Today";
    private static SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();
    private HashtagManager hashtagManager = HashtagManager.getInstance();

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
     * @param hashtagList
     */
    protected Task(long taskId, String taskTitle, String taskDetails,
                boolean priority, long dateCreated, long dateReminder,
                long repetitionInMS, long notifyBeforeReminderInMS,
                boolean isComplete, boolean archived, List<Hashtag> hashtagList) {
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
        this.hashtagList = hashtagList;
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

        for (String hashtag : hashtags) {
            Hashtag hashtagObject = new HashtagBuilder().withLabel(hashtag).withTaskId(taskId).build();
            hashtagManager.save(hashtagObject);
        }
        setHashtagList(hashtagManager.getHashtagsByTaskId(taskId));
    }

    /**
     * Gets the hashtags saved in TaskTitle and TaskDetails in memory
     *
     * @return List<String>
     */
    public List<String> getHashtagsLabelsFromUpdatedFields() {
        List<String> hashtags = HashtagManager.getHashtagListFromString(taskDetails);
        hashtags.addAll(HashtagManager.getHashtagListFromString(taskTitle));
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
        hashtagManager.notifyDistinctHashtagChanged();
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

        List<Hashtag> oldHashtagList = getCopyOfHashtagList();

        if (isArchived()) {
            if (oldHashtagList != null)
                for (Hashtag hashtag : oldHashtagList) {
                    hashtag.setArchived(true);
                    hashtagManager.save(hashtag);
                }
            setHashtagList(null);
            return;
        }

        List<String> newHashtagList = getHashtagsLabelsFromUpdatedFields();
        if (oldHashtagList != null) {
            for (Hashtag oldHashtag : oldHashtagList) {
                // if oldhashtags don't exist anymore, set them as archived
                if (!newHashtagList.contains(oldHashtag.getLabel())) {
                    oldHashtag.setArchived(true);
                    hashtagManager.save(oldHashtag);

                }
            }
        }
        if (newHashtagList != null) {
            for (String newHashtag : newHashtagList) {
                // if newhashtags don't exist, create them
                if (!doesValueExistInHashtagList(newHashtag)) {
                    Hashtag hashtag = new HashtagBuilder().withLabel(newHashtag).withTaskId(taskId).build();
                    hashtagManager.save(hashtag);
                }
            }
            setHashtagList(hashtagManager.getHashtagsByTaskId(this.getTaskId()));
        } else
            setHashtagList(null);

    }
    public Task clone() throws CloneNotSupportedException{
        Task clone=(Task)super.clone();
        clone.setHashtagList(getCopyOfHashtagList());
        return clone;
    }

    public boolean doesValueExistInHashtagList(String value) {
        List<Hashtag> array = getHashtagList();
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

    public List<Hashtag> getHashtagList() {
        if (this.hashtagList == null)
            this.hashtagList = hashtagManager.getHashtagsByTaskId(taskId);
        return this.hashtagList;
    }

    public List<Hashtag> getCopyOfHashtagList() {
        List<Hashtag> h = getHashtagList();
        List<Hashtag> copy = new ArrayList<Hashtag>();
        try {
            for (Hashtag hashtag : h)
                copy.add(hashtag.clone());
        }catch(Exception e){
            Log.d("Task - getCopyOfHashtagList", e.toString());
        }
        return copy;
    }

    public List<String> getHashtagLabelsList() {
        if (getHashtagList() == null)
            return null;
        List<String> list = new ArrayList<String>();
        for (Hashtag h : getHashtagList())
            list.add(h.getLabel());
        return list;
    }

    public void setHashtagList(List<Hashtag> hashtagList) {
        this.hashtagList = hashtagList;
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
