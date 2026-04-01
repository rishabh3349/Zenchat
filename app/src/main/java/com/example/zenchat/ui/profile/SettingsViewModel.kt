package com.example.zenchat.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
	private val repo: SettingsRepository
) : ViewModel() {

	val darkModeEnabled: StateFlow<Boolean> =
		repo.darkModeEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, false)

	val notificationsEnabled: StateFlow<Boolean> =
		repo.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)

	fun setDarkMode(enabled: Boolean) {
		viewModelScope.launch { repo.setDarkMode(enabled) }
	}

	fun setNotifications(enabled: Boolean) {
		viewModelScope.launch { repo.setNotifications(enabled) }
	}
}

