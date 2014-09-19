package com.ac.tdl.model;

import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class Model {

	public abstract void setModelInDb();

    protected static long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    protected static long floorDateByDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime().getTime();
    }

    protected static long getCurrentDate() {
        Calendar c = new GregorianCalendar();
        return floorDateByDay(c);
    }

	protected static int getIntFromBool(boolean isTrue) {
		if (isTrue) {
			return 1;
		}
		return 0;
	}

	protected boolean getBoolFromInt(int isTrue) throws Exception {
		if (isTrue == 1) {
			return true;
		} else if (isTrue == 0) {
			return false;
		}
		throw new Exception();
	}
}
