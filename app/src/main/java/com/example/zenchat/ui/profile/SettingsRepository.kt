package com.example.zenchat.ui.profile

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
	val darkModeEnabled: Flow<Boolean>
	val notificationsEnabled: Flow<Boolean>

	suspend fun setDarkMode(enabled: Boolean)
	suspend fun setNotifications(enabled: Boolean)
}

