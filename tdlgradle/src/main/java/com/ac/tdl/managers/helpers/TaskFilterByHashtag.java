package com.ac.tdl.managers.helpers;

import com.ac.tdl.model.Hashtag;
import com.ac.tdl.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public class TaskFilterByHashtag implements IFilter<Task>{

    private String hashtag;

    public TaskFilterByHashtag(String hashtag){
        this.hashtag = hashtag;
    }
    @Override
    public List<Task> filter(List<Task> list) {
        List<Task> filtered = new ArrayList<Task>();
        for (Task task : list)
            if (task.doesValueExistInHashtagList(hashtag))
                filtered.add(task);
        return filtered;
    }
}
