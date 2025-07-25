package com.vedatturkkal.stajokulu2025yoklama.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ActivityRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    fun getActivities(): Flow<List<Activity>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close()
            return@callbackFlow
        }

        val listenerRegistration = firestore.collection("users")
            .document(uid)
            .collection("activities")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList()) // hata durumunda boş liste gönder
                    return@addSnapshotListener
                }

                val activities = snapshot.documents.mapNotNull { it.toObject(Activity::class.java) }
                trySend(activities)
            }

        awaitClose { listenerRegistration.remove() }
    }
    suspend fun createActivity(activity: Activity): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        return try {
            firestore.collection("users")
                .document(uid)
                .collection("activities")
                .document(activity.id)
                .set(activity)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}