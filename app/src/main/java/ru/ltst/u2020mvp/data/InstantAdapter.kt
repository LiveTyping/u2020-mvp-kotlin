package ru.ltst.u2020mvp.data

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

import org.threeten.bp.Instant

@SuppressWarnings("unused") // Accessed via reflection by Moshi.
class InstantAdapter {
    @ToJson fun toJson(instant: Instant): String {
        return instant.toString()
    }

    @FromJson fun fromJson(value: String): Instant {
        return Instant.parse(value)
    }
}
