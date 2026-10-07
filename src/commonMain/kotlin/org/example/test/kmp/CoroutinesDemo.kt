package org.example.test.kmp

import kotlinx.coroutines.delay

// =====================================================================
// kotlinx.coroutines：协程是 KMP 官方推荐的跨平台并发模型
// common 代码中直接写 suspend 函数，各平台用自己的事件循环/线程池
// =====================================================================

suspend fun fetchProfile(id: String): ApiResponse<UserProfile> {
    delay(20) // 平台无关的挂起等待
    return when (id) {
        "1" -> ApiResponse.Ok(UserProfile("Ada", 36, listOf("kmp")))
        else -> ApiResponse.Err(404, "user '$id' not found")
    }
}

suspend fun demoCoroutines(): String = buildString {
    appendLine("suspend coroutines:")
    appendLine("  " + fetchProfile("1").render())
    appendLine("  " + fetchProfile("404").render())
}
