package com.ac.tdl;

import android.app.Activity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.model.Hashtag;
import com.ac.tdl.model.Task;
import com.ac.tdl.model.TaskBuilder;

public class EditActivity extends Activity implements View.OnClickListener{
    private Task task;
    private SQLiteDatabase db;
    private TextView tvTaskTitle;
    private TextView tvTaskDetail;
    private TextView tvHashtags;
    private CheckBox cbPriority;
    private TextView tvDate;
    private LinearLayout llReminder;
    private Button bCancel;
    private Button bSave;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        setupDatabase();
        Intent intent = getIntent();
        if(intent != null){
            int taskId = intent.getIntExtra("taskId",0);
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

        llReminder = (LinearLayout) findViewById(R.id.llReminder);
        llReminder.setOnClickListener(this);

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
        switch (v.getId()){
            case R.id.tvEditTaskTitle:
                break;
            case R.id.tvEditTaskDetail:
                break;
            case R.id.llReminder:
                break;
            case R.id.bEditCancel:
                finish();
                break;
            case R.id.bEditSave:
                task.saveModel();
                finish();
                break;
        }
    }
}
