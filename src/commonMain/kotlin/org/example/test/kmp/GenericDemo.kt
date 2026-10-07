package org.example.test.kmp

// =====================================================================
// 泛型 + reified 内联函数：inline reified 在所有平台均可用
// =====================================================================

inline fun <reified T : Any> pickOrNull(value: Any?): T? = value as? T

fun demoGenerics(): String {
    val any: Any = "kotlin"
    val asString: String? = pickOrNull<String>(any)
    val asInt: Int? = pickOrNull<Int>(any)
    return "reified generics: pickOrNull<String>=$asString, pickOrNull<Int>=$asInt"
}
