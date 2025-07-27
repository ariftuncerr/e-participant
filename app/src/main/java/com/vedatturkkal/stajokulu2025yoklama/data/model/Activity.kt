package com.vedatturkkal.stajokulu2025yoklama.data.model

import java.util.UUID

data class Activity(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val dateCreated: Long = System.currentTimeMillis()

){
    override fun toString(): String {
        return title
    }
}

