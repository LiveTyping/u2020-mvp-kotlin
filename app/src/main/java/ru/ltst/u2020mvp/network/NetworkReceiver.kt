package ru.ltst.u2020mvp.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.support.v4.content.LocalBroadcastManager

import java.util.Arrays

import ru.ltst.u2020mvp.util.*

class NetworkReceiver(vararg actions: (Boolean) -> Unit) : BroadcastReceiver() {

    private val mActions: List<(Boolean) -> Unit>

    init {
        this.mActions = Arrays.asList(*actions)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val hasConnection = context.hasConnection()
        for (action in mActions) {
            action(hasConnection)
        }
    }

    fun register(context: Context) {
        val intent = IntentFilter(ACTION)
        LocalBroadcastManager.getInstance(context).registerReceiver(this, intent)
    }

    fun unregister(context: Context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this)
    }

    companion object {

        val ACTION = NetworkReceiver::class.java.name + ".ACTION"

        fun sendIntent(context: Context) {
            val intent = Intent(ACTION)
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
        }
    }
}
