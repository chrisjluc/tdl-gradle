package com.ac.tdl.managers.helpers;

import com.ac.tdl.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public class TaskFilterByUnarchived implements IFilter<Task> {

    @Override
    public List<Task> filter(List<Task> tasks) {
        List<Task> filteredTasks = new ArrayList<Task>();
        for (Task task : tasks)
            if (!task.isArchived())
                filteredTasks.add(task);
        return filteredTasks;
    }

}
