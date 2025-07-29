package com.vedatturkkal.stajokulu2025yoklama.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import kotlinx.coroutines.tasks.await

class ParticipantAttendanceRepository {

    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Tüm katılımcıları o günkü yoklamaya `approval = false` olarak ekler.
     */
    suspend fun addAllParticipantsToAttendance(
        activityId: String,
        attendanceId: String
    ): Boolean {
        val uid = AuthManager.getCurrentUser()?.uid ?: return false

        return try {
            // Katılımcı listesini al
            val participantsSnapshot = firestore.collection("users")
                .document(uid)
                .collection("activities")
                .document(activityId)
                .collection("participants")
                .get()
                .await()

            val participants = participantsSnapshot.documents.mapNotNull {
                it.toObject(Participant::class.java)
            }

            val attendanceRef = firestore.collection("users")
                .document(uid)
                .collection("activities")
                .document(activityId)
                .collection("attendance")
                .document(attendanceId)
                .collection("participantAttendances")

            // Her birini approval = false ile ekle
            participants.forEach { participant ->
                val participantAttendance = ParticipantAttendance(
                    participant = participant,
                    approval = false
                )

                attendanceRef
                    .document(participant.id.toString())
                    .set(participantAttendance)
                    .await()
            }

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Belirli bir katılımcının approval değerini true yapar.
     */
    suspend fun approveParticipant(
        activityId: String,
        attendanceId: String,
        participantId: Int
    ): Boolean {
        val uid = AuthManager.getCurrentUser()?.uid ?: return false

        return try {
            val docRef = firestore.collection("users")
                .document(uid)
                .collection("activities")
                .document(activityId)
                .collection("attendance")
                .document(attendanceId)
                .collection("participantAttendances")
                .document(participantId.toString())

            // approval'ı true yap
            docRef.update("approval", true).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
