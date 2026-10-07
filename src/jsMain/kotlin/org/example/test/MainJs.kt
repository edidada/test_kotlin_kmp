package org.example.test

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.example.test.kmp.demoCoroutines
import org.example.test.kmp.runAllDemos

// JS 入口：Node 上没有 runBlocking（不能阻塞事件循环），用 MainScope 启动协程
fun main() {
    println("Hello from Kotlin/JS!")
    println(runAllDemos())
    MainScope().launch {
        println(demoCoroutines())
    }
}
