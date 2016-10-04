package ru.ltst.u2020mvp.data

import android.content.Intent
import android.net.Uri

/** Creates [Intent]s for launching into external applications.  */
interface IntentFactory {
    fun createUrlIntent(url: String): Intent

    companion object {
        val REAL = object : IntentFactory {
            override fun createUrlIntent(url: String) : Intent {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(url)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                return intent
            }
        }
    }
}
