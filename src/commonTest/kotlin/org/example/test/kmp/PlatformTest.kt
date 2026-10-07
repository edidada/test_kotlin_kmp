package org.example.test.kmp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// commonTest：同一套测试代码在 jvmTest 和 jsNodeTest 中各跑一遍
class PlatformTest {
    @Test
    fun platformNameNotBlank() {
        assertTrue(platformName.isNotBlank())
    }

    @Test
    fun pathSeparatorIsSingleChar() {
        assertEquals(1, pathSeparator.length)
    }

    @Test
    fun nowMillisIsPlausible() {
        assertTrue(nowMillis() > 1_700_000_000_000L)
    }

    @Test
    fun formatNowHasExpectedShape() {
        assertTrue(Regex("""\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}""").matches(formatNow()))
    }

    @Test
    fun repeatUsesExpectSideDefault() {
        assertEquals("abab", repeatText("ab"))
    }

    @Test
    fun repeatExplicitTimes() {
        assertEquals("xxx", repeatText("x", 3))
    }

    @Test
    fun companionDescribeNotBlank() {
        assertTrue(PlatformInfo.describe().isNotBlank())
    }

    @Test
    fun objectFlavorEndsWithDemo() {
        assertTrue(BuildConfig.flavor.endsWith("demo"))
    }
}
