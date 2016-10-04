package ru.ltst.u2020mvp.ui.screen.main

import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v4.widget.SwipeRefreshLayout
import android.widget.ImageView

import com.f2prateek.rx.preferences.Preference

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import javax.inject.Inject

import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.base.BaseTest
import ru.ltst.u2020mvp.data.NetworkDelay
import ru.ltst.u2020mvp.util.RecyclerViewMatcher
import ru.ltst.u2020mvp.util.TimerTestRule

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.swipeDown
import android.support.test.espresso.action.ViewActions.swipeUp
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.view.View
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matcher
import ru.ltst.u2020mvp.util.ViewActions

@RunWith(AndroidJUnit4::class)
class MainActivityTest : BaseTest() {
    lateinit var networkDelay: Preference<Long>

    @get:Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)
    @get:Rule
    var timerTestRule = TimerTestRule()

    @Before
    fun setup() {
        networkDelay = testComponent.networkDelay()
        networkDelay.set(500L)
    }

    @Test
    fun swipeTest() {
        //check loading displayed
        onView(withId(R.id.trending_loading)).check(matches(isCompletelyDisplayed()))
        timerTestRule.scheduleTimeout(2000)

        //check content displayed
        onView(withId(R.id.trending_swipe_refresh)).check(matches(isCompletelyDisplayed()))
        onView(RecyclerViewMatcher.withRecyclerView(R.id.trending_list).atPosition(0)).check(matches(isCompletelyDisplayed()))

        //check swipe working
        onView(withId(R.id.trending_list)).perform(swipeUp())
        onView(withId(R.id.trending_list)).perform(swipeDown())
    }

    @Test
    fun hamburgerMenuTest() {
        //click on navigation icon
        onView(allOf(instanceOf<View>(ImageView::class.java), withParent(withId(R.id.trending_toolbar)))).perform(click())
        onView(withId(R.id.main_navigation)).check(matches(isCompletelyDisplayed()))
    }

    @Test
    fun swipeToRefreshTest() {
        timerTestRule.scheduleTimeout(1000)
        onView(withId(R.id.trending_swipe_refresh))
                .perform(ViewActions.withCustomConstraints(swipeDown(), isDisplayingAtLeast(80)))
                .check { view, noViewFoundException -> (view as SwipeRefreshLayout).isRefreshing }
    }

    @Test
    fun spinnerTest() {
        onView(withId(R.id.trending_timespan)).perform(click())
        onView(withText("today")).check(matches(isCompletelyDisplayed())).perform(click())
        onView(withId(R.id.trending_swipe_refresh)).check { view, noViewFoundException -> (view as SwipeRefreshLayout).isRefreshing }
    }

    @Test
    fun navigationMenuTest() {
        //shortcut to open navigation menu
        hamburgerMenuTest()
        onView(withText("Search")).perform(click())
        onView(withId(R.id.main_navigation)).check(matches(not(isDisplayed())))
    }
}