package com.example.zenchat.data

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
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
	): Result<String> {
		return try {
			auth.createUserWithEmailAndPassword(email, password).await()
			val user = auth.currentUser
				?: return Result.failure(Exception("User creation failed"))
			user.sendEmailVerification().await()
			val uid = user.uid
			
			auth.signOut()
			Result.success(uid)
			
		} catch (e: Exception) {
			Result.failure(e)
		}
	}
	
	suspend fun forgotPassword(email: String): Result<Unit> {
		
		return try {
			auth.sendPasswordResetEmail(email).await()
			Result.success(Unit)
		} catch (e: Exception) {
			val message = when (e) {
				is FirebaseAuthException -> when (e.errorCode) {
					"ERROR_INVALID_EMAIL" -> "Invalid email address"
					"ERROR_USER_NOT_FOUND" -> "No account found with this email"
					else -> e.message ?: "Unable to send reset email"
				}
				
				else -> e.message ?: "Unable to send reset email"
			}
			Result.failure(Exception(message))
		}
	}
	
	fun logout() = auth.signOut()
	
	fun currentUser() = auth.currentUser
}