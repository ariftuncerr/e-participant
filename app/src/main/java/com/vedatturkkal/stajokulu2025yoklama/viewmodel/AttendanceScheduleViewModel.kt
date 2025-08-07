package com.vedatturkkal.stajokulu2025yoklama.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import com.vedatturkkal.stajokulu2025yoklama.data.model.ParticipantAttendance
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AttendanceScheduleRepository
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AttendanceSummary
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

class AttendanceScheduleViewModel(
    private val repo: AttendanceScheduleRepository = AttendanceScheduleRepository()
) : ViewModel() {

    private val _activities = MutableStateFlow<List<Activity>>(emptyList())
    val activities: StateFlow<List<Activity>> = _activities

    private val _selectedActivity = MutableStateFlow<Activity?>(null)
    val selectedActivity: StateFlow<Activity?> = _selectedActivity

    private val _selectedDate = MutableStateFlow<Long?>(null) // kullanıcının seçtiği gün (millis)
    val selectedDate: StateFlow<Long?> = _selectedDate

    private val _attendancesOfDate = MutableStateFlow<List<AttendanceSummary>>(emptyList())
    val attendancesOfDate: StateFlow<List<AttendanceSummary>> = _attendancesOfDate

    private val _selectedAttendanceId = MutableStateFlow<String?>(null)
    val selectedAttendanceId: StateFlow<String?> = _selectedAttendanceId

    private val _items = MutableStateFlow<List<ParticipantAttendance>>(emptyList())
    val items: StateFlow<List<ParticipantAttendance>> = _items

    private var listenJob: Job? = null

    fun setActivities(list: List<Activity>) { _activities.value = list }

    fun onActivitySelected(activity: Activity) {
        _selectedActivity.value = activity
        _selectedAttendanceId.value = null
        _attendancesOfDate.value = emptyList()
        _items.value = emptyList()
        refreshAttendancesIfReady()
    }

    fun onDateSelected(dayMillis: Long) {
        _selectedDate.value = dayMillis
        _selectedAttendanceId.value = null
        _attendancesOfDate.value = emptyList()
        _items.value = emptyList()
        refreshAttendancesIfReady()
    }

    fun onAttendanceSelected(attendanceId: String) {
        _selectedAttendanceId.value = attendanceId
        val a = _selectedActivity.value ?: return

        listenJob?.cancel()
        listenJob = viewModelScope.launch {
            repo.listenParticipantAttendances(a.id, attendanceId).collect { _items.value = it }
        }
    }

    fun approve(participantId: Int) {
        val a = _selectedActivity.value ?: return
        val attId = _selectedAttendanceId.value ?: return
        viewModelScope.launch { repo.approve(a.id, attId, participantId) }
    }

    fun reject(participantId: Int) {
        val a = _selectedActivity.value ?: return
        val attId = _selectedAttendanceId.value ?: return
        viewModelScope.launch { repo.reject(a.id, attId, participantId) }
    }

    /** Aktivite + tarih seçili ise o güne ait attendanceları yükler. */
    private fun refreshAttendancesIfReady() {
        val a = _selectedActivity.value ?: return
        val day = _selectedDate.value ?: return
        viewModelScope.launch {
            val (start, end) = istDayBounds(day)
            val list = repo.getAttendancesByDate(a.id, start, end)
            _attendancesOfDate.value = list
        }
    }

    /** İstanbul günü: [00:00, 24:00) */
    private fun istDayBounds(dayMillis: Long): Pair<Long, Long> {
        val tz = TimeZone.getTimeZone("Europe/Istanbul")
        val cal = Calendar.getInstance(tz).apply {
            timeInMillis = dayMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_MONTH, 1)
        val end = cal.timeInMillis
        return start to end
    }
}
