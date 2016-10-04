package ru.ltst.u2020mvp.data

import rx.functions.Func1

class Funcs private constructor() {

    init {
        throw AssertionError("No instances.")
    }

    companion object {
        fun <T> not(func: Func1<T, Boolean>): Func1<T, Boolean> {
            return Func1 { value -> !func.call(value) }
        }
    }
}
