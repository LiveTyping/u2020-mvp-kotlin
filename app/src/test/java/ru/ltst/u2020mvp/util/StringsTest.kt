package ru.ltst.u2020mvp.util

import org.junit.Test

import org.junit.Assert.*

class StringsTest {
    @Test
    @Throws(Exception::class)
    fun isBlank() {
        assertFalse("123".isBlank())
        assertTrue(" ".isBlank())
        assertTrue("".isBlank())
        assertTrue((null as String?).isBlank())
    }

    @Test
    @Throws(Exception::class)
    fun valueOrDefault() {
        val defaultVal = "default"
        assertEquals(defaultVal, " ".valueOrDefault(defaultVal))
        assertEquals(defaultVal, "".valueOrDefault(defaultVal))
        assertNotEquals(defaultVal, ",".valueOrDefault(defaultVal))
        assertEquals(defaultVal, (null as String?).valueOrDefault(defaultVal))
    }

    @Test
    @Throws(Exception::class)
    fun truncateAt() {
        assertEquals("", "abc".truncateAt(0))
        assertEquals("abc", "abcdef".truncateAt(3))
        assertEquals("abc", "abc".truncateAt(6))
        assertEquals("", "".truncateAt(4))
        assertNull((null as String?).truncateAt(2))
        assertEquals("abc", "abc".truncateAt(-1))
        assertNull((null as String?).truncateAt(-1))
    }

}