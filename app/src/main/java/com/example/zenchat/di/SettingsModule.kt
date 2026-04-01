package com.example.zenchat.di

import com.example.zenchat.ui.profile.SettingsDataStoreRepository
import com.example.zenchat.ui.profile.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {
	@Binds
	abstract fun bindSettingsRepository(
		impl: SettingsDataStoreRepository
	): SettingsRepository
}

