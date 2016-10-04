package ru.ltst.u2020mvp.data.api.mock


import java.util.Arrays
import java.util.Collections

import ru.ltst.u2020mvp.data.api.model.RepositoriesResponse
import ru.ltst.u2020mvp.data.api.model.Repository


enum class MockRepositoriesResponse constructor(val displayedName: String,
                                                val response: RepositoriesResponse) {
    SUCCESS("Success", RepositoriesResponse(Arrays.asList(//
            MockRepositories.BUTTERKNIFE, //
            MockRepositories.DAGGER, //
            MockRepositories.JAVAPOET, //
            MockRepositories.OKHTTP, //
            MockRepositories.OKIO, //
            MockRepositories.PICASSO, //
            MockRepositories.RETROFIT, //
            MockRepositories.SQLBRITE, //
            MockRepositories.TELESCOPE, //
            MockRepositories.U2020, //
            MockRepositories.WIRE, //
            MockRepositories.MOSHI))),
    ONE("One", RepositoriesResponse(listOf<Repository>(MockRepositories.DAGGER))),
    EMPTY("Empty", RepositoriesResponse(null));

    override fun toString(): String {
        return displayedName
    }
}
