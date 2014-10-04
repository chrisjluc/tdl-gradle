package com.ac.tdl.managers.helpers;

import com.ac.tdl.model.Task;

import java.util.Comparator;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public class TaskComparatorByDateReminder implements Comparator<Task> {

    @Override
    public int compare(Task a, Task b) {
        if (a.getDateReminder() < b.getDateReminder())
            return -1;
        else if (a.getDateReminder() > b.getDateReminder())
            return 1;
        return 0;
    }
}
