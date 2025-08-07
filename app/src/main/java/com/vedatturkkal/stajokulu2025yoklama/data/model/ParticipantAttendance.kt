package com.vedatturkkal.stajokulu2025yoklama.data.model

data class ParticipantAttendance(
    val participant: Participant = Participant(),
    val checkInTime: String = "",
    val checkOutTime: String = "",
    val approval: Boolean = false,
    val denied: Boolean = false
)


