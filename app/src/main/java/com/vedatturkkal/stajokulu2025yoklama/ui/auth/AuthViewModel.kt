package com.vedatturkkal.stajokulu2025yoklama.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.vedatturkkal.stajokulu2025yoklama.data.repository.AuthRepository

class AuthViewModel : ViewModel() {

    private val repo = AuthRepository()
    private val _authResult = MutableLiveData<Pair<Boolean, String?>>()
    val authResult: LiveData<Pair<Boolean, String?>> = _authResult

    fun register(name: String, email: String, password: String) {

        repo.register(name, email, password) { success, message ->
            _authResult.value = Pair(success, message)
        }

    }

    fun login(email: String, password: String) {

        repo.login(email, password) { success, message ->
            _authResult.value = Pair(success, message)
        }

    }
    fun logout(){
        repo.logout()
    }

    fun getCurrentUser() : FirebaseUser? {
        return repo.getCurrentUser()
    }

}