package com.ac.tdl;

/**
 * Created by aaronte on 2014-05-25.
 */
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import com.ac.tdl.SQL.DbContract;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.adapter.ExpandableTaskAdapter;
import com.ac.tdl.adapter.TaskAdapter;
import com.ac.tdl.model.Task;
import com.ac.tdl.model.TaskBuilder;
import com.ac.tdl.model.TdlDate;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener, ContextualUndoAdapter.DeleteItemCallback {

    private static final int EDIT_ACTIVITY = 1;
    private static final String INCOMPLETE = "incomplete";
    private static final String TODAY = "Today";

    private EditText etTaskTitle;
    private ImageButton bAdd;
    private SQLiteDatabase db;
    private HashMap<Integer, List<Task>> tasksByParentId;
    private List<TdlDate> dates;
    private List<String> years;
    private ListView lvTasks;
    private EditActivityListener editActivityListener = new EditActivityListener() {

        @Override
        public void startActivityFromHomeActivity(int i) {
            Intent intent = new Intent(getActivity().getApplicationContext(), EditActivity.class);
            intent.putExtra("taskId", i);
            startActivityForResult(intent, EDIT_ACTIVITY);
        }
    };

    public HomeFragment(){}

    View superView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        superView = rootView;
        setUpViews();
        setupDatabase();
        loadTasks();


        return rootView;
    }

    /**
     * Called first to set up the layout
     */
    private void setUpViews() {

        etTaskTitle = (EditText) superView.findViewById(R.id.etTaskTitle);
        etTaskTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addTask();
                }
                return false;
            }
        });
        bAdd = (ImageButton) superView.findViewById(R.id.bAdd);
        bAdd.setOnClickListener(this);
        lvTasks = (ListView) superView.findViewById(R.id.lvTasks);

    }

    /*
     *   Load task list
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EDIT_ACTIVITY)
            if (resultCode == getActivity().RESULT_OK) {
                boolean result = data.getBooleanExtra("isSaved", false);
                if (result)
                    loadTasks();
            }
    }

    public void loadTasks() {
        List<Task> tasks = getTasksList();
        List<String> headerList = new ArrayList<String>();
        HashMap<String, List<Task>> tasksByHeader = getTasksByHeader(tasks, headerList);
        HashMap<String, ContextualUndoAdapter> adapterMap = new HashMap<String, ContextualUndoAdapter>();
        tasksByParentId = new HashMap<Integer, List<Task>>();

        for (int i = 0; i < headerList.size(); i++) {
            String header = headerList.get(i);
            List<Task> taskList = tasksByHeader.get(header);
            TaskAdapter taskAdapter = new TaskAdapter(getActivity().getApplicationContext(), taskList);
            ContextualUndoAdapter adapter = new ContextualUndoAdapter(taskAdapter,
                    R.layout.undo_row, R.id.undo_row_undobutton, 2000, this);
            adapterMap.put(header, adapter);
            tasksByParentId.put(i, taskList);
        }

        ExpandableTaskAdapter expandableAdapter = new ExpandableTaskAdapter(getActivity().getApplicationContext(),
                headerList, tasksByHeader, adapterMap, editActivityListener);
        expandableAdapter.setAbsListView(lvTasks);
        lvTasks.setAdapter(expandableAdapter);
    }

    private HashMap<String, List<Task>> getTasksByHeader(List<Task> tasks, List<String> headers) {
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

    @Override
    public void deleteItem(final int position, View view) {
        View parent = (View) view.getParent();
        int parentId = parent.getId();
        List<Task> associatedTasks = tasksByParentId.get(parentId);
        Task task = associatedTasks.get(position);
        task.updateArchived(true);
        loadTasks();
    }

    /**
     * From Earliest task to latest task
     * tasks with no reminder date will always be the first under 'today' section
     *
     * @return tasksList
     */
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
        if(orderedTaskList.size() == 0)
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
        if(emptyReminderTaskList != null)
            mergedTaskList.addAll(emptyReminderTaskList);
        mergedTaskList.addAll(orderedTaskList.subList(index, orderedTaskList.size()));
        return mergedTaskList;
    }

    private long getCurrentDate() {
        Calendar c = new GregorianCalendar();
        return floorDateByDay(c);
    }

    private long floorDateByDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime().getTime();
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bAdd:
                addTask();
                break;
        }
    }

    private void addTask() {
        String taskTitle = etTaskTitle.getText().toString();
        if (taskTitle.isEmpty()) return;
        Task task = new TaskBuilder()
                .withTaskTitle(taskTitle)
                .withDb(db)
                .build();
        task.saveModel();
        etTaskTitle.setText("");
        etTaskTitle.setSelected(false);
        hideKeyboard(etTaskTitle);

        loadTasks();
    }

    /**
     * Don't need to check if fields were changed, we handle it here
     *
     * @param taskId
     * @param taskTitle
     * @param taskDetails
     */
    public void updateTask(int taskId, String taskTitle, String taskDetails) {
        Task task = new TaskBuilder()
                .withTaskId(taskId)
                .withDb(db)
                .build();
        task.getModelFromDb();
        if (!task.getTaskTitle().equals(taskTitle)) {
            task.updateTaskTitle(taskTitle);
        }
        if (!task.getTaskDetails().equals(taskDetails)) {
            task.updateTaskDetails(taskDetails);
        }
    }

    private String[] getHashtagLabelsInDb() {
        String[] projection = {DbContract.HashtagTable.COLUMN_NAME_HASHTAG_LABEL};
        Cursor cursor = db.query(true, DbContract.HashtagTable.TABLE_NAME, projection,
                null, null, null, null, null, null);
        ArrayList<String> hashtagArray = new ArrayList<String>();
        while (cursor.moveToNext()) {
            hashtagArray.add(cursor.getString(0));
        }
        return hashtagArray.toArray(new String[hashtagArray.size()]);
    }

    private long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }




    private void setupDatabase() {
        DbHelper mDbHelper = DbHelper.getInstance(getActivity().getBaseContext());
        db = mDbHelper.getWritableDatabase();
    }


    @SuppressWarnings("static-access")
    private void hideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                getActivity().getApplicationContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
}