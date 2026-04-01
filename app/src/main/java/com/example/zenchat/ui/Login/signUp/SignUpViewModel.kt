package com.example.zenchat.ui.Login.signUp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenchat.data.AuthRepository
import com.example.zenchat.data.DatabaseRepository
import com.example.zenchat.data.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val databaseRepository: DatabaseRepository
) : ViewModel(){
    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState

    fun signUp(name:String,email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading

            val result = authRepository.signUp(email, password)

            _loginState.value = if (result.isSuccess) {

                val uid = result.getOrNull()
                databaseRepository.addUser(uid,name, email)
                AuthState.Success

            } else {
                AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }
}