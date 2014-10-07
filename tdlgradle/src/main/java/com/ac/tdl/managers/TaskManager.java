package com.ac.tdl.managers;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ac.tdl.CalendarFragment;
import com.ac.tdl.GenericHelper;
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

    private CalendarFragment.DistinctDaysToHighlightChangeListener daysToHighlightListener;

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
        groupTasksToDisplayByDateAndPriority(getUnarchivedTasksListOrderedByTime());
        if(daysToHighlightListener != null)
            daysToHighlightListener.notifyChange(getDistinctTimestampsToHighlight());
    }

    private String getHeaderByTask(Task task){
        long currentDate = GenericHelper.getFlooredCurrentDate();

        if (task.getDateReminder() > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(task.getDateReminder());
            long taskReminderDate = GenericHelper.floorDateByDay(c);

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

    private void groupTasksToDisplayByDateAndPriority(List<Task> tasks) {

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

            addToTasksToDisplayByHeaderWithPriority(task, header);
        }
    }

    private void addToTasksToDisplayByHeaderWithPriority(Task task, String header){
        if(task.isPriority())
            tasksToDisplayByHeader.get(header).add(0,task);
        else
            tasksToDisplayByHeader.get(header).add(task);
    }

    public void setDistinctDaysToHighlightChangeListener(CalendarFragment.DistinctDaysToHighlightChangeListener listener) {
        this.daysToHighlightListener = listener;
    }

    public List<Long> getDistinctTimestampsToHighlight() {
        List<Long> timestamps = new ArrayList<Long>();
        List<Task> tasks = getUnarchivedTasksListOrderedByTime();
        for (Task task: tasks){
            long timestamp = GenericHelper.getFlooredCurrentDate();
            if (task.getDateReminder() > 0) {
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(task.getDateReminder());
                timestamp = GenericHelper.floorDateByDay(c);
            }
            if(!timestamps.contains(timestamp))
                timestamps.add(timestamp);
        }
        return timestamps;
    }

    /**
     * From Earliest task to latest task
     *
     * @return tasksList
     */
    public List<Task> getUnarchivedTasksListOrderedByTime() {
        long currentTime = GenericHelper.getFlooredCurrentDate();
        List<Task> unarchivedTasks = new TaskFilterByUnarchived().filter(this);
        if (taskListFilter != null && taskListFilter.getHashtagFilter() != null)
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
            t.setPriority(GenericHelper.getBoolFromInt(cursor.getInt(cursor
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
            t.setComplete(GenericHelper.getBoolFromInt(cursor.getInt(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_IS_COMPLETE))));
            t.setArchived(GenericHelper.getBoolFromInt(cursor.getInt(cursor
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

        if(taskListFilter != null && taskListFilter.getHashtagFilter() != null){
            if(!task.doesValueExistInHashtagList(taskListFilter.getHashtagFilter()))
                return;
        }

        //TODO: No way to compare headers to just add it, have to create from scratch (unless header object is made)
        //             tasksToDisplayByHeader.put(header, new ArrayList<Task>());
        if(!tasksToDisplayByHeader.containsKey(header)){
            refreshTasksToDisplayByHeader();
            return;
        }

        if(!tasksToDisplayByHeader.get(header).contains(task)){
            addToTasksToDisplayByHeaderWithPriority(task, header);
            if (daysToHighlightListener != null)
                daysToHighlightListener.notifyChange(getDistinctTimestampsToHighlight());
        }
    }

    private void removeTaskFromCache(Task task){
        String header = getHeaderByTask(task);
        if(tasksToDisplayByHeader.containsKey(header)
                && tasksToDisplayByHeader.get(header).contains(task)) {
            List<Task> list = tasksToDisplayByHeader.get(header);
            list.remove(task);
            if (daysToHighlightListener != null)
                daysToHighlightListener.notifyChange(getDistinctTimestampsToHighlight());
            if(list.size() == 0) {
                tasksToDisplayByHeader.remove(header);
                orderedHeaderList.remove(header);
            }
        }
    }

    public List<String> getOrderedHeaderList() {
        return orderedHeaderList;
    }

}
