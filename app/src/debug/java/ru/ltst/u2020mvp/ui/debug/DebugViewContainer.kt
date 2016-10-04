package ru.ltst.u2020mvp.ui.debug

import android.app.Activity
import android.content.Context.POWER_SERVICE
import android.os.PowerManager
import android.os.PowerManager.*
import android.support.v4.view.GravityCompat
import android.view.ContextThemeWrapper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
import android.widget.Toast
import com.f2prateek.rx.preferences.Preference
import com.jakewharton.madge.MadgeFrameLayout
import com.jakewharton.scalpel.ScalpelFrameLayout
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.base.mvp.ActivityHierarchyServer
import ru.ltst.u2020mvp.base.mvp.ViewContainer
import ru.ltst.u2020mvp.data.*
import ru.ltst.u2020mvp.ui.misc.KotterKnife
import ru.ltst.u2020mvp.ui.misc.bindView
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

/**
 * An [ViewContainer] for debug builds which wraps a sliding drawer on the right that holds
 * all of the debug information and settings.
 */
@ApplicationScope
class DebugViewContainer
@Inject
constructor(@SeenDebugDrawer private val seenDebugDrawer: Preference<Boolean>,
            @PixelGridEnabled private val pixelGridEnabled: Preference<Boolean>,
            @PixelRatioEnabled private val pixelRatioEnabled: Preference<Boolean>,
            @ScalpelEnabled private val scalpelEnabled: Preference<Boolean>,
            @ScalpelWireframeEnabled private val scalpelWireframeEnabled: Preference<Boolean>) : ViewContainer {

    internal class ViewHolder (override val rootView : View) : KotterKnife {
        val drawerLayout: DebugDrawerLayout by bindView(R.id.debug_drawer_layout)
        val debugDrawer: ViewGroup by bindView(R.id.debug_drawer)
        val madgeFrameLayout: MadgeFrameLayout by bindView(R.id.madge_container)
        val content: ScalpelFrameLayout by bindView(R.id.debug_content)
    }

    override fun forActivity(activity: Activity): ViewGroup {
        activity.setContentView(R.layout.debug_activity_frame)
        val viewHolder = ViewHolder(activity.findViewById(R.id.debug_drawer_layout))

        val drawerContext = ContextThemeWrapper(activity, R.style.Theme_U2020_Debug)
        val debugView = DebugView(drawerContext)
        viewHolder.debugDrawer.addView(debugView)

        // Set up the contextual actions to watch views coming in and out of the content area.
        val contextualActions = debugView.contextualDebugActions
        contextualActions.setActionClickListener(View.OnClickListener { viewHolder.drawerLayout.closeDrawers() })
        viewHolder.content.setOnHierarchyChangeListener(HierarchyTreeChangeListener.wrap(contextualActions))

        viewHolder.drawerLayout.setDrawerShadow(R.drawable.debug_drawer_shadow, GravityCompat.END)
        viewHolder.drawerLayout.setDrawerListener(object : DebugDrawerLayout.SimpleDrawerListener() {
            override fun onDrawerOpened(drawerView: View) {
                debugView.onDrawerOpened()
            }
        })

        // If you have not seen the debug drawer before, show it with a message
        if (!(seenDebugDrawer.get() ?: false)) {
            viewHolder.drawerLayout.postDelayed({
                viewHolder.drawerLayout.openDrawer(GravityCompat.END)
                Toast.makeText(drawerContext, R.string.debug_drawer_welcome, Toast.LENGTH_LONG).show()
            }, 1000)
            seenDebugDrawer.set(true)
        }

        val subscriptions = CompositeSubscription()
        setupMadge(viewHolder, subscriptions)
        setupScalpel(viewHolder, subscriptions)

        val app = activity.application
        app.registerActivityLifecycleCallbacks(object : ActivityHierarchyServer.Empty() {
            override fun onActivityDestroyed(activity: Activity) {
                if (activity === activity) {
                    subscriptions.unsubscribe()
                    app.unregisterActivityLifecycleCallbacks(this)
                }
            }
        })

        riseAndShine(activity)
        return viewHolder.content
    }

    private fun setupMadge(viewHolder: ViewHolder, subscriptions: CompositeSubscription) {
        subscriptions.add(pixelGridEnabled.asObservable().subscribe { enabled -> viewHolder.madgeFrameLayout.isOverlayEnabled = enabled })
        subscriptions.add(pixelRatioEnabled.asObservable().subscribe { enabled -> viewHolder.madgeFrameLayout.isOverlayRatioEnabled = enabled })
    }

    private fun setupScalpel(viewHolder: ViewHolder, subscriptions: CompositeSubscription) {
        subscriptions.add(scalpelEnabled.asObservable().subscribe { enabled -> viewHolder.content.isLayerInteractionEnabled = enabled })
        subscriptions.add(scalpelWireframeEnabled.asObservable().subscribe { enabled -> viewHolder.content.setDrawViews(!enabled) })
    }

    companion object {

        /**
         * Show the activity over the lock-screen and wake up the device. If you launched the app manually
         * both of these conditions are already true. If you deployed from the IDE, however, this will
         * save you from hundreds of power button presses and pattern swiping per day!
         */
        fun riseAndShine(activity: Activity) {
            activity.window.addFlags(FLAG_SHOW_WHEN_LOCKED)

            val power = activity.getSystemService(POWER_SERVICE) as PowerManager
            val lock = power.newWakeLock(FULL_WAKE_LOCK or ACQUIRE_CAUSES_WAKEUP or ON_AFTER_RELEASE, "wakeup!")
            lock.acquire()
            lock.release()
        }
    }
}
