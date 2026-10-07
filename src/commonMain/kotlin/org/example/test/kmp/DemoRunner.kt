package org.example.test.kmp

// =====================================================================
// 汇总入口：所有目标都能调用，输出各特性在小节
// =====================================================================

fun runAllDemos(): String = buildString {
    appendLine("=== expect/actual 平台信息 ===")
    appendLine("  platformName  = $platformName")
    appendLine("  pathSeparator = $pathSeparator")
    appendLine("  nowMillis     = ${nowMillis()}")
    appendLine("  formatNow     = ${formatNow()}")
    appendLine("  repeatText    = ${repeatText("ab", 3)}")
    appendLine("  PlatformInfo  = ${PlatformInfo.describe()}")
    appendLine("  BuildConfig   = ${BuildConfig.flavor}")
    appendLine()
    appendLine("=== 平台无关特性（commonMain）===")
    append(demoSealed())
    appendLine("  DSL builder   = ${demoDsl()}")
    appendLine("  ${demoGenerics()}")
    appendLine("  ${demoValueClass()}")
    appendLine("  ${demoJson()}")
}
