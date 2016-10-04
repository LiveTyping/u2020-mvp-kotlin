package ru.ltst.u2020mvp

import javax.inject.Inject

import ru.ltst.u2020mvp.data.LumberYard
import timber.log.Timber

class InternalU2020App : U2020App() {
    @Inject
    lateinit var lumberYard: LumberYard

    override fun onCreate() {
        super.onCreate()

        lumberYard.cleanUp()
        Timber.plant(lumberYard.tree())
    }

    override fun buildComponentAndInject() {
        super.buildComponentAndInject()
        component().inject(this)
    }
}
