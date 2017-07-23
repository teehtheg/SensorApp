package com.teeh.klimasensor.common.utils;

import java.util.Collection;

/**
 * Created by teeh on 16.07.2017.
 */

public class CalcUtil {

    public static <T extends Number> Double sum(Collection<T> collection) {
        Double sum = 0.0;

        for (T entry : collection) {
            sum = sum + (Double)entry;
        }

        return sum;
    }

}
