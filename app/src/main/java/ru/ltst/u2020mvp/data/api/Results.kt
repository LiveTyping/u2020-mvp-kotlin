package ru.ltst.u2020mvp.data.api

import retrofit2.adapter.rxjava.Result
import rx.functions.Func1

class Results private constructor() {

    init {
        throw AssertionError("No instances.")
    }

    companion object {
        val isSuccessful: Func1<Result<*>, Boolean> = Func1 { result -> !result.isError && result.response().isSuccessful }
    }
}
