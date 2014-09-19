package com.ac.tdl.tests.models;

import android.test.AndroidTestCase;

import com.ac.tdl.SQL.DbHelper;
import com.ac.tdl.managers.TaskManager;
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
    private TaskManager taskManager;

    @Override
    protected void setUp() throws Exception {
        RenamingDelegatingContext context
                = new RenamingDelegatingContext(getContext(), "test_");
        DbHelper.getInstance(context).create();
        taskManager = TaskManager.getInstance();
    }

    public void testUpdateTaskTitle() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE).build();
        taskManager.save(task);

        assertEquals(TASK_TITLE, task.getTaskTitle());
        task.setTaskTitle(DIFFERENT_TASK_TITLE);
        taskManager.save(task);
        Task task1 = taskManager.getTaskById(task.getTaskId());
        assertEquals(DIFFERENT_TASK_TITLE, task1.getTaskTitle());
    }

    public void testUpdateTaskTitleWithNewHashtag() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        taskManager.save(task);

        assertEquals(1,task.getHashtagArray().length);
        task.setTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS);
        taskManager.save(task);
        Task task1 = taskManager.getTaskById(task.getTaskId());
        assertEquals(2,task1.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task1.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,task1.getHashtagArray()[1].getLabel());
    }

    public void testUpdateTaskTitleDeletingOldHashtag() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        task.saveModel();
        assertEquals(2,task.getHashtagArray().length);
        task.setTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG);
        taskManager.save(task);
        Task task1 = taskManager.getTaskById(task.getTaskId());
        assertEquals(1,task1.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task1.getHashtagArray()[0].getLabel());
    }

    public void testUpdateTaskDetails() throws Exception {
        Task task = new TaskBuilder().withTaskDetails(TASK_TITLE).build();
        taskManager.save(task);

        task.setTaskDetails(DIFFERENT_TASK_TITLE);
        taskManager.save(task);

        Task task1 = taskManager.getTaskById(task.getTaskId());
        assertEquals(DIFFERENT_TASK_TITLE, task.getTaskDetails());
        assertEquals(DIFFERENT_TASK_TITLE, task1.getTaskDetails());
    }

    public void testUpdateTaskArchived() throws Exception {
        Task task = new TaskBuilder().build();
        task.saveModel();
        assertEquals(false,task.isArchived());
        task.setArchived(true);
        taskManager.save(task);
        Task task1 = taskManager.getTaskById(task.getTaskId());
        assertEquals(true,task1.isArchived());
    }

    public void testUpdateTaskAndHashtagArchived() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        taskManager.save(task);

        assertEquals(1,task.getHashtagArray().length);
        task.setArchived(true);
        taskManager.save(task);
        Task task1 = taskManager.getTaskById(task.getTaskId());
        assertEquals(null,task1.getHashtagArray());
    }

    public void test2TasksSameHashtagWith1Archived() {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        taskManager.save(task);

        Task task1 = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        taskManager.save(task1);


        assertEquals(1,task.getHashtagArray().length);
        assertEquals(1,task1.getHashtagArray().length);

        task.setArchived(true);
        taskManager.save(task);

        assertEquals(1,task1.getHashtagArray().length);

        Task taskFromDb = taskManager.getTaskById(task1.getTaskId());
        assertEquals(1,taskFromDb.getHashtagArray().length);
    }

    public void testAddTwoTasksGettingItFromDb() throws Exception {
        Task task1 = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        taskManager.save(task1);
        Task task2 = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        taskManager.save(task2);

        Task testTask = taskManager.getTaskById(task1.getTaskId());

        assertEquals(TASK_TITLE_WITH_FIRST_HASHTAG, testTask.getTaskTitle());
        testTask = taskManager.getTaskById(task2.getTaskId());

        assertEquals(TASK_TITLE_WITH_TWO_HASHTAGS, testTask.getTaskTitle());
    }

    public void testAddTaskWithEmptyHashtagArray() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE).build();
        taskManager.save(task);

        assertFalse(task.getTaskTitle().isEmpty());
        assertEquals(null, task.getHashtagArray());
    }

    public void testAddTaskWithInvalidHashtags() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_NO_VALID_HASHTAG).build();
        taskManager.save(task);

        assertEquals(null, task.getHashtagArray());
    }

    public void testAddTaskWithOneHashtag() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        taskManager.save(task);

        assertEquals(1, task.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG, task.getHashtagArray()[0].getLabel());
    }

    public void testAddTaskWithAnotherHashtag() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_THIRD_HASHTAGS).build();
        taskManager.save(task);

        assertEquals(1, task.getHashtagArray().length);
        assertEquals(THIRD_HASHTAG, task.getHashtagArray()[0].getLabel());
    }

    public void testAddTaskWithTwoHashtags() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        taskManager.save(task);

        assertEquals(2, task.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,task.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,task.getHashtagArray()[1].getLabel());
    }

    public void testAddTaskWithEmptyHashtagArrayByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE).build();
        taskManager.save(task);

        Task taskFromDb = taskManager.getTaskById(task.getTaskId());
        assertFalse(taskFromDb.getTaskTitle().isEmpty());
        assertEquals(null, taskFromDb.getHashtagArray());
    }

    public void testAddTaskWithInvalidHashtagsByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_NO_VALID_HASHTAG).build();
        taskManager.save(task);

        Task taskFromDb = taskManager.getTaskById(task.getTaskId());
        assertEquals(null, taskFromDb.getHashtagArray());
    }

    public void testAddTaskWithOneHashtagByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_FIRST_HASHTAG).build();
        taskManager.save(task);

        Task taskFromDb = taskManager.getTaskById(task.getTaskId());
        assertEquals(1, taskFromDb.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG, taskFromDb.getHashtagArray()[0].getLabel());
    }

    public void testAddTaskWithAnotherHashtagByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_THIRD_HASHTAGS).build();
        taskManager.save(task);

        Task taskFromDb = taskManager.getTaskById(task.getTaskId());
        assertEquals(1, taskFromDb.getHashtagArray().length);
        assertEquals(THIRD_HASHTAG, taskFromDb.getHashtagArray()[0].getLabel());
    }

    public void testAddTaskWithTwoHashtagsByGettingModelInDb() throws Exception {
        Task task = new TaskBuilder().withTaskTitle(TASK_TITLE_WITH_TWO_HASHTAGS).build();
        taskManager.save(task);

        Task taskFromDb = taskManager.getTaskById(task.getTaskId());
        assertEquals(2, taskFromDb.getHashtagArray().length);
        assertEquals(FIRST_HASHTAG,taskFromDb.getHashtagArray()[0].getLabel());
        assertEquals(SECOND_HASHTAG,taskFromDb.getHashtagArray()[1].getLabel());
    }

    @Override
    protected void tearDown() throws Exception{
        DbHelper.getInstance().clear();
        TaskManager.nullifyInstance();
        super.tearDown();
    }
}
