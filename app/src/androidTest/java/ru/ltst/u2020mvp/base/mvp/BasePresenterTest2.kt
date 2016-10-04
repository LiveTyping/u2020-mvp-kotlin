package ru.ltst.u2020mvp.base.mvp


import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import ru.ltst.u2020mvp.R
import ru.ltst.u2020mvp.ui.screen.main.MainActivity
import ru.ltst.u2020mvp.ui.screen.main.MainPresenter
import ru.ltst.u2020mvp.ui.screen.main.view.MainView
import ru.ltst.u2020mvp.util.SimpleViewAction

@RunWith(AndroidJUnit4::class)
class BasePresenterTest2 {
    @get:Rule
    var expectedException = ExpectedException.none()
    @get:Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    internal lateinit var mainPresenter: MainPresenter

    @Before
    fun setup() {
        mainPresenter = activityTestRule.activity.component.presenter()
    }

    @Test
    @Throws(Exception::class)
    fun takeView() {
        assertEquals(true, mainPresenter.hasView())
        mainPresenter.getView() //assert no errors
    }

    @Test
    @Throws(Exception::class)
    fun dropView() {
        expectedException.expect(NullPointerException::class.java)
        mainPresenter.dropView(null)
        onView(withId(R.id.main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                mainPresenter.dropView(view)
            }
        })
        assertEquals(false, mainPresenter.hasView())
        expectedException.expect(NullPointerException::class.java)
        mainPresenter.getView()
    }

    @Test
    @Throws(Exception::class)
    fun hasView() {
        assertEquals(true, mainPresenter.hasView())
        onView(withId(R.id.main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                mainPresenter.dropView(view)
            }
        })
        assertEquals(false, mainPresenter.hasView())
    }

    @Test
    @Throws(Exception::class)
    fun getView() {
        assertNotNull(mainPresenter.getView())
        onView(withId(R.id.main_drawer_layout)).perform(object : SimpleViewAction<MainView>() {
            override fun call(uiController: UiController, view: MainView) {
                mainPresenter.dropView(view)
            }
        })
        expectedException.expect(NullPointerException::class.java)
        mainPresenter.getView()
    }
}