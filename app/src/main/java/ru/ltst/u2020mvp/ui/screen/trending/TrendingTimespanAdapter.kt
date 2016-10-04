package ru.ltst.u2020mvp.ui.screen.trending

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.ui.misc.EnumAdapter


class TrendingTimespanAdapter(context: Context) :
        EnumAdapter<TrendingTimespan>(context, TrendingTimespan::class.java) {

    override fun newView(inflater: LayoutInflater, position: Int, container: ViewGroup): View {
        return inflater.inflate(R.layout.trending_timespan_view, container, false)
    }
}
