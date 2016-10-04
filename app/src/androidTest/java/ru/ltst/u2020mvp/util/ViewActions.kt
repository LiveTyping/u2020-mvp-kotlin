package ru.ltst.u2020mvp.util

import android.support.test.espresso.PerformException
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Swipe
import android.support.test.espresso.matcher.ViewMatchers.isRoot
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.util.HumanReadables
import android.support.test.espresso.util.TreeIterables
import android.view.View
import org.hamcrest.CoreMatchers.any
import org.hamcrest.Matcher
import java.util.concurrent.TimeoutException

object ViewActions {

    /**
     * Perform action of waiting for a specific view id.
     *
     *
     * E.g.:
     * onView(isRoot()).perform(waitId(R.id.dialogEditor, Sampling.SECONDS_15));

     * @param viewId
     * *
     * @param millis
     * *
     * @return
     */
    fun waitId(viewId: Int, millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return isRoot()
            }

            override fun getDescription(): String {
                return "wait for a specific view with id <$viewId> during $millis millis."
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadUntilIdle()
                val startTime = System.currentTimeMillis()
                val endTime = startTime + millis
                val viewMatcher = withId(viewId)

                do {
                    for (child in TreeIterables.breadthFirstViewTraversal(view)) {
                        // found view with required ID
                        if (viewMatcher.matches(child)) {
                            return
                        }
                    }

                    uiController.loopMainThreadForAtLeast(50)
                } while (System.currentTimeMillis() < endTime)

                // timeout happens
                throw PerformException.Builder().withActionDescription(this.description).withViewDescription(HumanReadables.describe(view)).withCause(TimeoutException()).build()
            }
        }
    }

    /**
     * Perform action of waiting for a specific time. Useful when you need
     * to wait for animations to end and Espresso fails at waiting.
     *
     *
     * E.g.:
     * onView(isRoot()).perform(waitAtLeast(Sampling.SECONDS_15));

     * @param millis
     * *
     * @return
     */
    fun waitAtLeast(millis: Long): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return any(View::class.java)
            }

            override fun getDescription(): String {
                return "wait for at least $millis millis."
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadUntilIdle()
                uiController.loopMainThreadForAtLeast(millis)
            }
        }
    }

    fun swipeTop(): ViewAction {
        return GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER, GeneralLocation.TOP_CENTER, Press.FINGER)
    }

    fun swipeBottom(): ViewAction {
        return GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER, GeneralLocation.BOTTOM_CENTER, Press.FINGER)
    }

    /**
     * Perform action of waiting until UI thread is free.
     *
     *
     * E.g.:
     * onView(isRoot()).perform(waitUntilIdle());

     * @return
     */
    fun waitUntilIdle(): ViewAction {
        return object : ViewAction {
            override fun getConstraints(): Matcher<View> {
                return any(View::class.java)
            }

            override fun getDescription(): String {
                return "wait until UI thread is free"
            }

            override fun perform(uiController: UiController, view: View) {
                uiController.loopMainThreadUntilIdle()
            }
        }
    }

    fun withCustomConstraints(action: ViewAction, constraints: Matcher<View>): ViewAction {
        return object : ViewAction {
            override fun getDescription(): String {
                return action.description
            }

            override fun getConstraints(): Matcher<View> {
                return constraints;
            }

            override fun perform(uiController: UiController, view: View) {
                action.perform(uiController, view)
            }
        }
    }
}
