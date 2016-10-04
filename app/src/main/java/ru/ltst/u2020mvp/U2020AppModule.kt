package ru.ltst.u2020mvp

import android.app.Application

import dagger.Module
import dagger.Provides

@Module
class U2020AppModule(private val app: U2020App) {

    @Provides
    @ApplicationScope
    fun provideApplication(): Application {
        return app
    }
}
