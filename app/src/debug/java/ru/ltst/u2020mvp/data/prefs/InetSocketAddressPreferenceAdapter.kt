package ru.ltst.u2020mvp.data.prefs

import android.content.SharedPreferences
import com.f2prateek.rx.preferences.Preference
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.Proxy.Type.HTTP

class InetSocketAddressPreferenceAdapter internal constructor() : Preference.Adapter<InetSocketAddress> {

    override fun set(key: String, address: InetSocketAddress,
                     editor: SharedPreferences.Editor) {
        var host: String? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            host = address.hostString
        } else {
            host = address.hostName
        }
        val port = address.port
        editor.putString(key, host + ":" + port)
    }

    override fun get(key: String, preferences: SharedPreferences): InetSocketAddress {
        val value = preferences.getString(key, null)!!
// Not called unless value is present.
        val parts = value.split(":".toRegex(), 2).toTypedArray()
        val host = parts[0]
        val port = if (parts.size > 1) Integer.parseInt(parts[1]) else 80
        return InetSocketAddress.createUnresolved(host, port)
    }

    companion object {
        val INSTANCE = InetSocketAddressPreferenceAdapter()

        fun parse(value: String?): InetSocketAddress? {
            if (value.isNullOrBlank()) {
                return null
            }
            val parts = value!!.split(":".toRegex(), 2).toTypedArray()
            if (parts.size == 0) {
                return null
            }
            val host = parts[0]
            val port = if (parts.size > 1) Integer.parseInt(parts[1]) else 80
            return InetSocketAddress.createUnresolved(host, port)
        }

        fun createProxy(address: InetSocketAddress?): Proxy? {
            if (address == null) {
                return null
            }
            return Proxy(HTTP, address)
        }
    }
}
