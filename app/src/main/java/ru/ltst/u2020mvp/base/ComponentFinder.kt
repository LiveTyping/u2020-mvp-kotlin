package ru.ltst.u2020mvp.base

import android.content.Context

object ComponentFinder {

    @Suppress("UNCHECKED_CAST")
    fun <C> findActivityComponent(context: Context): C {
        return (context as HasComponent<C>).component
    }
}
