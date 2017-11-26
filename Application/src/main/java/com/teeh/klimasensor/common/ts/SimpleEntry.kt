package com.teeh.klimasensor.common.ts

import java.time.LocalDateTime

data class SimpleEntry(val type: ValueType, val value: Double?, val timestamp: LocalDateTime)
