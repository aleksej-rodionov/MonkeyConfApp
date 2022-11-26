package com.example.neopidorapp.feature_call.di

import com.example.neopidorapp.feature_call.data.SocketRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope() = CoroutineScope(SupervisorJob())

    @Provides // todo try binds then?
    @Singleton
    fun provideSocketRepo(
        @ApplicationScope scope: CoroutineScope
    ): SocketRepo = SocketRepo(scope)
    // todo if doesnt work - remove @ApplocationScope annotation,
    // for it must work without this axplicit annotation as long as we have
    // only one our custom scope in the app.
}

@Retention(AnnotationRetention.RUNTIME)
@Qualifier
annotation class ApplicationScope