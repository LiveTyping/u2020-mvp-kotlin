package ru.ltst.u2020mvp

import android.app.Application

import com.squareup.picasso.Picasso

import ru.ltst.u2020mvp.base.navigation.activity.ActivityScreenSwitcher
import ru.ltst.u2020mvp.data.IntentFactory
import ru.ltst.u2020mvp.data.api.GithubService
import ru.ltst.u2020mvp.base.mvp.ActivityHierarchyServer
import ru.ltst.u2020mvp.base.mvp.ViewContainer

/**
 * A common interface implemented by both the Release and Debug flavored components.
 */
interface U2020Graph {
    fun inject(app: U2020App)
    fun application(): Application
    fun viewContainer(): ViewContainer
    fun picasso(): Picasso
    fun activityScreenSwitcher(): ActivityScreenSwitcher
    fun activityHierarchyServer(): ActivityHierarchyServer
    fun githubService(): GithubService
    fun intentFactory(): IntentFactory
}
