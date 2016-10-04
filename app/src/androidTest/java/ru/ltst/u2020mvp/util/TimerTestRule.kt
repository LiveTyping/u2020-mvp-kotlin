package ru.ltst.u2020mvp.util


import android.support.test.espresso.Espresso

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class TimerTestRule : TestRule {
    private val resource: TimerIdlingResource

    init {
        resource = TimerIdlingResource()
    }

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                Espresso.registerIdlingResources(resource)
                base.evaluate()
                Espresso.unregisterIdlingResources(resource)
            }
        }
    }

    fun scheduleTimeout(millis: Long) {
        resource.scheduleTimeout(millis)
    }
}
