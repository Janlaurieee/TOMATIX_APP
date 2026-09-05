package com.tomatix.app.di

import com.tomatix.app.BuildConfig
import com.tomatix.app.data.firebase.FirebaseService
import com.tomatix.app.data.gemini.GeminiService
import com.tomatix.app.data.local.AppPreferences
import com.tomatix.app.data.repository.SensorRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseService(): FirebaseService {
        return FirebaseService()
    }

    @Provides
    @Singleton
    fun provideSensorRepository(
        firebaseService: FirebaseService,
        appPreferences: AppPreferences
    ): SensorRepository {
        return SensorRepository(firebaseService, appPreferences)
    }

    @Provides
    @Singleton
    fun provideGeminiService(): GeminiService {
        return GeminiService(BuildConfig.GEMINI_API_KEY)
    }
}
