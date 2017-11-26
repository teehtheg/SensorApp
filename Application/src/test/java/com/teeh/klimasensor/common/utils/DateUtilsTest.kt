package com.teeh.klimasensor.common.utils

import org.testng.Assert
import org.testng.annotations.Test
import java.time.LocalDateTime
import java.util.*


class DateUtilsTest {

    @Test
    fun DateToLocalDate() {
        val date = Date()
        val localdate = DateUtils.toLocalDate(date)
        val date2 = DateUtils.toDate(localdate)

        Assert.assertEquals(date, date2)
    }

    @Test
    fun LongToDate() {
        val date = Date()
        val long = DateUtils.toLong(date)
        val date2 = DateUtils.toDate(long)

        Assert.assertEquals(date, date2)
    }

    @Test
    fun LongToLocalDate() {
        val date = LocalDateTime.now()
        val long = DateUtils.toLong(date)
        val date2 = DateUtils.toLocalDate(long)

        Assert.assertEquals(date, date2)
    }

    @Test
    fun LongToLong() {
        val date = LocalDateTime.now()
        val long = DateUtils.toLong(date)

        val date2 = DateUtils.toDate(date)
        val long2 = DateUtils.toLong(date2)

        Assert.assertEquals(long, long2)
    }

    @Test
    fun StringToString() {
        val date = LocalDateTime.now()
        val string = DateUtils.toString(date)

        val date2 = DateUtils.toDate(date)
        val string2 = DateUtils.toString(date2)

        Assert.assertEquals(string, string2)
    }
}