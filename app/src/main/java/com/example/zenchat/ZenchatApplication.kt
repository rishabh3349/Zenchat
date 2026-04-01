package com.example.zenchat

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.zenchat.data.local.ZenchatSettingsKeys
import com.example.zenchat.data.local.zenchatSettingsDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@HiltAndroidApp
class ZenchatApplication : Application() {

	override fun onCreate() {
		super.onCreate()
		val dark = runBlocking {
			zenchatSettingsDataStore.data.first()[ZenchatSettingsKeys.DARK_MODE] ?: false
		}
		AppCompatDelegate.setDefaultNightMode(
			if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
		)
	}
}