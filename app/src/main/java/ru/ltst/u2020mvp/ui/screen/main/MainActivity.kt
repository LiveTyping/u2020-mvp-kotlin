package ru.ltst.u2020mvp.ui.screen.main

import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Window
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.U2020Component
import ru.ltst.u2020mvp.base.HasComponent
import ru.ltst.u2020mvp.base.mvp.BaseActivity
import ru.ltst.u2020mvp.base.mvp.BasePresenter
import ru.ltst.u2020mvp.base.mvp.BaseView
import ru.ltst.u2020mvp.base.navigation.activity.ActivityScreenSwitcher
import ru.ltst.u2020mvp.data.api.oauth.OauthService
import javax.inject.Inject

class MainActivity : BaseActivity(), HasComponent<MainComponent> {
    override val component: MainComponent
        get() = mainComponent
    @Inject
    lateinit var presenter: MainPresenter
    @Inject
    lateinit var activityScreenSwitcher: ActivityScreenSwitcher

    lateinit private var mainComponent: MainComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Remove the status bar color. The DrawerLayout is responsible for drawing it from now on.
            setStatusBarColor(window)
        }
    }

    override fun onCreateComponent(u2020Component: U2020Component) {
        mainComponent = DaggerMainComponent
                .builder()
                .u2020Component(u2020Component)
                .build()
        mainComponent.inject(this)
    }

    override fun onStart() {
        super.onStart()
        activityScreenSwitcher.attach(this)
    }

    override fun onStop() {
        activityScreenSwitcher.detach(this)
        super.onStop()
    }

    override fun layoutId(): Int {
        return R.layout.main_activity
    }

    override fun presenter(): BasePresenter<out BaseView> {
        return presenter
    }

    override fun viewId(): Int {
        return R.id.main_drawer_layout
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val data = intent.data ?: return
        if ("u2020" == data.scheme) {
            val serviceIntent = Intent(this, OauthService::class.java)
            serviceIntent.data = data
            startService(serviceIntent)
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun setStatusBarColor(window: Window) {
        window.statusBarColor = Color.TRANSPARENT
    }
}
