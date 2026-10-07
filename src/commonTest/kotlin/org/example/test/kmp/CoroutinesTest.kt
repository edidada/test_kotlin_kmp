package org.example.test.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class CoroutinesTest {
    @Test
    fun fetchExistingUser() = runTest {
        val ok = assertIs<ApiResponse.Ok<UserProfile>>(fetchProfile("1"))
        assertEquals("Ada", ok.data.name)
    }

    @Test
    fun fetchMissingUser() = runTest {
        val err = assertIs<ApiResponse.Err>(fetchProfile("999"))
        assertEquals(404, err.code)
    }
}
