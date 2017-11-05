package com.teeh.klimasensor.common.utils

/**
 * Created by teeh on 24.06.2017.
 */
import com.teeh.klimasensor.TsEntry
import com.teeh.klimasensor.common.constants.Constants.STRING_DATE_FORMAT
import com.teeh.klimasensor.common.ts.SimpleTs
import java.text.SimpleDateFormat

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

object DateUtils {

    var format = SimpleDateFormat(STRING_DATE_FORMAT)

    fun toLong(date: LocalDateTime): Long {
        return date.atZone(ZoneId.systemDefault()).toEpochSecond()
    }

    fun toLong(date: Date): Long {
        return date.time
    }

    fun toLocalDate(date: Long?): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date!!), ZoneId.systemDefault())
    }

    fun toLocalDate(date: String): LocalDateTime {
        return LocalDateTime.parse(date, SimpleTs.tsFormat)
    }

    fun toDate(date: Long): Date {
        return Date(date)
    }

    fun toDate(date: String): Date {
        return format.parse(date)
    }

    fun toDate(date: Date): Date {
        return date
    }

    fun toString(date: LocalDateTime): String {
        return SimpleTs.tsFormat.format(date)
    }

    fun toString(date: Date): String {
        return format.format(date)
    }
}
