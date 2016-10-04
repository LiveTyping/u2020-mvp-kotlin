package ru.ltst.u2020mvp

import dagger.Module
import dagger.Provides

@Module
class DebugU2020Module {

    @Provides
    @ApplicationScope
    @IsInstrumentationTest
    fun provideIsInstrumentationTest(): Boolean {
        return false
    }
}
