package ru.ltst.u2020mvp.base.navigation.activity

import android.app.Activity
import android.content.Intent
import android.support.v4.app.ActivityCompat

import java.security.InvalidParameterException

import ru.ltst.u2020mvp.base.ActivityConnector
import ru.ltst.u2020mvp.base.navigation.Screen
import ru.ltst.u2020mvp.base.navigation.ScreenSwitcher

class ActivityScreenSwitcher : ActivityConnector<Activity>(), ScreenSwitcher {

    override fun open(screen: Screen) {
        val activity = attachedObject ?: return
        if (screen is ActivityScreen) {
            val intent = screen.intent(activity)
            ActivityCompat.startActivity(activity, intent, screen.activityOptions(activity))
        } else {
            throw InvalidParameterException("Only ActivityScreen objects allowed")
        }
    }

    override fun goBack() {
        val activity = attachedObject
        activity?.onBackPressed()
    }
}
