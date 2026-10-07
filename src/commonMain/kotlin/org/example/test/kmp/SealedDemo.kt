package org.example.test.kmp

// =====================================================================
// sealed 接口 + data class/data object：平台无关的代数数据类型（ADT）
// when 匹配 sealed 类型时编译器强制穷尽，任何平台都得到同样检查
// =====================================================================

sealed interface ApiResponse<out T> {
    data class Ok<T>(val data: T) : ApiResponse<T>
    data class Err(val code: Int, val message: String) : ApiResponse<Nothing>
    data object Loading : ApiResponse<Nothing>
}

fun <T> ApiResponse<T>.render(): String = when (this) {
    is ApiResponse.Ok -> "OK -> $data"
    is ApiResponse.Err -> "ERR($code) -> $message"
    ApiResponse.Loading -> "LOADING -> waiting"
}

fun demoSealed(): String = buildString {
    appendLine("sealed interface + exhaustive when:")
    val cases: List<ApiResponse<String>> = listOf(
        ApiResponse.Loading,
        ApiResponse.Ok("kotlin"),
        ApiResponse.Err(500, "internal error"),
    )
    cases.forEach { appendLine("  ${it.render()}") }
}
