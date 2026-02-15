package com.example.zenchat.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth
) {

    suspend fun signIn(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val user = currentUser()
            if (user == null) {
                return Result.failure(Exception("User not found"))
            }
            if (!user.isEmailVerified) {
                logout()
                return Result.failure(Exception("Email not verified"))
            }
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun signUp(
        email: String,
        password: String
    ): Result<Unit> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            val user = auth.currentUser
                ?: return Result.failure(Exception("User creation failed"))
            user.sendEmailVerification().await()
            auth.signOut()
            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun forgotPassword(email:String): Result<Unit> {

        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() = auth.signOut()

    fun currentUser() = auth.currentUser
}