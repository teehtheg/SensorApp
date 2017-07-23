package com.teeh.klimasensor.common.utils;

/**
 * Created by teeh on 24.06.2017.
 */
import com.teeh.klimasensor.common.ts.SimpleTs;

import java.util.Date;

public class DateUtils {

    public static Long toLong(Date date) {
        if (date != null) {
            return date.getTime();
        }
        return 0L;
    }

    public static Date toDate(Long date) {
        if (date != null) {
            return new Date(date);
        }
        return new Date(0);
    }

    public static String toString(Date date) {
        if (date == null) {
            return "1970-01-01 00:00:00";
        } else {
            return SimpleTs.tsFormat.format(date);
        }
    }
}
