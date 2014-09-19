package com.ac.tdl.managers.helpers;

import java.util.List;

/**
 * Created by chrisjluc on 2014-09-18.
 */
public interface IFilter<T> {
    public List<T> filter(List<T> list);
}
