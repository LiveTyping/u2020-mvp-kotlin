package ru.ltst.u2020mvp.ui.debug

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.f2prateek.rx.preferences.Preference
import com.jakewharton.rxbinding.widget.RxAdapterView
import com.squareup.leakcanary.internal.DisplayLeakActivity
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import retrofit2.mock.NetworkBehavior
import ru.ltst.u2020mvp.BuildConfig
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.U2020App
import ru.ltst.u2020mvp.data.ApiEndpoints
import ru.ltst.u2020mvp.data.LumberYard
import ru.ltst.u2020mvp.data.api.mock.MockGithubService
import ru.ltst.u2020mvp.data.api.mock.MockRepositoriesResponse
import ru.ltst.u2020mvp.data.prefs.InetSocketAddressPreferenceAdapter
import ru.ltst.u2020mvp.ui.logs.LogsDialog
import ru.ltst.u2020mvp.ui.misc.EnumAdapter
import ru.ltst.u2020mvp.ui.misc.bindView
import ru.ltst.u2020mvp.util.isBlank
import ru.ltst.u2020mvp.util.showKeyboard
import ru.ltst.u2020mvp.util.truncateAt
import rx.functions.Func1
import timber.log.Timber
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.TimeUnit.MILLISECONDS
import javax.inject.Inject
import javax.inject.Named

class DebugView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    val contextualTitleView: View by bindView(R.id.debug_contextual_title)
    val contextualListView: LinearLayout by bindView(R.id.debug_contextual_list)
    val endpointView: Spinner by bindView(R.id.debug_network_endpoint)
    val endpointEditView: View by bindView(R.id.debug_network_endpoint_edit)
    val networkDelayView: Spinner by bindView(R.id.debug_network_delay)
    val networkVarianceView: Spinner by bindView(R.id.debug_network_variance)
    val networkErrorView: Spinner by bindView(R.id.debug_network_error)
    val networkProxyView: Spinner by bindView(R.id.debug_network_proxy)
    val networkLoggingView: Spinner by bindView(R.id.debug_network_logging)

    val captureIntentsView: Switch by bindView(R.id.debug_capture_intents)
    val repositoriesResponseView: Spinner by bindView(R.id.debug_repositories_response)

    val uiAnimationSpeedView: Spinner by bindView(R.id.debug_ui_animation_speed)
    val uiPixelGridView: Switch by bindView(R.id.debug_ui_pixel_grid)
    val uiPixelRatioView: Switch by bindView(R.id.debug_ui_pixel_ratio)
    val uiScalpelView: Switch by bindView(R.id.debug_ui_scalpel)
    val uiScalpelWireframeView: Switch by bindView(R.id.debug_ui_scalpel_wireframe)
    val buildNameView: TextView by bindView(R.id.debug_build_name)

    val buildCodeView: TextView by bindView(R.id.debug_build_code)
    val buildShaView: TextView by bindView(R.id.debug_build_sha)
    val buildDateView: TextView by bindView(R.id.debug_build_date)
    val deviceMakeView: TextView by bindView(R.id.debug_device_make)

    val deviceModelView: TextView by bindView(R.id.debug_device_model)
    val deviceResolutionView: TextView by bindView(R.id.debug_device_resolution)
    val deviceDensityView: TextView by bindView(R.id.debug_device_density)
    val deviceReleaseView: TextView by bindView(R.id.debug_device_release)
    val deviceApiView: TextView by bindView(R.id.debug_device_api)
    val picassoIndicatorView: Switch by bindView(R.id.debug_picasso_indicators)

    val picassoCacheSizeView: TextView by bindView(R.id.debug_picasso_cache_size)
    val picassoCacheHitView: TextView by bindView(R.id.debug_picasso_cache_hit)
    val picassoCacheMissView: TextView by bindView(R.id.debug_picasso_cache_miss)
    val picassoDecodedView: TextView by bindView(R.id.debug_picasso_decoded)
    val picassoDecodedTotalView: TextView by bindView(R.id.debug_picasso_decoded_total)
    val picassoDecodedAvgView: TextView by bindView(R.id.debug_picasso_decoded_avg)
    val picassoTransformedView: TextView by bindView(R.id.debug_picasso_transformed)
    val picassoTransformedTotalView: TextView by bindView(R.id.debug_picasso_transformed_total)
    val picassoTransformedAvgView: TextView by bindView(R.id.debug_picasso_transformed_avg)
    val okHttpCacheMaxSizeView: TextView by bindView(R.id.debug_okhttp_cache_max_size)

    val okHttpCacheWriteErrorView: TextView by bindView(R.id.debug_okhttp_cache_write_error)
    val okHttpCacheRequestCountView: TextView by bindView(R.id.debug_okhttp_cache_request_count)
    val okHttpCacheNetworkCountView: TextView by bindView(R.id.debug_okhttp_cache_network_count)
    val okHttpCacheHitCountView: TextView by bindView(R.id.debug_okhttp_cache_hit_count)

    val showLogs : View by bindView(R.id.debug_logs_show)
    val showLeaks : View by bindView(R.id.debug_leaks_show)
    val editEndpoint : View by bindView(R.id.debug_network_endpoint_edit)

    @Inject
    lateinit var client: OkHttpClient
    @Inject
    @Named("Api")
    lateinit var apiClient: OkHttpClient
    @Inject
    lateinit var picasso: Picasso
    @Inject
    lateinit var lumberYard: LumberYard
    var isMockMode: Boolean = false
    var networkEndpoint: Preference<String>
    @Inject
    lateinit var networkProxyAddress: Preference<InetSocketAddress>
    var captureIntents: Preference<Boolean>
    var animationSpeed: Preference<Int>
    var picassoDebugging: Preference<Boolean>
    var pixelGridEnabled: Preference<Boolean>
    var pixelRatioEnabled: Preference<Boolean>
    var scalpelEnabled: Preference<Boolean>
    var scalpelWireframeEnabled: Preference<Boolean>
    @Inject
    lateinit var behavior: NetworkBehavior
    var networkDelay: Preference<Long>
    var networkFailurePercent: Preference<Int>
    var networkVariancePercent: Preference<Int>
    @Inject
    lateinit var mockGithubService: MockGithubService
    @Inject
    lateinit var app: Application

    val contextualDebugActions: ContextualDebugActions

    init {
        val component = U2020App[context].component();
        component.inject(this)
        isMockMode = component.isMockMode()
        networkEndpoint = component.apiEndpoint()
        captureIntents = component.captureIntents()
        animationSpeed = component.animationSpeed()
        picassoDebugging = component.picassoDebugging()
        pixelGridEnabled = component.pixelGridEnabled()
        pixelRatioEnabled = component.pixelRatioEnabled()
        scalpelEnabled = component.scalpelEnabled()
        scalpelWireframeEnabled = component.scalpelWireframeEnabled()
        networkDelay = component.networkDelay()
        networkFailurePercent = component.networkFailurePercent()
        networkVariancePercent = component.networkVariancePercent()

        // Inflate all of the controls and inject them.
        LayoutInflater.from(context).inflate(R.layout.debug_view_content, this)

        val debugActions = emptySet<ContextualDebugActions.DebugAction<in View>>()
        contextualDebugActions = ContextualDebugActions(this, debugActions)

        setupNetworkSection()
        setupMockBehaviorSection()
        setupUserInterfaceSection()
        setupBuildSection()
        setupDeviceSection()
        setupPicassoSection()
        setupOkHttpCacheSection()
        setupLogsSection()
    }

    fun onDrawerOpened() {
        refreshPicassoStats()
        refreshOkHttpCacheStats()
    }

    private fun setupLogsSection() {
        showLogs.setOnClickListener { showLogs() }
        showLeaks.setOnClickListener { showLeaks() }
    }

    private fun setupNetworkSection() {
        editEndpoint.setOnClickListener { onEditEndpointClicked() }
        val currentEndpoint = ApiEndpoints.from(networkEndpoint.get())
        val endpointAdapter = EnumAdapter(context, ApiEndpoints::class.java)
        endpointView.adapter = endpointAdapter
        endpointView.setSelection(currentEndpoint.ordinal)
        RxAdapterView.itemSelections(endpointView).map<ApiEndpoints>(Func1<Int, ApiEndpoints> { endpointAdapter.getItem(it) }).filter { item -> item !== currentEndpoint }.subscribe { selected ->
            if (selected === ApiEndpoints.CUSTOM) {
                Timber.d("Custom network endpoint selected. Prompting for URL.")
                showCustomEndpointDialog(currentEndpoint.ordinal, "http://")
            } else {
                setEndpointAndRelaunch(selected.url)
            }
        }

        val delayAdapter = NetworkDelayAdapter(context)
        networkDelayView.adapter = delayAdapter
        networkDelayView.setSelection(NetworkDelayAdapter.getPositionForValue(behavior.delay(MILLISECONDS)))
        RxAdapterView.itemSelections(networkDelayView).map<Long>(Func1<Int, Long> { delayAdapter.getItem(it) }).filter { item -> item !== behavior.delay(MILLISECONDS) }.subscribe { selected ->
            Timber.d("Setting network delay to %sms", selected)
            behavior.setDelay(selected, MILLISECONDS)
            networkDelay.set(selected)
        }

        val varianceAdapter = NetworkVarianceAdapter(context)
        networkVarianceView.adapter = varianceAdapter
        networkVarianceView.setSelection(NetworkVarianceAdapter.getPositionForValue(behavior.variancePercent()))

        RxAdapterView.itemSelections(networkVarianceView).map<Int>(Func1<Int, Int> { varianceAdapter.getItem(it) }).filter { item -> item !== behavior.variancePercent() }.subscribe { selected ->
            Timber.d("Setting network variance to %s%%", selected)
            behavior.setVariancePercent(selected)
            networkVariancePercent.set(selected)
        }

        val errorAdapter = NetworkErrorAdapter(context)
        networkErrorView.adapter = errorAdapter
        networkErrorView.setSelection(NetworkErrorAdapter.getPositionForValue(behavior.failurePercent()))
        RxAdapterView.itemSelections(networkErrorView).map<Int>(Func1<Int, Int> { errorAdapter.getItem(it) }).filter { item -> item !== behavior.failurePercent() }.subscribe { selected ->
            Timber.d("Setting network error to %s%%", selected)
            behavior.setFailurePercent(selected)
            networkFailurePercent.set(selected)
        }

        val currentProxyPosition = if (networkProxyAddress.isSet) ProxyAdapter.PROXY else ProxyAdapter.NONE
        val proxyAdapter = ProxyAdapter(context, networkProxyAddress)
        networkProxyView.adapter = proxyAdapter
        networkProxyView.setSelection(currentProxyPosition)

        RxAdapterView.itemSelections(networkProxyView).filter { position -> !networkProxyAddress.isSet || position !== ProxyAdapter.PROXY }.subscribe { position ->
            if (position === ProxyAdapter.NONE) {
                // Only clear the proxy and restart if one was previously set.
                if (currentProxyPosition != ProxyAdapter.NONE) {
                    Timber.d("Clearing network proxy")
                    // TODO: Keep the custom proxy around so you can easily switch back and forth.
                    networkProxyAddress.delete()
                    // Force a restart to re-initialize the app without a proxy.
                    ProcessPhoenix.triggerRebirth(context)
                }
            } else if (networkProxyAddress.isSet && position === ProxyAdapter.PROXY) {
                Timber.d("Ignoring re-selection of network proxy %s", networkProxyAddress.get())
            } else {
                Timber.d("New network proxy selected. Prompting for host.")
                showNewNetworkProxyDialog(proxyAdapter)
            }
        }

        // Only show the endpoint editor when a custom endpoint is in use.
        endpointEditView.visibility = if (currentEndpoint === ApiEndpoints.CUSTOM) View.VISIBLE else View.GONE

        if (currentEndpoint === ApiEndpoints.MOCK_MODE) {
            // Disable network proxy if we are in mock mode.
            networkProxyView.isEnabled = false
            networkLoggingView.isEnabled = false
        } else {
            // Disable network controls if we are not in mock mode.
            networkDelayView.isEnabled = false
            networkVarianceView.isEnabled = false
            networkErrorView.isEnabled = false
        }

        // We use the JSON rest adapter as the source of truth for the log level.
        //        final EnumAdapter<RestAdapter.LogLevel> loggingAdapter =
        //                new EnumAdapter<>(getContext(), RestAdapter.LogLevel.class);
        //        networkLoggingView.setAdapter(loggingAdapter);
        //        networkLoggingView.setSelection(restAdapter.getLogLevel().ordinal());
        //        networkLoggingView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        //            @Override
        //            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        //                RestAdapter.LogLevel selected = loggingAdapter.getItem(position);
        //                if (selected != restAdapter.getLogLevel()) {
        //                    Timber.d("Setting logging level to %s", selected);
        //                    restAdapter.setLogLevel(selected);
        //                } else {
        //                    Timber.d("Ignoring re-selection of logging level " + selected);
        //                }
        //            }
        //
        //            @Override
        //            public void onNothingSelected(AdapterView<?> adapterView) {
        //            }
        //        });
    }

    internal fun onEditEndpointClicked() {
        Timber.d("Prompting to edit custom endpoint URL.")
        // Pass in the currently selected position since we are merely editing.
        showCustomEndpointDialog(endpointView.selectedItemPosition, networkEndpoint?.get() ?: "")
    }

    private fun setupMockBehaviorSection() {
        captureIntentsView.isEnabled = isMockMode
        captureIntentsView.isChecked = captureIntents.get() ?: false
        captureIntentsView.setOnCheckedChangeListener { compoundButton, b ->
            Timber.d("Capture intents set to %s", b)
            captureIntents.set(b)
        }

        configureResponseSpinner(repositoriesResponseView, MockRepositoriesResponse::class.java)
    }

    /**
     * Populates a `Spinner` with the values of an `enum` and binds it to the value set
     * in
     * the mock service.
     */
    private fun <T : Enum<T>> configureResponseSpinner(spinner: Spinner,
                                                       responseClass: Class<T>) {
        val adapter = EnumAdapter(context, responseClass)
        spinner.isEnabled = isMockMode
        spinner.adapter = adapter
        spinner.setSelection(mockGithubService.getResponse(responseClass).ordinal)

        RxAdapterView.itemSelections(spinner).map<T>(Func1<Int, T> { adapter.getItem(it) }).filter { item -> item !== mockGithubService.getResponse(responseClass) }.subscribe { selected ->
            Timber.d("Setting %s to %s", responseClass.simpleName, selected)
            mockGithubService.setResponse(responseClass, selected)
            ProcessPhoenix.triggerRebirth(context)
        }
    }

    private fun setupUserInterfaceSection() {
        val speedAdapter = AnimationSpeedAdapter(context)
        uiAnimationSpeedView.adapter = speedAdapter
        val animationSpeedValue = animationSpeed.get() ?: 1
        uiAnimationSpeedView.setSelection(AnimationSpeedAdapter.getPositionForValue(animationSpeedValue))

        RxAdapterView.itemSelections(uiAnimationSpeedView).map<Int>(Func1<Int, Int> { speedAdapter.getItem(it) }).filter { item -> item !== animationSpeed.get() }.subscribe { selected ->
            Timber.d("Setting animation speed to %sx", selected)
            animationSpeed.set(selected)
            applyAnimationSpeed(selected)
        }

        // Ensure the animation speed value is always applied across app restarts.
        post { applyAnimationSpeed(animationSpeedValue) }

        val gridEnabled = pixelGridEnabled.get() ?: false
        uiPixelGridView.isChecked = gridEnabled
        uiPixelRatioView.isEnabled = gridEnabled
        uiPixelGridView.setOnCheckedChangeListener { buttonView, isChecked ->
            Timber.d("Setting pixel grid overlay enabled to %b", isChecked)
            pixelGridEnabled.set(isChecked)
            uiPixelRatioView.isEnabled = isChecked
        }

        uiPixelRatioView.isChecked = pixelRatioEnabled.get() ?: false
        uiPixelRatioView.setOnCheckedChangeListener { buttonView, isChecked ->
            Timber.d("Setting pixel scale overlay enabled to %b", isChecked)
            pixelRatioEnabled.set(isChecked)
        }

        uiScalpelView.isChecked = scalpelEnabled.get() ?: false
        uiScalpelWireframeView.isEnabled = scalpelEnabled.get() ?: false
        uiScalpelView.setOnCheckedChangeListener { buttonView, isChecked ->
            Timber.d("Setting scalpel interaction enabled to %b", isChecked)
            scalpelEnabled.set(isChecked)
            uiScalpelWireframeView.isEnabled = isChecked
        }

        uiScalpelWireframeView.isChecked = scalpelWireframeEnabled.get() ?: false
        uiScalpelWireframeView.setOnCheckedChangeListener { buttonView, isChecked ->
            Timber.d("Setting scalpel wireframe enabled to %b", isChecked)
            scalpelWireframeEnabled.set(isChecked)
        }
    }

    internal fun showLogs() {
        LogsDialog(ContextThemeWrapper(context, R.style.Theme_U2020), lumberYard).show()
    }

    internal fun showLeaks() {
        val intent = Intent(context, DisplayLeakActivity::class.java)
        context.startActivity(intent)
    }

    private fun setupBuildSection() {
        buildNameView.text = BuildConfig.VERSION_NAME
        buildCodeView.text = BuildConfig.VERSION_CODE.toString()
        buildShaView.text = BuildConfig.GIT_SHA

        val buildTime = Instant.ofEpochSecond(BuildConfig.GIT_TIMESTAMP)
        buildDateView.text = DATE_DISPLAY_FORMAT.format(buildTime)
    }

    private fun setupDeviceSection() {
        val displayMetrics = context.resources.displayMetrics
        val densityBucket = getDensityString(displayMetrics)
        deviceMakeView.text = Build.MANUFACTURER.truncateAt(20)
        deviceModelView.text = Build.MODEL.truncateAt(20)
//        deviceResolutionView.setText(displayMetrics.heightPixels + "x" + displayMetrics.widthPixels)
//        deviceDensityView.setText(displayMetrics.densityDpi + "dpi (" + densityBucket + ")")
        deviceReleaseView.text = Build.VERSION.RELEASE
        deviceApiView.text = Build.VERSION.SDK_INT.toString()
    }

    private fun setupPicassoSection() {
        val picassoDebuggingValue = picassoDebugging.get() ?: false
        picasso.setIndicatorsEnabled(picassoDebuggingValue)
        picassoIndicatorView.isChecked = picassoDebuggingValue
        picassoIndicatorView.setOnCheckedChangeListener { button, isChecked ->
            Timber.d("Setting Picasso debugging to %b", isChecked)
            picasso.setIndicatorsEnabled(isChecked)
            picassoDebugging.set(isChecked)
        }

        refreshPicassoStats()
    }

    private fun refreshPicassoStats() {
        val snapshot = picasso.snapshot
        val size = getSizeString(snapshot.size.toLong())
        val total = getSizeString(snapshot.maxSize.toLong())
        val percentage = (1f * snapshot.size / snapshot.maxSize * 100).toInt()
        picassoCacheSizeView.text = "$size / $total ($percentage%)"
        picassoCacheHitView.text = snapshot.cacheHits.toString()
        picassoCacheMissView.text = snapshot.cacheMisses.toString()
        picassoDecodedView.text = snapshot.originalBitmapCount.toString()
        picassoDecodedTotalView.text = getSizeString(snapshot.totalOriginalBitmapSize)
        picassoDecodedAvgView.text = getSizeString(snapshot.averageOriginalBitmapSize)
        picassoTransformedView.text = snapshot.transformedBitmapCount.toString()
        picassoTransformedTotalView.text = getSizeString(snapshot.totalTransformedBitmapSize)
        picassoTransformedAvgView.text = getSizeString(snapshot.averageTransformedBitmapSize)
    }

    private fun setupOkHttpCacheSection() {
        val cache = client.cache() // Shares the cache with apiClient, so no need to check both.
        okHttpCacheMaxSizeView.text = getSizeString(cache.maxSize())

        refreshOkHttpCacheStats()
    }

    private fun refreshOkHttpCacheStats() {
        val cache = client.cache() // Shares the cache with apiClient, so no need to check both.
        val writeTotal = cache.writeSuccessCount() + cache.writeAbortCount()
        val percentage = (1f * cache.writeAbortCount() / writeTotal * 100).toInt()
//        okHttpCacheWriteErrorView.setText(
//                cache.writeAbortCount() + " / " + writeTotal + " (" + percentage + "%)")
        okHttpCacheRequestCountView.text = cache.requestCount().toString()
        okHttpCacheNetworkCountView.text = cache.networkCount().toString()
        okHttpCacheHitCountView.text = cache.hitCount().toString()
    }

    private fun applyAnimationSpeed(multiplier: Int) {
        try {
            val method = ValueAnimator::class.java.getDeclaredMethod("setDurationScale", java.lang.Float.TYPE)
            method.invoke(null, multiplier.toFloat())
        } catch (e: Exception) {
            throw RuntimeException("Unable to apply animation speed.", e)
        }

    }

    private fun showNewNetworkProxyDialog(proxyAdapter: ProxyAdapter) {
        val originalSelection = if (networkProxyAddress.isSet) ProxyAdapter.PROXY else ProxyAdapter.NONE

        val view = LayoutInflater.from(app).inflate(R.layout.debug_drawer_network_proxy, null)
        val hostView = view.findViewById(R.id.debug_drawer_network_proxy_host) as EditText

        if (networkProxyAddress.isSet) {
            val host = networkProxyAddress.get()?.hostName
            hostView.setText(host) // Set the current host.
            hostView.setSelection(0, host?.length ?: 0) // Pre-select it for editing.

            // Show the keyboard. Post this to the next frame when the dialog has been attached.
            hostView.post { hostView.showKeyboard() }
        }

        android.support.v7.app.AlertDialog.Builder(context) //
                .setTitle("Set Network Proxy").setView(view).setNegativeButton("Cancel") { dialog, i ->
            networkProxyView.setSelection(originalSelection)
            dialog.cancel()
        }.setPositiveButton("Use") { dialog, i ->
            val `in` = hostView.text.toString()
            val address = InetSocketAddressPreferenceAdapter.parse(`in`)
            if (address != null) {
                networkProxyAddress.set(address)
                // Force a restart to re-initialize the app with the new proxy.
                ProcessPhoenix.triggerRebirth(context)
            } else {
                networkProxyView.setSelection(originalSelection)
            }
        }.setOnCancelListener { dialogInterface -> networkProxyView.setSelection(originalSelection) }.show()
    }

    private fun showCustomEndpointDialog(originalSelection: Int, defaultUrl: String) {
        val view = LayoutInflater.from(context).inflate(R.layout.debug_drawer_network_endpoint, null)
        val url = view.findViewById(R.id.debug_drawer_network_endpoint_url) as EditText
        url.setText(defaultUrl)
        url.setSelection(url.length())

        AlertDialog.Builder(context) //
                .setTitle("Set Network Endpoint").setView(view).setNegativeButton("Cancel") { dialog, i ->
            endpointView.setSelection(originalSelection)
            dialog.cancel()
        }.setPositiveButton("Use") { dialog, i ->
            val theUrl = url.text.toString()
            if (!theUrl.isBlank()) {
                setEndpointAndRelaunch(theUrl)
            } else {
                endpointView.setSelection(originalSelection)
            }
        }.setOnCancelListener { dialogInterface -> endpointView.setSelection(originalSelection) }.show()
    }

    private fun setEndpointAndRelaunch(endpoint: String) {
        Timber.d("Setting network endpoint to %s", endpoint)
        networkEndpoint.set(endpoint)

        ProcessPhoenix.triggerRebirth(context)
    }

    companion object {
        private val DATE_DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a", Locale.US).withZone(ZoneId.systemDefault())

        private fun getDensityString(displayMetrics: DisplayMetrics): String {
            when (displayMetrics.densityDpi) {
                DisplayMetrics.DENSITY_LOW -> return "ldpi"
                DisplayMetrics.DENSITY_MEDIUM -> return "mdpi"
                DisplayMetrics.DENSITY_HIGH -> return "hdpi"
                DisplayMetrics.DENSITY_XHIGH -> return "xhdpi"
                DisplayMetrics.DENSITY_XXHIGH -> return "xxhdpi"
                DisplayMetrics.DENSITY_XXXHIGH -> return "xxxhdpi"
                DisplayMetrics.DENSITY_TV -> return "tvdpi"
                else -> return displayMetrics.densityDpi.toString()
            }
        }

        private fun getSizeString(bytes: Long): String {
            var bytes = bytes
            val units = arrayOf("B", "KB", "MB", "GB")
            var unit = 0
            while (bytes >= 1024) {
                bytes /= 1024
                unit += 1
            }
            return String.format("%d%s", bytes, units[unit])
        }
    }
}
