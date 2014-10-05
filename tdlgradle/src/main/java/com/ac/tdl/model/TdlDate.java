package com.ac.tdl.model;

/**
 * Created by chrisjluc on 1/1/2014.
 */
public class TdlDate {
    private String dayName;
    private String dayNumber;
    private int month;
    private int year;
    private long currentDate;
    private boolean ishighlighted = false;

    public TdlDate(String dayName, String dayNumber, int month, int year, long currentDate) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.month = month;
        this.year = year;
        this.currentDate = currentDate;
    }

    public TdlDate() {
    }

    public String getDayName() {
        return dayName;
    }

    public void setDayName(String dayName) {
        this.dayName = dayName;
    }

    public String getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(String dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public long getCurrentDayTimestamp() {
        return currentDate;
    }

    public void setCurrentDate(long currentDate) {
        this.currentDate = currentDate;
    }

    public boolean isHighlighted() {
        return ishighlighted;
    }

    public void setHighlighted(boolean ishighlighted) {
        this.ishighlighted = ishighlighted;
    }
}
