package ru.ltst.u2020mvp.util

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class PreconditionsTest {

    @get:Rule
    var expectedException = ExpectedException.none()

    @Test
    @Throws(Exception::class)
    fun checkNotNull() {
        val ref = 1
        Preconditions.checkNotNull(ref)
        expectedException.expect(NullPointerException::class.java)
        Preconditions.checkNotNull<Any>(null)
    }

    @Test
    @Throws(Exception::class)
    fun checkNotNull1() {
        val ref = 1
        Preconditions.checkNotNull(ref)
        expectedException.expectMessage("message")
        Preconditions.checkNotNull<Any>(null, "message")
    }

}