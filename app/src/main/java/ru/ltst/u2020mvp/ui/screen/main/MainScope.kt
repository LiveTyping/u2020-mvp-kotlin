package ru.ltst.u2020mvp.ui.screen.main

import dagger.Component
import dagger.Module
import ru.ltst.u2020mvp.U2020Component
import javax.inject.Scope

/**
 * Created by Danil on 07.09.2016.
 */

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MainScope

@MainScope
@Component(dependencies = arrayOf(U2020Component::class))
interface MainComponent {
    fun inject(mainActivity: MainActivity)
    fun presenter(): MainPresenter
}
