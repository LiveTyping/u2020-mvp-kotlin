package ru.ltst.u2020mvp

import dagger.Component
import ru.ltst.u2020mvp.data.DebugDataModule
import ru.ltst.u2020mvp.ui.DebugUiModule
import ru.ltst.u2020mvp.ui.ExternalIntentActivityTest
import ru.ltst.u2020mvp.ui.screen.main.MainActivityTest
import ru.ltst.u2020mvp.ui.screen.main.view.MainViewTest

@ApplicationScope
@Component(modules = arrayOf(U2020AppModule::class, DebugUiModule::class,
        DebugDataModule::class, TestU2020Module::class))
interface TestU2020Component : U2020Component {
}
