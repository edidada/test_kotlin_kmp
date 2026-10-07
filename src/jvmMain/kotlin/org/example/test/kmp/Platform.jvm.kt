package org.example.test.kmp

import java.text.SimpleDateFormat
import java.util.Date

// =====================================================================
// JVM 侧 actual 实现：可以自由使用 JDK API
// =====================================================================

actual val platformName: String = "JVM / ${System.getProperty("os.name")}"
actual val pathSeparator: String = System.getProperty("file.separator")

actual fun nowMillis(): Long = System.currentTimeMillis()

actual fun formatNow(): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

actual fun repeatText(text: String, times: Int): String =
    buildString { repeat(times) { append(text) } }

actual class PlatformInfo {
    actual companion object {
        actual fun describe(): String =
            "JVM ${System.getProperty("java.version")} (${System.getProperty("java.vendor")})"
    }
}

actual object BuildConfig {
    actual val flavor: String = "jvm-demo"
}

actual annotation class PlatformMarker actual constructor()
