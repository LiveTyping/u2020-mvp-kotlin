package ru.ltst.u2020mvp.data.api

import javax.inject.Named

import dagger.Module
import dagger.Provides

import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit

import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.data.api.oauth.OauthInterceptor

@Module(includes = arrayOf(ApiModule::class))
class ReleaseApiModule {

    @Provides
    @ApplicationScope
    internal fun provideHttpUrl(): HttpUrl {
        return HttpUrl.parse(ApiModule.PRODUCTION_API_URL.toString())
    }


    @Provides
    @ApplicationScope
    internal fun provideGithubService(retrofit: Retrofit): GithubService {
        return retrofit.create(GithubService::class.java)
    }

    @Provides
    @ApplicationScope
    @Named("Api")
    internal fun provideApiClient(client: OkHttpClient,
                                  oauthInterceptor: OauthInterceptor): OkHttpClient {
        return ApiModule.createApiClient(client, oauthInterceptor).build()
    }
}
