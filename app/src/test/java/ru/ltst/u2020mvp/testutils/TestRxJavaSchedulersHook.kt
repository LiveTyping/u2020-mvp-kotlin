package ru.ltst.u2020mvp.testutils

import rx.Scheduler
import rx.plugins.RxJavaSchedulersHook
import rx.schedulers.Schedulers

class TestRxJavaSchedulersHook : RxJavaSchedulersHook() {
    override fun getComputationScheduler(): Scheduler {
        return Schedulers.immediate()
    }

    override fun getIOScheduler(): Scheduler {
        return Schedulers.immediate()
    }

    override fun getNewThreadScheduler(): Scheduler {
        return Schedulers.immediate()
    }
}