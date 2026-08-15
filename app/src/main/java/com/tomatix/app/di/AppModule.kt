package com.tomatix.app.di

import com.tomatix.app.data.firebase.FirebaseService
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
    fun provideSensorRepository(firebaseService: FirebaseService): SensorRepository {
        return SensorRepository(firebaseService)
    }
}
