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

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class EditActivity extends FragmentActivity implements View.OnClickListener {
    public final static int TASK_TITLE_DIALOG = 0;
    public final static int TASK_DETAIL_DIALOG = 1;
    private Task task;
    private SQLiteDatabase db = DbHelper.getInstance().getWritableDatabase();
    private TextView tvTaskTitle;
    private TextView tvTaskDetail;
    private TextView tvHashtags;
    private CheckBox cbPriority;
    private TextView tvDate;
    private TextView tvTime;
    private Button bCancel;
    private Button bSave;
    private Calendar calendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private DialogListener dl = new DialogListener() {

        @Override
        public void onDateChosen(int nYear, int nMonth, int nDay) {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            calendar.add(Calendar.YEAR, nYear - year);
            calendar.add(Calendar.MONTH, nMonth - month);
            calendar.add(Calendar.DAY_OF_YEAR, nDay - day);
            tvDate.setText(dateFormat.format(calendar.getTime()));
        }

        @Override
        public void onTimeChosen(int nHour, int nMinute) {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            calendar.add(Calendar.HOUR_OF_DAY, nHour - hour);
            calendar.add(Calendar.MINUTE, nMinute - minute);
            tvTime.setText(timeFormat.format(calendar.getTime()));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        if (intent != null) {
            int taskId = intent.getIntExtra("taskId", 0);
            task = new TaskBuilder().withTaskId(taskId).build();
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

        //if no date reminder
        if (task.getDateReminder() == 0) {
            tvDate.setText("No Completion date set");
        } else {
            calendar = Calendar.getInstance();
            calendar.setTimeInMillis(task.getDateReminder());
            tvDate.setText(dateFormat.format(calendar.getTime()));
            tvTime.setText(timeFormat.format(calendar.getTime()));
        }

        bCancel = (Button) findViewById(R.id.bEditCancel);
        bCancel.setOnClickListener(this);

        bSave = (Button) findViewById(R.id.bEditSave);
        bSave.setOnClickListener(this);

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
                Intent cancelIntent = new Intent();
                setResult(RESULT_CANCELED, cancelIntent);
                finish();
                break;
            case R.id.bEditSave:
                task.setPriority(cbPriority.isChecked());
                if (calendar != null)
                    task.setDateReminder(calendar.getTimeInMillis());
                task.updateModelInDb();
                Intent saveIntent = new Intent();
                saveIntent.putExtra("isSaved", true);
                saveIntent.putExtra("taskId", task.getTaskId());
                setResult(RESULT_OK, saveIntent);
                finish();
                break;
        }
    }

    public void showDatePickerDialog() {
        DialogFragment newFragment = new DatePickerFragment(calendar, dl);
        newFragment.show(getFragmentManager(), "datePicker");
    }

    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment(calendar, dl);
        newFragment.show(getFragmentManager(), "timePicker");
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {
        private Calendar calendar;
        private DialogListener dl;

        DatePickerFragment(Calendar calendar, DialogListener dl) {
            this.calendar = calendar;
            this.dl = dl;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = 0;
            int month = 0;
            int day = 0;

            if (calendar == null) {
                Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                day = c.get(Calendar.DAY_OF_MONTH);
            } else {
                year = calendar.get(Calendar.YEAR);
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
            }

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int nYear, int nMonth, int nDay) {
            dl.onDateChosen(nYear, nMonth, nDay);
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private Calendar calendar;
        private DialogListener dl;

        TimePickerFragment(Calendar calendar, DialogListener dl) {
            this.calendar = calendar;
            this.dl = dl;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = 0;
            int minute = 0;

            if (calendar == null) {
                Calendar c = Calendar.getInstance();
                hour = c.get(Calendar.HOUR_OF_DAY);
                minute = c.get(Calendar.MINUTE);
            } else {
                hour = calendar.get(Calendar.HOUR_OF_DAY);
                minute = calendar.get(Calendar.MINUTE);
            }

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            dl.onTimeChosen(hourOfDay, minute);
        }
    }


    private void showEditTextDialog(int flag) {
        final EditText etInput = new EditText(this);
        if (flag == TASK_TITLE_DIALOG) {
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
                }
            }).show();
        } else {
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
                }
            }).show();
        }
    }
}
