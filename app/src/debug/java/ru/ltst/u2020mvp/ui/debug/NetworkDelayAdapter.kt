package ru.ltst.u2020mvp.ui.debug

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import ru.ltst.u2020mvp.ui.misc.BindableAdapter

internal class NetworkDelayAdapter(context: Context) : BindableAdapter<Long>(context) {

    override fun getCount(): Int {
        return VALUES.size
    }

    override fun getItem(position: Int): Long? {
        return VALUES[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View? {
        return inflater.inflate(android.R.layout.simple_spinner_item, container, false)
    }

    override fun bindView(item: Long?, position: Int, view: View) {
        val tv = view.findViewById(android.R.id.text1) as TextView
        tv.text = String.format("%dms", item ?: 0)
    }

    override fun newDropDownView(inflater: LayoutInflater, position: Int, container: ViewGroup): View? {
        return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)
    }

    companion object {
        private val VALUES = longArrayOf(250, 500, 1000, 2000, 3000, 5000)

        fun getPositionForValue(value: Long): Int {
            for (i in VALUES.indices) {
                if (VALUES[i] == value) {
                    return i
                }
            }
            return 3 // Default to 2000 if something changes.
        }
    }
}
