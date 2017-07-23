package com.teeh.klimasensor.common.ts;

import com.teeh.klimasensor.TsEntry;
import com.teeh.klimasensor.common.utils.CalcUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by teeh on 16.07.2017.
 */

public class SimpleTs {

    public static final DateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss", Locale.ENGLISH);

    private List<SimpleEntry> data;
    private Map<Date, Integer> dateMap;

    public SimpleTs(List<SimpleEntry> data_) {
        data = data_;
        dateMap = new HashMap<Date, Integer>();

        for (int i=0; i<data.size(); i++) {
            dateMap.put(data.get(i).getTimestamp(), i);
        }
    }

    public List<SimpleEntry> getTs(){
        return data;
    }

    public SimpleEntry getEntry(Date d) {
        Integer index = dateMap.get(d);
        if (index != null && index < data.size()) {
            return data.get(index);
        }
        else {
            return null;
        }
    }

    public List<Double> getValueList() {
        return data.stream().map(x -> x.getValue()).collect(Collectors.toList());
    }


    ////////////
    // Limits //
    ////////////

    public Double getMin() {
        return Collections.min(getValueList());
    }

    public Double getMax() {
        return Collections.max(getValueList());
    }

    public SimpleEntry getLatestEntry() {
        return data.get(data.size()-1);
    }


    /////////////////
    // Aggregators //
    /////////////////

    public Double getLatestValue() {
        return getLatestEntry().getValue();
    }

    public Double getAvg() {
        List<Double> ts = getValueList();
        Double sum = CalcUtil.sum(ts);
        return sum / ts.size();
    }

    public Double getMedian() {
        List<Double> ts = getValueList();
        Collections.sort(ts);
        if (ts.size() % 2 == 0) {
            int half = ts.size()/2;
            return (ts.get(half-1) + ts.get(half)) / 2.0;
        }
        else {
            int half = (ts.size()-1)/2;
            return ts.get(half);
        }
    }

    public Date getLatestTimestamp() {
        if (data == null || data.isEmpty()) {
            return null;
        }
        else {
            SimpleEntry lastEntry = data.get(data.size()-1);
            return lastEntry.getTimestamp();
        }
    }

    public Date getFirstTimestamp() {
        if (data == null || data.isEmpty()) {
            return null;
        }
        else {
            SimpleEntry firstEntry = data.get(0);
            return firstEntry.getTimestamp();
        }
    }

    public String getLatestTimestampString() {
        Date lastTs = getLatestTimestamp();
        if (lastTs == null) {
            return "1970-01-01 00:00:00";
        } else {
            return tsFormat.format(lastTs);
        }
    }
}
