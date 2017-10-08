package com.teeh.klimasensor.common.ts

import java.util.Date

/**
 * Created by teeh on 16.07.2017.
 */

data class SimpleEntry(val type: ValueType, val value: Double?, val timestamp: Date)
