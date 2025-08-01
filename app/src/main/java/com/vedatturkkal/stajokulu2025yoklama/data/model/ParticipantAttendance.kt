package com.vedatturkkal.stajokulu2025yoklama.data.model

data class ParticipantAttendance(
    val participant: Participant,
    val checkInTime: String = "",   // örn. "09:00"
    val checkOutTime: String = "",   // örn. "17:30"
    val approval : Boolean = false  // onay

)

