package com.vedatturkkal.stajokulu2025yoklama.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatturkkal.stajokulu2025yoklama.data.model.Attendance
import kotlinx.coroutines.tasks.await

class AttendanceRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun addAttendance(activityId: String, date: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            val attendanceRef = firestore.collection("users")
                .document(uid)
                .collection("activities")
                .document(activityId)
                .collection("attendance")
                .document() // otomatik ID

            val attendance = Attendance(
                id = attendanceRef.id,
                date = date
            )

            attendanceRef.set(attendance).await()
            true
        } catch (e: Exception) {
            false
        }
    }

}
