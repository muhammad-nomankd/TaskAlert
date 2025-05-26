package com.durranitech.taskalert.di

import android.content.Context
import com.durranitech.taskalert.repositories.AuthRepository
import com.durranitech.taskalert.repositories.CreateTaskRepository
import com.durranitech.taskalert.repositories.GetTaskRepository
import com.durranitech.taskalert.repositories.SaveLocationRespository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAuthRepository(): AuthRepository {
        return AuthRepository()
    }

    @Provides
    @Singleton
    fun provideCreateTaskRepository(): CreateTaskRepository {
        return CreateTaskRepository()
    }

    @Provides
    @Singleton
    fun provideGetTaskRepository(): GetTaskRepository {
        return GetTaskRepository()
    }

    @Provides
    @Singleton
    fun provideSaveLocationRepository(): SaveLocationRespository {
        return SaveLocationRespository()
    }

    @Provides
    @Singleton
    fun provideTaskNotificationManager(@ApplicationContext context: Context) =
        TaskNotificationManager(context)
}