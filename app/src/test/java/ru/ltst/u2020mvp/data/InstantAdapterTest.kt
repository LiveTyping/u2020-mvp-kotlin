package ru.ltst.u2020mvp.data

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner
import org.threeten.bp.Instant

import org.junit.Assert.assertEquals

@RunWith(MockitoJUnitRunner::class)
class InstantAdapterTest {
    @Test
    @Throws(Exception::class)
    fun toJson() {
        val input = Instant.now()
        val expected = input.toString()
        assertEquals(expected, InstantAdapter().toJson(input))
    }

    @Test
    @Throws(Exception::class)
    fun fromJson() {
        val expected = Instant.now()
        val input = expected.toString()
        assertEquals(expected, InstantAdapter().fromJson(input))
    }
}