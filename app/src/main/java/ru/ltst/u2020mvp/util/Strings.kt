package ru.ltst.u2020mvp.util

object Strings {
    val EMPTY = ""
    val DOT = "."
    val COLON = ":"

    fun CharSequence?.isBlank(): Boolean {
        return isNullOrBlank()
    }

    fun String?.valueOrDefault(defaultString: String): String {
        return if (isBlank()) defaultString else this!!
    }

    fun String?.truncateAt(length: Int): String? {
        if (isBlank() || length < 0)
            return this
        return if (this!!.length > length) substring(0, length) else this
    }
}

fun CharSequence?.isBlank(): Boolean {
    return isNullOrBlank()
}

fun String?.valueOrDefault(defaultString: String): String {
    return if (isBlank()) defaultString else this!!
}

fun String?.truncateAt(length: Int): String? {
    if (isBlank() || length < 0)
        return this
    return if (this!!.length > length) substring(0, length) else this
}
