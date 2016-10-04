package ru.ltst.u2020mvp.util

import android.content.SharedPreferences

object EnumPreferences {

    fun <T : Enum<T>> getEnumValue(preferences: SharedPreferences, type: Class<T>,
                                   key: String, defaultValue: T?): T? {
        val name = preferences.getString(key, null)
        if (name != null) {
            try {
                return valueOf(type, name)
            } catch (ignored: IllegalArgumentException) {
            }

        }

        return defaultValue
    }

    fun saveEnumValue(preferences: SharedPreferences, key: String, value: Enum<*>) {
        preferences.edit().putString(key, value.name).apply()
    }
}

fun <T : Enum<T>> valueOf(enumType: Class<T>?, name: String?): T {
    if (enumType == null) {
        throw NullPointerException("enumType == null")
    } else if (name == null) {
        throw NullPointerException("name == null")
    }
    val values = enumType.enumConstants ?: throw IllegalArgumentException(enumType.canonicalName
            + " is not an enum type")
    for (value in values) {
        if (name == value.name) {
            return value
        }
    }
    throw IllegalArgumentException(name + " is not a constant in " + enumType.name)
}
