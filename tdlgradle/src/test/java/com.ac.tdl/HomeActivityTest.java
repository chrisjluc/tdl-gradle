package com.ac.tdl;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.ac.tdl.model.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class HomeActivityTest {

    private HomeActivity activity;
    private ImageButton bAdd;
    private EditText etTaskTitle;
    private ListView lvTasks;

    private static final String FIRST_TASK = "First task";
    private static final String FIRST_TASK_WITH_1_HASHTAG = "#hashtag First task yes ";


    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(HomeActivity.class).create().visible().get();
        assertTrue(activity != null);
        etTaskTitle = (EditText) activity.findViewById(R.id.etTaskTitle);
        bAdd = (ImageButton) activity.findViewById(R.id.bAdd);
        lvTasks = (ListView) activity.findViewById(R.id.lvTasks);
        assertTrue(etTaskTitle != null);
        assertTrue(bAdd != null);
        assertTrue(lvTasks != null);

    }

    @Test
    public void addingOneTask() throws Exception {
        addTask(FIRST_TASK, 1, 0);
    }

    @Test
    public void addingOneTaskWithHashtag() throws Exception {
        addTask(FIRST_TASK_WITH_1_HASHTAG, 1, 0);
    }

    @Test
    public void addingTwoTasks() throws Exception {
        addTask(FIRST_TASK, 1, 0);
        addTask(FIRST_TASK_WITH_1_HASHTAG, 2, 0);
    }

    @Test
    public void addingFiftyTasks() throws Exception {
        for (int i = 1; i <= 50; i++) {
            addTask(FIRST_TASK_WITH_1_HASHTAG + i, i, 0);
        }
        Task task = (Task) lvTasks.getItemAtPosition(49);
        assertEquals(FIRST_TASK_WITH_1_HASHTAG + 1,task.getTaskTitle());
    }

    @Test
    public void addingTwoHundredTasks() throws Exception {
        for (int i = 1; i <= 200; i++) {
            addTask(FIRST_TASK_WITH_1_HASHTAG + i, i, 0);
        }
        Task task = (Task) lvTasks.getItemAtPosition(199);
        assertEquals(FIRST_TASK_WITH_1_HASHTAG + 1,task.getTaskTitle());
    }

    private void addTask(String taskTitle, int expectedCount, int expectedIndex) {
        Robolectric.clickOn(etTaskTitle);
        etTaskTitle.setText(taskTitle);
        Robolectric.clickOn(bAdd);
        Task task = (Task) lvTasks.getItemAtPosition(expectedIndex);
        assertEquals(expectedCount, lvTasks.getAdapter().getCount());
        assertEquals(taskTitle, task.getTaskTitle());
    }
}
