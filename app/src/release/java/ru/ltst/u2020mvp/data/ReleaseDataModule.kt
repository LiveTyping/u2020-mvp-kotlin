package ru.ltst.u2020mvp.data

import android.app.Application
import android.net.Uri

import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso

import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.data.api.ReleaseApiModule
import timber.log.Timber

@Module(includes = arrayOf(DataModule::class, ReleaseApiModule::class))
class ReleaseDataModule {

    @Provides
    @ApplicationScope
    internal fun provideOkHttpClient(app: Application): OkHttpClient {
        return DataModule.createOkHttpClient(app).build()
    }

    @Provides
    @ApplicationScope
    internal fun providePicasso(app: Application, client: OkHttpClient): Picasso {
        return Picasso.Builder(app)
                .downloader(OkHttp3Downloader(client))
                .listener { picasso, uri, e -> Timber.e(e, "Failed to load image: %s", uri) }
                .build()
    }

    @Provides
    @ApplicationScope
    internal fun provideIntentFactory(): IntentFactory {
        return IntentFactory.REAL
    }
}
