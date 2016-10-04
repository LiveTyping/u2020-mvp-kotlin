package ru.ltst.u2020mvp.base.navigation.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.view.ViewCompat
import android.view.View

import ru.ltst.u2020mvp.base.navigation.Screen

abstract class ActivityScreen : Screen {

    private var transitionView: View? = null

    fun attachTransitionView(view: View?) {
        transitionView = view
    }

    protected fun detachTransitionView(): View? {
        val view = transitionView
        transitionView = null
        return view
    }

    fun intent(context: Context): Intent {
        val intent = Intent(context, activityClass())
        configureIntent(intent)
        return intent
    }

    fun activityOptions(activity: Activity): Bundle? {
        val transitionView = detachTransitionView() ?: return null
        return ActivityOptionsCompat.makeSceneTransitionAnimation(activity, transitionView, BF_TRANSITION_VIEW).toBundle()
    }

    protected abstract fun configureIntent(intent: Intent)
    protected abstract fun activityClass(): Class<out Activity>

    companion object {

        private val BF_TRANSITION_VIEW = "ActivityScreen.transitionView"

        fun setTransitionView(view: View) {
            ViewCompat.setTransitionName(view, BF_TRANSITION_VIEW)
        }
    }

}
