package ru.ltst.u2020mvp.base.navigation

interface ScreenSwitcher {
    fun open(screen: Screen)
    fun goBack()
}
