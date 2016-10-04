package ru.ltst.u2020mvp.data.api

enum class Sort constructor(private val value: String) {
    STARS("watchers"),
    FORKS("forks"),
    UPDATED("updated");

    override fun toString(): String {
        return value
    }
}
