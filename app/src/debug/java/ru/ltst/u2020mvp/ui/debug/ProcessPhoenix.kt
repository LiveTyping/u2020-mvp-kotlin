/*
 * Copyright (C) 2014 Jake Wharton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.ltst.u2020mvp.ui.debug

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.*
import android.os.Bundle

/**
 * Process Phoenix facilitates restarting your application process. This should only be used for
 * things like fundamental state changes in your debug builds (e.g., changing from staging to
 * production).
 *
 *
 * To use, add the following to your `AndroidManifest.xml`:
 * `&lt;activity
 * android:name=&quot;com.jakewharton.processphoenix.ProcessPhoenix&quot;
 * android:theme=&quot;@android:style/Theme.Translucent.NoTitleBar&quot;
 * android:process=&quot;:phoenix&quot;
 * /&gt;
` *
 * Trigger process recreation by calling [ProcessPhoenix.triggerRebirth()][.triggerRebirth]
 * with a [Context] instance.
 */
class ProcessPhoenix : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent.getParcelableExtra<Intent>(KEY_RESTART_INTENT)
        startActivity(intent)

        Runtime.getRuntime().exit(0) // Kill kill kill!
    }

    companion object {
        private val KEY_RESTART_INTENT = "phoenix_restart_intent"

        /**
         * Call to restart the application process using the specified intent.
         *
         *
         * Behavior of the current process after invoking this method is undefined.
         */
        @JvmOverloads @JvmStatic fun triggerRebirth(context: Context, nextIntent: Intent = getRestartIntent(context)) {
            val intent = Intent(context, ProcessPhoenix::class.java)
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK) // In case we are called with non-Activity context.
            intent.putExtra(KEY_RESTART_INTENT, nextIntent)
            context.startActivity(intent)

            Runtime.getRuntime().exit(0) // Kill kill kill!
        }

        private fun getRestartIntent(context: Context): Intent {
            val defaultIntent = Intent(ACTION_MAIN, null)
            defaultIntent.addFlags(FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK)
            defaultIntent.addCategory(CATEGORY_DEFAULT)

            val packageName = context.packageName
            val packageManager = context.packageManager
            for (resolveInfo in packageManager.queryIntentActivities(defaultIntent, 0)) {
                val activityInfo = resolveInfo.activityInfo
                if (activityInfo.packageName == packageName) {
                    defaultIntent.component = ComponentName(packageName, activityInfo.name)
                    return defaultIntent
                }
            }

            throw IllegalStateException("Unable to determine default activity for "
                    + packageName
                    + ". Does an activity specify the DEFAULT category?")
        }
    }
}
/**
 * Call to restart the application process using the [default][Intent.CATEGORY_DEFAULT]
 * activity as an intent.
 *
 *
 * Behavior of the current process after invoking this method is undefined.
 */
