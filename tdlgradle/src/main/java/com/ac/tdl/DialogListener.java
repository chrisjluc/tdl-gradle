package com.ac.tdl;

/**
 * Created by chrisjluc on 2014-03-29.
 */
public interface DialogListener {
    public void onDateChosen(int year,int month,int day);
    public void onTimeChosen(int hour, int minute);
}
