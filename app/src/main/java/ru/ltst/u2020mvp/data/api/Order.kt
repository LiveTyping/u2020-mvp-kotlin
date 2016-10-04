package ru.ltst.u2020mvp.data.api

enum class Order constructor(private val value: String) {
    ASC("asc"),
    DESC("desc");

    override fun toString(): String {
        return value
    }
}
