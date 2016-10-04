package ru.ltst.u2020mvp.base.mvp

interface BaseView {
    fun showLoading()
    fun showContent()
    fun showEmpty()
    fun showError(throwable: Throwable)
}
