package com.teeh.klimasensor.common.utils;

import com.teeh.klimasensor.common.ts.SimpleEntry;
import com.teeh.klimasensor.common.ts.SimpleTs;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by teeh on 16.07.2017.
 */

public class TsUtil {

    public static SimpleTs shiftBy(SimpleTs ts, Double shiftBy) {
        List<SimpleEntry> list = ts.getTs().stream()
                .map(x -> new SimpleEntry(x.getType(), x.getValue() + shiftBy, x.getTimestamp()))
                .collect(Collectors.toList());
        return new SimpleTs(list);
    }
}
