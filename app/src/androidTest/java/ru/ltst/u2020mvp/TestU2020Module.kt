package ru.ltst.u2020mvp

import dagger.Module
import dagger.Provides
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.IsInstrumentationTest

@Module
class TestU2020Module {
    @Provides
    @ApplicationScope
    @IsInstrumentationTest
    fun provideIsInstrumentationTest(): Boolean {
        return true
    }
}
