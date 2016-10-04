package ru.ltst.u2020mvp.data.api.oauth

import android.app.IntentService
import android.content.Intent
import javax.inject.Inject

class OauthService : IntentService(OauthService::class.java.simpleName) {
    @Inject
    lateinit var oauthManager: OauthManager

    override fun onHandleIntent(intent: Intent?) {
        oauthManager.handleResult(intent!!.data)
    }
}
