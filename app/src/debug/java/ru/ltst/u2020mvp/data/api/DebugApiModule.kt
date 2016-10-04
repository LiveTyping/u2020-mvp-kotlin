package ru.ltst.u2020mvp.data.api

import com.f2prateek.rx.preferences.Preference
import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.mock.MockRetrofit
import retrofit2.mock.NetworkBehavior
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.data.*
import ru.ltst.u2020mvp.data.api.mock.MockGithubService
import ru.ltst.u2020mvp.data.api.oauth.OauthInterceptor
import timber.log.Timber
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Named

@Module
class DebugApiModule {
    @Provides
    @ApplicationScope
    fun provideHttpUrl(@ApiEndpoint apiEndpoint: Preference<String>): HttpUrl {
        return HttpUrl.parse(apiEndpoint.get()!!)
    }

    @Provides
    @ApplicationScope
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor { message -> Timber.tag("OkHttp").v(message) }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.HEADERS
        return loggingInterceptor
    }

    @Provides
    @ApplicationScope
    @Named("Api")
    fun provideApiClient(client: OkHttpClient,
                                  oauthInterceptor: OauthInterceptor,
                                  loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return ApiModule.createApiClient(client, oauthInterceptor).addInterceptor(loggingInterceptor).build()
    }

    @Provides
    @ApplicationScope
    fun provideBehavior(@NetworkDelay networkDelay: Preference<Long>,
                                 @NetworkFailurePercent networkFailurePercent: Preference<Int>,
                                 @NetworkVariancePercent networkVariancePercent: Preference<Int>): NetworkBehavior {
        val behavior = NetworkBehavior.create()
        behavior.setDelay(networkDelay.get()!!, MILLISECONDS)
        behavior.setFailurePercent(networkFailurePercent.get()!!)
        behavior.setVariancePercent(networkVariancePercent.get()!!)
        return behavior
    }

    @Provides
    @ApplicationScope
    fun provideMockRetrofit(retrofit: Retrofit,
                                     behavior: NetworkBehavior): MockRetrofit {
        return MockRetrofit.Builder(retrofit).networkBehavior(behavior).build()
    }

    @Provides
    @ApplicationScope
    fun provideGithubService(retrofit: Retrofit,
                                      @IsMockMode isMockMode: Boolean, mockService: MockGithubService): GithubService {
        return if (isMockMode) mockService else retrofit.create(GithubService::class.java)
    }
}
