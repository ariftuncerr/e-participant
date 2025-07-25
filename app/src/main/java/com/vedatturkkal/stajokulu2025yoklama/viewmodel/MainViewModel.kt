package com.vedatturkkal.stajokulu2025yoklama.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.vedatturkkal.stajokulu2025yoklama.data.model.Activity
import com.vedatturkkal.stajokulu2025yoklama.data.repository.ActivityRepository

class MainViewModel : ViewModel() {
    private val activityRepository = ActivityRepository()

    private val _addActivityResult = MutableLiveData<Boolean>()
    val addActivityResult : LiveData<Boolean> = _addActivityResult

    suspend fun createActivity(activity: Activity){
        _addActivityResult.value = activityRepository.createActivity(activity)
    }
}