package ru.ltst.u2020mvp.data.api.mock

import android.content.SharedPreferences
import retrofit2.adapter.rxjava.Result
import retrofit2.mock.BehaviorDelegate
import retrofit2.mock.Calls
import retrofit2.mock.MockRetrofit
import ru.ltst.u2020mvp.ApplicationScope
import ru.ltst.u2020mvp.data.api.GithubService
import ru.ltst.u2020mvp.data.api.Order
import ru.ltst.u2020mvp.data.api.SearchQuery
import ru.ltst.u2020mvp.data.api.Sort
import ru.ltst.u2020mvp.data.api.model.RepositoriesResponse
import ru.ltst.u2020mvp.util.EnumPreferences
import rx.Observable
import java.util.*
import javax.inject.Inject

@ApplicationScope
class MockGithubService
@Inject
internal constructor(mockRetrofit: MockRetrofit, private val preferences: SharedPreferences) : GithubService {
    private val delegate: BehaviorDelegate<GithubService>
    private val responses = LinkedHashMap<Class<out Enum<*>>, Enum<*>?>()

    init {
        this.delegate = mockRetrofit.create(GithubService::class.java)

        // Initialize mock responses.
        loadResponse(MockRepositoriesResponse::class.java, MockRepositoriesResponse.SUCCESS)
    }

    /**
     * Initializes the current response for `responseClass` from `SharedPreferences`, or
     * uses `defaultValue` if a response was not found.
     */
    private fun <T : Enum<T>> loadResponse(responseClass: Class<T>, defaultValue: T?) {
        responses.put(responseClass, EnumPreferences.getEnumValue(preferences, responseClass, //
                responseClass.canonicalName, defaultValue))
    }

    fun <T : Enum<T>> getResponse(responseClass: Class<T>): T {
        return responseClass.cast(responses[responseClass])
    }

    fun <T : Enum<T>> setResponse(responseClass: Class<T>, value: T) {
        responses.put(responseClass, value)
        EnumPreferences.saveEnumValue(preferences, responseClass.canonicalName, value)
    }

    override fun repositories(query: SearchQuery,
                              sort: Sort, order: Order): Observable<Result<RepositoriesResponse>> {
        var response = getResponse(MockRepositoriesResponse::class.java).response

        if (response.items != null) {
            // Don't modify the original list when sorting.
            val items = ArrayList(response.items!!)
            SortUtil.sort(items, sort, order)
            response = RepositoriesResponse(items)
        }

        return delegate.returning(Calls.response(response)).repositories(query, sort, order)
    }
}
