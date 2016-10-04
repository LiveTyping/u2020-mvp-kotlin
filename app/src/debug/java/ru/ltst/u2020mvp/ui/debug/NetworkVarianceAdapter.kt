package ru.ltst.u2020mvp.ui.debug

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import ru.ltst.u2020mvp.ui.misc.BindableAdapter

internal class NetworkVarianceAdapter(context: Context) : BindableAdapter<Int>(context) {

    override fun getCount(): Int {
        return VALUES.size
    }

    override fun getItem(position: Int): Int? {
        return VALUES[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View? {
        return inflater.inflate(android.R.layout.simple_spinner_item, container, false)
    }

    override fun bindView(item: Int?, position: Int, view: View) {
        val tv = view.findViewById(android.R.id.text1) as TextView
        tv.text = "±$item%"
    }

    override fun newDropDownView(inflater: LayoutInflater, position: Int, container: ViewGroup): View? {
        return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)
    }

    companion object {
        private val VALUES = intArrayOf(20, 40, 60)

        fun getPositionForValue(value: Int): Int {
            for (i in VALUES.indices) {
                if (VALUES[i] == value) {
                    return i
                }
            }
            return 1 // Default to 40% if something changes.
        }
    }
}
