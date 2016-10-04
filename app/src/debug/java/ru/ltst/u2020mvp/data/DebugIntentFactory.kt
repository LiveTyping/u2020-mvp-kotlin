package ru.ltst.u2020mvp.data

import android.content.Intent

import com.f2prateek.rx.preferences.Preference

import ru.ltst.u2020mvp.ui.ExternalIntentActivity

/**
 * An [IntentFactory] implementation that wraps all `Intent`s with a debug action, which
 * launches an activity that allows you to inspect the content.
 */
class DebugIntentFactory(private val realIntentFactory: IntentFactory, private val isMockMode: Boolean,
                         private val captureIntents: Preference<Boolean>) : IntentFactory {

    override fun createUrlIntent(url: String): Intent {
        val baseIntent = realIntentFactory.createUrlIntent(url)
        if (!isMockMode || (!captureIntents.get()!!)) {
            return baseIntent
        } else {
            return ExternalIntentActivity.createIntent(baseIntent)
        }
    }
}
