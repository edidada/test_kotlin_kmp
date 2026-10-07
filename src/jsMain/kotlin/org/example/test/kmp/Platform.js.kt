package org.example.test.kmp

import kotlin.js.Date

// =====================================================================
// Kotlin/JS 侧 actual 实现：可用 js() 内联表达式直接互操作 Node.js API
// =====================================================================

// js() 内联表达式只能出现在函数体内，故包一层 helper 函数
private fun nodeVersion(): String = js("process.version").unsafeCast<String>()
private fun nodePathSep(): String = js("require('path').sep").unsafeCast<String>()

actual val platformName: String = "Kotlin/JS / Node ${nodeVersion()}"
actual val pathSeparator: String = nodePathSep()

actual fun nowMillis(): Long = Date.now().toLong()

actual fun formatNow(): String {
    fun p(n: Int) = n.toString().padStart(2, '0')
    val d = Date()
    return "${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} " +
        "${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}"
}

actual fun repeatText(text: String, times: Int): String = text.repeat(times)

actual class PlatformInfo {
    actual companion object {
        actual fun describe(): String = "Kotlin/JS on Node ${nodeVersion()}"
    }
}

actual object BuildConfig {
    actual val flavor: String = "js-demo"
}

actual annotation class PlatformMarker actual constructor()
