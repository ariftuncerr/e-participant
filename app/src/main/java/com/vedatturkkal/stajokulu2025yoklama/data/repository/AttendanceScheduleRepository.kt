package com.vedatturkkal.stajokulu2025yoklama.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AttendanceSummary(
    val id: String,
    val title: String,
    val timeText: String // "14:00"
)

class AttendanceScheduleRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun activityPath(uid: String, activityId: String) =
        db.collection("users").document(uid)
            .collection("activities").document(activityId)

    private fun attendanceDoc(uid: String, activityId: String, attendanceId: String) =
        activityPath(uid, activityId).collection("attendance").document(attendanceId)

    private fun participantAttendances(uid: String, activityId: String, attendanceId: String) =
        attendanceDoc(uid, activityId, attendanceId).collection("participantAttendances")

    private fun mapToSummary(id: String, data: Map<String, Any?>): AttendanceSummary {
        val title = (data["title"] as? String).orEmpty()
        val timeFromField = (data["startTime"] as? String) ?: (data["time"] as? String)
        val timeText = timeFromField
            ?: ((data["date"] as? Timestamp)?.toDate()?.let {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
            } ?: "")
        return AttendanceSummary(id = id, title = title, timeText = timeText)
    }

    /** Seçili gün aralığına (ms) göre attendanceları getirir. Timestamp ve String tarihleri destekler. */
    suspend fun getAttendancesByDate(
        activityId: String,
        dayStartMs: Long,
        dayEndMs: Long
    ): List<AttendanceSummary> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        val col = activityPath(uid, activityId).collection("attendance")

        // 1) Timestamp alanı varsa aralık sorgusu
        runCatching {
            val tsStart = Timestamp(Date(dayStartMs))
            val tsEnd = Timestamp(Date(dayEndMs))
            val snap = col.whereGreaterThanOrEqualTo("date", tsStart)
                .whereLessThan("date", tsEnd)
                .get().await()
            val list = snap.documents.mapNotNull { d -> d.data?.let { mapToSummary(d.id, it) } }
            if (list.isNotEmpty()) return list.sortedBy { it.timeText }
        }

        // 2) String tarih (client-side filtre)
        val all = runCatching { col.get().await() }.getOrNull() ?: return emptyList()
        val fFull = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val fDay = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val filtered = all.documents.mapNotNull { d ->
            val data = d.data ?: return@mapNotNull null
            val s = data["date"] as? String ?: return@mapNotNull null
            val parsed = runCatching { fFull.parse(s) }.getOrNull()
                ?: runCatching { fDay.parse(s) }.getOrNull()
            val ms = parsed?.time ?: return@mapNotNull null
            if (ms in dayStartMs until dayEndMs) mapToSummary(d.id, data) else null
        }
        return filtered.sortedBy { it.timeText }
    }

    /** Seçilen attendance’ın katılımcılarını canlı dinler. */
    fun listenParticipantAttendances(activityId: String, attendanceId: String) = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            trySend(emptyList<ParticipantAttendance>())
            close()
            return@callbackFlow
        }
        val reg = participantAttendances(uid, activityId, attendanceId)
            .addSnapshotListener { s, _ ->
                val list = s?.documents?.mapNotNull { it.toObject(ParticipantAttendance::class.java) }
                    ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    /** Onay (approval=true, denied=false) */
    suspend fun approve(activityId: String, attendanceId: String, participantId: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return runCatching {
            participantAttendances(uid, activityId, attendanceId)
                .document(participantId.toString())
                .update(mapOf("approval" to true, "denied" to false)).await()
        }.isSuccess
    }

    /** Red (approval=false, denied=true) */
    suspend fun reject(activityId: String, attendanceId: String, participantId: Int): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return runCatching {
            participantAttendances(uid, activityId, attendanceId)
                .document(participantId.toString())
                .update(mapOf("approval" to false, "denied" to true)).await()
        }.isSuccess
    }
}
