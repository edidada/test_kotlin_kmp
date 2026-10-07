package org.example.test.kmp

// =====================================================================
// 类型安全 DSL：@DslMarker 限制嵌套 receiver 作用域（纯 common 代码）
// =====================================================================

@DslMarker
annotation class KmpDsl

@KmpDsl
class ServerDsl {
    var host: String = "localhost"
    var port: Int = 8080
    private val routes = mutableListOf<String>()

    fun route(path: String) {
        routes += path
    }

    internal fun build(): String = "Server($host:$port) routes=$routes"
}

fun server(block: ServerDsl.() -> Unit): String = ServerDsl().apply(block).build()

fun demoDsl(): String = server {
    host = "0.0.0.0"
    port = 9090
    route("/health")
    route("/users")
}
