package ru.ltst.u2020mvp


import java.util.LinkedHashSet

import dagger.Module
import dagger.Provides
import ru.ltst.u2020mvp.ui.debug.ContextualDebugActions

import dagger.Provides.Type.SET_VALUES

@Module
class DebugActionsModule {
    @Provides(type = SET_VALUES)
    fun provideDebugActions(): Set<ContextualDebugActions.DebugAction<*>> {
        val actions = LinkedHashSet<ContextualDebugActions.DebugAction<*>>()
        return actions
    }
}
