package ru.ltst.u2020mvp.data.api.model

data class User(val login: String, val avatar_url: String?) {

    override fun toString(): String {
        return "User{" +
                "login='" + login + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                '}'
    }
}
