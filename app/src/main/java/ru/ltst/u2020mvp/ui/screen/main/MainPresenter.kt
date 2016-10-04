package ru.ltst.u2020mvp.ui.screen.main

import android.app.Application
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.ContextThemeWrapper
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.squareup.picasso.Picasso
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.base.mvp.BasePresenter
import ru.ltst.u2020mvp.base.navigation.activity.ActivityScreenSwitcher
import ru.ltst.u2020mvp.data.Funcs
import ru.ltst.u2020mvp.data.IntentFactory
import ru.ltst.u2020mvp.data.api.*
import ru.ltst.u2020mvp.data.api.model.Repository
import ru.ltst.u2020mvp.data.api.transforms.SearchResultToRepositoryList
import ru.ltst.u2020mvp.ui.misc.EnumAdapter
import ru.ltst.u2020mvp.ui.misc.SimpleItemSelectedListener
import ru.ltst.u2020mvp.ui.screen.main.view.MainViewImpl
import ru.ltst.u2020mvp.ui.screen.trending.TrendingAdapter
import ru.ltst.u2020mvp.ui.screen.trending.TrendingTimespan
import ru.ltst.u2020mvp.ui.screen.trending.TrendingTimespanAdapter
import ru.ltst.u2020mvp.util.Intents
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Action1
import rx.functions.Func1
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subscriptions.CompositeSubscription
import timber.log.Timber
import javax.inject.Inject

@MainScope
class MainPresenter
@Inject
constructor(private val githubService: GithubService,
            private val picasso: Picasso,
            private val intentFactory: IntentFactory,
            private val application: Application,
            private val activityScreenSwitcher: ActivityScreenSwitcher)
    : BasePresenter<MainViewImpl>(),
        SwipeRefreshLayout.OnRefreshListener,
        NavigationView.OnNavigationItemSelectedListener {

    private val timespanSubject: PublishSubject<TrendingTimespan>
    private val timespanAdapter: EnumAdapter<TrendingTimespan>
    private val trendingAdapter: TrendingAdapter
    private val subscriptions = CompositeSubscription()

    private var mainView: MainViewImpl? = null
    private var lastTimespanPosition = TrendingTimespan.WEEK.ordinal
    private val isFirstStart = true

    init {

        timespanSubject = PublishSubject.create<TrendingTimespan>()
        timespanAdapter = TrendingTimespanAdapter(
                ContextThemeWrapper(application, R.style.Theme_U2020_TrendingTimespan))
        trendingAdapter = TrendingAdapter(picasso, { onRepositoryClick(it) })
    }

    override fun onLoad(onActivityResult: BasePresenter.OnActivityResult?) {
        mainView = getView()
        val result = timespanSubject.flatMap(trendingSearch)
                .observeOn(AndroidSchedulers.mainThread())
                .share()
        subscriptions.add(result.filter(Results.isSuccessful)
                .map(SearchResultToRepositoryList.instance())
                .subscribe(trendingAdapter))
        subscriptions.add(result.filter(Funcs.not(Results.isSuccessful))
                .subscribe(trendingError))

        // Load the default selection.
        mainView!!.bindData(trendingAdapter,
                timespanAdapter,
                this,
                object : SimpleItemSelectedListener() {
                    override fun onItemSelected(position: Int) {
                        lastTimespanPosition = position
                        reloadData(position)
                    }
                },
                this)

        trendingAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                if (trendingAdapter.itemCount == 0) {
                    mainView!!.showEmpty()
                } else {
                    mainView!!.showContent()
                }
            }
        })
        mainView!!.setTimespanPosition(lastTimespanPosition)
    }


    override fun onRefresh() {
        reloadData(lastTimespanPosition)
    }

    private fun reloadData(position: Int) {
        mainView!!.showLoading()
        timespanSubject.onNext(timespanAdapter.getItem(position))
    }

    fun onRepositoryClick(repository: Repository) {
        Intents.maybeStartActivity(application, intentFactory.createUrlIntent(repository.html_url))
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_search ->
//                                activityScreenSwitcher.open(new YourActivity.Screen(activityParams));
                Toast.makeText(application, "Search!", LENGTH_SHORT).show()
            R.id.nav_trending -> Toast.makeText(application, "Trending!", LENGTH_SHORT).show()
            else -> throw IllegalStateException("Unknown navigation item: " + item.title)
        }

        mainView!!.closeDrawer()
        item.isChecked = true

        return true
    }

    override fun onRestore(savedInstanceState: Bundle) {
        if (savedInstanceState.containsKey(KEY_LAST_TIMESPAN)) {
            lastTimespanPosition = savedInstanceState.getInt(KEY_LAST_TIMESPAN)
        }
    }

    override fun onSave(outState: Bundle) {
        outState.putInt(KEY_LAST_TIMESPAN, lastTimespanPosition)
    }

    override fun onNetworkConnectionStateChanged(isConnected: Boolean) {
        if (isConnected) {
            mainView!!.setTimespanPosition(lastTimespanPosition)
            mainView!!.showContent()
        } else {
            mainView!!.onNetworkError()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptions.clear()
    }

    private val trendingSearch = Func1<ru.ltst.u2020mvp.ui.screen.trending.TrendingTimespan, rx.Observable<retrofit2.adapter.rxjava.Result<ru.ltst.u2020mvp.data.api.model.RepositoriesResponse>>> { trendingTimespan ->
        val trendingQuery = SearchQuery.Builder() //
                .createdSince(trendingTimespan.createdSince()) //
                .build()
        githubService.repositories(trendingQuery, Sort.STARS, Order.DESC).subscribeOn(Schedulers.io())
    }

    private val trendingError = Action1<retrofit2.adapter.rxjava.Result<ru.ltst.u2020mvp.data.api.model.RepositoriesResponse>> { result ->
        if (result.isError) {
            Timber.e(result.error(), "Failed to get trending repositories")
        } else {
            val response = result.response()
            Timber.e("Failed to get trending repositories. Server returned %d", response.code())
        }
        mainView!!.showError(result.error())
    }

    companion object {
        private val KEY_LAST_TIMESPAN = "MainPresenter.last.timespan"
    }
}
