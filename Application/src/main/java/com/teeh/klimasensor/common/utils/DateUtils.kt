package com.teeh.klimasensor.common.utils

/**
 * Created by teeh on 24.06.2017.
 */
import com.teeh.klimasensor.TsEntry
import com.teeh.klimasensor.common.ts.SimpleTs

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

object DateUtils {

    fun toLong(date: LocalDateTime): Long {
        return date.atZone(ZoneId.systemDefault()).toEpochSecond()
    }

    fun toDate(date: Long?): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date!!), ZoneId.systemDefault())
    }

    fun toString(date: LocalDateTime): String {
        return SimpleTs.tsFormat.format(date)
    }
}
