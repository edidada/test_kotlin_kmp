package org.example.test.kmp

// =====================================================================
// KMP 核心机制：expect / actual
// commonMain 里用 expect 声明"平台无关的接口"，各平台源集用 actual 提供实现
// =====================================================================

// 1. expect 属性：每个平台必须提供 actual val
expect val platformName: String
expect val pathSeparator: String

// 2. expect 函数
expect fun nowMillis(): Long
expect fun formatNow(): String

// 3. expect 函数带默认参数：默认值只能写在 expect 侧，actual 侧不允许重复声明
expect fun repeatText(text: String, times: Int = 2): String

// 4. expect 类 + companion object（actual 类必须提供全部 actual 成员）
expect class PlatformInfo {
    companion object {
        fun describe(): String
    }
}

// 5. expect 单例对象
expect object BuildConfig {
    val flavor: String
}

// 6. expect 注解类：各平台可映射到不同的真实注解实现
expect annotation class PlatformMarker()
