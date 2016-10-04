package ru.ltst.u2020mvp.data

import android.app.Application
import com.f2prateek.rx.preferences.Preference
import com.f2prateek.rx.preferences.RxSharedPreferences
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.mock.NetworkBehavior
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.IsInstrumentationTest
import ru.ltst.u2020mvp.data.api.DebugApiModule
import ru.ltst.u2020mvp.data.prefs.InetSocketAddressPreferenceAdapter
import timber.log.Timber
import java.net.InetSocketAddress
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

@Module(includes = arrayOf(DataModule::class, DebugApiModule::class))
class DebugDataModule {

    @Provides
    @ApplicationScope
    fun provideIntentFactory(@IsMockMode isMockMode: Boolean,
                                      @CaptureIntents captureIntents: Preference<Boolean>): IntentFactory {
        return DebugIntentFactory(IntentFactory.REAL, isMockMode, captureIntents)
    }

    @Provides
    @ApplicationScope
    fun provideOkHttpClient(app: Application,
                                     networkProxyAddress: Preference<InetSocketAddress>): OkHttpClient {
        return DataModule.createOkHttpClient(app).sslSocketFactory(createBadSslSocketFactory()).proxy(InetSocketAddressPreferenceAdapter.createProxy(networkProxyAddress.get())).build()
    }

    @Provides
    @ApplicationScope
    @ApiEndpoint
    fun provideEndpointPreference(prefs: RxSharedPreferences): Preference<String> {
        return prefs.getString("debug_endpoint", ApiEndpoints.MOCK_MODE.url)
    }

    @Provides
    @ApplicationScope
    @IsMockMode
    fun provideIsMockMode(@ApiEndpoint endpoint: Preference<String>,
                                   @IsInstrumentationTest isInstrumentationTest: Boolean): Boolean {
        // Running in an instrumentation forces mock mode.
        return isInstrumentationTest || ApiEndpoints.isMockMode(endpoint.get()!!)
    }

    @Provides
    @ApplicationScope
    @NetworkDelay
    fun provideNetworkDelay(prefs: RxSharedPreferences): Preference<Long> {
        return prefs.getLong("debug_network_delay", 2000L)
    }

    @Provides
    @ApplicationScope
    @NetworkFailurePercent
    fun provideNetworkFailurePercent(prefs: RxSharedPreferences): Preference<Int> {
        return prefs.getInteger("debug_network_failure_percent", 3)
    }

    @Provides
    @ApplicationScope
    @NetworkVariancePercent
    fun provideNetworkVariancePercent(prefs: RxSharedPreferences): Preference<Int> {
        return prefs.getInteger("debug_network_variance_percent", 40)
    }

    @Provides
    @ApplicationScope
    fun provideNetworkProxyAddress(preferences: RxSharedPreferences): Preference<InetSocketAddress> {
        return preferences.getObject("debug_network_proxy", InetSocketAddressPreferenceAdapter.INSTANCE)
    }

    @Provides
    @ApplicationScope
    @CaptureIntents
    fun provideCaptureIntentsPreference(prefs: RxSharedPreferences): Preference<Boolean> {
        return prefs.getBoolean("debug_capture_intents", DEFAULT_CAPTURE_INTENTS)
    }

    @Provides
    @ApplicationScope
    @AnimationSpeed
    fun provideAnimationSpeed(prefs: RxSharedPreferences): Preference<Int> {
        return prefs.getInteger("debug_animation_speed", DEFAULT_ANIMATION_SPEED)
    }

    @Provides
    @ApplicationScope
    @PicassoDebugging
    fun providePicassoDebugging(prefs: RxSharedPreferences): Preference<Boolean> {
        return prefs.getBoolean("debug_picasso_debugging", DEFAULT_PICASSO_DEBUGGING)
    }

    @Provides
    @ApplicationScope
    @PixelGridEnabled
    fun providePixelGridEnabled(prefs: RxSharedPreferences): Preference<Boolean> {
        return prefs.getBoolean("debug_pixel_grid_enabled", DEFAULT_PIXEL_GRID_ENABLED)
    }

    @Provides
    @ApplicationScope
    @PixelRatioEnabled
    fun providePixelRatioEnabled(prefs: RxSharedPreferences): Preference<Boolean> {
        return prefs.getBoolean("debug_pixel_ratio_enabled", DEFAULT_PIXEL_RATIO_ENABLED)
    }

    @Provides
    @ApplicationScope
    @SeenDebugDrawer
    fun provideSeenDebugDrawer(prefs: RxSharedPreferences): Preference<Boolean> {
        return prefs.getBoolean("debug_seen_debug_drawer", DEFAULT_SEEN_DEBUG_DRAWER)
    }

    @Provides
    @ApplicationScope
    @ScalpelEnabled
    fun provideScalpelEnabled(prefs: RxSharedPreferences): Preference<Boolean> {
        return prefs.getBoolean("debug_scalpel_enabled", DEFAULT_SCALPEL_ENABLED)
    }

    @Provides
    @ApplicationScope
    @ScalpelWireframeEnabled
    fun provideScalpelWireframeEnabled(prefs: RxSharedPreferences): Preference<Boolean> {
        return prefs.getBoolean("debug_scalpel_wireframe_drawer", DEFAULT_SCALPEL_WIREFRAME_ENABLED)
    }

    @Provides
    @ApplicationScope
    fun providePicasso(client: OkHttpClient, behavior: NetworkBehavior,
                                @IsMockMode isMockMode: Boolean, app: Application): Picasso {
        val builder = Picasso.Builder(app).downloader(OkHttp3Downloader(client))
        if (isMockMode) {
            builder.addRequestHandler(MockRequestHandler(behavior, app.assets))
        }
        builder.listener { picasso, uri, exception -> Timber.e(exception, "Error while loading image %s", uri) }
        return builder.build()
    }

    companion object {

        private val DEFAULT_ANIMATION_SPEED = 1 // 1x (normal) speed.
        private val DEFAULT_PICASSO_DEBUGGING = false // Debug indicators displayed
        private val DEFAULT_PIXEL_GRID_ENABLED = false // No pixel grid overlay.
        private val DEFAULT_PIXEL_RATIO_ENABLED = false // No pixel ratio overlay.
        private val DEFAULT_SCALPEL_ENABLED = false // No crazy 3D view tree.
        private val DEFAULT_SCALPEL_WIREFRAME_ENABLED = false // Draw views by
        private val DEFAULT_SEEN_DEBUG_DRAWER = false // Show debug drawer first time.
        private val DEFAULT_CAPTURE_INTENTS = true // Capture external intents.

        private fun createBadSslSocketFactory(): SSLSocketFactory {
            try {
                // Construct SSLSocketFactory that accepts any cert.
                val context = SSLContext.getInstance("TLS")
                val permissive = object : X509TrustManager {
                    @Throws(CertificateException::class)
                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    @Throws(CertificateException::class)
                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate?> {
                        return arrayOfNulls(0)
                    }
                }
                context.init(null, arrayOf<TrustManager>(permissive), null)
                return context.socketFactory
            } catch (e: Exception) {
                throw AssertionError(e)
            }

        }
    }
}
