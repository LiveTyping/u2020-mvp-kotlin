package ru.ltst.u2020mvp.util


import android.os.Handler
import android.os.Looper
import android.support.test.espresso.IdlingResource

class TimerIdlingResource : IdlingResource {
    private var resourceCallback: IdlingResource.ResourceCallback? = null
    private var idle = true
    private val handler = Handler(Looper.getMainLooper())

    fun scheduleTimeout(timer: Long) {
        idle = false
        handler.postDelayed({
            idle = true
            if (null != resourceCallback) {
                resourceCallback!!.onTransitionToIdle()
            }
        }, timer)
    }

    override fun getName(): String {
        return "CloseKeyboardIdlingResource"
    }

    override fun isIdleNow(): Boolean {
        return idle
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        resourceCallback = callback
        if (idle) {
            resourceCallback!!.onTransitionToIdle()
        }
    }
}
