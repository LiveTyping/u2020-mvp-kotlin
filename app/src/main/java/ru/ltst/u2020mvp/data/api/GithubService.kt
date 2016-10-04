package ru.ltst.u2020mvp.data.api

import retrofit2.adapter.rxjava.Result
import retrofit2.http.GET
import retrofit2.http.Query
import ru.ltst.u2020mvp.data.api.model.RepositoriesResponse
import rx.Observable

interface GithubService {
    @GET("search/repositories")
    fun repositories(
            @Query("q") query: SearchQuery,
            @Query("sort") sort: Sort,
            @Query("order") order: Order): Observable<Result<RepositoriesResponse>>
}
