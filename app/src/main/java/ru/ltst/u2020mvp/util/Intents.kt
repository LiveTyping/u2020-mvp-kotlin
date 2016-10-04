package ru.ltst.u2020mvp.util

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.widget.Toast

import ru.ltst.u2020mvp.R

import android.widget.Toast.LENGTH_LONG

class Intents private constructor() {

    init {
        throw AssertionError("No instances.")
    }

    companion object {
        /**
         * Attempt to launch the supplied [Intent]. Queries on-device packages before launching and
         * will display a simple message if none are available to handle it.
         */
        fun maybeStartActivity(context: Context, intent: Intent): Boolean {
            return maybeStartActivity(context, intent, false)
        }

        /**
         * Attempt to launch Android's chooser for the supplied [Intent]. Queries on-device
         * packages before launching and will display a simple message if none are available to handle
         * it.
         */
        fun maybeStartChooser(context: Context, intent: Intent): Boolean {
            return maybeStartActivity(context, intent, true)
        }

        private fun maybeStartActivity(context: Context, intent: Intent, chooser: Boolean): Boolean {
            var intent = intent
            if (hasHandler(context, intent)) {
                if (chooser) {
                    intent = Intent.createChooser(intent, null)
                }
                context.startActivity(intent)
                return true
            } else {
                Toast.makeText(context, R.string.no_intent_handler, LENGTH_LONG).show()
                return false
            }
        }

        /**
         * Queries on-device packages for a handler for the supplied [Intent].
         */
        private fun hasHandler(context: Context, intent: Intent): Boolean {
            val handlers = context.packageManager.queryIntentActivities(intent, 0)
            return !handlers.isEmpty()
        }
    }
}