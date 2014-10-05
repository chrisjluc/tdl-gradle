package com.ac.tdl;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by chrisjluc on 2014-10-05.
 */
public class GenericHelper {

    public static long getFlooredCurrentDate() {
        Calendar c = new GregorianCalendar();
        return floorDateByDay(c);
    }


    public static long floorDateByDay(Calendar c) {
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTime().getTime();
    }


    public static int getIntFromBool(boolean isTrue) {
        if (isTrue) {
            return 1;
        }
        return 0;
    }

    public static boolean getBoolFromInt(int isTrue) throws Exception {
        if (isTrue == 1) {
            return true;
        } else if (isTrue == 0) {
            return false;
        }
        throw new Exception();
    }

    public static long getCurrentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static boolean isNumber(String str) {
        try {
            double d = Double.parseDouble(str);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
