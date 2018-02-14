package com.teeh.klimasensor.common.utils

/**
 * Created by teeh on 24.06.2017.
 */
import com.teeh.klimasensor.TsEntry
import com.teeh.klimasensor.common.constants.Constants.STRING_DATE_FORMAT
import com.teeh.klimasensor.common.ts.SimpleTs
import java.text.SimpleDateFormat

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

object DateUtils {

    val dateFormat = SimpleDateFormat(STRING_DATE_FORMAT)
    val localDateFormat = DateTimeFormatter.ofPattern(STRING_DATE_FORMAT)

    fun toLong(date: LocalDateTime): Long {
        return date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun toLong(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun toLong(date: Date): Long {
        return date.time
    }

    fun toLocalDate(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    fun toLocalDate(date: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(date), ZoneId.systemDefault())
    }

    fun toLocalDate(date: String): LocalDateTime {
        return LocalDateTime.parse(date, localDateFormat)
    }

    fun toLocalDate(date: String, formatter: DateTimeFormatter): LocalDateTime {
        return LocalDateTime.parse(date, formatter)
    }

    fun toDate(date: Long): Date {
        return Date(date)
    }

    fun toDate(date: String): Date {
        return dateFormat.parse(date)
    }

    fun toDate(date: LocalDateTime): Date {
        return Date.from(date.atZone(ZoneId.systemDefault()).toInstant())
    }

    fun toString(date: LocalDate): String {
        return localDateFormat.format(date)
    }

    fun toString(date: LocalDateTime): String {
        return localDateFormat.format(date)
    }

    fun toString(date: Date): String {
        if (date == Date(0)) {
            return "-"
        }
        return dateFormat.format(date)
    }
}
