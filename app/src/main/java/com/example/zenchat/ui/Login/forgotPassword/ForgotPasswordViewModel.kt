package com.example.zenchat.ui.Login.forgotPassword

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zenchat.data.AuthRepository
import com.example.zenchat.data.model.AuthState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel(){
    private val _loginState = MutableLiveData<AuthState>()
    val loginState: LiveData<AuthState> = _loginState

    fun fp(email: String) {
        viewModelScope.launch {
            _loginState.value = AuthState.Loading

            val result = authRepository.forgotPassword(email)

            _loginState.value = if (result.isSuccess) {
                AuthState.Success
            } else {
                AuthState.Error(
                    result.exceptionOrNull()?.message ?: "Retry"
                )
            }
        }
    }
}