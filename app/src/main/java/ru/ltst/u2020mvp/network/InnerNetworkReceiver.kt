package ru.ltst.u2020mvp.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import ru.ltst.u2020mvp.util.*

class InnerNetworkReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //        boolean hasConnection = NetworkUtils.hasConnection(context);
        //        if (hasConnection) {
        NetworkReceiver.sendIntent(context)
        //        }
    }
}
