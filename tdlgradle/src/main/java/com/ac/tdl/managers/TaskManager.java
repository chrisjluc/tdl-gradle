package com.ac.tdl.managers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.managers.helpers.TaskComparatorByDateReminder;
import com.ac.tdl.managers.helpers.TaskFilterByHashtag;
import com.ac.tdl.managers.helpers.TaskFilterByUnarchived;
import com.ac.tdl.managers.helpers.TaskListFilter;
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
    private TaskListFilter taskListFilter;
    private HashtagManager hashtagManager = HashtagManager.getInstance();

    public static TaskManager getInstance() {
        if (instance == null)
            instance = new TaskManager();
        return instance;
    }

    /**
     * For testing ONLY
     *
     * @return
     */
    public static void nullifyInstance() {
        instance = null;
    }

    public HashMap<String, List<Task>> tasksToDisplayByHeader;
    public List<String> orderedHeaderList;

    public TaskManager() {
        putAllTasksIntoCache();
        tasksToDisplayByHeader = new HashMap<String, List<Task>>();
        orderedHeaderList = new ArrayList<String>();
    }

    private void putAllTasksIntoCache() {
        Cursor cursor = db.rawQuery("SELECT * FROM "
                + DbContract.TaskTable.TABLE_NAME, null);
        while (cursor.moveToNext())
            this.add(createTaskFromCursor(cursor));
    }

    public HashMap<String, List<Task>> getTasksToDisplayByHeader() {
        if (this.tasksToDisplayByHeader == null)
            refreshTasksToDisplayByHeader();
        return this.tasksToDisplayByHeader;
    }

    private void setTasksToDisplayByHeader(HashMap<String, List<Task>> tasksToDisplayByHeader) {
        this.tasksToDisplayByHeader = tasksToDisplayByHeader;
    }

    public void setFilter(TaskListFilter taskListFilter) {
        this.taskListFilter = taskListFilter;
        refreshTasksToDisplayByHeader();
    }



    public void refreshTasksToDisplayByHeader() {
        groupTasksToDisplayByDate(getUnarchivedTasksListOrderedByTime());
    }

    private String getHeaderByTask(Task task){
        long currentDate = getCurrentDate();

        if (task.getDateReminder() > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(task.getDateReminder());
            long taskReminderDate = floorDateByDay(c);

            if (taskReminderDate < currentDate)
                return INCOMPLETE;
            else if (taskReminderDate == currentDate)
                return TODAY;
            else {
                Date date = new Date(taskReminderDate);
                return new SimpleDateFormat("EEEE MMM d").format(date);
            }
        }
        return TODAY;
    }

    private void groupTasksToDisplayByDate(List<Task> tasks) {
        if(tasksToDisplayByHeader == null)
            tasksToDisplayByHeader = new HashMap<String, List<Task>>();
        else
            tasksToDisplayByHeader.clear();

        orderedHeaderList.clear();

        for (Task task : tasks) {

            String header = getHeaderByTask(task);

            if (!tasksToDisplayByHeader.containsKey(header)) {
                tasksToDisplayByHeader.put(header, new ArrayList<Task>());
                orderedHeaderList.add(header);
            }
            tasksToDisplayByHeader.get(header).add(task);
        }
    }

    /**
     * From Earliest task to latest task
     *
     * @return tasksList
     */
    public List<Task> getUnarchivedTasksListOrderedByTime() {
        long currentTime = getCurrentDate();
        List<Task> unarchivedTasks = new TaskFilterByUnarchived().filter(this);
        if (taskListFilter.getHashtagFilter() != null)
            unarchivedTasks = new TaskFilterByHashtag(taskListFilter.getHashtagFilter()).filter(unarchivedTasks);

        for (Task task : unarchivedTasks)
            if (task.getDateReminder() == 0)
                task.setDateReminder(currentTime);

        Collections.sort(unarchivedTasks, new TaskComparatorByDateReminder());

        for (Task task : unarchivedTasks)
            if (task.getDateReminder() == currentTime)
                task.setDateReminder(0);

        return unarchivedTasks;
    }


    private Task createTaskFromCursor(Cursor cursor) {
        Task t = new Task();
        try {
            t.setTaskId(cursor.getLong(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_ID)));
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
            addTaskToCache(task);
        } else if (existingTask.isArchived()) {
            existingTask.updateModel();
            removeTaskFromCache(existingTask);
        }else{
            existingTask.updateModel();
            //TODO: Allows headers to be added, but has to remake the whole map, should be a better way
            refreshTasksToDisplayByHeader();
        }
    }

    private void addTaskToCache(Task task){
        String header = getHeaderByTask(task);
        if(!this.contains(task))
            this.add(task);

        if(taskListFilter.anyFiltersApplied()){
            if(!task.doesValueExistInHashtagList(taskListFilter.getHashtagFilter()))
                return;
        }

        //TODO: No way to compare headers to just add it, have to create from scratch (unless header object is made)
        //             tasksToDisplayByHeader.put(header, new ArrayList<Task>());
        if(!tasksToDisplayByHeader.containsKey(header)){
            refreshTasksToDisplayByHeader();
            return;
        }

        if(!tasksToDisplayByHeader.get(header).contains(task))
            tasksToDisplayByHeader.get(header).add(task);
    }

    private void removeTaskFromCache(Task task){
        String header = getHeaderByTask(task);
        if(tasksToDisplayByHeader.containsKey(header)
                && tasksToDisplayByHeader.get(header).contains(task)) {
            List<Task> list = tasksToDisplayByHeader.get(header);
            list.remove(task);
            if(list.size() == 0) {
                tasksToDisplayByHeader.remove(header);
                orderedHeaderList.remove(header);
            }
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

    public List<String> getOrderedHeaderList() {
        return orderedHeaderList;
    }

}
