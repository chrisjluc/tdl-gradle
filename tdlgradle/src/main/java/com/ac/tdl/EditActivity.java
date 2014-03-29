package com.ac.tdl;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.model.Hashtag;
import com.ac.tdl.model.Task;
import com.ac.tdl.model.TaskBuilder;

import java.util.Calendar;

public class EditActivity extends FragmentActivity implements View.OnClickListener {
    public final static int TASK_TITLE_DIALOG = 0;
    public final static int TASK_DETAIL_DIALOG = 1;
    private Task task;
    private SQLiteDatabase db;
    private TextView tvTaskTitle;
    private TextView tvTaskDetail;
    private TextView tvHashtags;
    private CheckBox cbPriority;
    private TextView tvDate;
    private TextView tvTime;
    private Button bCancel;
    private Button bSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setupDatabase();
        Intent intent = getIntent();
        if (intent != null) {
            int taskId = intent.getIntExtra("taskId", 0);
            task = new TaskBuilder().withDb(db).withTaskId(taskId).build();
            task.getModelFromDb();
        }
        setupViews();
    }

    private void setupViews() {
        tvTaskTitle = (TextView) findViewById(R.id.tvEditTaskTitle);
        tvTaskTitle.setOnClickListener(this);
        tvTaskTitle.setText(task.getTaskTitle());

        tvTaskDetail = (TextView) findViewById(R.id.tvEditTaskDetail);
        tvTaskDetail.setOnClickListener(this);
        tvTaskDetail.setText(task.getTaskDetails());

        tvHashtags = (TextView) findViewById(R.id.tvEditHashtags);
        String hashtagString = Hashtag.createHashtagString(task.getHashtagArray());
        tvHashtags.setText(hashtagString);

        cbPriority = (CheckBox) findViewById(R.id.cbEditPriority);
        cbPriority.setChecked(task.isPriority());

        tvDate = (TextView) findViewById(R.id.tvEditReminderDate);
        tvDate.setOnClickListener(this);

        tvTime = (TextView) findViewById(R.id.tvEditReminderTime);
        tvTime.setOnClickListener(this);

        bCancel = (Button) findViewById(R.id.bEditCancel);
        bCancel.setOnClickListener(this);

        bSave = (Button) findViewById(R.id.bEditSave);
        bSave.setOnClickListener(this);

    }

    private void setupDatabase() {
        DbHelper mDbHelper = DbHelper.getInstance(getBaseContext());
        db = mDbHelper.getWritableDatabase();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvEditTaskTitle:
                showEditTextDialog(TASK_TITLE_DIALOG);
                break;
            case R.id.tvEditTaskDetail:
                showEditTextDialog(TASK_DETAIL_DIALOG);
                break;
            case R.id.tvEditReminderDate:
                showDatePickerDialog();
                break;
            case R.id.tvEditReminderTime:
                showTimePickerDialog();
                break;
            case R.id.bEditCancel:
                finish();
                break;
            case R.id.bEditSave:
                task.setPriority(cbPriority.isChecked());
                task.updateModelInDb();
                finish();
                break;
        }
    }

    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
        }
    }


    private void showEditTextDialog(int flag){
        final EditText etInput = new EditText(this);
        if(flag == TASK_TITLE_DIALOG) {
            etInput.setText(task.getTaskTitle());
            new AlertDialog.Builder(this)
                    .setTitle("Update Task")
                    .setMessage("")
                    .setView(etInput)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String input = etInput.getText().toString();
                            task.setTaskTitle(input);
                            tvTaskTitle.setText(input);
                            String hashtags = Hashtag.createHashtagString(task.getHashtagsLabelsFromUpdatedFields());
                            tvHashtags.setText(hashtags);
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
        }else{
            etInput.setText(task.getTaskDetails());
            new AlertDialog.Builder(this)
                    .setTitle("Update details")
                    .setMessage("")
                    .setView(etInput)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String input = etInput.getText().toString();
                            task.setTaskDetails(input);
                            tvTaskDetail.setText(input);
                            tvHashtags.setText(Hashtag.createHashtagString(task.getHashtagsLabelsFromUpdatedFields()));
                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Do nothing.
                }
            }).show();
        }
    }
}
