package ru.ltst.u2020mvp.util


import android.support.test.espresso.IdlingResource

import rx.Observable
import rx.functions.Action0

class ObservableIdlingResource<T>(private val observable: Observable<T>) : IdlingResource {
    private var callback: IdlingResource.ResourceCallback? = null
    private var isIdle: Boolean = false

    fun observe(): Observable<T> {
        isIdle = false
        return observable.doAfterTerminate(IdlingAction())
    }

    override fun getName(): String {
        return this.javaClass.name + hashCode()
    }

    override fun isIdleNow(): Boolean {
        return isIdle
    }

    override fun registerIdleTransitionCallback(resourceCallback: IdlingResource.ResourceCallback) {
        callback = resourceCallback
    }

    private fun notifyIdle() {
        if (callback != null) {
            callback!!.onTransitionToIdle()
        }
    }

    private inner class IdlingAction : Action0 {
        override fun call() {
            isIdle = true
            notifyIdle()
        }
    }
}
