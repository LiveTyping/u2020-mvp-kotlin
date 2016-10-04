package ru.ltst.u2020mvp.ui.screen.main.view

import android.support.design.widget.NavigationView
import android.support.v4.widget.SwipeRefreshLayout
import ru.ltst.u2020mvp.base.mvp.BaseView
import ru.ltst.u2020mvp.ui.misc.EnumAdapter
import ru.ltst.u2020mvp.ui.misc.SimpleItemSelectedListener
import ru.ltst.u2020mvp.ui.screen.trending.TrendingAdapter
import ru.ltst.u2020mvp.ui.screen.trending.TrendingTimespan

interface MainViewImpl : BaseView {
    fun bindData(trendingAdapter: TrendingAdapter,
                 timespanAdapter: EnumAdapter<TrendingTimespan>,
                 onRefreshListener: SwipeRefreshLayout.OnRefreshListener,
                 onTimespanItemSelectedListener: SimpleItemSelectedListener,
                 onNavigationItemSelectedListener: NavigationView.OnNavigationItemSelectedListener)

    fun setTimespanPosition(position: Int)

    fun closeDrawer()

    fun onNetworkError()
}
