package com.vedatturkkal.stajokulu2025yoklama.data.model

data class Attendance(
    val date: String = "",  // "2025-07-22"
    val statuses: Map<String, Boolean> = emptyMap()  // participantId -> true/false
)
