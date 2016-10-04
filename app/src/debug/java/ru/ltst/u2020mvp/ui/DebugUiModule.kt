package ru.ltst.u2020mvp.ui

import dagger.Module
import dagger.Provides
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.base.mvp.ActivityHierarchyServer
import ru.ltst.u2020mvp.base.mvp.Registry
import ru.ltst.u2020mvp.base.mvp.ViewContainer
import ru.ltst.u2020mvp.ui.debug.DebugViewContainer
import ru.ltst.u2020mvp.base.mvp.annotation.ActivityScreenSwitcherServer
import ru.ltst.u2020mvp.ui.debug.SocketActivityHierarchyServer

@Module(includes = arrayOf(UiModule::class))
class DebugUiModule {
    @Provides
    @ApplicationScope
    fun provideAppContainer(appContainer: DebugViewContainer): ViewContainer {
        return appContainer
    }

    @Provides
    @ApplicationScope
    fun provideActivityHierarchyServer(@ActivityScreenSwitcherServer server: ActivityHierarchyServer): ActivityHierarchyServer {
        val proxy = ActivityHierarchyServer.Proxy()
        proxy.addServer(server)
        proxy.addServer(Registry.SERVER)
        proxy.addServer(SocketActivityHierarchyServer())
        return proxy
    }
}
