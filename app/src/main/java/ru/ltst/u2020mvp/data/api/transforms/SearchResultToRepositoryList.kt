package ru.ltst.u2020mvp.data.api.transforms

import retrofit2.adapter.rxjava.Result
import ru.ltst.u2020mvp.data.api.model.RepositoriesResponse
import ru.ltst.u2020mvp.data.api.model.Repository
import rx.functions.Func1

class SearchResultToRepositoryList : Func1<Result<RepositoriesResponse>, List<Repository>> {

    override fun call(result: Result<RepositoriesResponse>): List<Repository> {
        val repositoriesResponse = result.response().body()
        return repositoriesResponse.items ?: emptyList<Repository>()
    }

    companion object {
        @Volatile private var instance: SearchResultToRepositoryList? = null

        fun instance(): SearchResultToRepositoryList {
            if (instance == null) {
                instance = SearchResultToRepositoryList()
            }
            return instance!!
        }
    }
}
