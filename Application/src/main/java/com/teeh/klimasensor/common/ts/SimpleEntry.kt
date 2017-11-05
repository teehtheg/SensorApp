package com.teeh.klimasensor.common.ts

import java.util.Date

data class SimpleEntry(val type: ValueType, val value: Double?, val timestamp: Date)
