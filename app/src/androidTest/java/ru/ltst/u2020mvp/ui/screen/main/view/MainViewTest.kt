package ru.ltst.u2020mvp.ui.screen.main.view

import android.accounts.NetworkErrorException
import android.support.test.espresso.UiController
import android.support.test.rule.ActivityTestRule
import android.support.v4.widget.SwipeRefreshLayout
import android.view.View
import android.widget.Spinner

import com.f2prateek.rx.preferences.Preference

import org.hamcrest.CustomTypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import javax.inject.Inject

import ru.ltst.u2020mvp.base.BaseTest
import ru.ltst.u2020mvp.data.NetworkDelay
import ru.ltst.u2020mvp.ui.screen.main.MainActivity
import ru.ltst.u2020mvp.util.SimpleViewAction

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import ru.ltst.u2020mvp.R.id.main_drawer_layout
import ru.ltst.u2020mvp.R.id.trending_empty
import ru.ltst.u2020mvp.R.id.trending_error
import ru.ltst.u2020mvp.R.id.trending_loading
import ru.ltst.u2020mvp.R.id.trending_network_error
import ru.ltst.u2020mvp.R.id.trending_swipe_refresh
import ru.ltst.u2020mvp.R.id.trending_timespan

class MainViewTest : BaseTest() {
    lateinit var networkDelay: Preference<Long>

    @get:Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setup() {
        networkDelay = testComponent.networkDelay()
        networkDelay.set(1000L)
    }

    @Test
    @Throws(Exception::class)
    fun setTimespanPosition() {
        onView(withId(main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                view.setTimespanPosition(1)
            }
        })
        onView(withId(trending_timespan)).check { view, noViewFoundException -> assertEquals(1, (view as Spinner).selectedItemPosition.toLong()) }
    }

    @Test
    @Throws(Exception::class)
    fun showLoading() {
        onView(withId(main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                view.showLoading()
            }
        })
        onView(withId(trending_loading)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun showLoading2() {
        onView(withId(main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                view.showContent()
                view.showLoading()
            }
        })
    }

    @Test
    @Throws(Exception::class)
    fun showContent() {
        onView(withId(main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                view.showContent()
            }
        })
        onView(withId(trending_swipe_refresh)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun showEmpty() {
        onView(withId(main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                view.showEmpty()
            }
        })
        onView(withId(trending_empty)).check(matches(isDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun showError() {
        onView(withId(main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                view.showError(NetworkErrorException())
            }
        })
        onView(withId(trending_error)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    @Throws(Exception::class)
    fun onNetworkError() {
        onView(withId(main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                view.onNetworkError()
            }
        })
        onView(withId(trending_network_error)).check(matches(isCompletelyDisplayed()))
    }

}