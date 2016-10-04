package ru.ltst.u2020mvp.ui.misc

import android.content.res.Resources
import android.support.annotation.ColorRes
import android.support.annotation.DimenRes
import android.support.v4.content.ContextCompat
import android.view.View
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

fun View.bindDimen(@DimenRes id: Int)
        : ReadOnlyProperty<View, Float> = required(id, dimenFinder)

fun View.bindColor(@ColorRes id: Int)
        : ReadOnlyProperty<View, Int> = required(id, colorFinder)

private val View.colorFinder: View.(Int) -> Int?
    get() = { ContextCompat.getColor(context, it)}

private val View.dimenFinder: View.(Int) -> Float?
    get() = {resources.getDimension(it)}

private fun resourceNotFound(id: Int, desc: KProperty<*>) : Nothing
        = throw Resources.NotFoundException()

@Suppress("UNCHECKED_CAST", "FINAL_UPPER_BOUND")
private fun <T, I> required(id : Int, finder: T.(Int) -> I?)
        = Lazy {t : T, desc -> t.finder(id) ?: resourceNotFound(id, desc)}

// Like Kotlin's lazy delegate but the initializer gets the target and metadata passed to it
class Lazy<T, V>(private val initializer: (T, KProperty<*>) -> V) : ReadOnlyProperty<T, V> {
    private object EMPTY
    private var value: Any? = EMPTY

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        if (value == EMPTY) {
            value = initializer(thisRef, property)
        }
        @Suppress("UNCHECKED_CAST")
        return value as V
    }
}