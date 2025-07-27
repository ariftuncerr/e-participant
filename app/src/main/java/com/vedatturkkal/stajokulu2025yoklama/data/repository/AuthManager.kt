package com.vedatturkkal.stajokulu2025yoklama.data.repository

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth = FirebaseAuth.getInstance()

    fun getCurrentUser() = auth.currentUser
    fun signOut() {
        auth.signOut()
    }
}
