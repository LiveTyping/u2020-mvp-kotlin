package ru.ltst.u2020mvp.base.mvp

import android.app.Activity
import android.app.Application
import android.os.Bundle

import java.util.ArrayList

/**
 * A "view server" adaptation which automatically hooks itself up to all activities.
 */
interface ActivityHierarchyServer : Application.ActivityLifecycleCallbacks {

    class Proxy : ActivityHierarchyServer {
        private val servers = ArrayList<ActivityHierarchyServer>()

        fun addServer(server: ActivityHierarchyServer) {
            servers.add(server)
        }

        fun removeServer(server: ActivityHierarchyServer) {
            servers.remove(server)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            for (server in servers) {
                server.onActivityCreated(activity, savedInstanceState)
            }
        }

        override fun onActivityStarted(activity: Activity) {
            for (server in servers) {
                server.onActivityStarted(activity)
            }
        }

        override fun onActivityResumed(activity: Activity) {
            for (server in servers) {
                server.onActivityResumed(activity)
            }
        }

        override fun onActivityPaused(activity: Activity) {
            for (server in servers) {
                server.onActivityPaused(activity)
            }
        }

        override fun onActivityStopped(activity: Activity) {
            for (server in servers) {
                server.onActivityStopped(activity)
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            for (server in servers) {
                server.onActivitySaveInstanceState(activity, outState)
            }
        }

        override fun onActivityDestroyed(activity: Activity) {
            for (server in servers) {
                server.onActivityDestroyed(activity)
            }
        }
    }

    open class Empty : ActivityHierarchyServer {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

        }

        override fun onActivityStarted(activity: Activity) {

        }

        override fun onActivityResumed(activity: Activity) {

        }

        override fun onActivityPaused(activity: Activity) {

        }

        override fun onActivityStopped(activity: Activity) {

        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {

        }
    }

    companion object {
        /**
         * An [ActivityHierarchyServer] which does nothing.
         */
        val NONE: ActivityHierarchyServer = object : ActivityHierarchyServer {
            override fun onActivityCreated(activity: Activity, bundle: Bundle) {
            }

            override fun onActivityStarted(activity: Activity) {
            }

            override fun onActivityResumed(activity: Activity) {
            }

            override fun onActivityPaused(activity: Activity) {
            }

            override fun onActivityStopped(activity: Activity) {
            }

            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {
            }

            override fun onActivityDestroyed(activity: Activity) {
            }
        }
    }
}
