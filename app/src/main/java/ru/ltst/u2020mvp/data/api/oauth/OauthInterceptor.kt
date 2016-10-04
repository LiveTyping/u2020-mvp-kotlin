package ru.ltst.u2020mvp.data.api.oauth

import com.f2prateek.rx.preferences.Preference
import okhttp3.Interceptor
import okhttp3.Response
import ru.ltst.u2020mvp.ApplicationScope
import java.io.IOException
import javax.inject.Inject

@ApplicationScope
class OauthInterceptor
@Inject
constructor(@AccessToken private val accessToken: Preference<String>) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = chain.request().newBuilder()

        if (accessToken.isSet) {
            builder.header("Authorization", "token " + accessToken.get()!!)
        }

        return chain.proceed(builder.build())
    }
}
