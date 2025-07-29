package com.vedatturkkal.stajokulu2025yoklama.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatturkkal.stajokulu2025yoklama.data.model.Attendance
import kotlinx.coroutines.tasks.await

class AttendanceRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addAttendance(activityId: String, date: String): Pair<Boolean, String?> {
        val uid = auth.currentUser?.uid ?: return Pair(false, null)
        return try {
            val attendanceRef = firestore.collection("users")
                .document(uid)
                .collection("activities")
                .document(activityId)
                .collection("attendance")
                .document() // otomatik ID oluşturur

            val attendance = Attendance(
                id = attendanceRef.id,
                date = date
            )

            attendanceRef.set(attendance).await()
            Pair(true, attendanceRef.id) // başarı ve ID'yi döner
        } catch (e: Exception) {
            Pair(false, null) // hata durumunda başarısızlık ve null ID
        }
    }


}
