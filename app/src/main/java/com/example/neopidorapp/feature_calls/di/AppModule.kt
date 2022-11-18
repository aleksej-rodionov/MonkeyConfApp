package com.example.neopidorapp.feature_calls.di

import com.example.neopidorapp.feature_calls.data.remote.SocketRepo
import com.example.neopidorapp.feature_calls.data.remote.SocketRepoImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides // todo replace with @Binds after re-watching Hilt Video by PL?
    fun provideSocketRepo(): SocketRepo = SocketRepoImpl()
}