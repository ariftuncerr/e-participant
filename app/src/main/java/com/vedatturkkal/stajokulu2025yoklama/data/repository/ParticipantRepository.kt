package com.vedatturkkal.stajokulu2025yoklama.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ParticipantRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addParticipant(activityId: String, name: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false

        return try {
            val participantsRef = firestore.collection("users")
                .document(uid)
                .collection("activities")
                .document(activityId)
                .collection("participants")

            // Katılımcı sayısını al
            val snapshot = participantsRef.get().await()
            val nextId = snapshot.size() + 1

            val participant = Participant(
                id = nextId,
                uuid = uid,
                name = name
            )

            participantsRef
                .document(nextId.toString()) // doküman ID’si de sayı olabilir
                .set(participant)
                .await()

            true
        } catch (e: Exception) {
            false
        }
    }


    fun getParticipants(activityId: String): Flow<List<Participant>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users")
            .document(uid)
            .collection("activities")
            .document(activityId)
            .collection("participants")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val participants = snapshot.documents.mapNotNull {
                    it.toObject(Participant::class.java)
                }
                trySend(participants)
            }

        awaitClose { listener.remove() }
    }
}
