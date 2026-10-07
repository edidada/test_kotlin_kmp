package org.example.test.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.serialization.json.Json

class FeaturesTest {
    @Test
    fun dslProducesConfiguredServer() {
        assertEquals("Server(0.0.0.0:9090) routes=[/health, /users]", demoDsl())
    }

    @Test
    fun sealedCasesRender() {
        assertEquals("LOADING -> waiting", ApiResponse.Loading.render())
        assertEquals("OK -> v", ApiResponse.Ok("v").render())
        assertEquals("ERR(404) -> nope", ApiResponse.Err(404, "nope").render())
    }

    @Test
    fun reifiedPickOrNull() {
        assertEquals("kotlin", pickOrNull<String>("kotlin"))
        assertNull(pickOrNull<Int>("kotlin"))
        assertEquals(42, pickOrNull<Int>(42))
    }

    @Test
    fun valueClassWrapsAndValidates() {
        assertEquals("@ada", UserId("ada").display)
        assertFailsWith<IllegalArgumentException> { UserId(" ") }
    }

    @Test
    fun jsonRoundTrip() {
        val profile = UserProfile("Ada", 36, listOf("kmp"))
        val encoded = Json.encodeToString(UserProfile.serializer(), profile)
        assertEquals(profile, Json.decodeFromString(UserProfile.serializer(), encoded))
    }
}
