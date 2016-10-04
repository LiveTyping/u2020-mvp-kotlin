package ru.ltst.u2020mvp.base


import android.support.test.InstrumentationRegistry

import ru.ltst.u2020mvp.TestU2020Application
import ru.ltst.u2020mvp.TestU2020Component

abstract class BaseTest {
    protected val app: TestU2020Application
        get() = InstrumentationRegistry.getTargetContext().applicationContext as TestU2020Application

    protected val testComponent: TestU2020Component
        get() = app.component() as TestU2020Component
}
