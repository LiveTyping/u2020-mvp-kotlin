package ru.ltst.u2020mvp.ui

import android.app.Activity

import dagger.Module
import dagger.Provides
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.base.mvp.ActivityHierarchyServer
import ru.ltst.u2020mvp.base.navigation.activity.ActivityScreenSwitcher
import ru.ltst.u2020mvp.base.mvp.annotation.ActivityScreenSwitcherServer

@Module
class UiModule {
    @Provides
    @ApplicationScope
    fun provideActivityScreenSwitcher(): ActivityScreenSwitcher {
        return ActivityScreenSwitcher()
    }

    @Provides
    @ApplicationScope
    @ActivityScreenSwitcherServer
    fun provideActivityScreenSwitcherServer(screenSwitcher: ActivityScreenSwitcher): ActivityHierarchyServer {
        return object : ActivityHierarchyServer.Empty() {
            override fun onActivityStarted(activity: Activity) {
                screenSwitcher.attach(activity)
            }

            override fun onActivityStopped(activity: Activity) {
                screenSwitcher.detach(activity)
            }
        }
    }
}
