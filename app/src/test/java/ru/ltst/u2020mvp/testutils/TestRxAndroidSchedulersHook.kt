package ru.ltst.u2020mvp.testutils

import rx.Scheduler
import rx.android.plugins.RxAndroidSchedulersHook
import rx.schedulers.Schedulers

class TestRxAndroidSchedulersHook : RxAndroidSchedulersHook() {
    override fun getMainThreadScheduler(): Scheduler {
        return Schedulers.immediate()
    }
}