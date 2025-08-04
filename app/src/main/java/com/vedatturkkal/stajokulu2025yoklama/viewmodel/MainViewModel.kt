package com.vedatturkkal.stajokulu2025yoklama.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import com.vedatturkkal.stajokulu2025yoklama.data.model.Participant
import com.vedatturkkal.stajokulu2025yoklama.data.repository.ActivityRepository
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AttendanceRepository
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AuthManager
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AuthRepository
import com.vedatturkkal.stajokulu2025yoklama.data.repository.ParticipantAttendanceRepository
import com.vedatturkkal.stajokulu2025yoklama.data.repository.ParticipantRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val activityRepository = ActivityRepository()

    private val _addActivityResult = MutableLiveData<Boolean>()
    val addActivityResult : LiveData<Boolean> = _addActivityResult

    private val _activitiesResult = MutableStateFlow<List<Activity>>(emptyList())
    val activitiesResult : StateFlow<List<Activity>> = _activitiesResult.asStateFlow()


    suspend fun createActivity(activity: Activity){
        _addActivityResult.value = activityRepository.createActivity(activity)
    }
     fun getActivities(){
        viewModelScope.launch {
            activityRepository.getActivities().collect { activities ->
                _activitiesResult.value = activities
            }
        }
    }

    private val participantRepository = ParticipantRepository()

    private val _addParticipantResult = MutableLiveData<Boolean>()
    val addParticipantResult : LiveData<Boolean> = _addParticipantResult

    suspend fun addParticipant(activityId : String, pName : String){
        _addParticipantResult.value = participantRepository.addParticipant(activityId,pName)
    }

    private val _participantList = MutableStateFlow<List<Participant>>(emptyList())
    val participantList : StateFlow<List<Participant>> = _participantList.asStateFlow()

    fun getParticipants(activityId : String){
        viewModelScope.launch {
            participantRepository.getParticipants(activityId).collect { participants ->
                _participantList.value = participants
            }
        }
    }

    private val _deleteParticipantResult = MutableLiveData<Boolean>()
    val deleteParticipantResult: LiveData<Boolean> = _deleteParticipantResult

    fun deleteParticipant(activityId: String, participantId: Int) {
        viewModelScope.launch {
            val result = participantRepository.deleteParticipant(activityId, participantId)
            _deleteParticipantResult.value = result
        }
    }

    private val attendanceRepository = AttendanceRepository()

    private val _addAttendanceResult = MutableLiveData< Pair<Boolean,String?>>()
    val addAttendanceResult : LiveData<Pair<Boolean, String?>> get() = _addAttendanceResult

    fun addAttendance(activityId: String, date: String){
        viewModelScope.launch {
            val result = attendanceRepository.addAttendance(activityId,date)
            _addAttendanceResult.value = result
        }
    }

    val currentUserEmail = AuthManager.getCurrentUser()?.email

    private val participantAttendanceRepository = ParticipantAttendanceRepository()

    private val _addAllResult = MutableLiveData<Boolean>()
    val addAllResult : LiveData<Boolean> = _addAllResult

    fun addAllPtToAttendance(activityId : String, attendanceId : String){
        viewModelScope.launch {
            val result = participantAttendanceRepository.addAllParticipantsToAttendance(activityId,attendanceId)
            _addAllResult.value = result
        }

    }

    private val _approveParticipantResult = MutableLiveData<Boolean>()
    val approveParticipantResult : LiveData<Boolean> = _approveParticipantResult

    fun approveParticipant( activityId: String,
                            attendanceId: String,
                            participantId: Int){
        viewModelScope.launch {
            val result = participantAttendanceRepository.approveParticipant(activityId,attendanceId,participantId)
            _approveParticipantResult.value = result
        }
    }

}