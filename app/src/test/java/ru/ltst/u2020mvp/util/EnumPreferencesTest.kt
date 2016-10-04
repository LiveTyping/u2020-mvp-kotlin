package ru.ltst.u2020mvp.util

import android.annotation.SuppressLint
import android.content.SharedPreferences

import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.runners.MockitoJUnitRunner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.mockito.Matchers.anyString
import org.mockito.Matchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@RunWith(MockitoJUnitRunner::class)
class EnumPreferencesTest {
    @Mock
    internal lateinit var sharedPreferences: SharedPreferences

    @Test
    @Throws(Exception::class)
    fun getEnumValue() {
        `when`(sharedPreferences.getString("key1", null)).thenReturn("ITEM_TWO")
        `when`(sharedPreferences.getString("key2", null)).thenReturn("ITEM_THREE")


        val defaultVal = TestEnumeration.ITEM_ONE
        assertEquals(TestEnumeration.ITEM_TWO, EnumPreferences.getEnumValue(sharedPreferences,
                TestEnumeration::class.java, "key1", defaultVal))
        assertEquals(TestEnumeration.ITEM_THREE, EnumPreferences.getEnumValue(sharedPreferences,
                TestEnumeration::class.java, "key2", defaultVal))
        `when`(sharedPreferences.getString("key3", defaultVal.name)).thenReturn(defaultVal.name)
        assertEquals(defaultVal, EnumPreferences.getEnumValue(sharedPreferences,
                TestEnumeration::class.java, "key3", defaultVal))

        `when`(sharedPreferences.getString("key4", null)).thenReturn(null)
        assertNull(EnumPreferences.getEnumValue(sharedPreferences,
                TestEnumeration::class.java, "key4", null))
    }

    @SuppressLint("CommitPrefEdits")
    @Test
    @Throws(Exception::class)
    fun saveEnumValue() {
        `when`(sharedPreferences.edit()).thenReturn(mock(SharedPreferences.Editor::class.java))
        `when`(sharedPreferences.edit().putString(anyString(), eq(TestEnumeration.ITEM_ONE.name))).thenReturn(mock(SharedPreferences.Editor::class.java))
        EnumPreferences.saveEnumValue(sharedPreferences, "key", TestEnumeration.ITEM_ONE)
    }

    private enum class TestEnumeration {
        ITEM_ONE,
        ITEM_TWO,
        ITEM_THREE
    }

}