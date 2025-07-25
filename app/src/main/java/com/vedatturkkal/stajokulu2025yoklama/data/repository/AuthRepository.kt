package com.vedatturkkal.stajokulu2025yoklama.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.vedatturkkal.stajokulu2025yoklama.data.model.User

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val fireStore = FirebaseFirestore.getInstance()

    //email ve password ile kayıt
    fun register(name : String, email : String, password: String, onResult: (Boolean, String?) -> Unit){
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val uid = auth.currentUser!!.uid
                    val user = User(uid,name,email)
                    fireStore.collection("users").document(uid)
                        .set(user)
                        .addOnSuccessListener { onResult(true,null) }
                        .addOnFailureListener { onResult(false,"firestore hatası") }

                }
                else{
                    onResult(false,task.exception?.message)
                }
            }
    }

    //email ve password ile login
    fun login(email : String, password: String, onResult: (Boolean, String?) -> Unit){
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    onResult(true,null)
                }
                else{
                    onResult(false,task.exception?.message)
                }
            }
    }

    fun getCurrentUser() = auth.currentUser
    fun logout() = auth.signOut()

}