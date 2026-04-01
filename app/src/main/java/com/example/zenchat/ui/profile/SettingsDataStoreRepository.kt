package com.example.zenchat.ui.profile

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.zenchat.data.local.ZenchatSettingsKeys
import com.example.zenchat.data.local.zenchatSettingsDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStoreRepository @Inject constructor(
	@ApplicationContext private val context: Context
) : SettingsRepository {
	override val darkModeEnabled: Flow<Boolean> =
		context.zenchatSettingsDataStore.data.map { prefs: Preferences ->
			prefs[ZenchatSettingsKeys.DARK_MODE] ?: false
		}

	override val notificationsEnabled: Flow<Boolean> =
		context.zenchatSettingsDataStore.data.map { prefs: Preferences ->
			prefs[ZenchatSettingsKeys.NOTIFICATIONS] ?: true
		}

	override suspend fun setDarkMode(enabled: Boolean) {
		context.zenchatSettingsDataStore.edit { it[ZenchatSettingsKeys.DARK_MODE] = enabled }
	}

	override suspend fun setNotifications(enabled: Boolean) {
		context.zenchatSettingsDataStore.edit { it[ZenchatSettingsKeys.NOTIFICATIONS] = enabled }
	}
}

