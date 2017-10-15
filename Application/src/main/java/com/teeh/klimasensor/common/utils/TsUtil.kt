package com.teeh.klimasensor.common.utils

import com.teeh.klimasensor.common.ts.SimpleEntry
import com.teeh.klimasensor.common.ts.SimpleTs
import java.util.stream.Collectors

/**
 * Created by teeh on 16.07.2017.
 */

object TsUtil {

    fun shiftBy(ts: SimpleTs, shiftBy: Double): SimpleTs {
        val list = ts.ts.stream()
                .map { (type, value, timestamp) -> SimpleEntry(type, value!! + shiftBy, timestamp) }
                .collect(Collectors.toList())
        return SimpleTs(list)
    }
}
