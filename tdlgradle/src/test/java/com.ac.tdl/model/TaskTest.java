package com.ac.tdl.model;

import android.database.sqlite.SQLiteDatabase;

import com.ac.tdl.HomeActivity;
import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.model.Task;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

@RunWith(RobolectricTestRunner.class)
public class TaskTest {
    private SQLiteDatabase db;

    private static final String TASK_TITLE = "First task";
    private static final String DIFFERENT_TASK_TITLE = "some other awesome task";
    private static final String FIRST_HASHTAG = "1helloyolo";
    private static final String SECOND_HASHTAG = "okay";
    private static final String THIRD_HASHTAG = "okay123";
    private static final String TASK_TITLE_WITH_FIRST_HASHTAG = "#"+ FIRST_HASHTAG + " First task yes ";
    private static final String TASK_TITLE_WITH_TWO_HASHTAGS = "#"+ FIRST_HASHTAG +" First task  HELLO    #" + SECOND_HASHTAG;
    private static final String TASK_TITLE_WITH_THIRD_HASHTAGS = " First task  HELLO    #" + THIRD_HASHTAG;
    private static final String TASK_TITLE_WITH_NO_VALID_HASHTAG = "1#hashtag First #123123 task #1 HELLO##123";

    @Before
    public void setUp() throws Exception{
        HomeActivity activity = new HomeActivity();
        DbHelper mDbHelper = DbHelper.getInstance(activity.getApplicationContext());
        db = mDbHelper.getWritableDatabase();
    }

    @Test
    public void updateTaskTitle() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE).build();
        task.saveModel();
        assertEquals(TASK_TITLE, task.getTaskTitle());
        task.updateTaskTitle(DIFFERENT_TASK_TITLE);
        Task task1 = new TaskBuilder().withDb(db).withTaskId(1).build();
        task1.getModelFromDb();
        assertEquals(DIFFERENT_TASK_TITLE, task1.getTaskTitle());
    }

    @Test
    public void updateTaskTitleWithNewHashtag() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task.saveModel();
        assertEquals(1,task.getHashtagArray().length);
        task.updateTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS);
        Task task1 = new TaskBuilder().withDb(db).withTaskId(1).build();
        task1.getModelFromDb();
        assertEquals(2,task1.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task1.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,task1.getHashtagArray()[1].getLabel());
    }

    @Test
    public void updateTaskTitleDeletingOldHashtag() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task.saveModel();
        assertEquals(2,task.getHashtagArray().length);
        task.updateTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG);
        Task task1 = new TaskBuilder().withDb(db).withTaskId(1).build();
        task1.getModelFromDb();
        assertEquals(1,task1.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task1.getHashtagArray()[0].getLabel());
    }

    @Test
    public void updateTaskDetails() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE).build();
        task.saveModel();
        task.updateTaskDetails(DIFFERENT_TASK_TITLE);
        Task task1 = new TaskBuilder().withDb(db).withTaskId(1).build();
        task1.getModelFromDb();
        assertEquals(DIFFERENT_TASK_TITLE, task1.getTaskDetails());
        assertEquals(TASK_TITLE, task1.getTaskTitle());
    }

    @Test
    public void updateTaskArchived() throws Exception {
        Task task = new TaskBuilder().withDb(db).build();
        task.saveModel();
        assertEquals(false,task.isArchived());
        task.updateArchived(true);
        Task task1 = new TaskBuilder().withDb(db).withTaskId(1).build();
        task1.getModelFromDb();
        assertEquals(true,task1.isArchived());
    }

    @Test
    public void updateTaskAndHashtagArchived() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).withDb(db).build();
        task.saveModel();
        assertEquals(1,task.getHashtagArray().length);
        task.updateArchived(true);
        Task task1 = new TaskBuilder().withDb(db).withTaskId(1).build();
        task1.getModelFromDb();
        assertArrayEquals(null,task1.getHashtagArray());
    }

    @Test
    public void updateTaskIsCompleted() throws Exception {
        Task task = new TaskBuilder().withDb(db).build();
        task.saveModel();
        assertEquals(false, task.isComplete());
        task.updateIsComplete(true);
        Task task1 = new TaskBuilder().withDb(db).withTaskId(1).build();
        task1.getModelFromDb();
        assertEquals(true, task1.isComplete());
    }

    @Test
    public void addTwoTasksGettingItFromDb() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task.saveModel();
        task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task.saveModel();
        Task task1 = new TaskBuilder().withDb(db).withTaskId(1).build();
        task1.getModelFromDb();
        assertEquals(TASK_TITLE_WITH_FIRST_HASHTAG, task1.getTaskTitle());
        task1 = new TaskBuilder().withDb(db).withTaskId(2).build();
        task1.getModelFromDb();
        assertEquals(TASK_TITLE_WITH_TWO_HASHTAGS, task1.getTaskTitle());
    }

    @Test
      public void addTaskHasDateCreated() throws Exception {
        Task task = new TaskBuilder().withDb(db).build();
        task.saveModel();
        assertNotEquals(0, task.getDateCreated());
    }

    @Test
      public void addTaskWithEmptyHashtagArray() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE).build();
        task.saveModel();
        assertFalse(task.getTaskTitle().isEmpty());
        assertArrayEquals(null,task.getHashtagArray());
    }

    @Test
      public void addTaskWithInvalidHashtags() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_NO_VALID_HASHTAG).build();
        task.saveModel();
        assertArrayEquals(null, task.getHashtagArray());
    }

    @Test
      public void addTaskWithOneHashtag() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task.saveModel();
        assertEquals(1, task.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG, task.getHashtagArray()[0].getLabel());
    }

    @Test
      public void addTaskWithAnotherHashtag() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_THIRD_HASHTAGS).build();
        task.saveModel();
        assertEquals(1, task.getHashtagArray().length);
        assertEquals(THIRD_HASHTAG, task.getHashtagArray()[0].getLabel());
    }

    @Test
      public void addTaskWithTwoHashtags() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task.saveModel();
        assertEquals(2, task.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,task.getHashtagArray()[1].getLabel());
    }

    @Test
    public void addTaskHasDateCreatedByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withDb(db).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(1).withDb(db).build();
        taskFromDb.getModelFromDb();
        assertNotEquals(0, taskFromDb.getDateCreated());
    }

    @Test
    public void addTaskWithEmptyHashtagArrayByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(1).withDb(db).build();
        taskFromDb.getModelFromDb();
        assertFalse(taskFromDb.getTaskTitle().isEmpty());
        assertArrayEquals(null,taskFromDb.getHashtagArray());
    }

    @Test
    public void addTaskWithInvalidHashtagsByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_NO_VALID_HASHTAG).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(1).withDb(db).build();
        taskFromDb.getModelFromDb();
        assertArrayEquals(null, taskFromDb.getHashtagArray());
    }

    @Test
    public void addTaskWithOneHashtagByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(1).withDb(db).build();
        taskFromDb.getModelFromDb();
        assertEquals(1, taskFromDb.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG, taskFromDb.getHashtagArray()[0].getLabel());
    }

    @Test
    public void addTaskWithAnotherHashtagByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_THIRD_HASHTAGS).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(1).withDb(db).build();
        taskFromDb.getModelFromDb();
        assertEquals(1, taskFromDb.getHashtagArray().length);
        assertEquals(THIRD_HASHTAG, taskFromDb.getHashtagArray()[0].getLabel());
    }

    @Test
    public void addTaskWithTwoHashtagsByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withDb(db).withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(1).withDb(db).build();
        taskFromDb.getModelFromDb();
        assertEquals(2, taskFromDb.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,taskFromDb.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,taskFromDb.getHashtagArray()[1].getLabel());
    }
}
