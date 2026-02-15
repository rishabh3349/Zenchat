package com.example.zenchat.data

import android.util.Log
import com.example.zenchat.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val databaseReference: DatabaseReference
) {

    fun addUser(name:String,email:String,uid:String?){
        databaseReference.child("user").child(uid!!).setValue(User(name,email,uid)).addOnCompleteListener{
            Log.d("User Added", "New user added successfully")
        }
    }

    fun getMessagesRef(): DatabaseReference {
        return databaseReference.child("chats")
    }
}