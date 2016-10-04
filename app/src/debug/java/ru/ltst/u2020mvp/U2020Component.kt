package ru.ltst.u2020mvp

import dagger.Component
import ru.ltst.u2020mvp.data.DebugDataModule
import ru.ltst.u2020mvp.ui.DebugUiModule

/**
 * The core debug getComponent for u2020 applications
 */
@ApplicationScope
@Component(modules = arrayOf(U2020AppModule::class, DebugUiModule::class, DebugDataModule::class, DebugU2020Module::class, DebugActionsModule::class))
interface U2020Component : DebugInternalU2020Graph {
    /**
     * An initializer that creates the graph from an application.
     */
    object Initializer {
        internal fun init(app: U2020App): ru.ltst.u2020mvp.U2020Component {
            return DaggerU2020Component.builder()
                    .u2020AppModule(U2020AppModule(app))
                    .build()
        }
    }// No instances.
}

