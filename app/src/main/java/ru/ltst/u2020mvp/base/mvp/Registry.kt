package ru.ltst.u2020mvp.base.mvp

import android.app.Activity
import android.os.Bundle
import android.support.annotation.IdRes
import ru.ltst.u2020mvp.util.Strings
import timber.log.Timber
import java.util.*

object Registry {
    private val registers = HashMap<String, Entry<BaseView>>()
    val SERVER: ActivityHierarchyServer.Empty = object : ActivityHierarchyServer.Empty() {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            val key = getKey(activity)
            val entry = registers[key]
            Timber.d("%s onActivityCreated", key)
            if (entry != null && entry.presenter != null && savedInstanceState != null) {
                entry.presenter.onRestore(savedInstanceState)
            }
        }

        @SuppressWarnings("unchecked")
        override fun onActivityStarted(activity: Activity) {
            val key = getKey(activity)
            val entry = registers[key]
            Timber.d("%s onActivityStarted", key)
            if (entry != null && entry.presenter != null) {
                val view = activity.findViewById(entry.viewId) as BaseView
                entry.presenter.takeView(view)
            }
        }

        @SuppressWarnings("unchecked")
        override fun onActivityStopped(activity: Activity) {
            val key = getKey(activity)
            val entry = registers[key]
            Timber.d("%s onActivityStopped", key)
            if (entry != null && entry.presenter != null) {
                val view = activity.findViewById(entry.viewId) as BaseView
                entry.presenter.dropView(view)
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            val key = getKey(activity)
            val entry = registers[key]
            Timber.d("%s onActivitySaveInstanceState", key)
            if (entry != null && entry.presenter != null) {
                entry.presenter.onSave(outState)
            }
        }

        override fun onActivityDestroyed(activity: Activity) {
            val key = getKey(activity)
            Timber.d("%s onActivityDestroyed", key)
            registers.remove(key)
        }
    }

    fun <V : BaseView> add(activity: Activity, @IdRes viewId: Int, presenter: BasePresenter<V>) {
        registers.put(getKey(activity), Entry(viewId, presenter) as Entry<BaseView>)
    }

    private fun getKey(activity: Activity): String {
        val builder = StringBuilder()
        builder.append(activity.javaClass.name)
        if (activity is BaseActivity) {
            val uniqueKey = activity.uniqueKey()
            if (!uniqueKey.isEmpty()) {
                builder.append(Strings.DOT).append(uniqueKey)
            }
        } else {
            val action = activity.intent.action
            if (action != null) {
                builder.append(Strings.DOT).append(action)
            }
            val data = activity.intent.data
            if (data != null) {
                builder.append(Strings.DOT).append(data.toString())
            }
            val extras = activity.intent.extras
            if (extras != null) {
                for (key in extras.keySet()) {
                    val value = extras.get(key) ?: continue
                    val valueString: String
                    if (value.javaClass.isArray) {
                        valueString = Arrays.toString(value as Array<Any>)
                    } else {
                        valueString = value.toString()
                    }

                    builder.append(Strings.DOT)
                    builder.append(key).append(Strings.COLON)
                    builder.append(valueString)
                }
            }
        }
        return builder.toString()
    }

    private class Entry<V : BaseView>(@IdRes
                                      val viewId: Int, val presenter: BasePresenter<V>?)
}
