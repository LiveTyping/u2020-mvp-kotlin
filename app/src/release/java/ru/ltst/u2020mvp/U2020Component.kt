package ru.ltst.u2020mvp

import dagger.Component
import ru.ltst.u2020mvp.data.ReleaseDataModule
import ru.ltst.u2020mvp.ui.ReleaseUiModule

/**
 * The core release getComponent for u2020 applications
 */
@ApplicationScope
@Component(modules = arrayOf(U2020AppModule::class, ReleaseUiModule::class, ReleaseDataModule::class))
interface U2020Component : U2020Graph {
    /**
     * An initializer that creates the graph from an application.
     */
    object Initializer {
        internal fun init(app: U2020App): U2020Component {
            return DaggerU2020Component
                    .builder()
                    .u2020AppModule(U2020AppModule(app))
                    .build()
        }
    }// No instances.
}

