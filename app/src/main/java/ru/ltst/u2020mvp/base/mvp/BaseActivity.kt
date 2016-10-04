package ru.ltst.u2020mvp.base.mvp

import android.content.Intent
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v7.app.AppCompatActivity
import ru.ltst.u2020mvp.U2020App
import ru.ltst.u2020mvp.U2020Component
import ru.ltst.u2020mvp.network.NetworkReceiver
import java.util.*
import javax.inject.Inject

abstract class BaseActivity : AppCompatActivity() {

    @Inject
    lateinit var viewContainer: ViewContainer

    lateinit private var uniqueKey: String

    private var mNetworkReceiver: NetworkReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val params = intent.extras
        if (params != null) {
            onExtractParams(params)
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(BF_UNIQUE_KEY)) {
            uniqueKey = savedInstanceState.getString(BF_UNIQUE_KEY)
        } else {
            uniqueKey = UUID.randomUUID().toString()
        }

        super.onCreate(savedInstanceState)

        val app = U2020App.get(this)
        onCreateComponent(app.component())
        Registry.add(this, viewId(), presenter())
        val layoutInflater = layoutInflater
        val container = viewContainer.forActivity(this)
        layoutInflater.inflate(layoutId(), container)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BF_UNIQUE_KEY, uniqueKey)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        uniqueKey = savedInstanceState.getString(BF_UNIQUE_KEY)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter().onActivityResult(requestCode, resultCode, data)
    }

    protected fun onExtractParams(params: Bundle) {
        // default no implementation
    }

    fun uniqueKey(): String {
        return uniqueKey
    }

    override fun onStart() {
        super.onStart()
        mNetworkReceiver = NetworkReceiver({ isConnected ->
            presenter().onNetworkConnectionStateChanged(isConnected)
        })
        mNetworkReceiver!!.register(this)
    }

    override fun onStop() {
        mNetworkReceiver!!.unregister(this)
        mNetworkReceiver = null
        super.onStop()
    }

    /**
     * Must be implemented by derived activities. Injection must be performed here.
     * Otherwise IllegalStateException will be thrown. Derived activity is
     * responsible to create and store it's getComponent.

     * @param u2020Component application level getComponent
     */
    protected abstract fun onCreateComponent(u2020Component: U2020Component)

    @LayoutRes
    protected abstract fun layoutId(): Int

    protected abstract fun presenter(): BasePresenter<out BaseView>

    @IdRes
    protected abstract fun viewId(): Int

    companion object {

        private val BF_UNIQUE_KEY = BaseActivity::class.java.name + ".unique.key"
    }
}
