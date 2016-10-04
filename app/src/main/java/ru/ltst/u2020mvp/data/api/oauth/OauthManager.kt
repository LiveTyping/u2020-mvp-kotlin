package ru.ltst.u2020mvp.data.api.oauth

import android.content.Intent
import android.net.Uri
import com.f2prateek.rx.preferences.Preference
import com.squareup.moshi.Moshi
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.data.IntentFactory
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@ApplicationScope
class OauthManager
@Inject
constructor(private val intentFactory: IntentFactory, private val client: OkHttpClient, private val moshi: Moshi,
            @AccessToken private val accessToken: Preference<String>) {

    fun createLoginIntent(): Intent {
        val authorizeUrl = HttpUrl.parse("https://github.com/login/oauth/authorize") //
                .newBuilder() //
                .addQueryParameter("client_id", CLIENT_ID) //
                .build()

        return intentFactory.createUrlIntent(authorizeUrl.toString())
    }

    fun handleResult(data: Uri?) {
        if (data == null) return

        val code = data.getQueryParameter("code") ?: return

        try {
            // Trade our code for an access token.
            val request = Request.Builder() //
                    .url("https://github.com/login/oauth/access_token") //
                    .header("Accept", "application/json") //
                    .post(FormBody.Builder() //
                            .add("client_id", CLIENT_ID) //
                            .add("client_secret", CLIENT_SECRET) //
                            .add("code", code) //
                            .build()) //
                    .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val accessTokenResponse = moshi.adapter(AccessTokenResponse::class.java).fromJson(response.body().string())
                if (accessTokenResponse != null && accessTokenResponse.access_token != null) {
                    accessToken.set(accessTokenResponse.access_token)
                }
            }
        } catch (e: IOException) {
            Timber.w(e, "Failed to get access token.")
        }

    }

    private class AccessTokenResponse private constructor(val access_token: String?, scope: String)

    companion object {
        private val CLIENT_ID = "5793abe5bcb6d90f0240"
        private val CLIENT_SECRET = "81a35659c60fc376629432a51fd81e5c66a8dace"
    }
}
