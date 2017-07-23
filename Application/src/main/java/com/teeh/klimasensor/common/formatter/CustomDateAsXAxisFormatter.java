package com.teeh.klimasensor.common.formatter;

import android.content.Context;

import com.jjoe64.graphview.DefaultLabelFormatter;

import java.text.DateFormat;
import java.util.Calendar;

public class CustomDateAsXAxisFormatter extends DefaultLabelFormatter {
    /**
     * the date format that will convert
     * the unix timestamp to string
     */
    protected final DateFormat mDateFormat;
    protected final DateFormat mTimeFormat;

    /**
     * calendar to avoid creating new date objects
     */
    protected final Calendar mCalendar;

    /**
     * create the formatter with the Android default date format to convert
     * the x-values.
     *
     * @param context the application context
     */
    public CustomDateAsXAxisFormatter(Context context) {
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);
        mCalendar = Calendar.getInstance();
    }

    /**
     * create the formatter with your own custom
     * date format to convert the x-values.
     *
     * @param context the application context
     * @param dateFormat custom date format
     */
    public CustomDateAsXAxisFormatter(Context context, DateFormat dateFormat) {
        mTimeFormat = android.text.format.DateFormat.getTimeFormat(context);
        mDateFormat = dateFormat;
        mCalendar = Calendar.getInstance();
    }

    /**
     * formats the x-values as date string.
     *
     * @param value raw value
     * @param isValueX true if it's a x value, otherwise false
     * @return value converted to string
     */
    @Override
    public String formatLabel(double value, boolean isValueX) {
        if (isValueX) {
            // format as date
            mCalendar.setTimeInMillis((long) value);
            String date = mDateFormat.format(mCalendar.getTimeInMillis());
            String time = mTimeFormat.format(mCalendar.getTimeInMillis());
            return date + " " + time;
        } else {
            return super.formatLabel(value, isValueX);
        }
    }
}

