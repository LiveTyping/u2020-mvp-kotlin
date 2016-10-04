package ru.ltst.u2020mvp.base

import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import ru.ltst.u2020mvp.ui.screen.main.MainActivity
import ru.ltst.u2020mvp.ui.screen.main.MainComponent

@RunWith(AndroidJUnit4::class)
class ComponentFinderTest {
    @get:Rule
    var activityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun findActivityComponentTest() {
        val context = activityTestRule.activity
        ViewMatchers.assertThat(ComponentFinder.findActivityComponent<Any>(context),
                CoreMatchers.instanceOf<Any>(MainComponent::class.java))
    }
}