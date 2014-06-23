package com.ac.tdl;

/**
 * Created by aaronte on 2014-05-25.
 */
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;


import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.adapter.TaskAdapter;
import com.ac.tdl.model.Task;
import com.ac.tdl.model.TaskBuilder;
import com.ac.tdl.model.TdlDate;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final int EDIT_ACTIVITY = 1;
    private EditText etTaskTitle;
    private ImageButton bAdd;
    private SQLiteDatabase db;
    private List<Task> tasks;
    private List<TdlDate> dates;
    private List<String> years;
    private ListView lvTasks;
    private EditActivityListener editActivityListener = new EditActivityListener() {

        @Override
        public void startActivityFromHomeActivity(int i) {
            Intent intent = new Intent(getActivity().getApplicationContext(), EditActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("taskId", i);
            startActivityForResult(intent, EDIT_ACTIVITY);
        }
    };

    public HomeFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        bAdd = (ImageButton) rootView.findViewById(R.id.bAdd);
        bAdd.setOnClickListener(this);
        lvTasks = (ListView) rootView.findViewById(R.id.lvTasks);


        setupDatabase();

        return rootView;
    }

    private void addTask() {
        String taskTitle = etTaskTitle.getText().toString();
        if (taskTitle.isEmpty()) return;
        Task task = new TaskBuilder()
                .withTaskTitle(taskTitle)
                .withDb(db)
                .build();
        task.saveModel();
        tasks.add(0, task);
        etTaskTitle.setText("");
        etTaskTitle.setSelected(false);

    }

    private long getCurrentDate() {
        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND,0);
        return c.getTime().getTime();
    }

    public List<Task> getTasksList() {
        Cursor cursor = getUnarchivedTaskIds();
        List<Task> orderedTaskList = new ArrayList<Task>();
        List<Task> emptyReminderTaskList = new ArrayList<Task>();
        while (cursor.moveToNext()) {
            Task t = new TaskBuilder().withDb(db).build();
            t.setTaskId(cursor.getInt(cursor
                    .getColumnIndexOrThrow(DbContract.TaskTable.COLUMN_NAME_ID)));
            t.getModelFromDb();

            if (t.getDateReminder() == 0)
                emptyReminderTaskList.add(t);
            else
                orderedTaskList.add(t);
        }
        if (emptyReminderTaskList.size() > 0) {
            return mergeListsOnReminderDate(orderedTaskList, emptyReminderTaskList);
        }
        return orderedTaskList;
    }

    private List<Task> mergeListsOnReminderDate(List<Task> orderedTaskList, List<Task> emptyReminderTaskList) {
        List<Task> mergedTaskList = new ArrayList<Task>();
        int index = 0;

        //12:00 am of currentday
        long currentDayTimestamp = getCurrentDate();

        for(Task orderedTask : orderedTaskList){

            //Ordered tasks at 12:00 am on current date will appear after empty reminders
            if(orderedTask.getDateReminder() >= currentDayTimestamp) {
                mergedTaskList.addAll(emptyReminderTaskList);
                break;
            }
            mergedTaskList.add(orderedTask);
            index++;
        }
        mergedTaskList.addAll(orderedTaskList.subList(index,orderedTaskList.size()));
        return mergedTaskList;
    }

    private Cursor getUnarchivedTaskIds() {
        String[] projection = {DbContract.TaskTable.COLUMN_NAME_ID};
        String sortOrder = DbContract.TaskTable.COLUMN_NAME_DATE_REMINDER + " ASC";
        String selection = DbContract.TaskTable.COLUMN_NAME_ARCHIVED + "=? ";
        String[] selectionArgs = {"0"};
        Cursor cursor = db.query(DbContract.TaskTable.TABLE_NAME, projection, selection,
                selectionArgs, null, null, sortOrder);
        return cursor;
    }

    private void setupDatabase() {
        DbHelper mDbHelper = DbHelper.getInstance(getActivity().getBaseContext());
        db = mDbHelper.getWritableDatabase();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bAdd:
                addTask();
                break;
        }
    }
}