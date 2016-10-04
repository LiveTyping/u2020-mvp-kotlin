package ru.ltst.u2020mvp.data

import android.app.Application
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.f2prateek.rx.preferences.Preference
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.OkHttpClient
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.data.api.ApiModule
import ru.ltst.u2020mvp.data.api.oauth.AccessToken
import java.io.File
import java.util.concurrent.TimeUnit

@Module(includes = arrayOf(ApiModule::class))
class DataModule {

    @Provides
    @ApplicationScope
    fun provideSharedPreferences(app: Application): SharedPreferences {
        return app.getSharedPreferences("u2020", MODE_PRIVATE)
    }

    @Provides
    @ApplicationScope
    fun provideClock(): Clock {
        return Clock.REAL
    }

    //    @Provides
    //    @ApplicationScope
    //    IntentFactory provideIntentFactory() {
    //        return IntentFactory.REAL;
    //    }

    @Provides
    @ApplicationScope
    fun provideRxSharedPreferences(prefs: SharedPreferences): RxSharedPreferences {
        return RxSharedPreferences.create(prefs)
    }

    @Provides
    @ApplicationScope
    @AccessToken
    fun provideAccessToken(prefs: RxSharedPreferences): Preference<String> {
        return prefs.getString("access-token")
    }

    @Provides
    @ApplicationScope
    fun provideMoshi(): Moshi {
        return Moshi.Builder().add(InstantAdapter()).build()
    }

    companion object {
        val DISK_CACHE_SIZE = 50 * 1024 * 1024 // 50MB

        fun createOkHttpClient(app: Application): OkHttpClient.Builder {
            // Install an HTTP cache in the application cache directory.
            val cacheDir = File(app.cacheDir, "http")
            val cache = Cache(cacheDir, DISK_CACHE_SIZE.toLong())
            return OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).cache(cache)

        }
    }
}
