package org.example.test.kmp

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// =====================================================================
// kotlinx.serialization：多平台序列化库，编译器插件在各目标生成序列化器
// =====================================================================

@Serializable
data class UserProfile(val name: String, val age: Int, val tags: List<String> = emptyList())

fun demoJson(): String {
    val json = Json
    val original = UserProfile("Ada Lovelace", 36, listOf("kmp", "math"))
    val encoded = json.encodeToString(UserProfile.serializer(), original)
    val decoded = json.decodeFromString(UserProfile.serializer(), encoded)
    return "serialization round-trip: $encoded -> $decoded (equal=${original == decoded})"
}
