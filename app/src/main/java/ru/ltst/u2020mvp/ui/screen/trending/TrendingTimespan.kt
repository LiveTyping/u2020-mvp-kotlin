package ru.ltst.u2020mvp.ui.screen.trending


import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalUnit

enum class TrendingTimespan
constructor(private val displayedName: String, duration: Int, private val durationUnit: TemporalUnit) {
    DAY("today", 1, ChronoUnit.DAYS),
    WEEK("last week", 1, ChronoUnit.WEEKS),
    MONTH("last month", 1, ChronoUnit.MONTHS);

    private val duration: Long

    init {
        this.duration = duration.toLong()
    }

    fun createdSince(): LocalDate {
        return LocalDate.now().minus(duration, durationUnit)
    }

    override fun toString(): String {
        return displayedName
    }
}
