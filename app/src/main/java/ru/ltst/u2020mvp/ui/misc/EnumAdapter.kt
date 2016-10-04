package ru.ltst.u2020mvp.ui.misc

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

open class EnumAdapter<T : Enum<T>> @JvmOverloads constructor(context: Context, enumType: Class<T>,
                                                              private val showNull: Boolean = false)
        : BindableAdapter<T>(context) {
    private val enumConstants: Array<T>
    private val nullOffset: Int

    init {
        this.enumConstants = enumType.enumConstants
        this.nullOffset = if (showNull) 1 else 0
    }

    override fun getCount(): Int {
        return enumConstants.size + nullOffset
    }

    override fun getItem(position: Int): T? {
        if (showNull && position == 0) {
            return null
        }

        return enumConstants[position - nullOffset]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View {
        return inflater.inflate(android.R.layout.simple_spinner_item, container, false)
    }

    override fun bindView(item: T?, position: Int, view: View) {
        val tv = view.findViewById(android.R.id.text1) as TextView
        tv.text = getName(item)
    }

    override fun newDropDownView(inflater: LayoutInflater, position: Int, container: ViewGroup): View {
        return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)
    }

    protected fun getName(item: T?): String {
        return item.toString()
    }
}
