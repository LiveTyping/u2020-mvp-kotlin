package ru.ltst.u2020mvp.ui.debug

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.f2prateek.rx.preferences.Preference

import java.net.InetSocketAddress

import ru.ltst.u2020mvp.ui.misc.BindableAdapter

internal class ProxyAdapter(context: Context,
                            private val proxyAddress: Preference<InetSocketAddress>?) :
        BindableAdapter<String>(context) {

    init {
        if (proxyAddress == null) {
            throw IllegalStateException("proxy == null")
        }
    }

    override fun getCount(): Int {
        return 2 /* "None" and "Set" */ + if (proxyAddress?.isSet ?: false) 1 else 0
    }

    override fun getItem(position: Int): String? {
        if (position == 0) {
            return "None"
        }
        if (position == count - 1) {
            return "Set…"
        }
        return proxyAddress?.get().toString()
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View? {
        return inflater.inflate(android.R.layout.simple_spinner_item, container, false)
    }

    override fun bindView(item: String?, position: Int, view: View) {
        val tv = view.findViewById(android.R.id.text1) as TextView
        tv.text = item
    }

    override fun newDropDownView(inflater: LayoutInflater, position: Int, container: ViewGroup): View? {
        return inflater.inflate(android.R.layout.simple_spinner_dropdown_item, container, false)
    }

    companion object {
        val NONE = 0
        val PROXY = 1
    }
}

