package ru.ltst.u2020mvp.ui

import dagger.Module
import dagger.Provides
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.base.mvp.ActivityHierarchyServer
import ru.ltst.u2020mvp.base.mvp.Registry
import ru.ltst.u2020mvp.base.mvp.ViewContainer
import ru.ltst.u2020mvp.base.mvp.annotation.ActivityScreenSwitcherServer

@Module(includes = arrayOf(UiModule::class))
class ReleaseUiModule {
    @Provides
    @ApplicationScope
    internal fun provideViewContainer(): ViewContainer {
        return ViewContainer.DEFAULT
    }

    @Provides
    @ApplicationScope
    internal fun provideActivityHierarchyServer(@ActivityScreenSwitcherServer server: ActivityHierarchyServer): ActivityHierarchyServer {
        val proxy = ActivityHierarchyServer.Proxy()
        proxy.addServer(server)
        proxy.addServer(Registry.SERVER)
        return proxy
    }
}
