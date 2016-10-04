package ru.ltst.u2020mvp.data.api

import com.squareup.moshi.Moshi

import javax.inject.Named
import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.data.api.oauth.OauthInterceptor

@Module
class ApiModule {

    //    @Provides
    //    @ApplicationScope
    //    HttpUrl provideBaseUrl() {
    //        return PRODUCTION_API_URL;
    //    }

    //    @Provides
    //    @ApplicationScope
    //    @Named("Api")
    //    OkHttpClient provideApiClient(OkHttpClient client,
    //                                  OauthInterceptor oauthInterceptor) {
    //        return createApiClient(client, oauthInterceptor).build();
    //    }


    //    @Provides
    //    @ApplicationScope
    //    GithubService provideGithubService(Retrofit retrofit) {
    //        return retrofit.create(GithubService.class);
    //    }

    @Provides
    @ApplicationScope
    fun provideRetrofit(baseUrl: HttpUrl, @Named("Api") client: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder().client(client).baseUrl(baseUrl).addConverterFactory(MoshiConverterFactory.create(moshi)).addCallAdapterFactory(RxJavaCallAdapterFactory.create()).build()
    }

    companion object {
        val PRODUCTION_API_URL = HttpUrl.parse("https://api.github.com/")


        fun createApiClient(client: OkHttpClient, oauthInterceptor: OauthInterceptor): OkHttpClient.Builder {
            return client.newBuilder().addInterceptor(oauthInterceptor)
        }
    }

}
