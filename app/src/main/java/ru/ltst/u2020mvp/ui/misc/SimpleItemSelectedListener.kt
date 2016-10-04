package ru.ltst.u2020mvp.ui.misc

import android.view.View
import android.widget.AdapterView

abstract class SimpleItemSelectedListener : AdapterView.OnItemSelectedListener {
    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        onItemSelected(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
    }

    abstract fun onItemSelected(position: Int)
}
