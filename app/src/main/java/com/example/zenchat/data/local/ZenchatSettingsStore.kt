package com.example.zenchat.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

val Context.zenchatSettingsDataStore by preferencesDataStore(name = "zenchat_settings")

object ZenchatSettingsKeys {
	val DARK_MODE = booleanPreferencesKey("dark_mode")
	val NOTIFICATIONS = booleanPreferencesKey("notifications")
}
