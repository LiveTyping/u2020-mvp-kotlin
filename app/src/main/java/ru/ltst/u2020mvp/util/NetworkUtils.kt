@file:JvmName(name = "NetworkUtils")

package ru.ltst.u2020mvp.util

import android.content.Context
import android.net.ConnectivityManager

fun Context.hasConnection(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo = cm.activeNetworkInfo
    return networkInfo != null && networkInfo.isConnectedOrConnecting
}

fun Context.hasWifiConnection(): Boolean {
    val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected
}
