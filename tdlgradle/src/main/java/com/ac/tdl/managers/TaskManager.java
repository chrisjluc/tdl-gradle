package com.ac.tdl.managers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.managers.helpers.TaskComparatorByDateReminder;
import com.ac.tdl.managers.helpers.TaskFilterByHashtag;
import com.ac.tdl.managers.helpers.TaskFilterByUnarchived;
import com.ac.tdl.model.Task;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public class TaskManager extends ArrayList<Task> implements ITaskManager {

    private static TaskManager instance;
    private static SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();
    private static final String INCOMPLETE = "Incomplete";
    private static final String TODAY = "Today";

    public static TaskManager getInstance() {
        if (instance == null)
            instance = new TaskManager();
        return instance;
    }

    /**
     * For testing ONLY
     * @return
     */
    public static void nullifyInstance() {
        instance = null;
    }

    public TaskManager() {
        putAllTasksIntoCache();
    }

    private void putAllTasksIntoCache() {
        Cursor cursor = db.rawQuery("SELECT * FROM "
                + DbContract.TaskTable.TABLE_NAME, null);
        while (cursor.moveToNext())
            this.add(createTaskFromCursor(cursor));
    }

    @Override
    public HashMap<String, List<Task>> getUnarchivedTasksByHeaderOrdered(List<String> orderedHeaderList) {
        return groupTasksByDate(orderedHeaderList, getUnarchivedTasksListOrderedByTime());
    }

    @Override
    public HashMap<String, List<Task>> getUnArchivedTasksByHeaderAndHashtagOrdered(List<String> orderedHeaderList, String hashtagLabel) {
        return groupTasksByDate(orderedHeaderList, getUnarchivedTasksListByHashtagOrderedByTime(hashtagLabel));
    }

    private HashMap<String, List<Task>> groupTasksByDate(List<String> headers, List<Task> tasks) {
        HashMap<String, List<Task>> map = new HashMap<String, List<Task>>();
        long currentDate = getCurrentDate();

        for (Task task : tasks) {

            //If Reminderdate is set
            if (task.getDateReminder() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(task.getDateReminder());
                long taskReminderDate = floorDateByDay(c);

                if (taskReminderDate < currentDate) {

                    if (!map.containsKey(INCOMPLETE)) {
                        List<Task> tasksList = new ArrayList<Task>();
                        tasksList.add(task);
                        map.put(INCOMPLETE, tasksList);
                        headers.add(INCOMPLETE);
                    } else {
                        List<Task> tasksList = map.get(INCOMPLETE);
                        tasksList.add(task);
                        map.put(INCOMPLETE, tasksList);
                    }

                } else if (taskReminderDate == currentDate) {

                    if (!map.containsKey(TODAY)) {
                        List<Task> tasksList = new ArrayList<Task>();
                        tasksList.add(task);
                        map.put(TODAY, tasksList);
                        headers.add(TODAY);
                    } else {
                        List<Task> tasksList = map.get(TODAY);
                        tasksList.add(task);
                        map.put(TODAY, tasksList);
                    }

                } else {

                    Date date = new Date(taskReminderDate);
                    String dateHeader = new SimpleDateFormat("EEEE MMM d").format(date);

                    if (!map.containsKey(dateHeader)) {
                        List<Task> tasksList = new ArrayList<Task>();
                        tasksList.add(task);
                        map.put(dateHeader, tasksList);
                        headers.add(dateHeader);
                    } else {
                        List<Task> tasksList = map.get(dateHeader);
                        tasksList.add(task);
                        map.put(dateHeader, tasksList);
                    }
                }
            } else {
                //if reminder date is zero, it's not set so just put that task under today
                if (!map.containsKey(TODAY)) {
                    List<Task> tasksList = new ArrayList<Task>();
                    tasksList.add(task);
                    map.put(TODAY, tasksList);
                    headers.add(TODAY);
                } else {
                    List<Task> tasksList = map.get(TODAY);
                    tasksList.add(task);
                    map.put(TODAY, tasksList);
                }
            }
        }
        return map;
    }

    /**
     * From Earliest task to latest task
     * tasks with no reminder date will always be the first under 'today' section
     *
     * @return tasksList
     */
    public List<Task> getUnarchivedTasksListByHashtagOrderedByTime(String hashtagLabel) {
        long currentTime = getCurrentTime();
        List<Task> unarchivedTasks = new TaskFilterByUnarchived().filter(this);
        if (hashtagLabel != null)
            unarchivedTasks = new TaskFilterByHashtag(hashtagLabel).filter(unarchivedTasks);

        for (Task task : unarchivedTasks)
            if (task.getDateReminder() == 0)
                task.setDateReminder(currentTime);

        Collections.sort(unarchivedTasks, new TaskComparatorByDateReminder());

        for (Task task : unarchivedTasks)
            if (task.getDateReminder() == currentTime)
                task.setDateReminder(0);

        return unarchivedTasks;
    }

    public List<Task> getUnarchivedTasksListOrderedByTime() {
        return getUnarchivedTasksListByHashtagOrderedByTime(null);
    }

      /* List<Task> orderedTaskList = new ArrayList<Task>();
        List<Task> emptyReminderTaskList = new ArrayList<Task>();
        while (cursor.moveToNext()) {
            Task t = new Task();
            t.setTaskId(cursor.getInt(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_ID)));
            t.getModelFromDb();

            if (t.getDateReminder() == 0)
                emptyReminderTaskList.add(t);
            else
                orderedTaskList.add(t);
        }
        if (orderedTaskList.size() == 0)
            return emptyReminderTaskList;
        else if (emptyReminderTaskList.size() > 0)
            return mergeListsOnReminderDate(orderedTaskList, emptyReminderTaskList);

        return orderedTaskList;
    }

    private List<Task> mergeListsOnReminderDate(List<Task> orderedTaskList, List<Task> emptyReminderTaskList) {
        List<Task> mergedTaskList = new ArrayList<Task>();
        int index = 0;

        //12:00 am of currentday
        long currentDayTimestamp = getCurrentDate();

        for (Task orderedTask : orderedTaskList) {

            //Ordered tasks at 12:00 am on current date will appear after empty reminders
            if (orderedTask.getDateReminder() >= currentDayTimestamp) {
                mergedTaskList.addAll(emptyReminderTaskList);
                emptyReminderTaskList = null;
                break;
            }
            mergedTaskList.add(orderedTask);
            index++;
        }
        if (emptyReminderTaskList != null)
            mergedTaskList.addAll(emptyReminderTaskList);
        mergedTaskList.addAll(orderedTaskList.subList(index, orderedTaskList.size()));
        return mergedTaskList;
    }*/

    private Task createTaskFromCursor(Cursor cursor) {
        Task t = new Task();
        try {
            t.setTaskTitle(cursor.getString(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_TITLE)));
            t.setTaskDetails(cursor.getString(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_DETAILS)));
            t.setPriority(getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_PRIORITY))));
            t.setDateCreated(cursor.getLong(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_DATE_CREATED)));
            t.setDateReminder(cursor
                    .getLong(cursor
                            .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_DATE_REMINDER)));
            t.setRepetitionInMS(cursor
                    .getLong(cursor
                            .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_REPETITION_MS)));
            t.setNotifyBeforeReminderInMS(cursor
                    .getLong(cursor
                            .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_NOTIFY_BEFORE_REMINDER_MS)));
            t.setComplete(getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_IS_COMPLETE))));
            t.setArchived(getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_ARCHIVED))));
            //getHashtagArrayFromDb();
        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return t;
    }

    @Override
    public Task getTaskById(long id) {
        for (Task task : this)
            if (task.getTaskId() == id)
                return task;
        return null;
    }

    public void save(Task task) {
        Task existingTask = getTaskById(task.getTaskId());
        if (existingTask == null) {
            task.saveModel();
            this.add(task);
        } else {
            existingTask.updateModel();
        }
    }

    protected static int getIntFromBool(boolean isTrue) {
        if (isTrue) {
            return 1;
        }
        return 0;
    }

    protected boolean getBoolFromInt(int isTrue) throws Exception {
        if (isTrue == 1) {
            return true;
        } else if (isTrue == 0) {
            return false;
        }
        throw new Exception();
    }

    protected static long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    protected static long floorDateByDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime().getTime();
    }

    protected static long getCurrentDate() {
        Calendar c = new GregorianCalendar();
        return floorDateByDay(c);
    }
}
