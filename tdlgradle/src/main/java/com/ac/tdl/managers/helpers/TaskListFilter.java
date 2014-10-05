package com.ac.tdl.managers.helpers;

/**
 * Created by chrisjluc on 2014-10-05.
 */
public class TaskListFilter {
    private String hashtagFilter;

    public TaskListFilter(String hashtagFilter) {
        this.hashtagFilter = hashtagFilter;
    }
    public boolean anyFiltersApplied(){
        if (hashtagFilter != null)
            return true;
        return false;
    }

    public String getHashtagFilter() {
        return hashtagFilter;
    }

    public void setHashtagFilter(String hashtagFilter) {
        this.hashtagFilter = hashtagFilter;
    }
}
