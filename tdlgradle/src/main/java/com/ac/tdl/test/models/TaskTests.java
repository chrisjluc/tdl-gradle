package com.ac.tdl.test.models;

import android.test.AndroidTestCase;

import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.model.Task;
import com.ac.tdl.model.TaskBuilder;

import android.test.RenamingDelegatingContext;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public class TaskTests extends AndroidTestCase {

    private static final String TASK_TITLE = "First task";
    private static final String DIFFERENT_TASK_TITLE = "some other awesome task";
    private static final String FIRST_HASHTAG = "1helloyolo";
    private static final String SECOND_HASHTAG = "okay";
    private static final String THIRD_HASHTAG = "okay123";
    private static final String TASK_TITLE_WITH_FIRST_HASHTAG = "#"+ FIRST_HASHTAG + " First task yes ";
    private static final String TASK_TITLE_WITH_TWO_HASHTAGS = "#"+ FIRST_HASHTAG +" First task  HELLO    #" + SECOND_HASHTAG;
    private static final String TASK_TITLE_WITH_THIRD_HASHTAGS = " First task  HELLO    #" + THIRD_HASHTAG;
    private static final String TASK_TITLE_WITH_NO_VALID_HASHTAG = "1#hashtag First #123123 task #1 HELLO##123";

    @Override
    protected void setUp() throws Exception {
        RenamingDelegatingContext context
                = new RenamingDelegatingContext(getContext(), "test_");
        DbHelper.getInstance(context).create();
    }

    public void testUpdateTaskTitle() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE).build();
        task.saveModel();
        assertEquals(TASK_TITLE, task.getTaskTitle());
        task.setTaskTitle(DIFFERENT_TASK_TITLE);
        task.updateModel();
        Task task1 = new TaskBuilder().withTaskId(task.getTaskId()).build();
        task1.getModelFromDb();
        assertEquals(DIFFERENT_TASK_TITLE, task1.getTaskTitle());
    }

    public void testUpdateTaskTitleWithNewHashtag() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task.saveModel();
        assertEquals(1,task.getHashtagArray().length);
        task.setTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS);
        task.updateModel();
        Task task1 = new TaskBuilder().withTaskId(task.getTaskId()).build();
        task1.getModelFromDb();
        assertEquals(2,task1.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task1.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,task1.getHashtagArray()[1].getLabel());
    }

    public void testUpdateTaskTitleDeletingOldHashtag() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task.saveModel();
        assertEquals(2,task.getHashtagArray().length);
        task.setTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG);
        task.updateModel();
        Task task1 = new TaskBuilder().withTaskId(task.getTaskId()).build();
        task1.getModelFromDb();
        assertEquals(1,task1.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task1.getHashtagArray()[0].getLabel());
    }

    public void testUpdateTaskDetails() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE).build();
        task.saveModel();
        task.setTaskDetails(DIFFERENT_TASK_TITLE);
        task.updateModel();

        Task task1 = new TaskBuilder().withTaskId(task.getTaskId()).build();
        task1.getModelFromDb();
        assertEquals(DIFFERENT_TASK_TITLE, task.getTaskDetails());
        assertEquals(TASK_TITLE, task1.getTaskTitle());
    }

    public void testUpdateTaskArchived() throws Exception {
        Task task = new TaskBuilder().build();
        task.saveModel();
        assertEquals(false,task.isArchived());
        task.updateArchived(true);
        Task task1 = new TaskBuilder().withTaskId(task.getTaskId()).build();
        task1.getModelFromDb();
        assertEquals(true,task1.isArchived());
    }

    public void testUpdateTaskAndHashtagArchived() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task.saveModel();
        assertEquals(1,task.getHashtagArray().length);
        task.updateArchived(true);
        Task task1 = new TaskBuilder().withTaskId(task.getTaskId()).build();
        task1.getModelFromDb();
        assertEquals(null,task1.getHashtagArray());
    }

    public void testUpdateTaskIsCompleted() throws Exception {
        Task task = new TaskBuilder().build();
        task.saveModel();
        assertEquals(false, task.isComplete());
        task.updateIsComplete(true);
        Task task1 = new TaskBuilder().withTaskId(task.getTaskId()).build();
        task1.getModelFromDb();
        assertEquals(true, task1.isComplete());
    }

    public void testAddTwoTasksGettingItFromDb() throws Exception {
        Task task1 = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task1.saveModel();
        Task task2 = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task2.saveModel();

        Task testTask = new TaskBuilder().withTaskId(task1.getTaskId()).build();
        testTask.getModelFromDb();
        assertEquals(TASK_TITLE_WITH_FIRST_HASHTAG, testTask.getTaskTitle());
        testTask = new TaskBuilder().withTaskId(task2.getTaskId()).build();
        testTask.getModelFromDb();
        assertEquals(TASK_TITLE_WITH_TWO_HASHTAGS, testTask.getTaskTitle());
    }

    public void testAddTaskWithEmptyHashtagArray() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE).build();
        task.saveModel();
        assertFalse(task.getTaskTitle().isEmpty());
        assertEquals(null, task.getHashtagArray());
    }

    public void testAddTaskWithInvalidHashtags() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_NO_VALID_HASHTAG).build();
        task.saveModel();
        assertEquals(null, task.getHashtagArray());
    }

    public void testAddTaskWithOneHashtag() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task.saveModel();
        assertEquals(1, task.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG, task.getHashtagArray()[0].getLabel());
    }

    public void testAddTaskWithAnotherHashtag() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_THIRD_HASHTAGS).build();
        task.saveModel();
        assertEquals(1, task.getHashtagArray().length);
        assertEquals(THIRD_HASHTAG, task.getHashtagArray()[0].getLabel());
    }

    public void testAddTaskWithTwoHashtags() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task.saveModel();
        assertEquals(2, task.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,task.getHashtagArray()[1].getLabel());
    }

    public void testAddTaskWithEmptyHashtagArrayByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(task.getTaskId()).build();
        taskFromDb.getModelFromDb();
        assertFalse(taskFromDb.getTaskTitle().isEmpty());
        assertEquals(null, taskFromDb.getHashtagArray());
    }

    public void testAddTaskWithInvalidHashtagsByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_NO_VALID_HASHTAG).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(task.getTaskId()).build();
        taskFromDb.getModelFromDb();
        assertEquals(null, taskFromDb.getHashtagArray());
    }

    public void testAddTaskWithOneHashtagByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(task.getTaskId()).build();
        taskFromDb.getModelFromDb();
        assertEquals(1, taskFromDb.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG, taskFromDb.getHashtagArray()[0].getLabel());
    }

    public void testAddTaskWithAnotherHashtagByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_THIRD_HASHTAGS).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(task.getTaskId()).build();
        taskFromDb.getModelFromDb();
        assertEquals(1, taskFromDb.getHashtagArray().length);
        assertEquals(THIRD_HASHTAG, taskFromDb.getHashtagArray()[0].getLabel());
    }

    public void testAddTaskWithTwoHashtagsByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task.saveModel();
        Task taskFromDb = new TaskBuilder().withTaskId(task.getTaskId()).build();
        taskFromDb.getModelFromDb();
        assertEquals(2, taskFromDb.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,taskFromDb.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,taskFromDb.getHashtagArray()[1].getLabel());
    }

    @Override
    protected void tearDown() throws Exception{
        DbHelper.getInstance().clear();
        super.tearDown();
    }
}
