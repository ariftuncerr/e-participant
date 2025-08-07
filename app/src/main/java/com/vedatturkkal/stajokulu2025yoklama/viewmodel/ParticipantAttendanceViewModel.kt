// ParticipantAttendanceViewModel.kt
package com.vedatturkkal.stajokulu2025yoklama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import com.vedatturkkal.stajokulu2025yoklama.data.repo.ParticipantAttendanceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ParticipantAttendanceViewModel(
    private val repo: ParticipantAttendanceRepository
) : ViewModel() {

    private val _items = MutableStateFlow<List<ParticipantAttendance>>(emptyList())
    val items: StateFlow<List<ParticipantAttendance>> = _items

    private var listenJob: Job? = null

    fun startListening(activityId: String, attendanceId: String) {
        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            repo.listenParticipantAttendances(activityId, attendanceId).collect {
                _items.value = it
            }
        }
    }

    suspend fun addAll(activityId: String, attendanceId: String): Boolean {
        return repo.addAllParticipantsToAttendance(activityId, attendanceId)
    }

    fun approve(activityId: String, attendanceId: String, participantId: Int) = viewModelScope.launch {
        repo.approveParticipant(activityId, attendanceId, participantId)
    }

    fun unapprove(activityId: String, attendanceId: String, participantId: Int) = viewModelScope.launch {
        repo.unapproveParticipant(activityId, attendanceId, participantId)
    }

    fun reject(activityId: String, attendanceId: String, participantId: Int) = viewModelScope.launch {
        repo.rejectParticipant(activityId, attendanceId, participantId)
    }

    fun unreject(activityId: String, attendanceId: String, participantId: Int) = viewModelScope.launch {
        repo.unrejectParticipant(activityId, attendanceId, participantId)
    }

    fun removeAll(activityId: String, attendanceId: String) = viewModelScope.launch {
        repo.removeAllFromAttendance(activityId, attendanceId)
    }
}
