package com.ac.tdl.managers;

import com.ac.tdl.model.Task;

import java.util.HashMap;
import java.util.List;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public interface ITaskManager {

    public Task getTaskById(long id);

    public HashMap<String, List<Task>> getUnArchivedTasksByHeaderAndHashtagOrdered(List<String> orderedHeaderList, String hashtagLabel);

    public HashMap<String, List<Task>> getUnarchivedTasksByHeaderOrdered(List<String> orderedHeaderList);
}
