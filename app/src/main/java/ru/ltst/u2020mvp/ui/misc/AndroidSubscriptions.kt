package ru.ltst.u2020mvp.ui.misc

import android.os.Looper

import rx.Scheduler
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action0
import rx.subscriptions.Subscriptions

class AndroidSubscriptions private constructor() {

    init {
        throw AssertionError("No instances")
    }

    companion object {

        /**
         * Create a [Subscription] that always runs the specified `unsubscribe` on the
         * UI thread.
         */

        fun unsubscribeInUiThread(function : () -> Unit): Subscription {
            return Subscriptions.create {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    function()
                } else {
                    val inner = AndroidSchedulers.mainThread().createWorker()
                    inner.schedule {
                        function()
                        inner.unsubscribe()
                    }
                }
            }
        }
    }
}
