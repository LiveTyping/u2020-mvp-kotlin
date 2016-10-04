package ru.ltst.u2020mvp.ui.screen.main.view

import android.annotation.TargetApi
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.support.design.widget.NavigationView
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import kotlinx.android.synthetic.main.main_activity.view.*
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.ui.misc.*
import ru.ltst.u2020mvp.ui.screen.trending.TrendingAdapter
import ru.ltst.u2020mvp.ui.screen.trending.TrendingTimespan

class MainView(context: Context, attrs: AttributeSet) : DrawerLayout(context, attrs), MainViewImpl {

    internal val statusBarColor: Int by bindColor(R.color.status_bar)
    internal val dividerPaddingStart: Float by bindDimen(R.dimen.trending_divider_padding_start)

    override fun onFinishInflate() {
        super.onFinishInflate()

        main_drawer_layout.setStatusBarBackgroundColor(statusBarColor)
        main_drawer_layout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START)

        val ellipsis = ContextCompat.getDrawable(context, R.drawable.dancing_ellipsis) as AnimationDrawable
        trending_loading_message.setCompoundDrawablesWithIntrinsicBounds(null, null, ellipsis, null)
        ellipsis.start()

        trending_toolbar.setNavigationIcon(R.drawable.menu_icon)
        trending_toolbar.setNavigationOnClickListener { v -> main_drawer_layout.openDrawer(GravityCompat.START) }

        trending_swipe_refresh.setColorSchemeResources(R.color.accent)

        trending_list.layoutManager = LinearLayoutManager(context)
        trending_list.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL_LIST,
                dividerPaddingStart, safeIsRtl()))
    }


    override fun bindData(trendingAdapter: TrendingAdapter,
                          timespanAdapter: EnumAdapter<TrendingTimespan>,
                          onRefreshListener: SwipeRefreshLayout.OnRefreshListener,
                          onTimespanItemSelectedListener: SimpleItemSelectedListener,
                          onNavigationItemSelectedListener: NavigationView.OnNavigationItemSelectedListener) {
        trending_timespan.adapter = timespanAdapter
        trending_list.adapter = trendingAdapter
        trending_swipe_refresh.setOnRefreshListener(onRefreshListener)
        trending_timespan.onItemSelectedListener = onTimespanItemSelectedListener
        main_navigation.setNavigationItemSelectedListener(onNavigationItemSelectedListener)
    }

    override fun setTimespanPosition(position: Int) {
        trending_timespan.setSelection(position)
    }

    override fun closeDrawer() {
        main_drawer_layout.closeDrawers()
    }

    override fun showLoading() {
        if (trending_animator.displayedChildId != R.id.trending_swipe_refresh) {
            trending_animator.displayedChildId = R.id.trending_loading
        } else {
            // For whatever reason, the SRL's spinner does not draw itself when we call setRefreshing(true)
            // unless it is posted.
            post { trending_swipe_refresh.isRefreshing = true }
        }
    }

    override fun showContent() {
        trending_swipe_refresh.isRefreshing = false
        trending_animator.displayedChildId = R.id.trending_swipe_refresh
    }

    override fun showEmpty() {
        trending_swipe_refresh.isRefreshing = false
        trending_animator.displayedChildId = R.id.trending_empty
    }

    override fun showError(throwable: Throwable) {
        trending_swipe_refresh.isRefreshing = false
        trending_animator.displayedChildId = R.id.trending_error
    }

    override fun onNetworkError() {
        trending_swipe_refresh.isRefreshing = false
        trending_animator.displayedChildId = R.id.trending_network_error
    }

    private fun safeIsRtl(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isRtl
    }

    private val isRtl: Boolean
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        get() = layoutDirection == LAYOUT_DIRECTION_RTL
}
