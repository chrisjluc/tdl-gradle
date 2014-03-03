package com.ac.tdl;

import android.app.ActionBar;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ac.tdl.SQL.DbContract.HashtagTable;
import com.ac.tdl.SQL.DbContract.TaskTable;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.adapter.DateArrayAdapter;
import com.ac.tdl.adapter.TaskAdapter;
import com.ac.tdl.model.Task;
import com.ac.tdl.model.TaskBuilder;
import com.ac.tdl.model.TdlDate;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import antistatic.spinnerwheel.AbstractWheel;
import antistatic.spinnerwheel.OnWheelChangedListener;
import antistatic.spinnerwheel.OnWheelClickedListener;
import antistatic.spinnerwheel.adapters.ArrayWheelAdapter;

public class HomeActivity extends FragmentActivity implements OnClickListener, OnWheelClickedListener, OnWheelChangedListener {
    // TaskFragment taskfragment;
    private static final String[] MONTH_NAME = {"JANUARY", "FEBRUARY", "MARCH", "APRIL", "MAY", "JUNE", "JULY", "AUGUST", "SEPTEMBER", "OCTOBER", "NOVEMBER", "DECEMBER"};
    private static final int DAY_COUNT = 364;
    private static final int YEAR_COUNT = 4;
    private AbstractWheel dateWheel,monthWheel,yearWheel;
    EditText etTaskTitle;
    ImageButton bAdd;
    SQLiteDatabase db;
    List<Task> tasks;
    List<TdlDate> dates;
    List<String> years;
    ListView lvTasks;
    AlphaInAnimationAdapter alphaInAnimationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        // Remember that you should never show the action bar if the
        // status bar is hidden, so hide that too if necessary.
        ActionBar actionBar = getActionBar();
        actionBar.hide();

        // Initialize setup
        setDates();
        setUpActionBar();
        setUpViews();
        setUpSpinners();
        setupDatabase();
        loadTasks();

        // if (findViewById(R.id.taskfragment) != null) {
        // if (savedInstanceState != null) {
        // return;
        // }
        // }
        //
        // TaskFragment firstFragment = new TaskFragment();
        //
        // firstFragment.setArguments(getIntent().getExtras());
        //
        // getSupportFragmentManager().beginTransaction()
        // .add(R.id.taskfragment, firstFragment).commit();
        //
        //
        // FragmentManager fragmentManager = getSupportFragmentManager();
        // FragmentTransaction fragmentTransaction =
        // fragmentManager.beginTransaction();
        // TaskFragment taskFragment = new TaskFragment();
        // fragmentTransaction.add(R.id.taskfragment, taskFragment);

    }

   /*
    *   Load task list
    */

    public void loadTasks () {
        tasks = getTasksList();
        TaskAdapter taskAdapter = new TaskAdapter(getApplicationContext(), tasks);
        alphaInAnimationAdapter = new AlphaInAnimationAdapter(taskAdapter);
        alphaInAnimationAdapter.setAbsListView(lvTasks);
        alphaInAnimationAdapter.setInitialDelayMillis(1000);
        lvTasks.setAdapter(alphaInAnimationAdapter);
    }

    public List<Task> getTasksList() {
        Cursor cursor = getUnarchivedTaskIds();
        List<Task> taskList = new ArrayList<Task>();
        while (cursor.moveToNext()) {
            Task t = new TaskBuilder().withDb(db).build();
            t.setTaskId(cursor.getInt(cursor
                    .getColumnIndexOrThrow(TaskTable.COLUMN_NAME_ID)));
            t.getModelFromDb();
            taskList.add(t);
        }
        return taskList;
    }

    private Cursor getUnarchivedTaskIds() {
        String[] projection = {TaskTable.COLUMN_NAME_ID};
        String sortOrder = TaskTable.COLUMN_NAME_ID + " DESC";
        String selection = TaskTable.COLUMN_NAME_ARCHIVED + "=? ";
        String[] selectionArgs = {"0"};
        Cursor cursor = db.query(TaskTable.TABLE_NAME, projection, selection,
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
        tasks.add(0, task);
        alphaInAnimationAdapter.notifyDataSetChanged();
        etTaskTitle.setText("");
        etTaskTitle.setSelected(false);
        hideKeyboard(etTaskTitle);

        loadTasks();
    }

    public void archiveTask(int taskId) {
        Task task = new TaskBuilder()
                .withTaskId(taskId)
                .withDb(db)
                .build();
        task.updateArchived(true);
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
        String[] projection = {HashtagTable.COLUMN_NAME_HASHTAG_LABEL};
        Cursor cursor = db.query(true, HashtagTable.TABLE_NAME, projection,
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

    /**
     * Called first to set up the layout
     */
    private void setUpViews() {
        setContentView(R.layout.activity_home);
        etTaskTitle = (EditText) findViewById(R.id.etTaskTitle);
        etTaskTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    addTask();
                }
                return false;
            }
        });
        bAdd = (ImageButton) findViewById(R.id.bAdd);
        bAdd.setOnClickListener(this);
        lvTasks = (ListView) findViewById(R.id.lvTasks);

        TextView header = (TextView) findViewById(R.id.headerTitle);
        header.setText("DAILY PLANNER");
    }

    private void setUpActionBar(){
        ActionBar ab = getActionBar();
        ab.setHomeButtonEnabled(true);
        ab.setBackgroundDrawable(new ColorDrawable(Color.BLACK));
    }

    private void setUpSpinners() {
        dateWheel = (AbstractWheel) findViewById(R.id.whvCalendar);
        DateArrayAdapter dateAdapter = new DateArrayAdapter(getApplicationContext(), dates);
        dateWheel.setViewAdapter(dateAdapter);
        //Set to current date
        dateWheel.setCurrentItem(DAY_COUNT / 2);
        dateWheel.addClickingListener(this);
        dateWheel.addChangingListener(this);

        monthWheel = (AbstractWheel) findViewById(R.id.whvMonth);
        ArrayWheelAdapter<String> monthAdapter =
                new ArrayWheelAdapter<String>(this, MONTH_NAME);
        monthAdapter.setItemResource(R.layout.month_item);
        monthAdapter.setItemTextResource(R.id.tvSimpleItem);
        monthWheel.setViewAdapter(monthAdapter);
        monthWheel.setCurrentItem(dates.get(DAY_COUNT/2).getMonth());
        monthWheel.setCyclic(true);
        monthWheel.setEnabled(false);

        yearWheel = (AbstractWheel) findViewById(R.id.whvYear);
        int currentYear = Calendar.getInstance(Locale.CANADA).get(Calendar.YEAR);
        String[] years = new String[YEAR_COUNT];
        for(int i = 0; i < YEAR_COUNT; i++){
            years[i] = Integer.toString(currentYear - YEAR_COUNT/2 + i);
        }
        this.years = Arrays.asList(years);
        ArrayWheelAdapter<String> yearAdapter =
                new ArrayWheelAdapter<String>(this, years);
        yearAdapter.setItemResource(R.layout.year_item);
        yearAdapter.setItemTextResource(R.id.tvSimpleItem);
        yearWheel.setViewAdapter(yearAdapter);
        yearWheel.setCurrentItem(YEAR_COUNT/2);
        yearWheel.setEnabled(false);
    }


    private void setupDatabase() {
        DbHelper mDbHelper = DbHelper.getInstance(getBaseContext());
        db = mDbHelper.getWritableDatabase();
    }

    private void setDates() {
        List<TdlDate> dates = new ArrayList<TdlDate>();
        Calendar calendar = Calendar.getInstance(Locale.CANADA);
        calendar.add(Calendar.DATE, -DAY_COUNT / 2);
        for (int i = 0; i < DAY_COUNT; i++) {
            TdlDate date = new TdlDate();
            DateFormat format = new SimpleDateFormat("EEEE");
            date.setDayName(format.format(calendar.getTime()).toUpperCase());
            format = new SimpleDateFormat("dd");
            date.setDayNumber(format.format(calendar.getTime()));
            date.setMonth(calendar.get(Calendar.MONTH));
            date.setYear(calendar.get(Calendar.YEAR));
            dates.add(date);
            calendar.add(Calendar.DATE, 1);
        }
        this.dates = dates;
    }

    @SuppressWarnings("static-access")
    private void hideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(
                getApplicationContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    @Override
    public void onChanged(AbstractWheel wheel, int oldValue, int newValue) {
        switch (wheel.getId()) {
            case R.id.whvCalendar :
                TdlDate date = dates.get(newValue);
                monthWheel.setCurrentItem(date.getMonth(),true);
                int index = years.indexOf(Integer.toString(date.getYear()));
                yearWheel.setCurrentItem(index,true);
                break;
            case R.id.whvMonth :
                break;
            case R.id.whvYear :
                break;
        }
    }

    @Override
    public void onItemClicked(AbstractWheel wheel, int itemIndex) {
        wheel.setCurrentItem(itemIndex, true);
    }


}
