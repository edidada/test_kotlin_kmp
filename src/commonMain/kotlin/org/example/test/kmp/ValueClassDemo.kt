package org.example.test.kmp

import kotlin.jvm.JvmInline

// =====================================================================
// value class：包装类型零开销（JVM 上 @JvmInline 内联为底层类型）
// =====================================================================

@JvmInline
value class UserId(val raw: String) {
    init {
        require(raw.isNotBlank()) { "UserId 不能为空" }
    }

    val display: String get() = "@$raw"
}

fun demoValueClass(): String {
    val id = UserId("ada")
    return "value class: UserId(raw=${id.raw}, display=${id.display})"
}
