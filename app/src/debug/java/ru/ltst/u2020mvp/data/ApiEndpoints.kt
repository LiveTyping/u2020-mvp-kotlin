package ru.ltst.u2020mvp.data

import ru.ltst.u2020mvp.data.api.ApiModule
import ru.ltst.u2020mvp.util.Strings

enum class ApiEndpoints constructor(val displayedName: String, val url: String) {
    PRODUCTION("Production", ApiModule.PRODUCTION_API_URL.toString()),
    MOCK_MODE("Mock Mode", "http://localhost/mock/"),
    CUSTOM("Custom", Strings.EMPTY);

    override fun toString(): String {
        return displayedName
    }

    companion object {

        fun from(endpoint: String?): ApiEndpoints {
            if (endpoint == null)
                return CUSTOM
            for (value in values()) {
                if (value.url == endpoint) {
                    return value
                }
            }
            return CUSTOM
        }

        fun isMockMode(endpoint: String): Boolean {
            return from(endpoint) == MOCK_MODE
        }
    }
}
