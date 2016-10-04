package ru.ltst.u2020mvp.base.navigation.activity

import android.content.Intent

abstract class NoParamsActivityScreen : ActivityScreen() {
    override fun configureIntent(intent: Intent) {
        // empty implementation
    }
}
