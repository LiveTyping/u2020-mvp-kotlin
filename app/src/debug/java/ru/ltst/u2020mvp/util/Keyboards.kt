@file:JvmName("Keyboards")

package ru.ltst.u2020mvp.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

fun View.showKeyboard() {
    context.getInputManager().showSoftInput(this, 0)
}

private fun Context.getInputManager(): InputMethodManager {
    return getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
}
