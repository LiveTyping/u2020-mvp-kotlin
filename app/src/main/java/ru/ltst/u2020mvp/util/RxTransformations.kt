package ru.ltst.u2020mvp.util

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun <T> applySchedulers(): Observable.Transformer<T, T> {
    return Observable.Transformer<T, T> {
        tObservable -> tObservable
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
}
fun <T> applyDelay(): Observable.Transformer<T, T> {
    return Observable.Transformer<T, T> {
        observable -> Observable.timer(1000, TimeUnit.MILLISECONDS)
            .flatMap { observable }
    }
}
