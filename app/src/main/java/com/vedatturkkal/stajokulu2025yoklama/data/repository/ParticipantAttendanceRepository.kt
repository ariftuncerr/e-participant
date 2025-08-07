// ParticipantAttendanceRepository.kt
package com.vedatturkkal.stajokulu2025yoklama.data.repo

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ParticipantAttendanceRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun path(uid: String, activityId: String, attendanceId: String) =
        firestore.collection("users").document(uid)
            .collection("activities").document(activityId)
            .collection("attendance").document(attendanceId)
            .collection("participantAttendances")

    suspend fun addAllParticipantsToAttendance(activityId: String, attendanceId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val partsSnap = firestore.collection("users").document(uid)
                .collection("activities").document(activityId)
                .collection("participants").get().await()

            val batch = firestore.batch()
            partsSnap.documents.forEach { doc ->
                val p = doc.toObject(Participant::class.java) ?: return@forEach
                val pa = ParticipantAttendance(participant = p, approval = false, denied = false)
                batch.set(path(uid, activityId, attendanceId).document(p.id.toString()), pa)
            }
            batch.commit().await()
            true
        } catch (_: Exception) { false }
    }

    suspend fun approveParticipant(activityId: String, attendanceId: String, participantId: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            path(uid, activityId, attendanceId).document(participantId.toString())
                .update(mapOf("approval" to true, "denied" to false)).await()
            true
        } catch (_: Exception) { false }
    }

    suspend fun unapproveParticipant(activityId: String, attendanceId: String, participantId: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            path(uid, activityId, attendanceId).document(participantId.toString())
                .update("approval", false).await()
            true
        } catch (_: Exception) { false }
    }

    suspend fun rejectParticipant(activityId: String, attendanceId: String, participantId: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            path(uid, activityId, attendanceId).document(participantId.toString())
                .update(mapOf("approval" to false, "denied" to true)).await()
            true
        } catch (_: Exception) { false }
    }

    suspend fun unrejectParticipant(activityId: String, attendanceId: String, participantId: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            path(uid, activityId, attendanceId).document(participantId.toString())
                .update("denied", false).await()
            true
        } catch (_: Exception) { false }
    }

    suspend fun removeAllFromAttendance(activityId: String, attendanceId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val snap = path(uid, activityId, attendanceId).get().await()
            val batch = firestore.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()
            true
        } catch (_: Exception) { false }
    }

    fun listenParticipantAttendances(activityId: String, attendanceId: String) = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val reg = path(uid, activityId, attendanceId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull { it.toObject(ParticipantAttendance::class.java) } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }
}
