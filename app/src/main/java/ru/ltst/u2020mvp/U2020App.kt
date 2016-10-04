package ru.ltst.u2020mvp

import android.app.Application
import android.content.Context

import com.jakewharton.threetenabp.AndroidThreeTen
import com.squareup.leakcanary.LeakCanary

import javax.inject.Inject

import ru.ltst.u2020mvp.base.mvp.ActivityHierarchyServer
import ru.ltst.u2020mvp.network.NetworkReceiver
import timber.log.Timber

import timber.log.Timber.DebugTree

open class U2020App : Application() {
    open lateinit protected var component: U2020Component

    @Inject
    lateinit var activityHierarchyServer: ActivityHierarchyServer

    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        //        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(DebugTree())
        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }

        buildComponentAndInject()

        registerActivityLifecycleCallbacks(activityHierarchyServer)
    }

    open fun buildComponentAndInject() {
        component = U2020Component.Initializer.init(this)
        component.inject(this)
    }

    open fun component(): U2020Component {
        return component
    }

    companion object {

        operator fun get(context: Context): U2020App {
            return context.applicationContext as U2020App
        }
    }
}
