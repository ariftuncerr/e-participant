package com.vedatturkkal.stajokulu2025yoklama.data.model

import java.util.UUID
import kotlin.uuid.Uuid

data class Activity(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val dateCreated: Long = System.currentTimeMillis()
)

