package com.ac.tdl.managers;

import com.ac.tdl.model.Hashtag;

import java.util.Collection;
import java.util.List;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public interface IHashtagManager {

    public List<Hashtag> getHashtagsByTaskId(long taskId);

    public Collection<String> getDistinctHashtags();

}
