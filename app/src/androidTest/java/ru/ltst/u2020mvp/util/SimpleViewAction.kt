package ru.ltst.u2020mvp.util

import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.view.View

import org.hamcrest.CoreMatchers
import org.hamcrest.Matcher

abstract class SimpleViewAction<V : View> : ViewAction {
    override fun getConstraints(): Matcher<View> {
        return CoreMatchers.instanceOf<View>(View::class.java)
    }

    override fun getDescription(): String {
        return "Simple view action"
    }

    @Suppress("UNCHECKED_CAST")
    override fun perform(uiController: UiController, view: View) {
        call(uiController, view as V)
    }

    protected abstract fun call(uiController: UiController, view: V)
}
