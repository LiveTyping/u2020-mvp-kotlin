package ru.ltst.u2020mvp.data.api.mock


import java.util.Collections
import java.util.Comparator

import ru.ltst.u2020mvp.data.api.Order
import ru.ltst.u2020mvp.data.api.Sort
import ru.ltst.u2020mvp.data.api.model.Repository

import ru.ltst.u2020mvp.data.api.Order.ASC
import ru.ltst.u2020mvp.data.api.Order.DESC

internal object SortUtil {
    private val STARS_ASC = StarsComparator(ASC)
    private val STARS_DESC = StarsComparator(DESC)
    private val FORKS_ASC = ForksComparator(ASC)
    private val FORKS_DESC = ForksComparator(DESC)
    private val UPDATED_ASC = UpdatedComparator(ASC)
    private val UPDATED_DESC = UpdatedComparator(DESC)

    fun sort(repositories: List<Repository>?, sort: Sort, order: Order) {
        if (repositories == null) return

        when (sort) {
            Sort.STARS -> Collections.sort(repositories, if (order === ASC) STARS_ASC else STARS_DESC)
            Sort.FORKS -> Collections.sort(repositories, if (order === ASC) FORKS_ASC else FORKS_DESC)
            Sort.UPDATED -> Collections.sort(repositories, if (order === ASC) UPDATED_ASC else UPDATED_DESC)
            else -> throw IllegalArgumentException("Unknown sort: " + sort)
        }
    }

    private abstract class OrderComparator<T> protected constructor(private val order: Order) : Comparator<T> {

        override fun compare(lhs: T, rhs: T): Int {
            return if (order === ASC) compareAsc(lhs, rhs) else -compareAsc(lhs, rhs)
        }

        protected abstract fun compareAsc(lhs: T, rhs: T): Int
    }

    private class StarsComparator(order: Order) : OrderComparator<Repository>(order) {

        public override fun compareAsc(lhs: Repository, rhs: Repository): Int {
            val left = lhs.watchers
            val right = rhs.watchers
            return if (left < right) -1 else if (left == right) 0 else 1
        }
    }

    private class ForksComparator(order: Order) : OrderComparator<Repository>(order) {

        public override fun compareAsc(lhs: Repository, rhs: Repository): Int {
            val left = lhs.forks
            val right = rhs.forks
            return if (left < right) -1 else if (left == right) 0 else 1
        }
    }

    private class UpdatedComparator(order: Order) : OrderComparator<Repository>(order) {

        public override fun compareAsc(lhs: Repository, rhs: Repository): Int {
            return lhs.updated_at.compareTo(rhs.updated_at)
        }
    }
}
